package me.furkankoc.aquafishing.codex;

public class PlayerFishProgress {

    private int catchCount;
    private double maxWeight;
    private final long firstDiscoveredMillis;

    public PlayerFishProgress(int catchCount, double maxWeight, long firstDiscoveredMillis) {
        this.catchCount = catchCount;
        this.maxWeight = maxWeight;
        this.firstDiscoveredMillis = firstDiscoveredMillis;
    }

    public void recordCatch(double weight) {
        this.catchCount++;
        if (weight > this.maxWeight) {
            this.maxWeight = weight;
        }
    }

    public int getCatchCount() {
        return catchCount;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public long getFirstDiscoveredMillis() {
        return firstDiscoveredMillis;
    }
}
