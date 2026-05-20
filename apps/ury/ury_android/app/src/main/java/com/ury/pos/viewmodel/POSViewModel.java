package com.ury.pos.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ury.pos.api.ApiClient;
import com.ury.pos.api.ApiService;
import com.ury.pos.api.response.GenericResponse;
import com.ury.pos.api.response.PosOpeningResponse;
import com.ury.pos.api.response.PosProfileResponse;
import com.ury.pos.api.response.PosSessionStatusResponse;
import com.ury.pos.model.MenuItem;
import com.ury.pos.model.OrderItem;
import com.ury.pos.model.Payment;
import com.ury.pos.model.PosProfile;
import com.ury.pos.repository.MenuRepository;
import com.ury.pos.repository.OrderRepository;
import com.ury.pos.util.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class POSViewModel extends AndroidViewModel {

    // ── Observables ───────────────────────────────────────────────────────────
    public final MutableLiveData<PosProfile>       posProfile     = new MutableLiveData<>();
    public final MutableLiveData<Boolean>          posOpen        = new MutableLiveData<>();
    public final MutableLiveData<PosSessionStatusResponse.Body> sessionStatus = new MutableLiveData<>();
    public final MutableLiveData<List<MenuItem>>   menuItems      = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<List<OrderItem>>  cartItems      = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<List<String>>     categories     = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<String>           selectedCategory = new MutableLiveData<>("All");
    public final MutableLiveData<String>           searchQuery    = new MutableLiveData<>("");
    public final MutableLiveData<Boolean>          specialFilter  = new MutableLiveData<>(false);
    public final MutableLiveData<String>           orderType      = new MutableLiveData<>("Take Away");
    public final MutableLiveData<String>           selectedTable  = new MutableLiveData<>();
    public final MutableLiveData<String>           selectedRoom   = new MutableLiveData<>();
    public final MutableLiveData<String>           selectedCustomer = new MutableLiveData<>();
    public final MutableLiveData<String>           currentInvoice = new MutableLiveData<>();
    public final MutableLiveData<String>           lastInvoice    = new MutableLiveData<>();
    public final MutableLiveData<List<String>>     paymentModes   = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<Boolean>          loading        = new MutableLiveData<>(false);
    public final MutableLiveData<String>           error          = new MutableLiveData<>();
    public final MutableLiveData<String>           syncedInvoice  = new MutableLiveData<>();

    private final MenuRepository  menuRepo;
    private final OrderRepository orderRepo;
    private final SessionManager  session;
    private final ApiService      api;

    public POSViewModel(@NonNull Application app) {
        super(app);
        menuRepo  = new MenuRepository(app);
        orderRepo = new OrderRepository(app);
        session   = SessionManager.getInstance(app);
        api       = ApiClient.getService(app);
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    public void init() {
        loadPosProfile();
    }

    private void loadPosProfile() {
        loading.setValue(true);
        api.getPosProfile().enqueue(new Callback<PosProfileResponse>() {
            @Override
            public void onResponse(Call<PosProfileResponse> call,
                                   Response<PosProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PosProfile profile = response.body().message;
                    posProfile.postValue(profile);
                    if (selectedCustomer.getValue() == null) {
                        selectedCustomer.postValue(profile.defaultCustomer);
                    }
                    checkSessionStatus(profile.name);
                    loadMenu(profile.name, null, "Dine In");
                    loadPaymentModes();
                } else {
                    loading.postValue(false);
                    error.postValue("Failed to load POS profile");
                }
            }

            @Override
            public void onFailure(Call<PosProfileResponse> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t.getMessage());
            }
        });
    }

    public void checkSessionStatus(String posProfileName) {
        api.getPosSessionStatus(posProfileName).enqueue(new Callback<PosSessionStatusResponse>() {
            @Override
            public void onResponse(Call<PosSessionStatusResponse> call,
                                   Response<PosSessionStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PosSessionStatusResponse.Body status = response.body().message;
                    sessionStatus.postValue(status);
                    posOpen.postValue(status.isOpen && !status.needsClosePrevious);
                }
            }

            @Override
            public void onFailure(Call<PosSessionStatusResponse> call, Throwable t) { }
        });
    }

    public void openSession(Runnable onDone, java.util.function.Consumer<String> onError) {
        PosProfile profile = posProfile.getValue();
        if (profile == null) return;
        loading.setValue(true);
        api.openPosSession(profile.name, profile.cashier)
                .enqueue(new Callback<GenericResponse<String>>() {
                    @Override
                    public void onResponse(Call<GenericResponse<String>> call,
                                           Response<GenericResponse<String>> response) {
                        loading.postValue(false);
                        checkSessionStatus(profile.name);
                        if (onDone != null) onDone.run();
                    }
                    @Override
                    public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                        loading.postValue(false);
                        if (onError != null) onError.accept(t.getMessage());
                    }
                });
    }

    public void closeSession(Runnable onDone, java.util.function.Consumer<String> onError) {
        PosProfile profile = posProfile.getValue();
        if (profile == null) return;
        loading.setValue(true);
        api.closePosSession(profile.name)
                .enqueue(new Callback<GenericResponse<String>>() {
                    @Override
                    public void onResponse(Call<GenericResponse<String>> call,
                                           Response<GenericResponse<String>> response) {
                        loading.postValue(false);
                        checkSessionStatus(profile.name);
                        if (onDone != null) onDone.run();
                    }
                    @Override
                    public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                        loading.postValue(false);
                        if (onError != null) onError.accept(t.getMessage());
                    }
                });
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    public void loadMenu(String profileName, String room, String type) {
        loading.setValue(true);
        menuRepo.getMenu(profileName, room, type, new MenuRepository.MenuCallback() {
            @Override
            public void onSuccess(List<MenuItem> items) {
                menuItems.postValue(items);
                buildCategories(items);
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    private void buildCategories(List<MenuItem> items) {
        Map<String, Boolean> seen = new LinkedHashMap<>();
        seen.put("All", true);
        for (MenuItem item : items) {
            if (item.course != null && !item.course.isEmpty()) {
                seen.put(item.course, true);
            }
        }
        categories.postValue(new ArrayList<>(seen.keySet()));
    }

    public List<MenuItem> getFilteredMenu() {
        List<MenuItem> all = menuItems.getValue();
        if (all == null) return new ArrayList<>();
        String cat     = selectedCategory.getValue();
        String query   = searchQuery.getValue();
        Boolean spec   = specialFilter.getValue();
        boolean specialOnly = spec != null && spec;
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : all) {
            boolean catMatch    = "All".equals(cat) || cat == null || cat.equals(item.course);
            boolean queryMatch  = query == null || query.isEmpty()
                    || item.itemName.toLowerCase().contains(query.toLowerCase());
            boolean specialMatch = !specialOnly || item.specialDish == 1;
            if (catMatch && queryMatch && specialMatch) result.add(item);
        }
        return result;
    }

    // ── Cart ──────────────────────────────────────────────────────────────────

    public void addToCart(MenuItem menuItem) {
        List<OrderItem> cart = new ArrayList<>(cartItems.getValue() != null
                ? cartItems.getValue() : new ArrayList<>());
        // Look for existing item (same item_code, no variant/addon)
        for (OrderItem oi : cart) {
            if (oi.itemCode.equals(menuItem.item) && oi.selectedVariant == null
                    && (oi.selectedAddons == null || oi.selectedAddons.isEmpty())) {
                oi.qty++;
                cartItems.setValue(cart);
                return;
            }
        }
        OrderItem newItem = new OrderItem(menuItem);
        cart.add(newItem);
        cartItems.setValue(cart);
    }

    public void updateCartItemQty(String uniqueId, int delta) {
        List<OrderItem> cart = new ArrayList<>(cartItems.getValue() != null
                ? cartItems.getValue() : new ArrayList<>());
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).uniqueId.equals(uniqueId)) {
                OrderItem updated = new OrderItem(cart.get(i));
                updated.qty += delta;
                if (updated.qty <= 0) {
                    cart.remove(i);
                } else {
                    cart.set(i, updated);
                }
                break;
            }
        }
        cartItems.setValue(cart);
    }

    public void removeCartItem(String uniqueId) {
        List<OrderItem> cart = new ArrayList<>(cartItems.getValue() != null
                ? cartItems.getValue() : new ArrayList<>());
        cart.removeIf(oi -> oi.uniqueId.equals(uniqueId));
        cartItems.setValue(cart);
    }

    public void clearCart() {
        PosProfile profile = posProfile.getValue();
        cartItems.setValue(new ArrayList<>());
        currentInvoice.setValue(null);
        lastInvoice.setValue(null);
        selectedTable.setValue(null);
        selectedRoom.setValue(null);
        orderType.setValue("Take Away");
        selectedCustomer.setValue(profile != null ? profile.defaultCustomer : null);
    }

    public void addNewOrder(Runnable onDone) {
        PosProfile profile = posProfile.getValue();
        if (profile == null) return;
        List<OrderItem> cart = cartItems.getValue();
        if (cart == null || cart.isEmpty()) return;

        loading.setValue(true);
        String customer = selectedCustomer.getValue() != null
                ? selectedCustomer.getValue() : profile.defaultCustomer;
        orderRepo.syncOrder(
                profile.name, profile.cashier, profile.owner,
                orderType.getValue() != null ? orderType.getValue() : "Take Away",
                selectedTable.getValue(), selectedRoom.getValue(),
                customer, profile.cashier, null,
                currentInvoice.getValue(), lastInvoice.getValue(),
                cart, null,
                new OrderRepository.Callback1<String>() {
                    @Override
                    public void onSuccess(String invoiceName) {
                        loading.postValue(false);
                        lastInvoice.postValue(currentInvoice.getValue());
                        currentInvoice.postValue(invoiceName);
                        syncedInvoice.postValue(invoiceName);
                        if (onDone != null) onDone.run();
                    }
                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
                        error.postValue(message);
                    }
                });
    }

    public double getCartTotal() {
        List<OrderItem> cart = cartItems.getValue();
        if (cart == null) return 0;
        double total = 0;
        for (OrderItem oi : cart) total += oi.getLineTotal();
        return total;
    }

    public int getCartCount() {
        List<OrderItem> cart = cartItems.getValue();
        if (cart == null) return 0;
        int count = 0;
        for (OrderItem oi : cart) count += (int) oi.qty;
        return count;
    }

    // ── Order sync ────────────────────────────────────────────────────────────

    public void placeOrder(List<Payment> payments) {
        PosProfile profile = posProfile.getValue();
        if (profile == null) return;
        List<OrderItem> cart = cartItems.getValue();
        if (cart == null || cart.isEmpty()) return;

        loading.setValue(true);
        String customer = selectedCustomer.getValue() != null
                ? selectedCustomer.getValue() : profile.defaultCustomer;
        orderRepo.syncOrder(
                profile.name,
                profile.cashier,
                profile.owner,
                orderType.getValue() != null ? orderType.getValue() : "Dine In",
                selectedTable.getValue(),
                selectedRoom.getValue(),
                customer,
                profile.cashier,
                null,
                currentInvoice.getValue(),
                lastInvoice.getValue(),
                cart, payments,
                new OrderRepository.Callback1<String>() {
                    @Override
                    public void onSuccess(String invoiceName) {
                        loading.postValue(false);
                        syncedInvoice.postValue(invoiceName);
                        lastInvoice.postValue(currentInvoice.getValue());
                        currentInvoice.postValue(invoiceName);
                        if (payments != null && !payments.isEmpty()) clearCart();
                    }

                    @Override
                    public void onError(String message) {
                        loading.postValue(false);
                        error.postValue(message);
                    }
                });
    }

    // ── Payment modes ─────────────────────────────────────────────────────────

    private void loadPaymentModes() {
        orderRepo.getPaymentModes(new OrderRepository.Callback1<List<String>>() {
            @Override
            public void onSuccess(List<String> modes) {
                paymentModes.postValue(modes);
            }

            @Override
            public void onError(String message) { }
        });
    }
}
