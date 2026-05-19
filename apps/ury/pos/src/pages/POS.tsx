import React, { useState, useRef } from 'react';
import { Star, TrendingUp, ShoppingCart } from 'lucide-react';
import Sidebar from '../components/Sidebar';
import OrderPanel from '../components/OrderPanel';
import ProductDialog from '../components/ProductDialog';
import MenuList from '../components/MenuList';
import { usePOSStore } from '../store/pos-store';
import { cn } from '../lib/utils';
import { Spinner } from '../components/ui/spinner';
import InitialLoader from '../components/InitialLoader';

export default function POS() {
  const {
    quickFilter,
    setQuickFilter,
    setSelectedItem,
    addToOrder,
    loading,
    error,
    isMenuInteractionDisabled,
    isInitializing,
    selectedCategory,
    setSelectedCategory,
    categories,
    activeOrders,
  } = usePOSStore();

  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [mobileCartOpen, setMobileCartOpen] = useState(false);
  const clickTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const clickCountRef = useRef(0);

  const handleItemClick = (item: any) => {
    if (isMenuInteractionDisabled()) return;

    clickCountRef.current += 1;

    if (clickTimerRef.current) {
      clearTimeout(clickTimerRef.current);
    }

    clickTimerRef.current = setTimeout(() => {
      if (clickCountRef.current === 1) {
        addToOrder({ ...item, quantity: 1 });
      } else if (clickCountRef.current === 2) {
        setSelectedItem(item);
        setIsDialogOpen(true);
      }
      clickCountRef.current = 0;
    }, 250);
  };

  const QuickFilterButton = ({ filter, icon: Icon, label }: {
    filter: 'all' | 'special';
    icon: React.ElementType;
    label: string;
  }) => (
    <button
      onClick={() => setQuickFilter(filter)}
      className={cn(
        'flex items-center gap-2 px-3 py-1.5 rounded-full text-sm font-medium transition-colors flex-shrink-0',
        quickFilter === filter
          ? 'bg-blue-100 text-blue-700'
          : 'bg-gray-100 text-gray-700 hover:bg-gray-200',
        isMenuInteractionDisabled() && 'opacity-50 cursor-not-allowed pointer-events-none'
      )}
      disabled={isMenuInteractionDisabled()}
    >
      <Icon className="w-4 h-4" />
      {label}
    </button>
  );

  if (isInitializing) {
    return <InitialLoader />;
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <p className="text-xl font-semibold text-red-600 mb-2">Failed to load POS</p>
          <p className="text-gray-600">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <Spinner message="Loading menu items..." />
      </div>
    );
  }

  return (
    <div className="flex flex-1 overflow-hidden">
      <Sidebar disabled={isMenuInteractionDisabled()} />

      <div className="flex-1 flex flex-col h-screen overflow-hidden lg:pr-96">
        {/* Mobile category bar — horizontal scroll pills, hidden on desktop */}
        <div className="flex lg:hidden overflow-x-auto gap-2 p-3 bg-white border-b border-gray-200 flex-shrink-0">
          <button
            onClick={() => setSelectedCategory('')}
            className={cn(
              'flex-shrink-0 px-3 py-1.5 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
              selectedCategory === ''
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            )}
            disabled={isMenuInteractionDisabled()}
          >
            All
          </button>
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={cn(
                'flex-shrink-0 px-3 py-1.5 rounded-full text-sm font-medium transition-colors whitespace-nowrap',
                selectedCategory === cat
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              )}
              disabled={isMenuInteractionDisabled()}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Filter bar */}
        <div className="p-3 sm:p-4 bg-white border-b border-gray-200 flex-shrink-0">
          <div className="max-w-screen-xl mx-auto">
            <div className="flex items-center gap-2 overflow-x-auto overflow-y-hidden">
              <QuickFilterButton filter="all" icon={Star} label="All" />
              <QuickFilterButton filter="special" icon={TrendingUp} label="Special Items" />
            </div>
          </div>
        </div>

        <MenuList onItemClick={handleItemClick} />
      </div>

      {/* Mobile cart FAB — shown only on mobile, above the footer */}
      <button
        className="lg:hidden fixed bottom-20 right-4 z-40 w-14 h-14 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 rounded-full shadow-xl flex items-center justify-center text-white transition-colors"
        onClick={() => setMobileCartOpen(true)}
        aria-label="Open cart"
      >
        <ShoppingCart className="w-6 h-6" />
        {activeOrders.length > 0 && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-medium">
            {activeOrders.length > 9 ? '9+' : activeOrders.length}
          </span>
        )}
      </button>

      <OrderPanel
        isMobileOpen={mobileCartOpen}
        onMobileClose={() => setMobileCartOpen(false)}
      />
      {isDialogOpen && <ProductDialog onClose={() => setIsDialogOpen(false)} />}
    </div>
  );
}
