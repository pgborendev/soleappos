import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { usePOSStore } from '../store/pos-store';
import { useRootStore } from '../store/root-store';
import { cn } from '../lib/utils';
import { Button } from './ui';
import TableSelectionDialog from './TableSelectionDialog';
import { DEFAULT_ORDER_TYPE, DINE_IN, ORDER_TYPES, type OrderType } from '../data/order-types';
import { HandPlatter } from 'lucide-react';
import { isUserRestrictedFromTableOrders } from '../lib/role-utils';

const ORDER_TYPE_KEY_MAP: Record<string, string> = {
  'Dine In': 'order_type_dine_in',
  'Take Away': 'order_type_take_away',
  'Delivery': 'order_type_delivery',
  'Phone In': 'order_type_phone_in',
  'Aggregators': 'order_type_aggregators',
};

interface OrderTypeSelectProps {
  disabled?: boolean;
}

const OrderTypeSelect = ({ disabled }: OrderTypeSelectProps) => {
  const { t } = useTranslation();
  const { selectedOrderType, setSelectedOrderType, selectedTable, posProfile, isUpdatingOrder } = usePOSStore();
  const { user } = useRootStore();
  const [showTableDialog, setShowTableDialog] = useState(false);

  const isRestrictedFromTableOrders = isUserRestrictedFromTableOrders(user, posProfile);

  const handleOrderTypeSelect = (type: OrderType) => {
    if (type === DINE_IN && isRestrictedFromTableOrders) return;
    setSelectedOrderType(type);
    if (type === DINE_IN) setShowTableDialog(true);
  };

  const handleTableDialogClose = () => {
    setShowTableDialog(false);
    setTimeout(() => {
      const currentState = usePOSStore.getState();
      if (currentState.selectedOrderType === DINE_IN && !currentState.selectedTable) {
        setSelectedOrderType(DEFAULT_ORDER_TYPE);
      }
    }, 100);
  };

  return (
    <div>
      <div className="flex gap-2 overflow-x-auto pb-2 -mx-2 px-2">
        {ORDER_TYPES.map(({ value, icon: Icon }) => {
          const isDineIn = value === DINE_IN;
          const isDisabled = disabled || (isDineIn && isRestrictedFromTableOrders) || isUpdatingOrder;

          return (
            <Button
              key={value}
              onClick={() => handleOrderTypeSelect(value)}
              variant={selectedOrderType === value ? 'default' : 'outline'}
              className={cn(
                'h-fit flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap bg-white border transition-colors',
                selectedOrderType === value
                  ? 'text-primary-700 bg-primary-50 border-primary-600 hover:bg-primary-50'
                  : 'text-gray-700 border-gray-200 hover:bg-gray-50',
                isDisabled && 'opacity-50 cursor-not-allowed'
              )}
              disabled={isDisabled}
              title={isDineIn && isRestrictedFromTableOrders ? t('dine_in_not_available') : undefined}
            >
              <Icon className="w-4 h-4" />
              {t(ORDER_TYPE_KEY_MAP[value] || value)}
            </Button>
          );
        })}
      </div>

      {selectedOrderType === DINE_IN && selectedTable && (
        <Button
          onClick={() => setShowTableDialog(true)}
          variant="ghost"
          className="h-fit w-fit gap-x-2 mt-2 text-sm text-primary-600 hover:text-primary-700"
          disabled={disabled}
        >
          <HandPlatter className="w-4 h-4" /> {selectedTable}
        </Button>
      )}

      {showTableDialog && (
        <TableSelectionDialog onClose={handleTableDialogClose} />
      )}
    </div>
  );
};

export default OrderTypeSelect;
