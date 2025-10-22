package com.cnn.mushroom.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cnn.mushroom.R
data class Mushroom(
    @DrawableRes val imageResourceId: Int,
    @StringRes val name: Int,
)


@Entity(tableName = "mushrooms")
data class MushroomEntity(

    val imagePath: String,
    val name: String,
    val timestamp: Long,
    val confidenceScore: Float? = null,
    val isEdible: Boolean? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}



val mushrooms = listOf(
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
    Mushroom(R.drawable.logo_background, R.string.app_name),
)