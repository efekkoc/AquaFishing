package me.furkankoc.aquafishing.minigame;

import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.fish.FishRarity;
import me.furkankoc.aquafishing.fish.FishTemplate;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FishingSession Minigame Logic Unit Tests")
class FishingSessionTest {

    private CaughtFish createDummyFish(FishRarity rarity) {
        FishTemplate template = new FishTemplate(
                "test_fish", "Test Fish", Material.COD, 1001, rarity, 1.0, 5.0, List.of()
        );
        return new CaughtFish(template, 3.5, java.util.UUID.randomUUID(), "Steve", System.currentTimeMillis());
    }

    @Test
    @DisplayName("Green zone calculation: higher rod power bonus should widen the green target area")
    void testGreenZoneWidthExpandsWithPowerBonus() {
        CaughtFish fish = createDummyFish(FishRarity.COMMON);

        FishingSession basicSession = new FishingSession(null, null, fish, 0, 1.0);
        FishingSession powerfulSession = new FishingSession(null, null, fish, 8, 1.0);

        assertTrue(powerfulSession.getGreenZoneWidth() > basicSession.getGreenZoneWidth(),
                "Higher rod power bonus must expand the green zone width");
        assertEquals(20, basicSession.getBarLength());
    }

    @Test
    @DisplayName("Target hits: legendary fish should require more hits than common fish unless high tier rod is used")
    void testTargetHitsRequirement() {
        CaughtFish commonFish = createDummyFish(FishRarity.COMMON);
        CaughtFish legendaryFish = createDummyFish(FishRarity.LEGENDARY);

        FishingSession commonSession = new FishingSession(null, null, commonFish, 0, 1.0);
        FishingSession legendarySession = new FishingSession(null, null, legendaryFish, 0, 1.0);

        assertEquals(3, commonSession.getTargetHits(), "Common fish requires 3 hits");
        assertEquals(4, legendarySession.getTargetHits(), "Legendary fish requires 4 hits");

        FishingSession ancientRodSession = new FishingSession(null, null, legendaryFish, 6, 1.0);
        assertEquals(3, ancientRodSession.getTargetHits(), "High tier rod reduces target hits by 1");
    }

    @Test
    @DisplayName("Cursor oscillation: cursor moves back and forth within bar bounds [0 .. barLength-1]")
    void testCursorOscillationWithinBounds() {
        CaughtFish fish = createDummyFish(FishRarity.COMMON);
        FishingSession session = new FishingSession(null, null, fish, 0, 1.0);

        int barLength = session.getBarLength();
        assertEquals(0, session.getCursor());

        // Advance 25 times - it should bounce off the edge (19) and head back
        for (int i = 0; i < 25; i++) {
            session.advanceCursor();
            assertTrue(session.getCursor() >= 0 && session.getCursor() < barLength,
                    "Cursor must always remain within [0, " + (barLength - 1) + "], was: " + session.getCursor());
        }
    }

    @Test
    @DisplayName("Catch and Escape conditions")
    void testCatchAndEscapeProgression() {
        CaughtFish fish = createDummyFish(FishRarity.COMMON);
        FishingSession session = new FishingSession(null, null, fish, 0, 1.0);

        assertFalse(session.isCaught(), "New session must not be caught");
        assertFalse(session.isEscaped(), "New session must not be escaped");

        // Move cursor until inside green zone, then hit
        while (!session.isHit()) {
            session.advanceCursor();
        }

        assertTrue(session.isHit());
        boolean hitSuccess = session.recordClick();
        assertTrue(hitSuccess);
        assertEquals(1, session.getCurrentHits());

        // Hit enough times to catch
        while (session.getCurrentHits() < session.getTargetHits()) {
            session.recordClick();
        }

        assertTrue(session.isCaught(), "Session should be marked caught after satisfying targetHits");
    }
}
