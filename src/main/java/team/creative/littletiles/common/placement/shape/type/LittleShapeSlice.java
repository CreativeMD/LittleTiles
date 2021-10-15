package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tile.math.box.slice.LittleSlice;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeSlice extends LittleShape {
    
    public LittleShapeSlice() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleBox box = selection.getOverallBox();
        boxes.add(new LittleTransformableBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, LittleSlice.values()[selection.getNBT().getInt("slice")]));
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<String> list) {
        
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        return new ArrayList<>();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {}
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {
        LittleSlice slice = LittleSlice.values()[nbt.getInt("slice")];
        slice = slice.rotate(rotation);
        nbt.putInt("slice", slice.ordinal());
    }
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {
        LittleSlice slice = LittleSlice.values()[nbt.getInt("slice")];
        slice = slice.flip(axis);
        nbt.putInt("slice", slice.ordinal());
    }
    
}
