package com.nosferatu.divinemachinerylegacy.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/** Loads the tiny AE2 rv3 compatibility transformer required by bmaddon parity. */
@IFMLLoadingPlugin.Name("Divine Machinery Legacy AE2 Compatibility")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"com.nosferatu.divinemachinerylegacy.core"})
public final class DivineMachineryLegacyCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"com.nosferatu.divinemachinerylegacy.core.Ae2UpgradeInventoryTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // No environment-specific mappings are needed: the transformed AE2
        // class and method descriptor are mod-owned names and never obfuscated.
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
