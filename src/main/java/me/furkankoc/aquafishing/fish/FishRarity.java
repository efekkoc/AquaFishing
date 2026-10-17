package me.furkankoc.aquafishing.fish;

public enum FishRarity {
    COMMON("<gray>[Yaygın]</gray>", "Yaygın", 60),
    RARE("<aqua>[Nadir]</aqua>", "Nadir", 25),
    EPIC("<light_purple>[Epik]</light_purple>", "Epik", 12),
    LEGENDARY("<gradient:#ffe259:#ffa751><b>[Efsanevi]</b></gradient>", "Efsanevi", 3);

    private final String formattedTag;
    private final String displayName;
    private final int weight;

    FishRarity(String formattedTag, String displayName, int weight) {
        this.formattedTag = formattedTag;
        this.displayName = displayName;
        this.weight = weight;
    }

    public String getFormattedTag() {
        return formattedTag;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWeight() {
        return weight;
    }
}
