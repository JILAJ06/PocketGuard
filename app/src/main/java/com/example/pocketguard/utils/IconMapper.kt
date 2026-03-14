package com.example.pocketguard.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Mapper para convertir nombres de iconos (strings) a Material Icons (ImageVector)
 * Usado para persistir y mostrar iconos de categorías
 */
object IconMapper {

    /**
     * Obtener ImageVector desde nombre de icono
     */
    fun getIconByName(iconName: String?): ImageVector {
        return when (iconName?.lowercase()) {
            // Comida y bebida
            "restaurant" -> Icons.Outlined.Restaurant
            "fastfood" -> Icons.Outlined.Fastfood
            "coffee", "localcafe" -> Icons.Outlined.LocalCafe
            "lunch", "lunchdining" -> Icons.Outlined.LunchDining
            "pizza" -> Icons.Outlined.LocalPizza
            "dinner", "diningout" -> Icons.Outlined.DinnerDining

            // Transporte
            "car", "directionscar" -> Icons.Outlined.DirectionsCar
            "bus", "directionsbus" -> Icons.Outlined.DirectionsBus
            "train", "directionsrail" -> Icons.Outlined.DirectionsRailway
            "flight", "airplane" -> Icons.Outlined.Flight
            "bike", "directionsbike" -> Icons.Outlined.DirectionsBike
            "subway" -> Icons.Outlined.Subway
            "taxi" -> Icons.Outlined.LocalTaxi

            // Entretenimiento
            "movie", "movies" -> Icons.Outlined.Movie
            "music", "musicnote" -> Icons.Outlined.MusicNote
            "videogame", "gamepad" -> Icons.Outlined.VideogameAsset
            "theater" -> Icons.Outlined.TheaterComedy
            "sports" -> Icons.Outlined.SportsSoccer

            // Compras
            "shopping", "shoppingcart" -> Icons.Outlined.ShoppingCart
            "shoppingbag" -> Icons.Outlined.ShoppingBag
            "gift", "cardgiftcard" -> Icons.Outlined.CardGiftcard
            "store" -> Icons.Outlined.Storefront
            "clothing", "checkroom" -> Icons.Outlined.Checkroom

            // Salud y fitness
            "health", "favorite" -> Icons.Outlined.FavoriteBorder
            "gym", "fitness", "fitnesscenter" -> Icons.Outlined.FitnessCenter
            "medical", "localhospital" -> Icons.Outlined.LocalHospital
            "spa" -> Icons.Outlined.Spa

            // Educación y trabajo
            "school", "education" -> Icons.Outlined.School
            "book" -> Icons.Outlined.Book
            "work", "business" -> Icons.Outlined.BusinessCenter
            "laptop", "computer" -> Icons.Outlined.Computer

            // Hogar y servicios
            "home" -> Icons.Outlined.Home
            "build", "tools" -> Icons.Outlined.Build
            "lightbulb" -> Icons.Outlined.Lightbulb
            "cleaning" -> Icons.Outlined.CleaningServices
            "plumbing" -> Icons.Outlined.Plumbing

            // Tecnología
            "phone", "smartphone" -> Icons.Outlined.PhoneAndroid
            "wifi" -> Icons.Outlined.Wifi
            "tv" -> Icons.Outlined.Tv
            "headphones" -> Icons.Outlined.Headphones
            "camera" -> Icons.Outlined.CameraAlt

            // Finanzas
            "money", "attachmoney" -> Icons.Outlined.AttachMoney
            "creditcard", "payment" -> Icons.Outlined.CreditCard
            "savings", "accountbalance" -> Icons.Outlined.AccountBalance
            "receipt" -> Icons.Outlined.Receipt

            // Otros
            "pets" -> Icons.Outlined.Pets
            "child" -> Icons.Outlined.ChildCare
            "beach" -> Icons.Outlined.BeachAccess
            "celebration", "party" -> Icons.Outlined.Celebration
            "description", "note" -> Icons.Outlined.Description

            // Default
            else -> Icons.Outlined.Category
        }
    }

    /**
     * Obtener nombre desde ImageVector (para guardar en DB)
     * Mapeo inverso de los iconos más comunes
     */
    fun getNameFromIcon(icon: ImageVector): String {
        return when (icon) {
            Icons.Outlined.Restaurant -> "restaurant"
            Icons.Outlined.Fastfood -> "fastfood"
            Icons.Outlined.LocalCafe -> "coffee"
            Icons.Outlined.DirectionsCar -> "car"
            Icons.Outlined.DirectionsBus -> "bus"
            Icons.Outlined.Flight -> "flight"
            Icons.Outlined.DirectionsBike -> "bike"
            Icons.Outlined.Movie -> "movie"
            Icons.Outlined.MusicNote -> "music"
            Icons.Outlined.ShoppingCart -> "shopping"
            Icons.Outlined.ShoppingBag -> "shoppingbag"
            Icons.Outlined.CardGiftcard -> "gift"
            Icons.Outlined.FavoriteBorder -> "health"
            Icons.Outlined.FitnessCenter -> "gym"
            Icons.Outlined.School -> "school"
            Icons.Outlined.Book -> "book"
            Icons.Outlined.Home -> "home"
            Icons.Outlined.Build -> "build"
            Icons.Outlined.PhoneAndroid -> "phone"
            Icons.Outlined.Checkroom -> "clothing"
            Icons.Outlined.Pets -> "pets"
            Icons.Outlined.BeachAccess -> "beach"
            Icons.Outlined.AttachMoney -> "money"
            Icons.Outlined.CreditCard -> "creditcard"
            else -> "category"
        }
    }

    /**
     * Lista de iconos disponibles para selección en UI
     */
    val availableIcons = listOf(
        Icons.Outlined.Restaurant,
        Icons.Outlined.Fastfood,
        Icons.Outlined.LocalCafe,
        Icons.Outlined.DirectionsCar,
        Icons.Outlined.DirectionsBus,
        Icons.Outlined.Flight,
        Icons.Outlined.DirectionsBike,
        Icons.Outlined.Movie,
        Icons.Outlined.MusicNote,
        Icons.Outlined.ShoppingCart,
        Icons.Outlined.ShoppingBag,
        Icons.Outlined.CardGiftcard,
        Icons.Outlined.FavoriteBorder,
        Icons.Outlined.FitnessCenter,
        Icons.Outlined.School,
        Icons.Outlined.Book,
        Icons.Outlined.Home,
        Icons.Outlined.Build,
        Icons.Outlined.PhoneAndroid,
        Icons.Outlined.Checkroom,
        Icons.Outlined.Pets,
        Icons.Outlined.BeachAccess,
        Icons.Outlined.AttachMoney,
        Icons.Outlined.CreditCard
    )
}

