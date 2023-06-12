package team.creative.littletiles.common.structure.connection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ILevelPositionProvider {
    
    public Level getStructureLevel();
    
    public BlockPos getStructurePos();
    
    public void structureDestroyed();
    
}
