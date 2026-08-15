package com.nosferatu.divinemachinerylegacy.botania;

/**
 * Mana Spark progression from Botanical Machinery Extra Reforked.
 * Crystal intentionally has no Spark tier upstream.
 */
public enum SparkTier {
    BASE("base", 1000),
    MALACHITE("malachite", 25000),
    SAFFRON("saffron", 100000),
    SHADOW("shadow", 500000),
    CRIMSON("crimson", 1000000),
    AUREATE("aureate", 2500000),
    MAZARINE("mazarine", 5000000);

    private final String key;
    private final int transferRate;

    SparkTier(String key, int transferRate) {
        this.key = key;
        this.transferRate = transferRate;
    }

    public String getKey() {
        return key;
    }

    public int getTransferRate() {
        return transferRate;
    }

    public static SparkTier fromOrdinal(int ordinal) {
        SparkTier[] values = values();
        if (ordinal < 0 || ordinal >= values.length) return BASE;
        return values[ordinal];
    }
}
