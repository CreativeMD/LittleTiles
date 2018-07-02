package com.creativemd.littletiles.common.utils.shape;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class DragShape {
	
	public static LinkedHashMap<String, DragShape> shapes = new LinkedHashMap<>();
	
	public static DragShape box = new DragShapeBox();
	public static DragShape sphere = new DragShapeSphere();
	public static DragShape cylinder = new DragShapeCylinder();
	public static DragShape wall = new DragShapeWall();
	public static DragShape line = new DragShapeLine();
	
	public static DragShape slice = new DragShapeSliced();
	
	public static DragShape defaultShape = box;
	
	public static DragShape getShape(String name)
	{
		DragShape shape = DragShape.shapes.get(name);
		return shape == null ? DragShape.defaultShape : shape;
	}
	
	public final String key;
	
	public DragShape(String name) {
		shapes.put(name, this);
		this.key = name;
		new SelectShape.DragSelectShape(this);
	}
	
	public abstract LittleBoxes getBoxes(LittleBoxes boxes, LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTilePos originalMin, LittleTilePos originalMax);
	
	public abstract void addExtraInformation(NBTTagCompound nbt, List<String> list);
	
	@SideOnly(Side.CLIENT)
	public abstract List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context);
	
	@SideOnly(Side.CLIENT)
	public abstract void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context);
	
	public abstract void rotate(NBTTagCompound nbt, Rotation rotation);
	
	public abstract void flip(NBTTagCompound nbt, Axis axis);
	
}
