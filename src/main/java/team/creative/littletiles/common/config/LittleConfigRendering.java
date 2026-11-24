package team.creative.littletiles.common.config;

import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.client.render.cache.build.RenderingThread;

public class LittleConfigRendering implements ICreativeConfig {
    
    @CreativeConfig
    public boolean useCubeCache = true;
    
    @CreativeConfig
    @CreativeConfig.IntRange(slider = false, min = 1, max = 1024)
    public int renderingThreadCount = 2;
    
    @CreativeConfig
    public boolean highlightStructureBox = true;
    
    @CreativeConfig
    public PreviewRenderConfig preview = new PreviewRenderConfig();
    
    @CreativeConfig
    public boolean enableRandomDisplayTick = false;
    
    @CreativeConfig
    public boolean uploadToVBODirectly = true;
    
    @CreativeConfig
    public boolean showTooltip = true;
    
    @CreativeConfig
    public boolean itemShowRotation = false;
    
    @CreativeConfig
    public int itemCacheDuration = 5000;
    
    @CreativeConfig
    public int itemLowResolutionBoxCount = 1000;
    
    @CreativeConfig
    public int entityCacheBuildThreads = 1;
    
    @CreativeConfig
    public int connectedShapeBlocksLimit = 128;
    
    @CreativeConfig
    public int wrenchInfoRange = 5;
    
    @CreativeConfig
    public double wrenchInfoRefresh = 1;
    
    @Override
    public void configured(Side side) {
        if (side.isClient())
            RenderingThread.initThreads(renderingThreadCount);
    }
    
    public static class PreviewRenderConfig {
        
        @CreativeConfig
        public boolean darkerBoxShading = false;
        
        @CreativeConfig
        public Color boxColor = new Color(1, 1, 1, 0.5F);
        
        @CreativeConfig
        @CreativeConfig.DecimalRange(min = 0, max = 1)
        public double boxAlphaFade = 0.2;
        
        @CreativeConfig
        public boolean alwaysUseLines = false;
        
        @CreativeConfig
        public double lineThickness = 2;
        
        @CreativeConfig
        public double hightlightLineThickness = 4;
        
        @CreativeConfig
        public Color lineColor = new Color(0, 0, 0, 0.4F);
        
        @CreativeConfig
        public Color highlightLineColor = new Color(1F, 0.3F, 0.0F, 1);
        
    }
    
}
