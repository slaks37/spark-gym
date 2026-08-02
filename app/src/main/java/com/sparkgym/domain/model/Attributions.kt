package com.sparkgym.domain.model

/**
 * Third-party work this app ships or relies on, and the terms it comes under.
 *
 * This is a legal obligation rather than a courtesy. The anatomical mesh is
 * CC BY-SA 4.0 and Open Food Facts is ODbL: both require attribution *to the
 * user*, which a file in the source tree does not provide. It has to be
 * reachable from inside the installed app.
 *
 * Static data with no Android dependency, so the list can be unit-tested — a
 * missing licence line is a compliance failure, not a cosmetic one.
 */
object Attributions {

    data class Entry(
        val name: String,
        val licence: String,
        val url: String,
        /** What it is used for, in plain words. */
        val use: String,
        /** Set when the licence obliges derivatives to stay under it. */
        val shareAlike: Boolean = false
    )

    val all: List<Entry> = listOf(
        Entry(
            name = "Z-Anatomy",
            licence = "CC BY-SA 4.0",
            url = "https://www.z-anatomy.com",
            use = "The anatomical muscle model shown on the heat map. Filtered to " +
                "the superficial muscles this app tracks, merged into 19 groups and " +
                "decimated to run on a phone.",
            shareAlike = true
        ),
        Entry(
            name = "Open Food Facts",
            licence = "ODbL 1.0",
            url = "https://world.openfoodfacts.org",
            use = "Barcode and online food lookup. The bundled food list is this " +
                "app's own; anything fetched by barcode comes from Open Food Facts.",
            shareAlike = true
        ),
        Entry(
            name = "Three.js",
            licence = "MIT",
            url = "https://threejs.org",
            use = "Renders the 3D body. Bundled in the app so the heat map works offline."
        ),
        Entry(
            name = "Jetpack Compose, Room, DataStore and the AndroidX libraries",
            licence = "Apache 2.0",
            url = "https://developer.android.com/jetpack",
            use = "The interface, the database and the settings store."
        ),
        Entry(
            name = "Kotlin, kotlinx.coroutines, kotlinx.serialization",
            licence = "Apache 2.0",
            url = "https://kotlinlang.org",
            use = "The language and its standard libraries."
        ),
        Entry(
            name = "Retrofit and OkHttp",
            licence = "Apache 2.0",
            url = "https://square.github.io/retrofit/",
            use = "Talking to Fitbit and Open Food Facts."
        ),
        Entry(
            name = "Material Symbols",
            licence = "Apache 2.0",
            url = "https://fonts.google.com/icons",
            use = "The icons throughout the app."
        )
    )

    /** Licences that oblige derived work to carry the same terms. */
    val shareAlike: List<Entry> get() = all.filter { it.shareAlike }
}
