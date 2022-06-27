package team.creative.littletiles.client.render.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.render.model.CreativeBakedBoxModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.level.LevelAwareHandler;

public class ItemRenderCache implements LevelAwareHandler {
    
    public static final RenderingThreadItem THREAD = new RenderingThreadItem();
    
    public static CreativeItemBoxModel get(ItemStack stack) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        if (model instanceof CreativeBakedBoxModel)
            return (CreativeItemBoxModel) ((CreativeBakedBoxModel) model).item;
        return null;
    }
    
    private HashMap<ItemStack, ItemModelCache> caches = new HashMap<>();
    
    public ItemRenderCache() {}
    
    public int countCaches() {
        return caches.size();
    }
    
    public List<BakedQuad> requestCache(ItemStack stack, RenderType layer, Facing facing) {
        synchronized (caches) {
            ItemModelCache cache = caches.get(stack);
            if (cache != null)
                return cache.getQuads(layer, facing);
            CreativeItemBoxModel renderer = get(stack);
            if (renderer != null) {
                if (renderer.hasTranslucentLayer(stack))
                    cache = new ItemModelCacheLayered();
                else
                    cache = new ItemModelCache();
                caches.put(stack, cache);
                THREAD.items.add(new Pair<>(stack, cache));
            }
            return null;
        }
    }
    
    @Override
    public void unload() {
        caches.clear();
        THREAD.items.clear();
    }
    
    @Override
    public void slowTick() {
        for (Iterator<ItemModelCache> iterator = caches.values().iterator(); iterator.hasNext();)
            if (iterator.next().expired())
                iterator.remove();
    }
    
    public static class RenderingThreadItem extends Thread {
        
        public ConcurrentLinkedQueue<Pair<ItemStack, ItemModelCache>> items = new ConcurrentLinkedQueue<>();
        
        public RenderingThreadItem() {
            start();
        }
        
        @Override
        public void run() {
            while (true) {
                if (Minecraft.getInstance().level != null && !items.isEmpty()) {
                    Pair<ItemStack, ItemModelCache> pair = items.poll();
                    CreativeItemBoxModel renderer = get(pair.getKey());
                    
                    if (renderer != null) {
                        RenderType[] layers = renderer.getLayers(pair.key, true);
                        RandomSource rand = RandomSource.create();
                        for (int i = 0; i < layers.length; i++) {
                            RenderType layer = layers[i];
                            for (int j = 0; j < Facing.values().length; j++) {
                                Facing facing = Facing.values()[j];
                                pair.value.setQuads(layer, facing, CreativeBakedBoxModel.compileBoxes(renderer.getBoxes(pair.key, layer), facing, layer, rand, true));
                            }
                        }
                        pair.value.complete();
                    }
                    
                } else
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {}
            }
        }
    }
    
}
