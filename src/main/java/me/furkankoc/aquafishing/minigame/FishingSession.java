package me.furkankoc.aquafishing.minigame;

import me.furkankoc.aquafishing.fish.CaughtFish;
import me.furkankoc.aquafishing.fish.FishRarity;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

public class FishingSession {

    private final Player player;
    private final FishHook hook;
    private final CaughtFish fish;

    // Bar Parametreleri (20 birimlik net ve takip edilebilir çubuk)
    private final int barLength = 20;
    private final int greenZoneStart;
    private final int greenZoneWidth;
    private final int targetHits;   // Balığı yakalamak için gereken isabet sayısı
    private int currentHits = 0;    // Başarılı yeşil vuruşlar
    private int misses = 0;         // Iska sayısı
    private final int maxMisses = 3;// 3 ıskada misina kopar

    private int cursor = 0;         // 0 .. barLength - 1
    private int direction = 1;      // +1 sağa, -1 sola
    private final int speedTicks;   // İmlecin adım hızı (2 tick = 100ms)
    private int tickCounter = 0;
    private int remainingTimeSeconds = 15;

    // Son vuruşun ekranda net görünmesi için geri bildirim
    private String lastFeedback = null;
    private int feedbackTicks = 0;

    private BossBar bossBar;

    public FishingSession(Player player, FishHook hook, CaughtFish fish, int powerBonus, double speedMultiplier) {
        this.player = player;
        this.hook = hook;
        this.fish = fish;

        FishRarity rarity = fish.template().rarity();

        // 1. Yakalamak için gereken isabet sayısı:
        // Sıradan/Nadir: 3 vuruş, Epik/Efsanevi: 4 vuruş
        // Güçlü oltalar (Kadim olta vb.) 1 vuruş avantaj sağlar
        int baseHits = switch (rarity) {
            case COMMON, RARE -> 3;
            case EPIC, LEGENDARY -> 4;
        };
        if (powerBonus >= 6) {
            this.targetHits = Math.max(2, baseHits - 1); // Kadim olta 2-3 vuruşta alır
        } else {
            this.targetHits = baseHits;
        }

        // 2. Yeşil Bölge Genişliği:
        // Common: 6, Rare: 5, Epic: 4, Legendary: 4
        // Kadim olta + güç bonusu ile yeşil alan 10-11 karaktere kadar genişler (Barın %55'i yeşil!)
        int baseWidth = switch (rarity) {
            case COMMON -> 6;
            case RARE -> 5;
            case EPIC -> 4;
            case LEGENDARY -> 4;
        };
        this.greenZoneWidth = Math.min(12, baseWidth + (powerBonus / 2));

        // Yeşil bölgeyi çubuğun merkezine yerleştir
        this.greenZoneStart = (barLength - this.greenZoneWidth) / 2;

        // 3. İbre Hızı:
        // Her 2 tickte bir ilerler. Çok hızlı olup kaçmaz, oyuncu rahatça nişan alıp yeşilde tıklar!
        this.speedTicks = 2;
    }

    public boolean isHit() {
        return cursor >= greenZoneStart && cursor < (greenZoneStart + greenZoneWidth);
    }

    public void advanceCursor() {
        cursor += direction;
        if (cursor >= barLength - 1) {
            cursor = barLength - 1;
            direction = -1;
        } else if (cursor <= 0) {
            cursor = 0;
            direction = 1;
        }
    }

    /**
     * Oyuncu sağ tıkladığında çağrılır.
     */
    public boolean recordClick() {
        if (isHit()) {
            currentHits++;
            lastFeedback = "<green><b>✔ MÜKEMMEL VURUŞ! (" + currentHits + "/" + targetHits + ")</b></green>";
            feedbackTicks = 18; // ~1 saniye parlar
            return true;
        } else {
            misses++;
            lastFeedback = "<red><b>✘ ISKA! (Kalan Hak: " + Math.max(0, maxMisses - misses) + ")</b></red>";
            feedbackTicks = 18;
            return false;
        }
    }

    public void tickTimer() {
        tickCounter++;
        if (feedbackTicks > 0) {
            feedbackTicks--;
            if (feedbackTicks == 0) {
                lastFeedback = null;
            }
        }
        if (tickCounter % 20 == 0) {
            remainingTimeSeconds--;
        }
    }

    public boolean isCaught() {
        return currentHits >= targetHits;
    }

    public boolean isEscaped() {
        return misses >= maxMisses || remainingTimeSeconds <= 0;
    }

    public Player getPlayer() {
        return player;
    }

    public FishHook getHook() {
        return hook;
    }

    public CaughtFish getFish() {
        return fish;
    }

    public int getBarLength() {
        return barLength;
    }

    public int getGreenZoneStart() {
        return greenZoneStart;
    }

    public int getGreenZoneWidth() {
        return greenZoneWidth;
    }

    public int getCursor() {
        return cursor;
    }

    public int getSpeedTicks() {
        return speedTicks;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public int getTargetHits() {
        return targetHits;
    }

    public int getCurrentHits() {
        return currentHits;
    }

    public int getMisses() {
        return misses;
    }

    public int getMaxMisses() {
        return maxMisses;
    }

    public int getRemainingTimeSeconds() {
        return remainingTimeSeconds;
    }

    public String getLastFeedback() {
        return lastFeedback;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }
}
