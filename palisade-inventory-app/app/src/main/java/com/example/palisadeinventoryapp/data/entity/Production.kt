package com.example.palisadeinventoryapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "production",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT   // Keeps the production history even if the product is later archived
        )
    ],
    indices = [Index("productId")]
)
data class Production(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val quantityProduced: Int,
    val date: Long = System.currentTimeMillis()
)
