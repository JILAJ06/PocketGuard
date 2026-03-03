package com.example.pocketguard.presentation.di

import android.content.Context
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.repository.AuthRepository
import com.example.pocketguard.data.repository.BanksRepository
import com.example.pocketguard.data.repository.CardsRepository
import com.example.pocketguard.data.repository.CategoriesRepository
import com.example.pocketguard.data.repository.DashboardRepository
import com.example.pocketguard.data.repository.ExpensesRepository
import com.example.pocketguard.data.repository.NotificationsRepository
import com.example.pocketguard.data.repository.PreferencesRepository
import com.example.pocketguard.data.repository.SubscriptionsRepository
import com.example.pocketguard.data.storage.TokenManager
import com.example.pocketguard.presentation.session.SessionManager
import com.example.pocketguard.presentation.viewmodel.*

object ServiceLocator {

    private var tokenManager: TokenManager? = null
    private var authRepository: AuthRepository? = null
    private var expensesRepository: ExpensesRepository? = null
    private var subscriptionsRepository: SubscriptionsRepository? = null
    private var categoriesRepository: CategoriesRepository? = null
    private var banksRepository: BanksRepository? = null
    private var cardsRepository: CardsRepository? = null
    private var preferencesRepository: PreferencesRepository? = null
    private var notificationsRepository: NotificationsRepository? = null
    private var dashboardRepository: DashboardRepository? = null
    private var sessionManager: SessionManager? = null
    private var loginViewModelFactory: LoginViewModelFactory? = null
    private var registerViewModelFactory: RegisterViewModelFactory? = null
    private var authViewModelFactory: AuthViewModelFactory? = null
    private var expensesViewModelFactory: ExpensesViewModelFactory? = null
    private var subscriptionsViewModelFactory: SubscriptionsViewModelFactory? = null
    private var addSubscriptionViewModelFactory: AddSubscriptionViewModelFactory? = null
    private var categoriesViewModelFactory: CategoriesViewModelFactory? = null
    private var banksViewModelFactory: BanksViewModelFactory? = null
    private var cardsViewModelFactory: CardsViewModelFactory? = null
    private var preferencesViewModelFactory: PreferencesViewModelFactory? = null
    private var notificationsViewModelFactory: NotificationsViewModelFactory? = null
    private var dashboardViewModelFactory: DashboardViewModelFactory? = null

    fun initializeServices(context: Context) {
        if (tokenManager == null) {
            tokenManager = TokenManager(context)
            val authService = RetrofitClient.getAuthService(context)
            authRepository = AuthRepository(authService, tokenManager!!)
            expensesRepository = ExpensesRepository(context)
            subscriptionsRepository = SubscriptionsRepository(context)
            categoriesRepository = CategoriesRepository(context)
            banksRepository = BanksRepository(context)
            cardsRepository = CardsRepository(context)
            preferencesRepository = PreferencesRepository(context)
            notificationsRepository = NotificationsRepository(context)
            dashboardRepository = DashboardRepository(RetrofitClient.getDashboardService(context))
            sessionManager = SessionManager(context, tokenManager!!)
            loginViewModelFactory = LoginViewModelFactory(authRepository!!)
            registerViewModelFactory = RegisterViewModelFactory(authRepository!!)
            authViewModelFactory = AuthViewModelFactory(authRepository!!)
            expensesViewModelFactory = ExpensesViewModelFactory(expensesRepository!!)
            subscriptionsViewModelFactory = SubscriptionsViewModelFactory(subscriptionsRepository!!)
            addSubscriptionViewModelFactory = AddSubscriptionViewModelFactory(subscriptionsRepository!!)
            categoriesViewModelFactory = CategoriesViewModelFactory(categoriesRepository!!)
            banksViewModelFactory = BanksViewModelFactory(banksRepository!!)
            cardsViewModelFactory = CardsViewModelFactory(cardsRepository!!)
            preferencesViewModelFactory = PreferencesViewModelFactory(preferencesRepository!!, context)
            notificationsViewModelFactory = NotificationsViewModelFactory(notificationsRepository!!)
            dashboardViewModelFactory = DashboardViewModelFactory(dashboardRepository!!)
        }
    }

    fun getTokenManager(): TokenManager {
        return tokenManager ?: throw IllegalStateException("Services not initialized")
    }

    fun getAuthRepository(): AuthRepository {
        return authRepository ?: throw IllegalStateException("Services not initialized")
    }

    fun getSessionManager(): SessionManager {
        return sessionManager ?: throw IllegalStateException("Services not initialized")
    }

    fun getLoginViewModelFactory(): LoginViewModelFactory {
        return loginViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getRegisterViewModelFactory(): RegisterViewModelFactory {
        return registerViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getAuthViewModelFactory(): AuthViewModelFactory {
        return authViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getExpensesViewModelFactory(): ExpensesViewModelFactory {
        return expensesViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getSubscriptionsViewModelFactory(): SubscriptionsViewModelFactory {
        return subscriptionsViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getAddSubscriptionViewModelFactory(): AddSubscriptionViewModelFactory {
        return addSubscriptionViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getCategoriesViewModelFactory(): CategoriesViewModelFactory {
        return categoriesViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getBanksViewModelFactory(): BanksViewModelFactory {
        return banksViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getCardsViewModelFactory(): CardsViewModelFactory {
        return cardsViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getPreferencesViewModelFactory(): PreferencesViewModelFactory {
        return preferencesViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getNotificationsViewModelFactory(): NotificationsViewModelFactory {
        return notificationsViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun getDashboardViewModelFactory(): DashboardViewModelFactory {
        return dashboardViewModelFactory ?: throw IllegalStateException("Services not initialized")
    }

    fun reset() {
        tokenManager = null
        authRepository = null
        expensesRepository = null
        subscriptionsRepository = null
        categoriesRepository = null
        banksRepository = null
        cardsRepository = null
        preferencesRepository = null
        notificationsRepository = null
        dashboardRepository = null
        sessionManager = null
        loginViewModelFactory = null
        registerViewModelFactory = null
        authViewModelFactory = null
        expensesViewModelFactory = null
        subscriptionsViewModelFactory = null
        addSubscriptionViewModelFactory = null
        categoriesViewModelFactory = null
        banksViewModelFactory = null
        cardsViewModelFactory = null
        preferencesViewModelFactory = null
        notificationsViewModelFactory = null
        dashboardViewModelFactory = null
        RetrofitClient.resetInstances()
    }
}
