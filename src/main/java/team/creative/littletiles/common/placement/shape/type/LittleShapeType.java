package team.creative.littletiles.common.placement.shape.type;

import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.ShapeSelection;
import team.creative.littletiles.common.placement.shape.ShapeSelection.ShapeSelectPos;

public class LittleShapeType extends LittleShapeSelectable {
    
    public LittleShapeType() {
        super(1);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        for (ShapeSelectPos pos : selection) {
            if (pos.result.isComplete()) {
                if (pos.result.parent.isStructure())
                    continue;
                
                LittleTile tile = pos.result.tile;
                for (LittleTile toDestroy : pos.result.be.noneStructureTiles())
                    if (tile.canBeCombined(toDestroy) && toDestroy.canBeCombined(tile))
                        addBox(boxes, selection.inside, selection.getGrid(), pos.result.te.noneStructureTiles(), toDestroy.getBox(), pos.pos.facing);
                    
            } else
                addBox(boxes, selection.inside, selection.getGrid(), pos.ray.getBlockPos(), pos.pos.facing);
        }
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<String> list) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {}
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {}
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
}
