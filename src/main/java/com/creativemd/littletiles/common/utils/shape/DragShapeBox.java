package com.creativemd.littletiles.common.utils.shape;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapeBox extends DragShape {
	
	public DragShapeBox() {
		super("box");
	}
	
	@Override
	public LittleBoxes getBoxes(LittleBoxes boxes, LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTilePos originalMin, LittleTilePos originalMax) {
		LittleTileBox box = new LittleTileBox(min, max);
		if (nbt.getBoolean("hollow")) {
			int thickness = nbt.getInteger("thickness");
			LittleTileSize size = box.getSize();
			if (thickness * 2 >= size.sizeX || thickness * 2 >= size.sizeY || thickness * 2 >= size.sizeZ)
				boxes.add(box);
			else {
				boxes.add(new LittleTileBox(min.x, min.y, min.z, max.x, max.y, min.z + thickness));
				boxes.add(new LittleTileBox(min.x, min.y + thickness, min.z + thickness, min.x + thickness, max.y - thickness, max.z - thickness));
				boxes.add(new LittleTileBox(max.x - thickness, min.y + thickness, min.z + thickness, max.x, max.y - thickness, max.z - thickness));
				boxes.add(new LittleTileBox(min.x, min.y, min.z + thickness, max.x, min.y + thickness, max.z - thickness));
				boxes.add(new LittleTileBox(min.x, max.y - thickness, min.z + thickness, max.x, max.y, max.z - thickness));
				boxes.add(new LittleTileBox(min.x, min.y, max.z - thickness, max.x, max.y, max.z));
			}
		} else
			boxes.add(box);
		return boxes;
	}
	
	@Override
	public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
		if (nbt.getBoolean("hollow")) {
			list.add("type: hollow");
			list.add("thickness: " + nbt.getInteger("thickness") + " tiles");
		} else
			list.add("type: solid");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
		List<GuiControl> controls = new ArrayList<>();
		
		controls.add(new GuiCheckBox("hollow", 5, 0, nbt.getBoolean("hollow")));
		controls.add(new GuiSteppedSlider("thickness", 5, 20, 100, 14, nbt.getInteger("thickness"), 1, context.size));
		
		return controls;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
		
		GuiCheckBox box = (GuiCheckBox) gui.get("hollow");
		nbt.setBoolean("hollow", box.value);
		if (box.value) {
			GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
			nbt.setInteger("thickness", (int) slider.value);
		}
	}
	
	@Override
	public void rotate(NBTTagCompound nbt, Rotation rotation) {
		
	}
	
	@Override
	public void flip(NBTTagCompound nbt, Axis axis) {
		
	}
	
}
