package com.nosferatu.divinemachinerylegacy.bloodmagic;

public enum BloodMagicPatternKind {
    BLOOD_ALTAR("blood_altar"),
    ALCHEMY_TABLE("alchemy_table");

    private final String id;

    BloodMagicPatternKind(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static BloodMagicPatternKind fromId(String id) {
        if (id == null) return null;
        for (BloodMagicPatternKind kind : values()) {
            if (kind.id.equals(id)) return kind;
        }
        return null;
    }
}
