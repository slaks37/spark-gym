package com.sparkgym.domain.model

/**
 * The slots a day's food is divided into.
 *
 * This lives in the domain rather than beside the repository because the meal
 * plans are seed data: keeping it here means the plans and their tests do not
 * have to drag the database layer in with them.
 */
enum class Meal(val displayName: String, val nameId: String) {
    BREAKFAST("Breakfast", "Sarapan"),
    LUNCH("Lunch", "Makan Siang"),
    DINNER("Dinner", "Makan Malam"),
    SNACK("Snacks", "Camilan");

    companion object {
        fun fromKey(key: String): Meal = entries.firstOrNull { it.name == key } ?: SNACK
    }
}
