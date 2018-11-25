package com.creativemd.littletiles.common.utils.shape;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;

public class DragShapeSliced extends DragShape {
	
	public DragShapeSliced() {
		super("slice");
	}
	
	@Override
	public LittleBoxes getBoxes(LittleBoxes boxes, LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTilePos originalMin, LittleTilePos originalMax) {
		boxes.add(new LittleTileSlicedOrdinaryBox(min.x, min.y, min.z, max.x, max.y, max.z, LittleSlice.values()[nbt.getInteger("slice")]));
		return boxes;
	}
	
	@Override
	public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
		
	}
	
	@Override
	public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
		return new ArrayList<>();
	}
	
	@Override
	public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
		
	}
	
	@Override
	public void rotate(NBTTagCompound nbt, Rotation rotation) {
		LittleSlice slice = LittleSlice.values()[nbt.getInteger("slice")];
		slice = slice.rotate(rotation);
		nbt.setInteger("slice", slice.ordinal());
	}
	
	@Override
	public void flip(NBTTagCompound nbt, Axis axis) {
		LittleSlice slice = LittleSlice.values()[nbt.getInteger("slice")];
		slice = slice.flip(axis);
		nbt.setInteger("slice", slice.ordinal());
	}
	
}
