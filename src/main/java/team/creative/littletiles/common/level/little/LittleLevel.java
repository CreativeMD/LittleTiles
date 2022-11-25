package team.creative.littletiles.common.level.little;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
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
    
    public UUID key();
    
    public void registerLevelBoundListener(LevelBoundsListener listener);
    
    public void load(ChunkPos pos, CompoundTag nbt);
    
    public void unload(LevelChunk chunk);
    
    public Iterable<Entity> entities();
    
    public int getFreeMapId();
    
    public Iterable<? extends ChunkAccess> chunks();
    
    public void tick();
    
    public PacketListener getPacketListener();
    
}
