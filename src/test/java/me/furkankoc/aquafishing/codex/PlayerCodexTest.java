package me.furkankoc.aquafishing.codex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerCodex Unit Tests")
class PlayerCodexTest {

    private UUID playerUuid;
    private PlayerCodex codex;

    @BeforeEach
    void setUp() {
        playerUuid = UUID.randomUUID();
        codex = new PlayerCodex(playerUuid);
    }

    @Test
    @DisplayName("First discovery of a fish species should return true and record accurate data")
    void testFirstCatchRecordsDiscovery() {
        boolean isFirst = codex.recordCatch("golden_trout", 5.42);

        assertTrue(isFirst, "First catch of a species must return true");
        assertTrue(codex.hasDiscovered("golden_trout"), "Codex should mark species as discovered");

        PlayerFishProgress progress = codex.getProgress("golden_trout");
        assertNotNull(progress, "Progress should not be null for discovered fish");
        assertEquals(1, progress.getCatchCount(), "Initial catch count should be 1");
        assertEquals(5.42, progress.getMaxWeight(), 0.001, "Max weight should match caught weight");
        assertEquals(1, codex.getTotalCatches(), "Total catches across all species should be 1");
    }

    @Test
    @DisplayName("Subsequent catches of the same species should update catch count and update max weight only when exceeded")
    void testSubsequentCatchUpdatesMaxWeight() {
        codex.recordCatch("neon_tetra", 2.50);

        // Catch with smaller weight
        boolean isSecond = codex.recordCatch("neon_tetra", 1.80);
        assertFalse(isSecond, "Subsequent catch must return false");

        PlayerFishProgress progress = codex.getProgress("neon_tetra");
        assertEquals(2, progress.getCatchCount(), "Catch count should increment to 2");
        assertEquals(2.50, progress.getMaxWeight(), 0.001, "Max weight should remain at highest (2.50)");

        // Catch with higher weight (new personal record)
        codex.recordCatch("neon_tetra", 3.75);
        assertEquals(3, progress.getCatchCount(), "Catch count should increment to 3");
        assertEquals(3.75, progress.getMaxWeight(), 0.001, "Max weight should update to 3.75");
        assertEquals(3, codex.getTotalCatches());
    }

    @Test
    @DisplayName("Multiple distinct fish species should track independently")
    void testMultipleDistinctSpecies() {
        codex.recordCatch("salmon", 4.0);
        codex.recordCatch("cod", 2.0);
        codex.recordCatch("magma_squid", 15.0);

        assertEquals(3, codex.getDiscoveries().size(), "Should have 3 distinct species recorded");
        assertEquals(3, codex.getTotalCatches(), "Total catches should sum up correctly");
        assertTrue(codex.hasDiscovered("salmon"));
        assertTrue(codex.hasDiscovered("cod"));
        assertTrue(codex.hasDiscovered("magma_squid"));
        assertFalse(codex.hasDiscovered("leviathan"));
    }
}
