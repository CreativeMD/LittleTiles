package team.creative.littletiles.common.config;

import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;

public class LittlePermissionInteract {
    
    static {
        ConfigTypeConveration.registerTypeCreator(LittlePermissionInteract.class, () -> new LittlePermissionInteract());
    }
    
    @CreativeConfig
    public boolean interactWithStructure = true;
    
    @CreativeConfig
    public boolean useBed = true;
    
    @CreativeConfig
    public boolean storageGui = true;
    @CreativeConfig
    public boolean blankomaticGui = true;
    @CreativeConfig
    public boolean particleGui = true;
    @CreativeConfig
    public boolean itemHolderGui = true;
    @CreativeConfig
    public boolean structureBuilderGui = true;
    @CreativeConfig
    public boolean workbenchGui = true;
    @CreativeConfig
    public boolean printer3dGui = true;
    
    @CreativeConfig
    public boolean wrenchEditSignal = true;
    
    @CreativeConfig
    public boolean allowFlowingWater = true;
    @CreativeConfig
    public boolean allowFlowingLava = true;
    
}
