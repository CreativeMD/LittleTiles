package team.creative.littletiles.common.structure.connection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWorldPositionProvider {
    
    public World getWorld();
    
    public BlockPos getPos();
    
    public void onStructureDestroyed();
    
}
