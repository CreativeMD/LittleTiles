package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import team.creative.littletiles.LittleTiles;

public class ItemModelCache {
    
    private boolean building = true;
    private List<BakedQuad> cache;
    private long lastUsed;
    
    public ItemModelCache() {
        lastUsed = System.currentTimeMillis();
    }
    
    public boolean expired() {
        return System.currentTimeMillis() - lastUsed >= LittleTiles.CONFIG.rendering.itemCacheDuration;
    }
    
    public void setQuads(boolean translucent, List<BakedQuad> baked) {
        if (translucent)
            return;
        this.cache = baked;
    }
    
    public boolean isBuilding() {
        return building;
    }
    
    public void complete() {
        building = false;
    }
    
    public List<BakedQuad> getQuads(boolean translucent) {
        lastUsed = System.currentTimeMillis();
        if (translucent)
            return Collections.EMPTY_LIST;
        return cache;
    }
    
}
