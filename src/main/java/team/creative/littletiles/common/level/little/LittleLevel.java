package team.creative.littletiles.common.level.little;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.creativecore.common.level.IOrientatedLevel;

public interface LittleLevel extends IOrientatedLevel {
    
    public default Level asLevel() {
        return (Level) this;
    }
    
    @Override
    public Entity getHolder();
    
    @Override
    public void setHolder(Entity entity);
    
    public void registerLevelBoundListener(LevelBoundsListener listener);
    
    public void load(LevelChunk chunk);
    
    public void unload(LevelChunk chunk);
    
    public void onChunkLoaded(LevelChunk chunk);
    
    public Iterable<Entity> entities();
    
    public int getFreeMapId();
    
    public void tickBlockEntities();
    
    public Iterable<? extends ChunkAccess> chunks();
    
}
