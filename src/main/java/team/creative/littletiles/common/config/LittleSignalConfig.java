package team.creative.littletiles.common.config;

import team.creative.creativecore.common.config.api.CreativeConfig;

public class LittleSignalConfig {
    
    @CreativeConfig
    public double overallDurationScale = 1;
    
    @CreativeConfig
    public double andDuration = 0.1;
    @CreativeConfig
    public double orDuration = 0.01;
    @CreativeConfig
    public double xorDuration = 0.2;
    @CreativeConfig
    public double bandDuration = 0.2;
    @CreativeConfig
    public double borDuration = 0.02;
    @CreativeConfig
    public double bxorDuration = 0.4;
    @CreativeConfig
    public double notDuration = 0.01;
    @CreativeConfig
    public double bnotDuration = 0.02;
    @CreativeConfig
    public double variableDuration = 0.01;
    
    @CreativeConfig
    public double addDuration = 0.5;
    @CreativeConfig
    public double subDuration = 0.5;
    @CreativeConfig
    public double mulDuration = 0.5;
    @CreativeConfig
    public double divDuration = 5;
    
}
