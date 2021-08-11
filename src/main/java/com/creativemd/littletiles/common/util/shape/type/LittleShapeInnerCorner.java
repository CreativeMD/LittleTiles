package com.creativemd.littletiles.common.util.shape.type;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.VectorUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxCorner;
import com.creativemd.creativecore.common.utils.math.box.BoxFace;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.LittleShape;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;

public class LittleShapeInnerCorner extends LittleShape {
    
    public LittleShapeInnerCorner() {
        super(2);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        LittleTransformableBox box = new LittleTransformableBox(selection.getOverallBox(), new int[1]);
        CornerCache cache = box.new CornerCache(false);
        
        Vec3i vec = getVec(selection.getNBT());
        EnumFacing facing = getFacing(selection.getNBT());
        Axis axis = facing.getAxis();
        if ((facing.getAxisDirection() == AxisDirection.POSITIVE) != (VectorUtils.get(axis, vec) > 0))
            facing = EnumFacing.getFacingFromAxis(VectorUtils.get(axis, vec) > 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, axis);
        EnumFacing x = vec.getX() > 0 ? EnumFacing.EAST : EnumFacing.WEST;
        EnumFacing y = vec.getY() > 0 ? EnumFacing.UP : EnumFacing.DOWN;
        EnumFacing z = vec.getZ() > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
        
        BoxCorner corner = BoxCorner.getCorner(x, y, z);
        BoxFace face = BoxFace.get(facing);
        boolean flipped = false;
        if (face.getCornerInQuestion(false, false) != corner && face.getCornerInQuestion(true, false) != corner)
            flipped = true;
        
        cache.setAbsolute(corner, axis, box.get(facing.getOpposite()));
        
        box.setData(cache.getData());
        if (selection.getNBT().getBoolean("second"))
            flipped = !flipped;
        box.setFlipped(facing, flipped);
        boxes.add(box);
    }
    
    public Vec3i getVec(NBTTagCompound nbt) {
        if (nbt.hasKey("vec")) {
            int[] array = nbt.getIntArray("vec");
            if (array.length == 3)
                return new Vec3i(array[0], array[1], array[2]);
        }
        return new Vec3i(1, 1, 1);
    }
    
    public EnumFacing getFacing(NBTTagCompound nbt) {
        if (nbt.hasKey("direction"))
            return EnumFacing.VALUES[nbt.getInteger("direction")];
        return EnumFacing.UP;
    }
    
    public void setFacing(NBTTagCompound nbt, EnumFacing facing) {
        nbt.setInteger("direction", facing.ordinal());
    }
    
    public void setVec(NBTTagCompound nbt, Vec3i vec) {
        nbt.setIntArray("vec", new int[] { vec.getX(), vec.getY(), vec.getZ() });
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
        list.add("second-type: " + nbt.getBoolean("second"));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        List<GuiControl> controls = new ArrayList<>();
        controls.add(new GuiCheckBox("second-type", 5, 5, nbt.getBoolean("second")));
        return controls;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        GuiCheckBox box = (GuiCheckBox) gui.get("second-type");
        nbt.setBoolean("second", box.value);
    }
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        setVec(nbt, RotationUtils.rotate(getVec(nbt), rotation));
        setFacing(nbt, RotationUtils.rotate(getFacing(nbt), rotation));
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        setVec(nbt, RotationUtils.flip(getVec(nbt), axis));
        setFacing(nbt, RotationUtils.flip(getFacing(nbt), axis));
    }
}
