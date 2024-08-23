package team.creative.littletiles.client.render.cache.build;

import java.util.concurrent.ConcurrentLinkedQueue;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.Level;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

public class RenderingBlockQueue {
    
    private ConcurrentLinkedQueue<RenderingBlockContext> queue = new ConcurrentLinkedQueue<>();
    private Object2ObjectMap<Level, Long2IntMap> levels = new Object2ObjectOpenHashMap<>();
    
    public void requeue(RenderingBlockContext context) {
        queue.add(context);
    }
    
    public synchronized void queue(BETiles tiles, long pos) {
        var level = tiles.getLevel();
        var handler = RenderingLevelHandler.of(level);
        
        RenderingBlockContext context = new RenderingBlockContext(tiles, pos, handler);
        var sections = levels.computeIfAbsent(level, x -> new Long2IntOpenHashMap());
        pos = context.queuedSection(); // get rid of different render chunks if it is just one in cause of an animation
        int count = sections.getOrDefault(pos, 0);
        count++;
        sections.put(pos, count);
        queue.add(context);
    }
    
    public synchronized RenderChunkExtender unqeue(RenderingBlockContext context) {
        var level = context.getLevel();
        var sections = levels.get(level);
        if (sections == null)
            return null;
        long pos = context.queuedSection(); // get rid of different render chunks if it is just one in cause of an animation
        int count = sections.get(pos);
        count--;
        if (count <= 0) {
            sections.remove(pos);
            if (sections.isEmpty())
                levels.remove(level);
            return context.getRenderChunk();
        }
        sections.put(pos, count);
        return null;
    }
    
    public RenderingBlockContext poll() {
        return queue.poll();
    }
    
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    public int levelCount() {
        return levels.size();
    }
    
    public int sectionCount() {
        int sections = 0;
        for (Long2IntMap map : levels.values())
            sections += map.size();
        return sections;
    }
    
    public int size() {
        return queue.size();
    }
    
    public void clear() {
        levels.clear();
        queue.clear();
    }
    
}
