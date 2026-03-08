package com.bayramenu.shared.repository

import com.bayramenu.shared.model.Restaurant
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {
    fun getRestaurantsStream(): Flow<List<Restaurant>>
    suspend fun getRestaurantById(id: String): Restaurant?
}
