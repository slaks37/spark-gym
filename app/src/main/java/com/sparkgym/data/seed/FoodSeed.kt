package com.sparkgym.data.seed

data class SeedFood(
    val slug: String,
    val name: String,
    val brand: String = "",
    /** Macros are per 100 g (or 100 ml for drinks). */
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val servingLabel: String = "100 g",
    val servingGrams: Double = 100.0
)

/**
 * Starter food database. Weighted toward what an Indonesian user actually eats,
 * because a calorie tracker that only knows about oatmeal and Greek yoghurt gets
 * abandoned in a week. Everything is stored per 100 g so portions scale cleanly.
 */
object FoodSeed {

    private fun f(
        slug: String, name: String, kcal: Double, p: Double, c: Double, fat: Double,
        fiber: Double = 0.0, serving: String = "100 g", grams: Double = 100.0, brand: String = ""
    ) = SeedFood(slug, name, brand, kcal, p, c, fat, fiber, serving, grams)

    /** Staples plus the warung / restaurant / supplement shelf. */
    val foods: List<SeedFood> get() = staples + FoodSeedExtended.foods

    private val staples: List<SeedFood> = listOf(

        // ---- Indonesian staples ----
        f("nasi-putih", "Nasi Putih (steamed white rice)", 130.0, 2.7, 28.2, 0.3, 0.4, "1 centong (150 g)", 150.0),
        f("nasi-merah", "Nasi Merah (brown rice)", 111.0, 2.6, 23.0, 0.9, 1.8, "1 centong (150 g)", 150.0),
        f("nasi-goreng", "Nasi Goreng", 186.0, 5.4, 24.0, 7.3, 0.8, "1 porsi (250 g)", 250.0),
        f("mie-goreng", "Mie Goreng", 210.0, 5.0, 27.0, 9.0, 1.2, "1 porsi (200 g)", 200.0),
        f("mie-instan", "Mie Instan (rebus, dengan bumbu)", 448.0, 9.4, 60.0, 18.0, 2.0, "1 bungkus (85 g)", 85.0),
        f("bubur-ayam", "Bubur Ayam", 95.0, 4.5, 13.0, 2.6, 0.5, "1 mangkuk (300 g)", 300.0),
        f("ayam-goreng", "Ayam Goreng", 260.0, 24.0, 8.0, 15.0, 0.3, "1 potong (100 g)", 100.0),
        f("ayam-bakar", "Ayam Bakar", 195.0, 27.0, 3.5, 8.0, 0.2, "1 potong (110 g)", 110.0),
        f("dada-ayam-rebus", "Dada Ayam Rebus (skinless)", 165.0, 31.0, 0.0, 3.6, 0.0, "1 potong (120 g)", 120.0),
        f("rendang", "Rendang Sapi", 293.0, 22.0, 6.0, 20.0, 1.0, "1 potong (80 g)", 80.0),
        f("sate-ayam", "Sate Ayam (tanpa bumbu kacang)", 190.0, 25.0, 3.0, 8.0, 0.0, "5 tusuk (100 g)", 100.0),
        f("bumbu-kacang", "Bumbu Kacang", 380.0, 13.0, 20.0, 28.0, 4.0, "2 sdm (30 g)", 30.0),
        f("tempe-goreng", "Tempe Goreng", 225.0, 18.5, 12.0, 12.0, 5.0, "2 potong (60 g)", 60.0),
        f("tempe-rebus", "Tempe Kukus", 192.0, 20.3, 7.6, 10.8, 6.0, "1 potong (50 g)", 50.0),
        f("tahu-putih", "Tahu Putih", 76.0, 8.1, 1.9, 4.8, 0.4, "1 potong (50 g)", 50.0),
        f("tahu-goreng", "Tahu Goreng", 190.0, 12.0, 6.0, 13.0, 1.0, "2 potong (60 g)", 60.0),
        f("telur-rebus", "Telur Ayam Rebus", 155.0, 12.6, 1.1, 10.6, 0.0, "1 butir (55 g)", 55.0),
        f("telur-dadar", "Telur Dadar", 210.0, 13.0, 1.5, 16.5, 0.0, "1 butir (60 g)", 60.0),
        f("ikan-lele-goreng", "Lele Goreng", 240.0, 22.0, 4.0, 15.0, 0.0, "1 ekor (120 g)", 120.0),
        f("ikan-kembung", "Ikan Kembung", 125.0, 21.4, 0.0, 4.0, 0.0, "1 ekor (90 g)", 90.0),
        f("gado-gado", "Gado-gado", 137.0, 6.0, 12.0, 7.5, 3.0, "1 porsi (300 g)", 300.0),
        f("soto-ayam", "Soto Ayam", 58.0, 5.5, 3.5, 2.4, 0.4, "1 mangkuk (350 g)", 350.0),
        f("bakso", "Bakso Sapi (kuah)", 90.0, 8.0, 6.0, 4.0, 0.3, "1 mangkuk (300 g)", 300.0),
        f("pecel-lele", "Pecel Lele + Sambal", 265.0, 20.0, 8.0, 17.0, 1.5, "1 porsi (200 g)", 200.0),
        f("sayur-bayam", "Sayur Bayam Bening", 25.0, 2.5, 3.0, 0.4, 2.0, "1 mangkuk (150 g)", 150.0),
        f("capcay", "Capcay", 65.0, 3.5, 7.0, 2.8, 2.2, "1 porsi (200 g)", 200.0),
        f("kerupuk", "Kerupuk Udang", 470.0, 5.0, 62.0, 22.0, 1.0, "5 keping (20 g)", 20.0),
        f("sambal", "Sambal Terasi", 90.0, 2.5, 8.0, 5.5, 2.5, "1 sdm (15 g)", 15.0),
        f("pisang-goreng", "Pisang Goreng", 260.0, 2.5, 38.0, 11.0, 2.0, "2 potong (80 g)", 80.0),
        f("martabak-manis", "Martabak Manis", 380.0, 7.0, 48.0, 18.0, 1.5, "1 potong (100 g)", 100.0),
        f("es-teh-manis", "Es Teh Manis", 38.0, 0.0, 9.5, 0.0, 0.0, "1 gelas (250 ml)", 250.0),
        f("kopi-susu", "Kopi Susu Gula Aren", 75.0, 1.5, 12.0, 2.2, 0.0, "1 cup (250 ml)", 250.0),

        // ---- Proteins ----
        f("dada-ayam-mentah", "Chicken Breast, raw", 120.0, 22.5, 0.0, 2.6, 0.0, "1 fillet (150 g)", 150.0),
        f("paha-ayam", "Chicken Thigh, skinless", 177.0, 24.8, 0.0, 8.2, 0.0, "1 piece (110 g)", 110.0),
        f("daging-sapi-giling", "Beef Mince, 10% fat", 217.0, 26.0, 0.0, 12.0, 0.0, "1 serving (150 g)", 150.0),
        f("steak-sirloin", "Beef Sirloin Steak", 206.0, 29.0, 0.0, 9.8, 0.0, "1 steak (200 g)", 200.0),
        f("salmon", "Salmon Fillet", 208.0, 20.4, 0.0, 13.4, 0.0, "1 fillet (150 g)", 150.0),
        f("tuna-kaleng", "Tuna, canned in water", 116.0, 25.5, 0.0, 0.8, 0.0, "1 can (95 g)", 95.0),
        f("udang", "Prawns", 99.0, 24.0, 0.2, 0.3, 0.0, "1 serving (100 g)", 100.0),
        f("putih-telur", "Egg White", 52.0, 10.9, 0.7, 0.2, 0.0, "1 white (33 g)", 33.0),
        f("whey-protein", "Whey Protein Isolate", 373.0, 82.0, 6.0, 3.0, 0.0, "1 scoop (30 g)", 30.0),
        f("casein", "Casein Protein", 360.0, 78.0, 8.0, 2.5, 0.0, "1 scoop (32 g)", 32.0),
        f("greek-yogurt", "Greek Yoghurt, plain 0%", 59.0, 10.2, 3.6, 0.4, 0.0, "1 pot (170 g)", 170.0),
        f("susu-uht", "Milk, full cream", 61.0, 3.2, 4.8, 3.3, 0.0, "1 glass (250 ml)", 250.0),
        f("susu-skim", "Milk, skimmed", 34.0, 3.4, 5.0, 0.1, 0.0, "1 glass (250 ml)", 250.0),
        f("keju-cheddar", "Cheddar Cheese", 402.0, 25.0, 1.3, 33.0, 0.0, "1 slice (20 g)", 20.0),
        f("cottage-cheese", "Cottage Cheese", 98.0, 11.1, 3.4, 4.3, 0.0, "1 serving (150 g)", 150.0),

        // ---- Carbs ----
        f("oatmeal", "Rolled Oats, dry", 389.0, 16.9, 66.3, 6.9, 10.6, "1 serving (40 g)", 40.0),
        f("roti-tawar", "White Bread", 265.0, 9.0, 49.0, 3.2, 2.7, "1 slice (30 g)", 30.0),
        f("roti-gandum", "Wholemeal Bread", 247.0, 13.0, 41.0, 3.4, 7.0, "1 slice (35 g)", 35.0),
        f("kentang-rebus", "Potato, boiled", 87.0, 1.9, 20.1, 0.1, 1.8, "1 medium (170 g)", 170.0),
        f("ubi-jalar", "Sweet Potato", 86.0, 1.6, 20.1, 0.1, 3.0, "1 medium (150 g)", 150.0),
        f("singkong", "Singkong (cassava), boiled", 160.0, 1.4, 38.1, 0.3, 1.8, "1 potong (120 g)", 120.0),
        f("jagung", "Sweetcorn", 86.0, 3.2, 19.0, 1.2, 2.7, "1 cob (90 g)", 90.0),
        f("pasta", "Pasta, cooked", 158.0, 5.8, 30.9, 0.9, 1.8, "1 serving (180 g)", 180.0),
        f("quinoa", "Quinoa, cooked", 120.0, 4.4, 21.3, 1.9, 2.8, "1 serving (150 g)", 150.0),
        f("beras-ketan", "Sticky Rice", 97.0, 2.0, 21.1, 0.2, 1.0, "1 serving (150 g)", 150.0),

        // ---- Fruit & veg ----
        f("pisang", "Banana", 89.0, 1.1, 22.8, 0.3, 2.6, "1 medium (120 g)", 120.0),
        f("apel", "Apple", 52.0, 0.3, 13.8, 0.2, 2.4, "1 medium (180 g)", 180.0),
        f("jeruk", "Orange", 47.0, 0.9, 11.8, 0.1, 2.4, "1 medium (150 g)", 150.0),
        f("pepaya", "Papaya", 43.0, 0.5, 10.8, 0.3, 1.7, "1 slice (150 g)", 150.0),
        f("mangga", "Mango", 60.0, 0.8, 15.0, 0.4, 1.6, "1 medium (200 g)", 200.0),
        f("semangka", "Watermelon", 30.0, 0.6, 7.6, 0.2, 0.4, "1 slice (200 g)", 200.0),
        f("alpukat", "Avocado", 160.0, 2.0, 8.5, 14.7, 6.7, "1 half (100 g)", 100.0),
        f("anggur", "Grapes", 69.0, 0.7, 18.1, 0.2, 0.9, "1 handful (100 g)", 100.0),
        f("strawberry", "Strawberries", 32.0, 0.7, 7.7, 0.3, 2.0, "1 cup (150 g)", 150.0),
        f("brokoli", "Broccoli", 34.0, 2.8, 6.6, 0.4, 2.6, "1 serving (100 g)", 100.0),
        f("wortel", "Carrot", 41.0, 0.9, 9.6, 0.2, 2.8, "1 medium (70 g)", 70.0),
        f("timun", "Cucumber", 15.0, 0.7, 3.6, 0.1, 0.5, "1 medium (150 g)", 150.0),
        f("tomat", "Tomato", 18.0, 0.9, 3.9, 0.2, 1.2, "1 medium (120 g)", 120.0),
        f("selada", "Lettuce", 15.0, 1.4, 2.9, 0.2, 1.3, "1 bowl (80 g)", 80.0),
        f("kangkung", "Kangkung (water spinach)", 19.0, 2.6, 3.1, 0.2, 2.1, "1 porsi (100 g)", 100.0),
        f("buncis", "Green Beans", 31.0, 1.8, 7.0, 0.2, 2.7, "1 serving (100 g)", 100.0),

        // ---- Fats, nuts, oils ----
        f("kacang-tanah", "Peanuts", 567.0, 25.8, 16.1, 49.2, 8.5, "1 handful (30 g)", 30.0),
        f("almond", "Almonds", 579.0, 21.2, 21.6, 49.9, 12.5, "1 handful (28 g)", 28.0),
        f("mete", "Cashews", 553.0, 18.2, 30.2, 43.9, 3.3, "1 handful (28 g)", 28.0),
        f("selai-kacang", "Peanut Butter", 588.0, 25.1, 20.0, 50.4, 6.0, "1 tbsp (16 g)", 16.0),
        f("minyak-goreng", "Cooking Oil", 884.0, 0.0, 0.0, 100.0, 0.0, "1 tbsp (14 g)", 14.0),
        f("minyak-zaitun", "Olive Oil", 884.0, 0.0, 0.0, 100.0, 0.0, "1 tbsp (14 g)", 14.0),
        f("santan", "Coconut Milk", 230.0, 2.3, 5.5, 23.8, 2.2, "100 ml", 100.0),
        f("mentega", "Butter", 717.0, 0.9, 0.1, 81.1, 0.0, "1 tbsp (14 g)", 14.0),
        f("chia-seed", "Chia Seeds", 486.0, 16.5, 42.1, 30.7, 34.4, "1 tbsp (12 g)", 12.0),

        // ---- Drinks & snacks ----
        f("air-putih", "Water", 0.0, 0.0, 0.0, 0.0, 0.0, "1 glass (250 ml)", 250.0),
        f("kopi-hitam", "Black Coffee", 2.0, 0.3, 0.0, 0.0, 0.0, "1 cup (240 ml)", 240.0),
        f("teh-tawar", "Unsweetened Tea", 1.0, 0.0, 0.3, 0.0, 0.0, "1 cup (240 ml)", 240.0),
        f("soda", "Cola", 42.0, 0.0, 10.6, 0.0, 0.0, "1 can (330 ml)", 330.0),
        f("jus-jeruk", "Orange Juice", 45.0, 0.7, 10.4, 0.2, 0.2, "1 glass (250 ml)", 250.0),
        f("sports-drink", "Sports Drink", 24.0, 0.0, 6.0, 0.0, 0.0, "1 bottle (500 ml)", 500.0),
        f("dark-chocolate", "Dark Chocolate 70%", 598.0, 7.8, 45.9, 42.6, 10.9, "2 squares (20 g)", 20.0),
        f("biskuit", "Biscuits, sweet", 480.0, 6.0, 65.0, 21.0, 2.0, "3 pieces (30 g)", 30.0),
        f("protein-bar", "Protein Bar", 350.0, 30.0, 35.0, 9.0, 5.0, "1 bar (60 g)", 60.0),
        f("keripik-kentang", "Potato Chips", 536.0, 7.0, 53.0, 34.6, 4.8, "1 small bag (40 g)", 40.0),
        f("madu", "Honey", 304.0, 0.3, 82.4, 0.0, 0.2, "1 tbsp (21 g)", 21.0),
        f("gula-pasir", "Sugar", 387.0, 0.0, 100.0, 0.0, 0.0, "1 tsp (4 g)", 4.0)
    )
}
