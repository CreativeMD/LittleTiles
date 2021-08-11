package team.creative.littletiles.common.structure.connection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ILevelPositionProvider {
    
    public Level getLevel();
    
    public BlockPos getPos();
    
    public void onStructureDestroyed();
    
}
