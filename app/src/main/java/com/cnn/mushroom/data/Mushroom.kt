package com.cnn.mushroom.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.cnn.mushroom.R
data class Mushroom(
    @DrawableRes val imageResourceId: Int,
    @StringRes val name: Int,
)

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