package team.creative.littletiles.common.level.little;

import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;

public interface LittleLevel extends IOrientatedLevel {
    
    public default Level asLevel() {
        return (Level) this;
    }
    
    public void registerBlockChangeListener(LevelBlockChangeListener listener);
    
    @Override
    public Entity getHolder();
    
    @Override
    public void setHolder(Entity entity);
    
    public UUID key();
    
    public void unload(LevelChunk chunk);
    
    @Override
    public void unload();
    
    public Iterable<Entity> entities();
    
    public Iterable<? extends ChunkAccess> chunks();
    
    public void tick();
    
    public default boolean allowPlacement() {
        return true;
    }
    
    public void removeEntityById(int id, RemovalReason reason);
    
    @OnlyIn(Dist.CLIENT)
    public LittleEntityRenderManager getRenderManager();
    
}
