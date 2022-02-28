package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class LittleShapeSlice extends LittleShape {
    
    public LittleShapeSlice() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        CornerCache cache = box.new CornerCache(false);
        
        Vec3i vec = getVec(selection.getNBT());
        Facing facing = getFacing(selection.getNBT());
        Axis axis = facing.axis;
        if ((facing.positive) != (VectorUtils.get(axis, vec) > 0))
            facing = Facing.get(axis, VectorUtils.get(axis, vec) > 0);
        Facing x = vec.getX() > 0 ? Facing.EAST : Facing.WEST;
        Facing y = vec.getY() > 0 ? Facing.UP : Facing.DOWN;
        Facing z = vec.getZ() > 0 ? Facing.SOUTH : Facing.NORTH;
        
        BoxCorner corner = BoxCorner.getCorner(x, y, z);
        
        cache.setAbsolute(corner, axis, box.get(facing.opposite()));
        cache.setAbsolute(corner.mirror(facing.axis), axis, box.get(facing.opposite()));
        
        box.setData(cache.getData());
        boxes.add(box);
    }
    
    public Vec3i getVec(CompoundTag nbt) {
        if (nbt.contains("vec")) {
            int[] array = nbt.getIntArray("vec");
            if (array.length == 3)
                return new Vec3i(array[0], array[1], array[2]);
        }
        return new Vec3i(1, 1, 1);
    }
    
    public Facing getFacing(CompoundTag nbt) {
        if (nbt.contains("direction"))
            return Facing.values()[nbt.getInt("direction")];
        return Facing.UP;
    }
    
    public void setFacing(CompoundTag nbt, Facing facing) {
        nbt.putInt("direction", facing.ordinal());
    }
    
    public void setVec(CompoundTag nbt, Vec3i vec) {
        nbt.putIntArray("vec", new int[] { vec.getX(), vec.getY(), vec.getZ() });
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {}
    
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
        setVec(nbt, rotation.transform(getVec(nbt)));
        setFacing(nbt, rotation.rotate(getFacing(nbt)));
    }
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {
        setVec(nbt, axis.mirror(getVec(nbt)));
        setFacing(nbt, axis.mirror(getFacing(nbt)));
    }
    
}
