package com.nosferatu.divinemachinerylegacy.botania;

public enum MachineTier {
    MALACHITE("malachite", 2_500_000, 4, 25_000),
    SAFFRON("saffron", 10_000_000, 8, 100_000),
    SHADOW("shadow", 50_000_000, 16, 500_000),
    CRIMSON("crimson", 100_000_000, 32, 1_000_000);

    private final String key;
    private final int manaCapacity;
    private final int parallelCrafts;
    private final int referenceSparkTransfer;

    MachineTier(String key, int manaCapacity, int parallelCrafts, int referenceSparkTransfer) {
        this.key = key;
        this.manaCapacity = manaCapacity;
        this.parallelCrafts = parallelCrafts;
        this.referenceSparkTransfer = referenceSparkTransfer;
    }

    public String getKey() {
        return key;
    }

    public int getManaCapacity() {
        return manaCapacity;
    }

    public int getParallelCrafts() {
        return parallelCrafts;
    }

    public int getReferenceSparkTransfer() {
        return referenceSparkTransfer;
    }

    public int getLivingrockSlots() {
        return this == MALACHITE ? 1 : 3;
    }

    public int getUpgradeSlots() {
        if (this == SHADOW) return 1;
        if (this == CRIMSON) return 2;
        return 0;
    }

    public static MachineTier fromMeta(int meta) {
        int safe = meta & 3;
        return values()[safe];
    }
}
