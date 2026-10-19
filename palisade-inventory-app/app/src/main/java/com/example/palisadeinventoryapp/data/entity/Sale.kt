package com.example.palisadeinventoryapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT   // Don't allow deleting a product that has sales history
        )
    ],
    indices = [Index("productId")]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val quantitySold: Int,
    val salePrice: Double,  // Price at the time of sale (price can change)
    val date: Long = System.currentTimeMillis()
)
