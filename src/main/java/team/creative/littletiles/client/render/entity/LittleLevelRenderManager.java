package team.creative.littletiles.client.render.entity;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.CreativeLevel;

@OnlyIn(Dist.CLIENT)
public class LittleLevelRenderManager {
    
    public final CreativeLevel level;
    private HashMap<Long, LittleRenderChunk> chunks = new HashMap<>();
    
    public LittleLevelRenderManager(CreativeLevel level) {
        this.level = level;
    }
    
    public synchronized LittleRenderChunk getChunk(BlockPos pos) {
        return chunks.get(SectionPos.asLong(pos));
    }
    
    public synchronized LittleRenderChunk getChunk(SectionPos pos) {
        return chunks.get(pos.asLong());
    }
    
    public synchronized void backToRAM() {
        
    }
    
    public synchronized void unload() {
        
    }
    
}
