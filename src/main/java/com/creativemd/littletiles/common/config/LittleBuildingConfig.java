package com.creativemd.littletiles.common.config;

import com.creativemd.creativecore.common.config.api.CreativeConfig;
import com.creativemd.creativecore.common.config.converation.ConfigTypeConveration;

public class LittleBuildingConfig {
    
    static {
        ConfigTypeConveration.registerTypeCreator(LittleBuildingConfig.class, () -> new LittleBuildingConfig());
    }
    
    public LittleBuildingConfig() {
        limitAffectedBlocks = true;
        editUnbreakable = false;
        minimumTransparency = 0;
        limitEditBlocks = true;
        limitPlaceBlocks = true;
        harvestLevelBlock = 0;
        maxAffectedBlocks = 0;
        maxEditBlocks = 0;
        maxPlaceBlocks = 0;
    }
    
    public LittleBuildingConfig(boolean survival) {
        limitAffectedBlocks = survival;
        editUnbreakable = !survival;
        minimumTransparency = survival ? 255 : 0;
        limitEditBlocks = survival;
        limitPlaceBlocks = survival;
        harvestLevelBlock = survival ? 1 : 4;
    }
    
    @CreativeConfig
    public boolean limitAffectedBlocks;
    
    @CreativeConfig
    public int maxAffectedBlocks = 2;
    
    @CreativeConfig
    public int harvestLevelBlock;
    
    @CreativeConfig
    public boolean editUnbreakable;
    
    @CreativeConfig
    @CreativeConfig.IntRange(min = 0, max = 255)
    public int minimumTransparency;
    
    @CreativeConfig
    public boolean limitEditBlocks;
    @CreativeConfig
    public int maxEditBlocks = 10;
    
    @CreativeConfig
    public boolean limitPlaceBlocks;
    @CreativeConfig
    public int maxPlaceBlocks = 10;
    
    @CreativeConfig
    public boolean limitRecipeSize = false;
    
    @CreativeConfig
    public int recipeBlocksLimit = 16;
    
}
