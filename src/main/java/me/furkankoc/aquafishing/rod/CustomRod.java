package me.furkankoc.aquafishing.rod;

import java.util.List;

public record CustomRod(
        String id,
        String name,
        int tier,                    // Kademe: 1, 2, 3, 4, 5
        String previousRodId,        // Bir önceki kademe olta (Kümülatif yükseltme şartı)
        int customModelData,
        int powerBonus,              // Yeşil bölgeyi kaç karakter genişlettiği
        double speedMultiplier,      // İbrenin hız kontrol katsayısı
        double luckMultiplier,       // Efsanevi balık şansı çarpanı
        int biteSpeedReductionTicks, // Isırma bekleme süresini ne kadar kısalttığı (Daha hızlı balık vurur)
        int priceGold,               // Altın Külçesi maliyeti
        int requiredCatches,         // Kaç balık yakalamış olması gerektiği şartı
        List<String> lore
) {}
