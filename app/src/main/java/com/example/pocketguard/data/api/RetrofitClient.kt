package com.example.pocketguard.data.api

import android.content.Context
import com.example.pocketguard.constants.ApiConstants
import com.example.pocketguard.data.remote.DashboardService
import com.example.pocketguard.data.storage.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var retrofit: Retrofit? = null
    private var authService: AuthService? = null
    private var expensesService: ExpensesService? = null
    private var subscriptionsService: SubscriptionsService? = null
    private var categoriesService: CategoriesService? = null
    private var banksService: BanksService? = null
    private var cardsService: CardsService? = null
    private var preferencesService: PreferencesService? = null
    private var notificationsService: NotificationsService? = null
    private var dashboardService: DashboardService? = null
    private var tokenManager: TokenManager? = null

    fun getRetrofitInstance(context: Context): Retrofit {
        if (retrofit == null) {
            tokenManager = TokenManager(context)
            retrofit = Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(getHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .build()
        }
        return retrofit!!
    }

    fun getAuthService(context: Context): AuthService {
        if (authService == null) {
            authService = getRetrofitInstance(context).create(AuthService::class.java)
        }
        return authService!!
    }

    fun getExpensesService(context: Context): ExpensesService {
        if (expensesService == null) {
            expensesService = getRetrofitInstance(context).create(ExpensesService::class.java)
        }
        return expensesService!!
    }

    fun getSubscriptionsService(context: Context): SubscriptionsService {
        if (subscriptionsService == null) {
            subscriptionsService = getRetrofitInstance(context).create(SubscriptionsService::class.java)
        }
        return subscriptionsService!!
    }

    fun getCategoriesService(context: Context): CategoriesService {
        if (categoriesService == null) {
            categoriesService = getRetrofitInstance(context).create(CategoriesService::class.java)
        }
        return categoriesService!!
    }

    fun getBanksService(context: Context): BanksService {
        if (banksService == null) {
            banksService = getRetrofitInstance(context).create(BanksService::class.java)
        }
        return banksService!!
    }

    fun getCardsService(context: Context): CardsService {
        if (cardsService == null) {
            cardsService = getRetrofitInstance(context).create(CardsService::class.java)
        }
        return cardsService!!
    }

    fun getPreferencesService(context: Context): PreferencesService {
        if (preferencesService == null) {
            preferencesService = getRetrofitInstance(context).create(PreferencesService::class.java)
        }
        return preferencesService!!
    }

    fun getNotificationsService(context: Context): NotificationsService {
        if (notificationsService == null) {
            notificationsService = getRetrofitInstance(context).create(NotificationsService::class.java)
        }
        return notificationsService!!
    }

    fun getDashboardService(context: Context): DashboardService {
        if (dashboardService == null) {
            dashboardService = getRetrofitInstance(context).create(DashboardService::class.java)
        }
        return dashboardService!!
    }

    private fun getHttpClient(context: Context): OkHttpClient {
        tokenManager = TokenManager(context)
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // Aumentado a 60 segundos
            .readTimeout(60, TimeUnit.SECONDS)     // Aumentado a 60 segundos
            .writeTimeout(60, TimeUnit.SECONDS)    // Aumentado a 60 segundos
            .retryOnConnectionFailure(true)        // Reintentar en caso de fallo
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(TokenRefreshInterceptor(context, tokenManager!!))
            .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            })
            .cookieJar(okhttp3.CookieJar.NO_COOKIES)
            .build()
    }

    private fun createGson() = GsonBuilder()
        .setLenient()
        .create()

    fun resetInstances() {
        retrofit = null
        authService = null
        expensesService = null
        subscriptionsService = null
        categoriesService = null
        banksService = null
        cardsService = null
        preferencesService = null
        notificationsService = null
        dashboardService = null
        tokenManager = null
    }
}
