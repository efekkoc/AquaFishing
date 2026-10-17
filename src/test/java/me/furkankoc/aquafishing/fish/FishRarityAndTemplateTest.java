package me.furkankoc.aquafishing.fish;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fish Rarity & Template Unit Tests")
class FishRarityAndTemplateTest {

    @Test
    @DisplayName("FishRarity weights must sum up to 100 for predictable probability distribution")
    void testRarityWeightsSumToHundred() {
        int totalWeight = 0;
        for (FishRarity rarity : FishRarity.values()) {
            totalWeight += rarity.getWeight();
        }
        assertEquals(100, totalWeight, "Total probability weight of all rarities must equal 100");
    }

    @Test
    @DisplayName("FishTemplate creation and boundary validation")
    void testFishTemplateProperties() {
        FishTemplate template = new FishTemplate(
                "ancient_coelacanth",
                "Kadim Coelacanth",
                Material.COD,
                1040,
                FishRarity.LEGENDARY,
                45.0,
                120.0,
                List.of("<gray>Derin sularda yaşayan tarih öncesi balık.")
        );

        assertEquals("ancient_coelacanth", template.id());
        assertEquals("Kadim Coelacanth", template.name());
        assertEquals(Material.COD, template.material());
        assertEquals(1040, template.customModelData());
        assertEquals(FishRarity.LEGENDARY, template.rarity());
        assertTrue(template.minWeight() < template.maxWeight(), "Min weight must be strictly less than max weight");
        assertEquals(1, template.lore().size());
    }
}
