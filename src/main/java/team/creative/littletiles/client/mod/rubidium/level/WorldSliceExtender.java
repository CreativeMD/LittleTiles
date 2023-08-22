package team.creative.littletiles.client.mod.rubidium.level;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;

public interface WorldSliceExtender {
    
    public static WorldSlice createEmptySlice() {
        return CreativeHackery.allocateInstance(WorldSlice.class);
    }
    
    public void setParent(Level level);
    
}
