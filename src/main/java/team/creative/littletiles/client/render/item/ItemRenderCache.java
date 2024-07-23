package team.creative.littletiles.client.render.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.client.render.model.CreativeBakedBoxModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.level.LevelAwareHandler;

public class ItemRenderCache implements LevelAwareHandler {
    
    public static final RenderingThreadItem THREAD = new RenderingThreadItem();
    
    public static CreativeItemBoxModel get(ItemStack stack) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        if (model instanceof CreativeBakedBoxModel)
            return (CreativeItemBoxModel) ((CreativeBakedBoxModel) model).item;
        return null;
    }
    
    private RenderedStack temp = new RenderedStack();
    private HashMap<RenderedStack, ItemModelCache> caches = new HashMap<>();
    private int slowTicker = 0;
    private int timeToCheckSlowTick = 100;
    
    public ItemRenderCache() {
        NeoForge.EVENT_BUS.addListener(this::tick);
    }
    
    public int countCaches() {
        return caches.size();
    }
    
    public List<BakedQuad> requestCache(ItemStack stack, boolean translucent) {
        synchronized (caches) {
            if (ILittleTool.getData(stack).isEmpty())
                return null;
            ItemModelCache cache = caches.get(temp.set(stack));
            if (cache != null)
                return cache.getQuads(translucent);
            CreativeItemBoxModel renderer = get(stack);
            if (renderer != null) {
                if (renderer.hasTranslucentLayer(stack))
                    cache = new ItemModelCacheLayered();
                else
                    cache = new ItemModelCache();
                caches.put(new RenderedStack().set(stack), cache);
                THREAD.items.add(new Pair<>(stack, cache));
            }
            return null;
        }
    }
    
    public void clearCache() {
        synchronized (caches) {
            caches.clear();
            THREAD.items.clear();
        }
    }
    
    @Override
    public void unload() {
        caches.clear();
        THREAD.items.clear();
    }
    
    public void tick(ClientTickEvent event) {
        slowTicker++;
        if (slowTicker >= timeToCheckSlowTick) {
            for (Iterator<ItemModelCache> iterator = caches.values().iterator(); iterator.hasNext();)
                if (iterator.next().expired())
                    iterator.remove();
            slowTicker = 0;
        }
    }
    
    public static class RenderedStack {
        
        private Item item;
        private CompoundTag nbt;
        
        public RenderedStack set(ItemStack stack) {
            item = stack.getItem();
            nbt = ILittleTool.getData(stack);
            return this;
        }
        
        @Override
        public int hashCode() {
            return nbt.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RenderedStack stack)
                return stack.item == item && stack.nbt.equals(nbt);
            return false;
        }
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
                        boolean translucent = renderer.hasTranslucentLayer(pair.key);
                        RandomSource rand = RandomSource.create();
                        List<BakedQuad> quads = new ArrayList<>();
                        for (int j = 0; j < Facing.VALUES.length; j++)
                            CreativeBakedBoxModel.compileBoxes(renderer.getBoxes(pair.key, false), Facing.VALUES[j], Sheets.cutoutBlockSheet(), rand, true, quads);
                        pair.value.setQuads(false, quads);
                        if (translucent) {
                            quads = new ArrayList<>();
                            for (int j = 0; j < Facing.VALUES.length; j++)
                                CreativeBakedBoxModel.compileBoxes(renderer.getBoxes(pair.key, true), Facing.VALUES[j], Sheets.translucentCullBlockSheet(), rand, true, quads);
                            pair.value.setQuads(true, quads);
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
