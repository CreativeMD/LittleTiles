package team.creative.littletiles.client.render.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.render.model.CreativeBakedModel;
import team.creative.creativecore.client.render.model.CreativeRenderItem;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.level.LevelAwareHandler;

public class ItemRenderCache implements LevelAwareHandler {
    
    public static final RenderingThreadItem THREAD = new RenderingThreadItem();
    
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
            CreativeRenderItem renderer = CreativeCoreClient.RENDERED_ITEMS.get(stack.getItem());
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
                    CreativeRenderItem renderer = CreativeCoreClient.RENDERED_ITEMS.get(pair.key.getItem());
                    
                    if (renderer != null) {
                        RenderType[] layers = renderer.getLayers(pair.key, true);
                        Random rand = new Random();
                        for (int i = 0; i < layers.length; i++) {
                            RenderType layer = layers[i];
                            for (int j = 0; j < Facing.values().length; j++) {
                                Facing facing = Facing.values()[j];
                                pair.value.setQuads(layer, facing, CreativeBakedModel.compileBoxes(renderer.getBoxes(pair.key, layer), facing, layer, rand, true));
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
