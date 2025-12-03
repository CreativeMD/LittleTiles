package team.creative.littletiles.common.config;

import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.littletiles.client.render.cache.build.RenderingThread;

public class LittleConfigRendering implements ICreativeConfig {
    
    @CreativeConfig
    public boolean useQuadCache = false;
    
    @CreativeConfig
    public boolean useCubeCache = true;
    
    @CreativeConfig
    @CreativeConfig.IntRange(slider = false, min = 1, max = 1024)
    public int renderingThreadCount = 2;
    
    @CreativeConfig
    public boolean highlightStructureBox = true;
    
    @CreativeConfig
    public boolean previewLines = false;
    
    @CreativeConfig
    public double previewLineThickness = 2;
    
    public boolean darkerPreviewBoxShading = false;
    
    @CreativeConfig
    public boolean enableRandomDisplayTick = false;
    
    @CreativeConfig
    public boolean uploadToVBODirectly = true;
    
    @CreativeConfig
    public boolean showTooltip = true;
    
    @CreativeConfig
    public int itemCacheDuration = 5000;
    
    @CreativeConfig
    public int itemLowResolutionBoxCount = 1000;
    
    @CreativeConfig
    public int entityCacheBuildThreads = 1;
    
    @CreativeConfig
    public int connectedShapeBlocksLimit = 128;
    
    @Override
    public void configured(Side side) {
        if (side.isClient())
            RenderingThread.initThreads(renderingThreadCount);
    }
    
}
