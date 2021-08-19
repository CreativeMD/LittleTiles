package team.creative.littletiles.common.tile.group;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleGroupExtension extends LittleGroupStructure {
    
    public LittleGroupExtension(CompoundTag nbt, LittleGrid grid) {
        super(nbt, grid);
    }
    
    @Override
    public LittleGroupType type() {
        return LittleGroupType.EXTENSION;
    }
}
