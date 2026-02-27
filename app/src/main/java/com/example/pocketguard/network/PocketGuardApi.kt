package com.example.pocketguard.network

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface PocketGuardApi {

    // --- AUTH ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): ApiResponse<UserResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(): ApiResponse<RefreshResponse>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    // --- BANKS ---
    @GET("banks")
    suspend fun getBanks(): ApiResponse<BanksResponse>

    @GET("banks/global")
    suspend fun getGlobalBanks(): ApiResponse<BanksResponse>

    @GET("banks/{id}")
    suspend fun getBankById(@Path("id") id: String): ApiResponse<BankResponse>

    @POST("banks")
    suspend fun createBank(@Body request: CreateBankRequest): ApiResponse<BankResponse>

    @PATCH("banks/{id}")
    suspend fun updateBank(
        @Path("id") id: String,
        @Body request: CreateBankRequest
    ): ApiResponse<BankResponse>

    @DELETE("banks/{id}")
    suspend fun deleteBank(@Path("id") id: String): ApiResponse<Unit>

    // --- CATEGORIES ---
    @GET("categories")
    suspend fun getCategories(): ApiResponse<CategoriesResponse>

    @GET("categories/global")
    suspend fun getGlobalCategories(): ApiResponse<CategoriesResponse>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: String): ApiResponse<CategoryResponse>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): ApiResponse<CategoryResponse>

    @PATCH("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: CreateCategoryRequest
    ): ApiResponse<CategoryResponse>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): ApiResponse<Unit>

    // --- CARDS ---
    @GET("cards")
    suspend fun getCards(): ApiResponse<CardsResponse>

    @GET("cards/{id}")
    suspend fun getCardById(@Path("id") id: String): ApiResponse<CardResponse>

    @POST("cards")
    suspend fun createCard(@Body request: CreateCardRequest): ApiResponse<CardResponse>

    @PATCH("cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body request: CreateCardRequest
    ): ApiResponse<CardResponse>

    @PATCH("cards/{id}/default")
    suspend fun setCardAsDefault(@Path("id") id: String): ApiResponse<Unit>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): ApiResponse<Unit>

    // --- SUBSCRIPTIONS ---
    @GET("subscriptions/billing-cycles")
    suspend fun getBillingCycles(): ApiResponse<BillingCyclesResponse>

    @GET("subscriptions/metrics")
    suspend fun getSubscriptionMetrics(): ApiResponse<MetricsResponse>

    @GET("subscriptions")
    suspend fun getSubscriptions(): ApiResponse<SubscriptionsResponse>

    @GET("subscriptions/{id}")
    suspend fun getSubscriptionById(@Path("id") id: String): ApiResponse<SubscriptionResponse>

    @POST("subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): ApiResponse<SubscriptionResponse>

    @PATCH("subscriptions/{id}")
    suspend fun updateSubscription(
        @Path("id") id: String,
        @Body request: UpdateSubscriptionRequest
    ): ApiResponse<SubscriptionResponse>

    @DELETE("subscriptions/{id}")
    suspend fun deleteSubscription(@Path("id") id: String): ApiResponse<Unit>

    // --- PREFERENCES ---
    @GET("preferences")
    suspend fun getPreferences(): ApiResponse<PreferencesResponse>

    @PATCH("preferences")
    suspend fun updatePreferences(@Body request: UpdatePreferencesRequest): ApiResponse<PreferencesResponse>

    // --- NOTIFICATION SETTINGS ---
    @GET("notifications/settings")
    suspend fun getNotificationSettings(): ApiResponse<NotificationSettingsResponse>

    @PATCH("notifications/settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest
    ): ApiResponse<NotificationSettingsResponse>

    // --- EXPENSES ---
    @GET("expenses/summary")
    suspend fun getExpensesSummary(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): ApiResponse<ExpensesSummaryResponse>

    @GET("expenses")
    suspend fun getExpenses(): ApiResponse<ExpensesResponse>

    @GET("expenses/{id}")
    suspend fun getExpenseById(@Path("id") id: String): ApiResponse<ExpenseResponse>

    @POST("expenses")
    suspend fun createExpense(@Body request: CreateExpenseRequest): ApiResponse<ExpenseResponse>

    @PATCH("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body request: UpdateExpenseRequest
    ): ApiResponse<ExpenseResponse>

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): ApiResponse<Unit>
}

