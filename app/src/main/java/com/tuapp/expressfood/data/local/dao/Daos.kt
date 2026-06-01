package com.tuapp.expressfood.data.local.dao

import androidx.room.*
import com.tuapp.expressfood.data.local.entity.*
import com.tuapp.expressfood.data.local.relation.*
import kotlinx.coroutines.flow.Flow

// =============================================================================
// UserDao
// =============================================================================
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getById(uid: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<UserEntity>>

    @Transaction
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserWithOrders(uid: String): Flow<UserWithOrders?>

    @Transaction
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserWithCartItems(uid: String): Flow<UserWithCartItems?>
}

// =============================================================================
// ItemDao
// =============================================================================
@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("SELECT * FROM items WHERE item_id = :itemId")
    suspend fun getById(itemId: String): ItemEntity?

    @Query("SELECT * FROM items WHERE active = 1")
    fun getActiveItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE synced = 0")
    suspend fun getUnsynced(): List<ItemEntity>

    @Query("UPDATE items SET synced = 1 WHERE item_id = :itemId")
    suspend fun markAsSynced(itemId: String)
}

// =============================================================================
// OrderDao
// =============================================================================
@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: OrderEntity)

    @Update
    suspend fun update(order: OrderEntity)

    @Delete
    suspend fun delete(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE order_id = :orderId")
    suspend fun getById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE client_id = :clientId ORDER BY date DESC")
    fun getByClient(clientId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = :status")
    fun getByStatus(status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE synced = 0")
    suspend fun getUnsynced(): List<OrderEntity>

    @Query("UPDATE orders SET synced = 1 WHERE order_id = :orderId")
    suspend fun markAsSynced(orderId: String)

    @Transaction
    @Query("SELECT * FROM orders WHERE order_id = :orderId")
    fun getOrderWithDetails(orderId: String): Flow<OrderWithDetails?>

    @Transaction
    @Query("SELECT * FROM orders WHERE client_id = :clientId ORDER BY date DESC")
    fun getOrdersWithDetailsByClient(clientId: String): Flow<List<OrderWithDetails>>
}

// =============================================================================
// OrderDetailDao
// =============================================================================
@Dao
interface OrderDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: OrderDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(details: List<OrderDetailEntity>)

    @Update
    suspend fun update(detail: OrderDetailEntity)

    @Delete
    suspend fun delete(detail: OrderDetailEntity)

    @Query("SELECT * FROM order_detail WHERE order_id = :orderId")
    fun getByOrder(orderId: String): Flow<List<OrderDetailEntity>>

    @Transaction
    @Query("SELECT * FROM order_detail WHERE order_id = :orderId")
    fun getDetailsWithItems(orderId: String): Flow<List<OrderDetailWithItem>>

    @Query("UPDATE order_detail SET rating = :rating WHERE detail_id = :detailId")
    suspend fun updateRating(detailId: String, rating: Float)
}

// =============================================================================
// CartItemDao
// =============================================================================
@Dao
interface CartItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItemEntity)

    @Update
    suspend fun update(cartItem: CartItemEntity)

    @Delete
    suspend fun delete(cartItem: CartItemEntity)

    @Query("SELECT * FROM cart_item WHERE client_id = :clientId")
    fun getByClient(clientId: String): Flow<List<CartItemEntity>>

    @Transaction
    @Query("SELECT * FROM cart_item WHERE client_id = :clientId")
    fun getCartWithItems(clientId: String): Flow<List<CartItemWithItem>>

    @Query("DELETE FROM cart_item WHERE client_id = :clientId")
    suspend fun clearCart(clientId: String)

    @Query("SELECT COUNT(*) FROM cart_item WHERE client_id = :clientId")
    fun getCartItemCount(clientId: String): Flow<Int>
}

// =============================================================================
// StorageDao
// =============================================================================
@Dao
interface StorageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(storage: StorageEntity)

    @Delete
    suspend fun delete(storage: StorageEntity)

    @Query("SELECT * FROM storage WHERE item_id = :itemId")
    fun getByItem(itemId: String): Flow<List<StorageEntity>>

    @Query("SELECT * FROM storage WHERE path = :path")
    suspend fun getByPath(path: String): StorageEntity?
}