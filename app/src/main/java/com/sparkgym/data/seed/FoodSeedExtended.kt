package com.sparkgym.data.seed

/**
 * The rest of the food database: warung and restaurant dishes, more protein
 * sources, supplements, and the fast food people actually eat on a rest day.
 *
 * Values are per 100 g. Cooked dishes are estimates for a typical warung
 * portion — close enough to steer a diet, and every entry is editable.
 */
object FoodSeedExtended {

    private fun f(
        slug: String, name: String, kcal: Double, p: Double, c: Double, fat: Double,
        fiber: Double = 0.0, serving: String = "100 g", grams: Double = 100.0, brand: String = ""
    ) = SeedFood(slug, name, brand, kcal, p, c, fat, fiber, serving, grams)

    val foods: List<SeedFood> = listOf(

        // ---- Warung & rumah makan ----
        f("nasi-uduk", "Nasi Uduk", 168.0, 3.2, 26.0, 5.4, 0.6, "1 porsi (200 g)", 200.0),
        f("nasi-kuning", "Nasi Kuning", 165.0, 3.0, 26.5, 5.0, 0.7, "1 porsi (200 g)", 200.0),
        f("lontong", "Lontong", 110.0, 2.0, 24.0, 0.2, 0.4, "2 potong (150 g)", 150.0),
        f("ketupat", "Ketupat", 105.0, 1.9, 23.0, 0.2, 0.4, "1 buah (120 g)", 120.0),
        f("rawon", "Rawon Daging", 118.0, 9.5, 4.0, 7.2, 0.8, "1 mangkuk (300 g)", 300.0),
        f("soto-betawi", "Soto Betawi", 140.0, 8.0, 5.5, 10.0, 0.5, "1 mangkuk (300 g)", 300.0),
        f("sop-buntut", "Sop Buntut", 135.0, 11.0, 3.5, 8.8, 0.6, "1 mangkuk (350 g)", 350.0),
        f("opor-ayam", "Opor Ayam", 175.0, 15.0, 4.5, 11.0, 0.7, "1 potong + kuah (150 g)", 150.0),
        f("gulai-kambing", "Gulai Kambing", 190.0, 14.0, 5.0, 13.0, 0.8, "1 porsi (150 g)", 150.0),
        f("ayam-geprek", "Ayam Geprek", 285.0, 23.0, 12.0, 17.0, 1.0, "1 porsi (150 g)", 150.0),
        f("ayam-pop", "Ayam Pop", 175.0, 25.0, 1.0, 8.0, 0.0, "1 potong (110 g)", 110.0),
        f("dendeng-balado", "Dendeng Balado", 330.0, 28.0, 8.0, 21.0, 1.2, "1 porsi (80 g)", 80.0),
        f("ikan-bakar", "Ikan Bakar", 150.0, 24.0, 2.0, 5.0, 0.0, "1 ekor (150 g)", 150.0),
        f("udang-balado", "Udang Balado", 160.0, 20.0, 6.0, 6.0, 0.8, "1 porsi (120 g)", 120.0),
        f("telur-balado", "Telur Balado", 185.0, 11.0, 5.0, 13.5, 0.6, "1 butir (65 g)", 65.0),
        f("perkedel", "Perkedel Kentang", 200.0, 4.5, 18.0, 12.0, 1.5, "2 buah (60 g)", 60.0),
        f("sayur-asem", "Sayur Asem", 40.0, 2.0, 7.0, 0.6, 2.5, "1 mangkuk (200 g)", 200.0),
        f("sayur-lodeh", "Sayur Lodeh", 85.0, 2.8, 7.0, 5.4, 2.4, "1 mangkuk (200 g)", 200.0),
        f("tumis-kangkung", "Tumis Kangkung", 65.0, 2.8, 5.0, 4.0, 2.0, "1 porsi (120 g)", 120.0),
        f("ketoprak", "Ketoprak", 155.0, 6.5, 18.0, 6.5, 2.5, "1 porsi (300 g)", 300.0),
        f("siomay", "Siomay + Bumbu", 175.0, 9.0, 16.0, 8.5, 1.5, "1 porsi (200 g)", 200.0),
        f("batagor", "Batagor", 245.0, 9.5, 22.0, 13.0, 1.2, "1 porsi (180 g)", 180.0),
        f("pempek", "Pempek", 200.0, 11.0, 24.0, 6.5, 0.5, "1 porsi (150 g)", 150.0),
        f("mie-ayam", "Mie Ayam", 145.0, 7.0, 20.0, 4.2, 1.0, "1 mangkuk (300 g)", 300.0),
        f("nasi-padang", "Nasi Padang (campur)", 215.0, 10.0, 24.0, 9.0, 1.2, "1 porsi (350 g)", 350.0),
        f("bubur-kacang-hijau", "Bubur Kacang Hijau", 105.0, 3.5, 17.0, 2.8, 2.0, "1 mangkuk (250 g)", 250.0),
        f("es-campur", "Es Campur", 95.0, 1.0, 21.0, 1.2, 0.8, "1 gelas (300 g)", 300.0),
        f("klepon", "Klepon", 230.0, 2.5, 44.0, 5.5, 1.5, "5 buah (75 g)", 75.0),
        f("risoles", "Risoles", 250.0, 6.0, 25.0, 14.0, 1.0, "2 buah (80 g)", 80.0),

        // ---- Lean protein ----
        f("dada-ayam-panggang", "Grilled Chicken Breast", 165.0, 31.0, 0.0, 3.6, 0.0, "1 fillet (150 g)", 150.0),
        f("kalkun-giling", "Turkey Mince, lean", 150.0, 27.0, 0.0, 4.5, 0.0, "1 serving (150 g)", 150.0),
        f("daging-sapi-tanpa-lemak", "Beef, lean 5% fat", 137.0, 21.0, 0.0, 5.0, 0.0, "1 serving (150 g)", 150.0),
        f("ikan-dori", "Dory Fillet", 90.0, 16.0, 0.0, 2.8, 0.0, "1 fillet (120 g)", 120.0),
        f("ikan-tuna-segar", "Tuna Steak, fresh", 130.0, 28.0, 0.0, 1.3, 0.0, "1 steak (150 g)", 150.0),
        f("sarden-kaleng", "Sardines, canned", 208.0, 24.6, 0.0, 11.5, 0.0, "1 can (95 g)", 95.0),
        f("cumi", "Squid", 92.0, 15.6, 3.1, 1.4, 0.0, "1 serving (120 g)", 120.0),
        f("bakso-ikan", "Fish Balls", 105.0, 12.0, 8.0, 2.5, 0.0, "5 buah (75 g)", 75.0),
        f("dada-bebek", "Duck Breast, skinless", 135.0, 19.9, 0.0, 5.9, 0.0, "1 fillet (130 g)", 130.0),
        f("hati-ayam", "Chicken Liver", 119.0, 16.9, 0.7, 4.8, 0.0, "1 serving (100 g)", 100.0),
        f("edamame", "Edamame", 121.0, 11.9, 8.9, 5.2, 5.2, "1 cup (155 g)", 155.0),
        f("tempe-mendoan", "Tempe Mendoan", 250.0, 13.0, 18.0, 15.0, 3.5, "2 potong (70 g)", 70.0),
        f("oncom", "Oncom", 187.0, 13.0, 22.6, 6.0, 4.0, "1 potong (50 g)", 50.0),
        f("susu-kedelai", "Soy Milk, unsweetened", 33.0, 3.3, 1.8, 1.8, 0.6, "1 glass (250 ml)", 250.0),
        f("yogurt-plain", "Yoghurt, plain", 61.0, 3.5, 4.7, 3.3, 0.0, "1 pot (150 g)", 150.0),
        f("skyr", "Skyr", 63.0, 11.0, 4.0, 0.2, 0.0, "1 pot (150 g)", 150.0),

        // ---- Carbs & grains ----
        f("nasi-shirataki", "Shirataki Rice", 10.0, 0.2, 3.0, 0.0, 2.9, "1 pack (200 g)", 200.0),
        f("kentang-panggang", "Baked Potato", 93.0, 2.5, 21.1, 0.1, 2.2, "1 medium (170 g)", 170.0),
        f("kentang-goreng", "French Fries", 312.0, 3.4, 41.0, 15.0, 3.8, "1 medium (117 g)", 117.0),
        f("roti-sourdough", "Sourdough Bread", 260.0, 10.0, 48.0, 2.0, 3.0, "1 slice (50 g)", 50.0),
        f("tortilla", "Flour Tortilla", 310.0, 8.0, 51.0, 8.0, 3.0, "1 piece (45 g)", 45.0),
        f("bihun", "Rice Vermicelli, cooked", 109.0, 1.8, 24.9, 0.2, 0.9, "1 porsi (150 g)", 150.0),
        f("kwetiau", "Kwetiau, cooked", 108.0, 1.9, 24.0, 0.2, 0.8, "1 porsi (200 g)", 200.0),
        f("sereal-jagung", "Corn Flakes", 357.0, 7.5, 84.0, 0.4, 3.0, "1 bowl (40 g)", 40.0),
        f("granola", "Granola", 471.0, 10.0, 64.0, 20.0, 7.0, "1 serving (50 g)", 50.0),
        f("barley", "Pearl Barley, cooked", 123.0, 2.3, 28.2, 0.4, 3.8, "1 serving (150 g)", 150.0),
        f("couscous", "Couscous, cooked", 112.0, 3.8, 23.2, 0.2, 1.4, "1 serving (150 g)", 150.0),

        // ---- Legumes & veg ----
        f("kacang-merah", "Red Kidney Beans, cooked", 127.0, 8.7, 22.8, 0.5, 6.4, "1 serving (150 g)", 150.0),
        f("buncis-kaleng", "Chickpeas, cooked", 164.0, 8.9, 27.4, 2.6, 7.6, "1 serving (150 g)", 150.0),
        f("lentil", "Lentils, cooked", 116.0, 9.0, 20.1, 0.4, 7.9, "1 serving (150 g)", 150.0),
        f("labu-siam", "Chayote", 19.0, 0.8, 4.5, 0.1, 1.7, "1 porsi (120 g)", 120.0),
        f("terong", "Aubergine", 25.0, 1.0, 5.9, 0.2, 3.0, "1 medium (120 g)", 120.0),
        f("paprika", "Bell Pepper", 31.0, 1.0, 6.0, 0.3, 2.1, "1 medium (120 g)", 120.0),
        f("jamur-tiram", "Oyster Mushroom", 33.0, 3.3, 6.1, 0.4, 2.3, "1 porsi (100 g)", 100.0),
        f("bayam", "Spinach", 23.0, 2.9, 3.6, 0.4, 2.2, "1 porsi (100 g)", 100.0),
        f("sawi-hijau", "Pak Choi", 13.0, 1.5, 2.2, 0.2, 1.0, "1 porsi (100 g)", 100.0),
        f("kembang-kol", "Cauliflower", 25.0, 1.9, 5.0, 0.3, 2.0, "1 porsi (100 g)", 100.0),
        f("kol", "Cabbage", 25.0, 1.3, 5.8, 0.1, 2.5, "1 porsi (100 g)", 100.0),
        f("toge", "Bean Sprouts", 30.0, 3.0, 5.9, 0.2, 1.8, "1 porsi (100 g)", 100.0),

        // ---- Fruit ----
        f("nanas", "Pineapple", 50.0, 0.5, 13.1, 0.1, 1.4, "1 slice (150 g)", 150.0),
        f("melon", "Melon", 34.0, 0.8, 8.2, 0.2, 0.9, "1 slice (150 g)", 150.0),
        f("jambu-biji", "Guava", 68.0, 2.6, 14.3, 0.9, 5.4, "1 buah (120 g)", 120.0),
        f("salak", "Salak", 82.0, 0.8, 20.9, 0.4, 1.5, "3 buah (100 g)", 100.0),
        f("kelengkeng", "Longan", 60.0, 1.3, 15.1, 0.1, 1.1, "10 buah (80 g)", 80.0),
        f("durian", "Durian", 147.0, 1.5, 27.1, 5.3, 3.8, "3 biji (100 g)", 100.0),
        f("kurma", "Dates", 277.0, 1.8, 75.0, 0.2, 6.7, "3 buah (25 g)", 25.0),
        f("blueberry", "Blueberries", 57.0, 0.7, 14.5, 0.3, 2.4, "1 cup (150 g)", 150.0),
        f("kiwi", "Kiwi", 61.0, 1.1, 14.7, 0.5, 3.0, "1 buah (75 g)", 75.0),

        // ---- Supplements & sports nutrition ----
        f("creatine", "Creatine Monohydrate", 0.0, 0.0, 0.0, 0.0, 0.0, "1 scoop (5 g)", 5.0),
        f("mass-gainer", "Mass Gainer", 380.0, 20.0, 65.0, 4.0, 2.0, "1 scoop (100 g)", 100.0),
        f("eaa", "EAA / BCAA Powder", 40.0, 9.0, 0.5, 0.0, 0.0, "1 scoop (10 g)", 10.0),
        f("whey-concentrate", "Whey Concentrate", 400.0, 75.0, 10.0, 6.0, 0.0, "1 scoop (30 g)", 30.0),
        f("protein-shake-siap", "Ready-to-drink Protein Shake", 62.0, 10.0, 3.0, 1.0, 0.0, "1 bottle (330 ml)", 330.0),
        f("gel-energi", "Energy Gel", 250.0, 0.0, 62.0, 0.0, 0.0, "1 sachet (40 g)", 40.0),

        // ---- Fast food & rest-day reality ----
        f("burger-keju", "Cheeseburger", 295.0, 15.0, 25.0, 14.0, 1.5, "1 burger (150 g)", 150.0),
        f("pizza-keju", "Pizza, cheese", 266.0, 11.0, 33.0, 10.0, 2.3, "1 slice (107 g)", 107.0),
        f("ayam-krispi", "Fried Chicken, crispy", 320.0, 21.0, 14.0, 20.0, 0.8, "1 potong (120 g)", 120.0),
        f("kebab", "Kebab Wrap", 215.0, 13.0, 20.0, 9.5, 1.8, "1 wrap (250 g)", 250.0),
        f("sushi-roll", "Sushi Roll", 145.0, 6.0, 25.0, 2.0, 1.2, "6 pieces (180 g)", 180.0),
        f("donat", "Doughnut", 452.0, 5.0, 51.0, 25.0, 1.5, "1 buah (60 g)", 60.0),
        f("es-krim", "Ice Cream", 207.0, 3.5, 24.0, 11.0, 0.7, "1 scoop (70 g)", 70.0),
        f("boba-milk-tea", "Boba Milk Tea", 88.0, 1.0, 18.0, 1.6, 0.2, "1 cup (400 ml)", 400.0),
        f("bir", "Beer", 43.0, 0.5, 3.6, 0.0, 0.0, "1 can (330 ml)", 330.0),

        // ---- Condiments ----
        f("kecap-manis", "Kecap Manis", 265.0, 4.0, 60.0, 0.5, 0.5, "1 sdm (18 g)", 18.0),
        f("saus-tomat", "Tomato Ketchup", 101.0, 1.0, 25.0, 0.1, 0.3, "1 sdm (17 g)", 17.0),
        f("mayones", "Mayonnaise", 680.0, 1.0, 0.6, 75.0, 0.0, "1 sdm (14 g)", 14.0),
        f("mayo-rendah-lemak", "Light Mayonnaise", 240.0, 0.9, 9.0, 22.0, 0.0, "1 sdm (14 g)", 14.0),
        f("mustard", "Mustard", 66.0, 4.0, 5.8, 3.3, 3.3, "1 tsp (5 g)", 5.0),
        f("saus-sambal", "Chilli Sauce", 93.0, 1.2, 21.0, 0.3, 1.4, "1 sdm (15 g)", 15.0)
    )
}
