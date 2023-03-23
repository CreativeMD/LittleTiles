package team.creative.littletiles.common.structure.type.animation;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;

public class LittleSlidingDoor extends LittleDoor {
    
    @StructureDirectional
    public Facing direction;
    public LittleGrid grid;
    public int distance;
    
    public LittleSlidingDoor(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        super.loadExtra(nbt);
        distance = nbt.getInt("dis");
        grid = LittleGrid.get(nbt.getInt("disG"));
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        super.saveExtra(nbt);
        nbt.putInt("dis", distance);
        nbt.putInt("disG", grid.count);
    }
    
}
