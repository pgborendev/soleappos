package com.ury.pos.api;

import com.ury.pos.api.response.AggregatorListResponse;
import com.ury.pos.api.response.CustomerListResponse;
import com.ury.pos.api.response.GenericResponse;
import com.ury.pos.api.response.InvoiceListResponse;
import com.ury.pos.api.response.InvoiceItemsResponse;
import com.ury.pos.api.response.LoginResponse;
import com.ury.pos.api.response.MenuResponse;
import com.ury.pos.api.response.OrderResponse;
import com.ury.pos.api.response.PaymentModeResponse;
import com.ury.pos.api.response.PosOpeningResponse;
import com.ury.pos.api.response.PosProfileResponse;
import com.ury.pos.api.response.PosSessionStatusResponse;
import com.ury.pos.api.response.RoomListResponse;
import com.ury.pos.api.response.SyncOrderResponse;
import com.ury.pos.api.response.TableListResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @FormUrlEncoded
    @POST("api/method/login")
    Call<LoginResponse> login(
            @Field("usr") String username,
            @Field("pwd") String password
    );

    @GET("api/method/frappe.auth.get_logged_user")
    Call<GenericResponse<String>> getLoggedUser();

    @POST("api/method/logout")
    Call<GenericResponse<String>> logout();

    // ── POS Profile ───────────────────────────────────────────────────────────

    @GET("api/method/ury.ury_pos.api.getPosProfile")
    Call<PosProfileResponse> getPosProfile();

    @GET("api/method/ury.ury_pos.api.posOpening")
    Call<PosOpeningResponse> checkPosOpening();

    @FormUrlEncoded
    @POST("api/method/ury.ury_pos.api.validate_pos_close")
    Call<GenericResponse<String>> validatePosClose(
            @Field("pos_profile") String posProfile
    );

    @GET("api/method/ury.ury_pos.api.get_pos_session_status")
    Call<PosSessionStatusResponse> getPosSessionStatus(
            @Query("pos_profile") String posProfile
    );

    @FormUrlEncoded
    @POST("api/method/ury.ury_pos.api.open_pos_session")
    Call<GenericResponse<String>> openPosSession(
            @Field("pos_profile") String posProfile,
            @Field("cashier") String cashier
    );

    @FormUrlEncoded
    @POST("api/method/ury.ury_pos.api.close_pos_session")
    Call<GenericResponse<String>> closePosSession(
            @Field("pos_profile") String posProfile
    );

    // ── Menu ──────────────────────────────────────────────────────────────────

    @GET("api/method/ury.ury_pos.api.getRestaurantMenu")
    Call<MenuResponse> getRestaurantMenu(
            @Query("pos_profile") String posProfile,
            @Query("room") String room,
            @Query("order_type") String orderType
    );

    @GET("api/method/ury.ury_pos.api.getAggregatorItem")
    Call<MenuResponse> getAggregatorMenu(
            @Query("aggregator") String aggregator
    );

    // ── Tables & Rooms ────────────────────────────────────────────────────────

    @GET("api/resource/URY Room")
    Call<RoomListResponse> getRooms(
            @Query("fields") String fields,
            @Query("filters") String filters,
            @Query("limit") int limit
    );

    @GET("api/resource/URY Table")
    Call<TableListResponse> getTables(
            @Query("fields") String fields,
            @Query("filters") String filters,
            @Query("limit") int limit
    );

    // ── Orders ────────────────────────────────────────────────────────────────

    @GET("api/method/ury.ury.doctype.ury_order.ury_order.get_order_invoice")
    Call<OrderResponse> getTableOrder(
            @Query("table") String table
    );

    @POST("api/method/ury.ury.doctype.ury_order.ury_order.sync_order")
    Call<SyncOrderResponse> syncOrder(
            @Body Map<String, Object> body
    );

    // ── Invoices ──────────────────────────────────────────────────────────────

    @GET("api/method/ury.ury_pos.api.getPosInvoice")
    Call<InvoiceListResponse> getPosInvoices(
            @Query("status") String status,
            @Query("limit") int limit,
            @Query("limit_start") int limitStart
    );

    @GET("api/method/ury.ury_pos.api.getPosInvoiceItems")
    Call<InvoiceItemsResponse> getPosInvoiceItems(
            @Query("invoice") String invoiceId
    );

    @FormUrlEncoded
    @POST("api/method/ury.ury_pos.api.updatePosInvoiceStatus")
    Call<GenericResponse<String>> updateInvoiceStatus(
            @Field("invoice") String invoice,
            @Field("status") String status
    );

    @GET("api/method/ury.ury_pos.api.searchPosInvoice")
    Call<GenericResponse<Object>> searchPosInvoice(
            @Query("query") String query,
            @Query("status") String status
    );

    // ── Payment ───────────────────────────────────────────────────────────────

    @GET("api/method/ury.ury_pos.api.getModeOfPayment")
    Call<PaymentModeResponse> getPaymentModes();

    // ── Aggregators ───────────────────────────────────────────────────────────

    @GET("api/resource/URY Aggregator")
    Call<AggregatorListResponse> getAggregators(
            @Query("fields") String fields,
            @Query("limit") int limit
    );

    // ── Customers ─────────────────────────────────────────────────────────────

    @GET("api/resource/Customer")
    Call<CustomerListResponse> searchCustomers(
            @Query("fields") String fields,
            @Query("filters") String filters,
            @Query("limit") int limit
    );

    // ── Print ─────────────────────────────────────────────────────────────────

    @FormUrlEncoded
    @POST("api/method/ury.ury.api.ury_print.network_printing")
    Call<GenericResponse<String>> networkPrint(
            @Field("doctype") String doctype,
            @Field("name") String name,
            @Field("printer_setting") String printerSetting,
            @Field("print_format") String printFormat
    );

    @FormUrlEncoded
    @POST("api/method/ury.ury.api.ury_print.qz_print_update")
    Call<GenericResponse<String>> updatePrintStatus(
            @Field("invoice") String invoice
    );
}
