package team.creative.littletiles.client.render.item;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public class ItemModelCacheLayered extends ItemModelCache {
    
    private List<BakedQuad> translucentCache;
    
    public ItemModelCacheLayered() {
        super();
    }
    
    @Override
    public void setQuads(boolean translucent, List<BakedQuad> baked) {
        if (translucent)
            translucentCache = baked;
        else
            super.setQuads(translucent, baked);
    }
    
    @Override
    public List<BakedQuad> getQuads(boolean translucent) {
        if (translucent)
            return translucentCache;
        return super.getQuads(translucent);
    }
    
}
