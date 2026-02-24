package team.creative.littletiles.common.config;

import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.holder.ConfigHolderDynamic;

public class LittleConfigBuilding {
    
    @CreativeConfig
    public ConfigHolderDynamic buildingMode;
    
    @CreativeConfig
    public boolean invertStickToGrid = false;
    
    @CreativeConfig
    public int maxSavedActions = 32;
    
    @CreativeConfig
    public boolean useALTForEverything = false;
    
    @CreativeConfig
    public boolean useAltWhenFlying = true;
    
    @CreativeConfig
    public int lowResolutionBoxCount = 2000;
    
}
