package com.creativemd.littletiles.common.items.geo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ChiselShape {
	
	public static LinkedHashMap<String, ChiselShape> shapes = new LinkedHashMap<>();
	
	public static ChiselShape box = new ChiselShapeBox();
	public static ChiselShape sphere = new ChiselShapeSphere();
	public static ChiselShape cylinder = new ChiselShapeCylinder();
	public static ChiselShape wall = new ChiselShapeWall();
	
	public static ChiselShape defaultShape = box;
	
	public static ChiselShape getShape(String name)
	{
		ChiselShape shape = ChiselShape.shapes.get(name);
		return shape == null ? ChiselShape.defaultShape : shape;
	}
	
	public final String key;
	
	public ChiselShape(String name) {
		shapes.put(name, this);
		this.key = name;
		new SelectShape.ChiselSelectShape(this);
	}
	
	public abstract List<LittleTileBox> getBoxes(LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTileVec originalMin, LittleTileVec originalMax);
	
	public abstract void addExtraInformation(NBTTagCompound nbt, List<String> list);
	
	@SideOnly(Side.CLIENT)
	public abstract List<GuiControl> getCustomSettings(NBTTagCompound nbt);
	
	@SideOnly(Side.CLIENT)
	public abstract void saveCustomSettings(GuiParent gui, NBTTagCompound nbt);
	
}
