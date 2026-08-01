package com.sparkgym.data.seed

import com.sparkgym.domain.model.Meal

/**
 * Full-day meal templates.
 *
 * Every item points at a food slug and a gram weight, so a plan is not a picture
 * of food — it is something the app can scale to your calorie target and drop
 * straight into your diary.
 *
 * Items marked `fixed` do not scale: a single egg stays one egg, a creatine
 * scoop stays 5 g. Everything else grows and shrinks with your budget.
 */
data class SeedMealItem(
    val foodSlug: String,
    val grams: Double,
    val fixed: Boolean = false
)

data class SeedMealSlot(
    val meal: Meal,
    val title: String,
    val timing: String,
    val items: List<SeedMealItem>
)

data class SeedMealPlan(
    val slug: String,
    val name: String,
    val description: String,
    /** What the plan totals before scaling — used as the scaling reference. */
    val baseCalories: Int,
    val goalTag: String,
    val coachNote: String,
    val slots: List<SeedMealSlot>
)

object MealPlanSeed {

    private fun item(slug: String, grams: Double, fixed: Boolean = false) = SeedMealItem(slug, grams, fixed)

    val plans: List<SeedMealPlan> = listOf(

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "cut-warung",
            name = "Cutting — Warung Edition",
            description = "A deficit built out of food you can buy on any street corner. Protein anchors " +
                "every meal, rice is portioned rather than banned, and the two snacks exist so you are not " +
                "starving by 4pm.",
            baseCalories = 1800,
            goalTag = "Fat loss",
            coachNote = "Cutting fails on hunger, not on maths. Every meal here has 30 g+ of protein and " +
                "real volume from vegetables — that combination keeps you full on fewer calories better " +
                "than anything else. Keep the rice: cutting the carb you enjoy is how people quit in week three.",
            slots = listOf(
                SeedMealSlot(Meal.BREAKFAST, "Telur, tempe, nasi merah", "07:00", listOf(
                    item("telur-rebus", 110.0),
                    item("tempe-rebus", 50.0),
                    item("nasi-merah", 100.0),
                    item("tumis-kangkung", 120.0),
                    item("kopi-hitam", 240.0, fixed = true)
                )),
                SeedMealSlot(Meal.SNACK, "Buah + yoghurt", "10:00", listOf(
                    item("greek-yogurt", 170.0),
                    item("pepaya", 150.0)
                )),
                SeedMealSlot(Meal.LUNCH, "Ayam bakar + sayur", "13:00", listOf(
                    item("ayam-bakar", 130.0),
                    item("nasi-putih", 120.0),
                    item("capcay", 200.0),
                    item("sambal", 15.0, fixed = true)
                )),
                SeedMealSlot(Meal.SNACK, "Pre-workout", "16:30", listOf(
                    item("pisang", 120.0),
                    item("kopi-hitam", 240.0, fixed = true)
                )),
                SeedMealSlot(Meal.DINNER, "Ikan + tahu", "19:30", listOf(
                    item("ikan-kembung", 120.0),
                    item("tahu-putih", 100.0),
                    item("nasi-merah", 100.0),
                    item("sayur-bayam", 150.0)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "cut-high-protein",
            name = "Cutting — High Protein",
            description = "For an aggressive deficit where holding muscle is the whole point. Protein sits " +
                "near 2.4 g per kg, fat is kept at the functional minimum, and carbohydrate is placed " +
                "around training.",
            baseCalories = 2000,
            goalTag = "Aggressive cut",
            coachNote = "Run this for eight to twelve weeks, not forever. Two things end a cut early: " +
                "losing strength and losing your mind. This plan protects the first with protein and the " +
                "second by putting most of the carbohydrate in the meal before and after you train.",
            slots = listOf(
                SeedMealSlot(Meal.BREAKFAST, "Oats + whey", "07:00", listOf(
                    item("oatmeal", 60.0),
                    item("whey-protein", 30.0),
                    item("putih-telur", 100.0),
                    item("blueberry", 80.0)
                )),
                SeedMealSlot(Meal.LUNCH, "Chicken and rice", "12:30", listOf(
                    item("dada-ayam-panggang", 180.0),
                    item("nasi-merah", 130.0),
                    item("brokoli", 150.0),
                    item("minyak-zaitun", 7.0, fixed = true)
                )),
                SeedMealSlot(Meal.SNACK, "Pre-workout", "16:00", listOf(
                    item("pisang", 120.0),
                    item("whey-protein", 30.0),
                    item("creatine", 5.0, fixed = true)
                )),
                SeedMealSlot(Meal.DINNER, "Beef and potato", "20:00", listOf(
                    item("daging-sapi-tanpa-lemak", 150.0),
                    item("kentang-panggang", 250.0),
                    item("selada", 100.0),
                    item("timun", 100.0)
                )),
                SeedMealSlot(Meal.SNACK, "Before bed", "22:30", listOf(
                    item("cottage-cheese", 150.0),
                    item("almond", 15.0)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "maintain-balanced",
            name = "Maintenance — Balanced",
            description = "What eating well looks like when you are not chasing anything. Enough protein " +
                "to keep building, enough carbohydrate to train hard, and enough room for the meal you " +
                "did not plan.",
            baseCalories = 2400,
            goalTag = "Maintenance",
            coachNote = "Most people should spend more of the year here than they do. Maintenance is where " +
                "you get strong, where your lifts consolidate, and where your relationship with food " +
                "recovers from the last cut.",
            slots = listOf(
                SeedMealSlot(Meal.BREAKFAST, "Nasi uduk sehat", "07:00", listOf(
                    item("nasi-uduk", 150.0),
                    item("telur-dadar", 120.0),
                    item("tempe-goreng", 60.0),
                    item("timun", 80.0)
                )),
                SeedMealSlot(Meal.SNACK, "Mid-morning", "10:30", listOf(
                    item("pisang", 120.0),
                    item("selai-kacang", 20.0)
                )),
                SeedMealSlot(Meal.LUNCH, "Soto + nasi", "13:00", listOf(
                    item("soto-ayam", 350.0),
                    item("nasi-putih", 150.0),
                    item("telur-rebus", 55.0),
                    item("sambal", 15.0, fixed = true)
                )),
                SeedMealSlot(Meal.SNACK, "Afternoon", "16:30", listOf(
                    item("greek-yogurt", 170.0),
                    item("granola", 30.0)
                )),
                SeedMealSlot(Meal.DINNER, "Sate + gado-gado", "19:30", listOf(
                    item("sate-ayam", 150.0),
                    item("nasi-putih", 130.0),
                    item("gado-gado", 200.0)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "lean-bulk",
            name = "Lean Bulk",
            description = "A controlled surplus — about 300 kcal over maintenance, which is roughly 0.25 to " +
                "0.5 kg of gain a month. Slow enough that most of it is muscle.",
            baseCalories = 2900,
            goalTag = "Lean gaining",
            coachNote = "The single most common mistake in bulking is going too fast. If you are gaining " +
                "more than 0.5 kg a week you are buying fat you will pay to remove later. Weigh yourself " +
                "daily, average it weekly, and only add calories when the average has been flat for two weeks.",
            slots = listOf(
                SeedMealSlot(Meal.BREAKFAST, "Big breakfast", "07:00", listOf(
                    item("oatmeal", 80.0),
                    item("susu-uht", 300.0),
                    item("telur-rebus", 110.0),
                    item("pisang", 120.0),
                    item("selai-kacang", 25.0)
                )),
                SeedMealSlot(Meal.SNACK, "Mid-morning", "10:30", listOf(
                    item("roti-gandum", 70.0),
                    item("keju-cheddar", 30.0),
                    item("susu-uht", 250.0)
                )),
                SeedMealSlot(Meal.LUNCH, "Nasi padang, sensibly", "13:00", listOf(
                    item("nasi-putih", 200.0),
                    item("ayam-pop", 130.0),
                    item("sayur-lodeh", 150.0),
                    item("tempe-goreng", 60.0)
                )),
                SeedMealSlot(Meal.SNACK, "Post-workout", "17:30", listOf(
                    item("whey-protein", 30.0),
                    item("kurma", 40.0),
                    item("creatine", 5.0, fixed = true)
                )),
                SeedMealSlot(Meal.DINNER, "Beef and rice", "20:00", listOf(
                    item("daging-sapi-giling", 170.0),
                    item("nasi-putih", 200.0),
                    item("capcay", 200.0),
                    item("minyak-zaitun", 10.0, fixed = true)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "mass-gain",
            name = "Mass Gain — Hard Gainer",
            description = "For people who genuinely struggle to eat enough. Calorie-dense, liquid calories " +
                "where solid food will not fit, and five feeds a day so no single meal is unmanageable.",
            baseCalories = 3600,
            goalTag = "Mass",
            coachNote = "If you are 'eating a lot' and not gaining, you are not eating a lot — you are " +
                "eating a lot on some days. Log everything for two weeks and the answer will be obvious. " +
                "Liquid calories are your friend here: a shake goes down when a fifth plate of rice will not.",
            slots = listOf(
                SeedMealSlot(Meal.BREAKFAST, "Loaded oats", "06:30", listOf(
                    item("oatmeal", 100.0),
                    item("susu-uht", 400.0),
                    item("selai-kacang", 32.0),
                    item("pisang", 120.0),
                    item("madu", 21.0)
                )),
                SeedMealSlot(Meal.SNACK, "Shake", "10:00", listOf(
                    item("mass-gainer", 100.0),
                    item("susu-uht", 300.0),
                    item("alpukat", 70.0)
                )),
                SeedMealSlot(Meal.LUNCH, "Rice, chicken, egg", "13:00", listOf(
                    item("nasi-putih", 250.0),
                    item("ayam-goreng", 150.0),
                    item("telur-balado", 65.0),
                    item("sayur-asem", 200.0)
                )),
                SeedMealSlot(Meal.SNACK, "Post-workout", "17:30", listOf(
                    item("whey-protein", 30.0),
                    item("nasi-putih", 150.0),
                    item("kurma", 50.0),
                    item("creatine", 5.0, fixed = true)
                )),
                SeedMealSlot(Meal.DINNER, "Big dinner", "20:30", listOf(
                    item("steak-sirloin", 200.0),
                    item("kentang-panggang", 300.0),
                    item("brokoli", 150.0),
                    item("mentega", 14.0, fixed = true)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "vegetarian-protein",
            name = "Vegetarian — High Protein",
            description = "Indonesia is the best place in the world to be a vegetarian lifter: tempe, tahu " +
                "and oncom do most of the work. Dairy and eggs fill the rest.",
            baseCalories = 2300,
            goalTag = "Vegetarian",
            coachNote = "Plant protein is slightly less efficient gram for gram, so aim about 10% higher " +
                "than you would on an omnivorous plan. Tempe is the standout — it is fermented, so it " +
                "digests better than plain soy, and it has more protein per rupiah than almost anything.",
            slots = listOf(
                SeedMealSlot(Meal.BREAKFAST, "Tempe scramble", "07:00", listOf(
                    item("telur-dadar", 120.0),
                    item("tempe-rebus", 80.0),
                    item("roti-gandum", 70.0),
                    item("alpukat", 50.0)
                )),
                SeedMealSlot(Meal.SNACK, "Protein shake", "10:30", listOf(
                    item("whey-protein", 30.0),
                    item("susu-kedelai", 250.0),
                    item("pisang", 120.0)
                )),
                SeedMealSlot(Meal.LUNCH, "Gado-gado plus", "13:00", listOf(
                    item("gado-gado", 300.0),
                    item("tahu-putih", 100.0),
                    item("telur-rebus", 55.0),
                    item("nasi-merah", 120.0)
                )),
                SeedMealSlot(Meal.SNACK, "Afternoon", "16:30", listOf(
                    item("edamame", 100.0),
                    item("greek-yogurt", 170.0)
                )),
                SeedMealSlot(Meal.DINNER, "Tempe curry", "19:30", listOf(
                    item("tempe-goreng", 100.0),
                    item("oncom", 50.0),
                    item("sayur-lodeh", 200.0),
                    item("nasi-merah", 130.0),
                    item("kacang-merah", 100.0)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "budget-anak-kos",
            name = "Budget — Anak Kos",
            description = "Built around the cheapest complete proteins available: eggs, tempe, tahu and " +
                "chicken thigh. No supplements, no imported anything, and nothing that needs a kitchen " +
                "you do not have.",
            baseCalories = 2400,
            goalTag = "Budget",
            coachNote = "Eggs and tempe are the two best protein-per-rupiah foods in the country, and " +
                "neither needs refrigeration for a day. You do not need whey, you do not need chicken " +
                "breast, and you do not need a gym membership to eat like a lifter.",
            slots = listOf(
                SeedMealSlot(Meal.BREAKFAST, "Telur + nasi", "07:00", listOf(
                    item("telur-dadar", 120.0),
                    item("nasi-putih", 150.0),
                    item("tempe-goreng", 60.0),
                    item("sambal", 15.0, fixed = true)
                )),
                SeedMealSlot(Meal.SNACK, "Warung snack", "10:30", listOf(
                    item("pisang", 240.0),
                    item("kacang-tanah", 30.0)
                )),
                SeedMealSlot(Meal.LUNCH, "Warteg", "13:00", listOf(
                    item("nasi-putih", 200.0),
                    item("paha-ayam", 120.0),
                    item("tahu-goreng", 60.0),
                    item("tumis-kangkung", 120.0)
                )),
                SeedMealSlot(Meal.SNACK, "Sore", "16:30", listOf(
                    item("susu-uht", 250.0),
                    item("roti-tawar", 60.0)
                )),
                SeedMealSlot(Meal.DINNER, "Lele + lalapan", "19:30", listOf(
                    item("pecel-lele", 200.0),
                    item("nasi-putih", 150.0),
                    item("timun", 100.0),
                    item("selada", 60.0)
                ))
            )
        ),

        // ------------------------------------------------------------------
        SeedMealPlan(
            slug = "if-16-8",
            name = "16:8 Intermittent Fasting",
            description = "All the food in an eight-hour window, from noon to eight. Three large meals " +
                "instead of five small ones, which suits people who find grazing harder than fasting.",
            baseCalories = 2200,
            goalTag = "Fat loss",
            coachNote = "Fasting does not burn more fat than eating the same calories across the day — the " +
                "research is clear on that. What it does do is make a deficit easier for some people to " +
                "hold, because three satisfying meals beat five unsatisfying ones. If it suits you, use it. " +
                "If it makes you miserable, it is not a rule.",
            slots = listOf(
                SeedMealSlot(Meal.LUNCH, "Break the fast — 12:00", "12:00", listOf(
                    item("dada-ayam-panggang", 200.0),
                    item("nasi-merah", 150.0),
                    item("brokoli", 150.0),
                    item("alpukat", 70.0)
                )),
                SeedMealSlot(Meal.SNACK, "Pre-workout — 16:00", "16:00", listOf(
                    item("greek-yogurt", 170.0),
                    item("kurma", 30.0),
                    item("whey-protein", 30.0)
                )),
                SeedMealSlot(Meal.DINNER, "Final meal — 19:30", "19:30", listOf(
                    item("salmon", 170.0),
                    item("kentang-panggang", 250.0),
                    item("sayur-bayam", 150.0),
                    item("minyak-zaitun", 10.0, fixed = true),
                    item("cottage-cheese", 150.0)
                ))
            )
        )
    )

    fun bySlug(slug: String): SeedMealPlan? = plans.firstOrNull { it.slug == slug }
}
