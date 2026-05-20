import React, { useState, useRef, useMemo, useEffect, useCallback } from 'react';
import { CreditCard as Edit3, Save, Users, Move, X, Grid3x3 as Grid3X3, ZoomIn, ZoomOut, RotateCcw } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { cn, formatInvoiceTime } from '../lib/utils';
import { Table, updateTableLayout } from '../lib/table-api';
import { getTableOrder, POSInvoice } from '../lib/order-api';
import { Button } from './ui';

interface Props {
  selectedRoom: string;
  tables: Table[];
  onBackToGrid: () => void;
  onRefresh?: () => void;
}

const LayoutView: React.FC<Props> = ({ selectedRoom, tables, onBackToGrid, onRefresh }) => {
  const { t } = useTranslation();
  const [isEditMode, setIsEditMode] = useState(false);
  const [localLayouts, setLocalLayouts] = useState<Record<string, Partial<Table>>>({});
  const [draggedTable, setDraggedTable] = useState<string | null>(null);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const [selectedTable, setSelectedTable] = useState<string | null>(null);
  const [selectedTableOrder, setSelectedTableOrder] = useState<POSInvoice | null>(null);
  const canvasRef = useRef<HTMLDivElement>(null);
  const [zoom, setZoom] = useState(1);
  const [panOffset, setPanOffset] = useState({ x: 0, y: 0 });
  const [isPanning, setIsPanning] = useState(false);
  const [panStart, setPanStart] = useState({ x: 0, y: 0 });
  const [capacityInput, setCapacityInput] = useState<string>('');

  const tablesWithPosition = useMemo(() => {
    return tables.map((table, index) => {
      const local = localLayouts[table.name] || {};
      const x = local.layout_x ?? table.layout_x ?? (100 + (index % 5) * 150);
      const y = local.layout_y ?? table.layout_y ?? (100 + Math.floor(index / 5) * 150);
      return {
        ...table, x, y,
        table_shape: local.table_shape ?? table.table_shape,
        no_of_seats: local.no_of_seats ?? table.no_of_seats,
      };
    });
  }, [tables, localLayouts]);

  useEffect(() => {
    if (selectedTable) {
      const table = tablesWithPosition.find(t => t.name === selectedTable);
      setCapacityInput(table?.no_of_seats?.toString() ?? '');
    }
  }, [selectedTable, tablesWithPosition]);

  const getTableDimensions = (shape: string, capacity: number = 4) => {
    const size = Math.max(60, Math.min(250, 60 + (capacity * 10)));
    const normalizedShape = shape?.toLowerCase() || 'rectangle';
    switch (normalizedShape) {
      case 'circle': return { width: size, height: size };
      case 'square': return { width: size, height: size };
      default: return { width: size * 1.5, height: size };
    }
  };

  const handleZoomIn = () => setZoom(prev => Math.min(prev + 0.1, 3));
  const handleZoomOut = () => setZoom(prev => Math.max(prev - 0.1, 0.3));
  const handleResetZoom = () => { setZoom(1); setPanOffset({ x: 0, y: 0 }); };

  const handleWheel = useCallback((e: WheelEvent) => {
    e.preventDefault();
    e.stopPropagation();
    const delta = e.deltaY > 0 ? -0.1 : 0.1;
    setZoom(prev => Math.max(0.3, Math.min(3, prev + delta)));
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (canvas) canvas.addEventListener('wheel', handleWheel, { passive: false });
    return () => { if (canvas) canvas.removeEventListener('wheel', handleWheel); };
  }, [handleWheel]);

  const handleCanvasMouseDown = (e: React.MouseEvent) => {
    setIsPanning(true);
    setPanStart({ x: e.clientX - panOffset.x, y: e.clientY - panOffset.y });
  };

  const handleCanvasMouseMove = (e: React.MouseEvent) => {
    if (isPanning) {
      setPanOffset({ x: e.clientX - panStart.x, y: e.clientY - panStart.y });
      return;
    }
    if (!draggedTable || !isEditMode || !canvasRef.current) return;
    const canvasRect = canvasRef.current.getBoundingClientRect();
    const newX = (e.clientX - canvasRect.left) / zoom - dragOffset.x - panOffset.x / zoom;
    const newY = (e.clientY - canvasRect.top) / zoom - dragOffset.y - panOffset.y / zoom;
    setLocalLayouts(prev => ({
      ...prev,
      [draggedTable]: { ...(prev[draggedTable] || {}), layout_x: newX, layout_y: newY }
    }));
  };

  const persistTableUpdate = (tableName: string, changes: Partial<Table>) => {
    const table = tablesWithPosition.find(t => t.name === tableName);
    if (!table) return Promise.reject('Table not found');
    const payload = {
      layout_x: changes.layout_x ?? table.x,
      layout_y: changes.layout_y ?? table.y,
      table_shape: changes.table_shape ?? table.table_shape,
      no_of_seats: changes.no_of_seats ?? table.no_of_seats,
      minimum_seating: table.minimum_seating
    };
    return updateTableLayout(tableName, payload);
  };

  const handleCanvasMouseUp = () => {
    if (draggedTable && isEditMode) {
      const table = tablesWithPosition.find(t => t.name === draggedTable);
      if (table) {
        persistTableUpdate(table.name, { layout_x: table.x, layout_y: table.y })
          .catch(err => console.error('Failed to save layout', err));
      }
    }
    setIsPanning(false);
    setDraggedTable(null);
    setDragOffset({ x: 0, y: 0 });
  };

  const getTableStatusColor = (occupied: number) =>
    occupied
      ? 'bg-amber-100 border-amber-300 text-amber-900 shadow-sm'
      : 'bg-emerald-50 border-emerald-200 text-emerald-800 hover:shadow-md';

  const handleMouseDown = (e: React.MouseEvent, table: typeof tablesWithPosition[0]) => {
    e.stopPropagation();
    setSelectedTable(table.name);
    if (table.occupied) {
      getTableOrder(table.name).then(res => setSelectedTableOrder(res.message)).catch(console.error);
    } else {
      setSelectedTableOrder(null);
    }
    if (!isEditMode) return;
    const canvasRect = canvasRef.current?.getBoundingClientRect();
    if (!canvasRect) return;
    setDraggedTable(table.name);
    setDragOffset({
      x: (e.clientX - canvasRect.left) / zoom - table.x - panOffset.x / zoom,
      y: (e.clientY - canvasRect.top) / zoom - table.y - panOffset.y / zoom
    });
  };

  const TableShape = ({ table }: { table: typeof tablesWithPosition[0] }) => {
    const dimensions = getTableDimensions(table.table_shape, table.no_of_seats);
    const baseClasses = cn(
      'absolute border-2 flex items-center justify-center text-sm font-semibold cursor-pointer transition-all select-none',
      getTableStatusColor(table.occupied),
      isEditMode && 'hover:ring-2 hover:ring-blue-400 cursor-move',
      draggedTable === table.name && 'shadow-xl scale-105 z-20',
      selectedTable === table.name && 'ring-2 ring-blue-600 z-10'
    );
    const shapeLower = table.table_shape?.toLowerCase();
    const shapeClasses = { circle: 'rounded-full', square: 'rounded-lg', rectangle: 'rounded-md' };
    const roundedClass = shapeClasses[shapeLower as keyof typeof shapeClasses] || shapeClasses.rectangle;

    return (
      <div
        className={cn(baseClasses, roundedClass)}
        style={{
          left: table.x, top: table.y,
          width: dimensions.width, height: dimensions.height,
          transform: `scale(${zoom})`, transformOrigin: 'top left',
        }}
        onMouseDown={(e) => handleMouseDown(e, table)}
      >
        <div className="text-center p-1 overflow-hidden pointer-events-none">
          <div className="font-bold truncate px-1">{table.name}</div>
          <div className="text-[10px] flex items-center justify-center gap-1 opacity-80">
            <Users className="w-3 h-3" />
            {table.no_of_seats || '-'}
          </div>
        </div>
        {isEditMode && (
          <div className="absolute -top-1 -right-1 bg-blue-500 text-white rounded-full p-0.5 shadow-sm">
            <Move className="w-2 h-2" />
          </div>
        )}
      </div>
    );
  };

  const handleCapacityChange = (capacityStr: string) => {
    if (!selectedTable) return;
    setCapacityInput(capacityStr);
    const capacity = parseInt(capacityStr);
    if (isNaN(capacity) || capacity < 1 || capacity > 20) return;
    setLocalLayouts(prev => ({
      ...prev,
      [selectedTable]: { ...(prev[selectedTable] || {}), no_of_seats: capacity }
    }));
    updateTableLayout(selectedTable, { no_of_seats: capacity }).catch(console.error);
  };

  const handleDropdownShapeChange = (shape: string) => {
    if (!selectedTable) return;
    setLocalLayouts(prev => ({
      ...prev,
      [selectedTable]: { ...(prev[selectedTable] || {}), table_shape: shape as any }
    }));
    updateTableLayout(selectedTable, { table_shape: shape as any }).catch(console.error);
  };

  const selectedTableData = tablesWithPosition.find(t => t.name === selectedTable);

  return (
    <div className="flex flex-col h-full bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button onClick={onBackToGrid} variant="outline" className="flex items-center gap-2">
              <Grid3X3 className="w-4 h-4" />
              {t('grid_view')}
            </Button>
            <h2 className="text-lg font-semibold">
              {selectedRoom} <span className="text-gray-400 mx-2">|</span> {t('layout')}
            </h2>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                if (isEditMode) onRefresh?.();
                setIsEditMode(!isEditMode);
              }}
              className={cn(
                'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium border transition-all',
                isEditMode
                  ? 'bg-blue-600 hover:bg-blue-700 text-white border-green-700'
                  : 'bg-white hover:bg-gray-50 text-gray-700 border-gray-200'
              )}
            >
              {isEditMode ? <Save className="w-4 h-4" /> : <Edit3 className="w-4 h-4" />}
              {isEditMode ? t('finish_editing') : t('edit_layout')}
            </button>
          </div>
        </div>
      </div>

      {/* Canvas Area */}
      <div className="flex-1 relative">
        {/* Zoom Controls */}
        <div className="absolute top-4 left-4 z-30 flex flex-col gap-2">
          <button
            onClick={handleZoomIn}
            className="p-2 bg-white hover:bg-gray-50 rounded-lg shadow-lg border border-gray-200 transition-colors"
            title={t('zoom_in')}
          >
            <ZoomIn className="w-5 h-5 text-gray-700" />
          </button>
          <button
            onClick={handleZoomOut}
            className="p-2 bg-white hover:bg-gray-50 rounded-lg shadow-lg border border-gray-200 transition-colors"
            title={t('zoom_out')}
          >
            <ZoomOut className="w-5 h-5 text-gray-700" />
          </button>
          <button
            onClick={handleResetZoom}
            className="p-2 bg-white hover:bg-gray-50 rounded-lg shadow-lg border border-gray-200 transition-colors"
            title={t('reset_zoom')}
          >
            <RotateCcw className="w-5 h-5 text-gray-700" />
          </button>
          <div className="px-2 py-1 bg-white rounded-lg shadow-lg border border-gray-200 text-xs font-medium text-gray-600">
            {Math.round(zoom * 100)}%
          </div>
        </div>

        {/* Instructions */}
        <div className="absolute bottom-4 right-4 z-30 pointer-events-none">
          {isEditMode ? (
            <div className="bg-blue-50/90 backdrop-blur border border-blue-200 rounded-lg p-3 text-sm text-blue-800 shadow-lg">
              <div className="font-medium mb-1">{t('editing_layout')}</div>
              <div>• {t('drag_tables_hint')}</div>
              <div>• {t('changes_autosave')}</div>
            </div>
          ) : (
            <div className="bg-white/80 backdrop-blur border border-gray-200 rounded-lg p-2 text-xs text-gray-500 shadow-sm">
              {t('scroll_zoom_hint')}
            </div>
          )}
        </div>

        <div
          ref={canvasRef}
          className="w-full h-full relative bg-white overflow-hidden cursor-grab active:cursor-grabbing"
          style={{
            backgroundImage: `
              linear-gradient(to right, #e5e7eb 1px, transparent 1px),
              linear-gradient(to bottom, #e5e7eb 1px, transparent 1px)
            `,
            backgroundSize: `${20 * zoom}px ${20 * zoom}px`,
            backgroundPosition: `${panOffset.x}px ${panOffset.y}px`
          }}
          onMouseDown={handleCanvasMouseDown}
          onMouseMove={handleCanvasMouseMove}
          onMouseUp={handleCanvasMouseUp}
          onMouseLeave={handleCanvasMouseUp}
        >
          <div style={{ transform: `translate(${panOffset.x}px, ${panOffset.y}px)`, transformOrigin: 'top left' }}>
            {tablesWithPosition.map(table => <TableShape key={table.name} table={table} />)}
          </div>
        </div>
      </div>

      {/* Table Properties Panel */}
      {selectedTable && selectedTableData && (
        <div className="absolute right-0 bottom-0 top-36 bg-white rounded-t-lg shadow-xl border-t border-l border-gray-200 p-4 w-full max-w-xs z-40 max-h-[72vh] overflow-y-auto">
          <div className="flex justify-between items-center mb-3">
            <h4 className="font-semibold">
              {isEditMode ? t('edit_table_settings') : t('table_info')}
            </h4>
            <button onClick={() => setSelectedTable(null)} className="p-1 hover:bg-gray-100 rounded">
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium mb-1">{t('table_name')}</label>
              <input
                type="text"
                value={selectedTableData.name}
                disabled
                className="w-full px-3 py-2 border rounded-md text-sm border-gray-200 bg-gray-50 cursor-not-allowed"
                title={t('table_names_managed_backend')}
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">{t('capacity')}</label>
              <input
                type="number"
                min="0"
                max="20"
                value={capacityInput}
                onChange={(e) => handleCapacityChange(e.target.value)}
                disabled={!isEditMode}
                className={cn(
                  'w-full px-3 py-2 border rounded-md text-sm',
                  isEditMode ? 'border-gray-300 bg-white' : 'border-gray-200 bg-gray-50 cursor-not-allowed'
                )}
                placeholder={t('enter_1_20')}
              />
              <p className="text-xs text-gray-500 mt-1">{t('valid_range_pax')}</p>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">{t('shape')}</label>
              <select
                value={selectedTableData.table_shape || 'Rectangle'}
                onChange={(e) => handleDropdownShapeChange(e.target.value)}
                disabled={!isEditMode}
                className={cn(
                  'w-full px-3 py-2 border rounded-md text-sm',
                  isEditMode ? 'border-gray-300 bg-white' : 'border-gray-200 bg-gray-50 cursor-not-allowed'
                )}
              >
                <option value="Circle">{t('shape_circle')}</option>
                <option value="Square">{t('shape_square')}</option>
                <option value="Rectangle">{t('shape_rectangle')}</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">{t('status')}</label>
              <div className="w-full px-3 py-2 border border-gray-200 bg-gray-50 rounded-md text-sm cursor-not-allowed capitalize">
                {selectedTableData.occupied ? t('occupied') : t('available')}
              </div>
            </div>

            <div className="pt-3 border-t border-gray-200">
              <label className="block text-sm font-medium mb-2">{t('position')}</label>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div><span className="text-gray-500">X:</span><span className="ml-1">{Math.round(selectedTableData.x)}px</span></div>
                <div><span className="text-gray-500">Y:</span><span className="ml-1">{Math.round(selectedTableData.y)}px</span></div>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">{t('size')}</label>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div><span className="text-gray-500">W:</span><span className="ml-1">{getTableDimensions(selectedTableData.table_shape || 'Rectangle').width}px</span></div>
                <div><span className="text-gray-500">H:</span><span className="ml-1">{getTableDimensions(selectedTableData.table_shape || 'Rectangle').height}px</span></div>
              </div>
            </div>

            {selectedTableData.latest_invoice_time && (
              <div className="pt-3 border-t border-gray-200">
                <label className="block text-sm font-medium mb-2">{t('current_bill')}</label>
                <div className="bg-blue-50 p-3 rounded-md text-sm">
                  <div className="flex justify-between mb-1">
                    <span>{t('started_at_colon')}</span>
                    <span>{formatInvoiceTime(selectedTableData.latest_invoice_time)}</span>
                  </div>
                  {selectedTableOrder && (
                    <div className="flex justify-between items-center pt-2 mt-2 border-t border-blue-200">
                      <span>{t('total_amount')}</span>
                      <span className="font-bold text-lg text-blue-800">
                        {selectedTableOrder.grand_total.toFixed(2)}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default LayoutView;
