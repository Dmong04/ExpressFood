package com.tuapp.expressfood.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.tuapp.expressfood.data.local.entity.*

// -----------------------------------------------------------------------------
// UserWithOrders
// Un usuario con todas sus órdenes
// -----------------------------------------------------------------------------
data class UserWithOrders(

    @Embedded
    val user: UserEntity,

    @Relation(
        parentColumn = "uid",
        entityColumn = "client_id"
    )
    val orders: List<OrderEntity>
)

// -----------------------------------------------------------------------------
// OrderWithDetails
// Una orden con todos sus detalles de línea
// -----------------------------------------------------------------------------
data class OrderWithDetails(

    @Embedded
    val order: OrderEntity,

    @Relation(
        parentColumn = "order_id",
        entityColumn = "order_id"
    )
    val details: List<OrderDetailEntity>
)

// -----------------------------------------------------------------------------
// OrderDetailWithItem
// Un detalle de orden junto al producto asociado
// -----------------------------------------------------------------------------
data class OrderDetailWithItem(

    @Embedded
    val detail: OrderDetailEntity,

    @Relation(
        parentColumn = "item_id",
        entityColumn = "item_id"
    )
    val item: ItemEntity
)

// -----------------------------------------------------------------------------
// UserWithCartItems
// Un usuario con todos los ítems de su carrito
// -----------------------------------------------------------------------------
data class UserWithCartItems(

    @Embedded
    val user: UserEntity,

    @Relation(
        parentColumn = "uid",
        entityColumn = "client_id"
    )
    val cartItems: List<CartItemEntity>
)

// -----------------------------------------------------------------------------
// CartItemWithItem
// Un ítem de carrito junto al producto completo
// -----------------------------------------------------------------------------
data class CartItemWithItem(

    @Embedded
    val cartItem: CartItemEntity,

    @Relation(
        parentColumn = "item_id",
        entityColumn = "item_id"
    )
    val item: ItemEntity
)