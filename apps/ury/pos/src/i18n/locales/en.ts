const en = {
  // Sidebar
  categories: 'Categories',
  all_items: 'All Items',

  // AuthGuard
  access_denied: 'Access Denied',
  config_error: 'Configuration Error',
  pos_profile_not_configured: 'POS Profile not found or not configured.',
  permission_required: 'Permission Required',
  no_permission: 'You do not have permission to access this application.',
  required_roles: 'Required roles:',
  recheck_permissions: 'Recheck Permissions',

  // ProductDialog
  special_instructions: 'Special Instructions',
  special_instructions_placeholder: 'Add any special instructions or notes for this item...',
  quantity: 'Quantity',
  variants: 'Variants',
  loading_addons: 'Loading add-ons...',
  addons: 'Add-ons',
  no_addons: 'No add ons',
  add_to_order: 'Add to Order',

  // MenuList
  error_loading_menu: 'Error loading menu items',
  no_items_found: 'No items found',
  no_items_hint: 'Try adjusting your filters or search term',

  // TableSelectionDialog
  select_table: 'Select Table',

  // ScreenSizeDialog
  desktop_only: 'Desktop Only',
  desktop_only_message: 'This POS system is designed for desktop computers and tablets with larger screens.',
  mobile_support_coming: 'Mobile support will be available in a future update. Please use a device with a screen width of 1024px or larger.',
  current_screen_width: 'Current screen width:',
  required_width: 'Required:',
  required_width_value: '1024px or larger',
  mobile_version_hint: 'You can use URY POS Version 1 for mobile devices.',
  switch_to_version1: 'Switch to Version 1',

  // Common
  cancel: 'Cancel',
  close: 'Close',
  save: 'Save',
  apply: 'Apply',
  all: 'All',
  loading: 'Loading...',
  retry: 'Retry',
  page: 'Page',
  previous: 'Previous',
  next: 'Next',
  print: 'Print',
  payment: 'Payment',
  total: 'Total',
  change: 'Change',
  preview: 'Preview',
  no_options: 'No options',
  searching: 'Searching...',

  // Header
  search_orders_placeholder: 'Search orders, menu items, or customers...',
  search_orders: 'Search Orders',
  search_menu: 'Search Menu',
  switch_to_desk: 'Switch To Desk',
  clear_cache: 'Clear Cache',
  logout: 'Logout',
  logout_failed: 'Failed to logout. Please try again.',

  // Footer nav
  nav_pos: 'POS',
  nav_table: 'Table',
  nav_orders: 'Orders',

  // POS page
  failed_load_pos: 'Failed to load POS',
  loading_menu_items: 'Loading menu items...',
  special_items: 'Special Items',
  open_cart: 'Open cart',

  // Orders page
  failed_load_orders: 'Failed to load orders',
  no_orders_found: 'No orders found',
  select_order_prompt: 'Select an order to view details',
  select_order_hint: 'Click on any order card to view its details',
  order_items: 'Order Items',
  taxes_charges: 'Taxes & Charges',
  qty: 'Qty: {{qty}}',
  failed_load_order_details: 'Failed to load order details',
  please_print_before_payment: 'Please print invoice before making payment',
  order_moved_to_draft: 'Order moved to Draft after printing.',
  printed_successfully: 'Printed Successfully',
  print_failed: 'Print failed: {{error}}',
  cancel_order: 'Cancel Order',
  cancel_order_reason_prompt: 'Please provide a reason for cancelling this order.',
  enter_cancel_reason: 'Enter cancel reason',
  confirm_cancel: 'Confirm Cancel',
  cancelling: 'Cancelling...',
  order_cancelled_success: 'Order cancelled successfully',
  failed_cancel_order: 'Failed to cancel order',
  enter_reason_for_cancellation: 'Please enter a reason for cancellation.',

  // Table page
  loading_rooms: 'Loading rooms...',
  no_rooms_found: 'No rooms found for this branch',
  loading_tables: 'Loading tables...',
  no_tables_found: 'No tables found for this room',
  failed_load_rooms: 'Failed to load rooms',
  failed_load_tables: 'Failed to load tables',
  layout_view: 'Layout view',
  occupied: 'Occupied',
  available: 'Available',
  room: 'Room',
  seats: 'Seats',
  started_at: 'Started at',
  printing: 'Printing...',
  tap_to_start_order: 'Tap to start a new dine-in order',
  take_away_badge: 'Take away',
  pos_profile_not_loaded: 'POS profile not loaded yet',
  no_active_order_for_table: 'No active order found for this table',
  failed_print_order: 'Failed to print order',

  // Order Panel
  current_order: 'Current Order',
  cart_empty: 'Your cart is empty',
  cart_empty_hint: 'Add items to get started with your order',
  click_to_add: 'Click items to add them',
  double_click_customize: 'Double-click for customization options',
  loading_order_details: 'Loading order details...',
  clear_cart: 'Clear cart',
  updating_order: 'Updating Order...',
  processing_order: 'Processing Order...',
  update_order: 'Update Order',
  add_new_order: 'Add New Order',
  add_comment: 'Add comment',
  edit_comment: 'Edit comment',
  select_aggregator_first: 'Please select an aggregator before proceeding',
  select_customer_first: 'Please select a customer before proceeding',
  select_table_for_order: 'Please select a table for {{orderType}} orders',
  order_updated_success: 'Order updated successfully',
  order_created_success: 'Order created successfully',
  failed_process_order: 'Failed to process order',

  // Payment Dialog
  apply_discount: 'Apply Discount',
  enter_percent: 'Enter %',
  payment_methods: 'Payment Methods',
  amount: 'Amount',
  total_entered: 'Total Entered',
  order_summary: 'Order Summary',
  subtotal: 'Subtotal',
  discount: 'Discount',
  adjustment: 'Adjustment',
  processing: 'Processing...',
  pay_amount: 'Pay {{amount}}',
  invalid_discount: 'Please enter a valid discount value',
  discount_exceed_100: 'Percentage discount cannot exceed 100%',

  // Comment Dialog
  order_comments: 'Order Comments',
  add_comments_for_order: 'Add comments for this order',
  comment_placeholder: 'Enter any special instructions or comments...',
  save_comment: 'Save Comment',

  // Order Status Sidebar
  order_status: 'Order Status',
  status_draft: 'Draft',
  status_unbilled: 'Unbilled',
  status_recently_paid: 'Recently Paid',
  status_paid: 'Paid',
  status_consolidated: 'Consolidated',
  status_return: 'Return',

  // Initial Loader
  loading_ury_pos: 'Loading URY POS...',
  please_wait_setup: 'Please wait while we set things up',

  // POS Opening Provider
  checking_pos_status: 'Checking POS status...',

  // POS Opening Dialog
  pos_not_opened: 'POS Not Opened',
  prev_pos_not_closed: 'Previous POS Not Closed',
  open_pos_entry: 'Please open POS Entry to continue using the system.',
  close_prev_pos: 'Please close the previous POS Entry to continue.',
  reload_page: 'Reload Page',
  switch_to_desk: 'Switch to Desk',

  // Customer Select
  search_customer: 'Search customer...',
  type_to_search: 'Please type to search...',
  failed_search_customers: 'Failed to search customers',
  no_customers_found: 'No customers found',
  add_new_customer: 'Add New Customer',
  add_customer_with_name: 'Add "{{name}}"...',
  customer_name: 'Name',
  name_required: 'Name is required',
  phone: 'Phone',
  phone_required: 'Phone is required',
  customer_group: 'Customer Group',
  select_group: 'Select group',
  territory: 'Territory',
  select_territory: 'Select territory',
  adding_customer: 'Adding Customer...',
  add_customer: 'Add Customer',
  failed_create_customer: 'Failed to create customer. Please try again.',

  // Aggregator Select
  loading_aggregators: 'Loading aggregators...',
  select_aggregator: 'Select an aggregator',

  // Order Type Select
  dine_in_not_available: 'Dine In is not available for your role',

  // Order Types
  order_type_dine_in: 'Dine In',
  order_type_take_away: 'Take Away',
  order_type_delivery: 'Delivery',
  order_type_phone_in: 'Phone In',
  order_type_aggregators: 'Aggregators',

  // Layout View
  grid_view: 'Grid View',
  layout: 'Layout',
  edit_layout: 'Edit Layout',
  finish_editing: 'Finish Editing',
  zoom_in: 'Zoom In',
  zoom_out: 'Zoom Out',
  reset_zoom: 'Reset Zoom & Pan',
  edit_table_settings: 'Edit Table Settings',
  table_info: 'Table Info',
  table_name: 'Table Name',
  table_names_managed_backend: 'Table names are managed in backend',
  capacity: 'Capacity',
  enter_1_20: 'Enter 1-20',
  valid_range_pax: 'Valid range: 1-20 pax',
  shape: 'Shape',
  shape_circle: 'Circle',
  shape_square: 'Square',
  shape_rectangle: 'Rectangle',
  status: 'Status',
  position: 'Position',
  size: 'Size',
  current_bill: 'Current Bill',
  started_at_colon: 'Started at:',
  total_amount: 'Total Amount:',
  editing_layout: 'Editing Layout',
  drag_tables_hint: 'Drag tables to reposition',
  changes_autosave: 'Changes autosave',
  scroll_zoom_hint: 'Use Scroll to zoom • Drag background to pan',
};

export default en;
export type TranslationKeys = typeof en;
