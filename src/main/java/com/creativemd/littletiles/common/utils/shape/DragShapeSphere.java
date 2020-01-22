package com.creativemd.littletiles.common.utils.shape;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tiles.math.box.LittleBox;
import com.creativemd.littletiles.common.tiles.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tiles.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tiles.math.vec.LittleVec;
import com.creativemd.littletiles.common.tiles.preview.LittlePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapeSphere extends DragShape {
	
	public DragShapeSphere() {
		super("sphere");
	}
	
	@Override
	public LittleBoxes getBoxes(LittleBoxes boxes, LittleVec min, LittleVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleAbsoluteVec originalMin, LittleAbsoluteVec originalMax) {
		LittleBox box = new LittleBox(min, max);
		
		boolean hollow = nbt.getBoolean("hollow");
		LittleVec size = box.getSize();
		if (preview && size.getPercentVolume(boxes.context) > 4) {
			boxes.add(box);
			return boxes;
		}
		
		LittleVec center = size.calculateCenter();
		LittleVec invCenter = size.calculateInvertedCenter();
		invCenter.invert();
		
		double a = Math.pow(Math.max(1, size.x / 2), 2);
		double b = Math.pow(Math.max(1, size.y / 2), 2);
		double c = Math.pow(Math.max(1, size.z / 2), 2);
		
		double a2 = 1;
		double b2 = 1;
		double c2 = 1;
		
		int thickness = nbt.getInteger("thickness");
		
		if (hollow && size.x > thickness * 2 && size.y > thickness * 2 && size.z > thickness * 2) {
			int all = size.x + size.y + size.z;
			
			double sizeXValue = (double) size.x / all;
			double sizeYValue = (double) size.y / all;
			double sizeZValue = (double) size.z / all;
			
			if (sizeXValue > 0.5)
				sizeXValue = 0.5;
			if (sizeYValue > 0.5)
				sizeYValue = 0.5;
			if (sizeZValue > 0.5)
				sizeZValue = 0.5;
			
			a2 = Math.pow(Math.max(1, (sizeXValue * all - thickness * 2) / 2), 2);
			b2 = Math.pow(Math.max(1, (sizeYValue * all - thickness * 2) / 2), 2);
			c2 = Math.pow(Math.max(1, (sizeZValue * all - thickness * 2) / 2), 2);
		} else
			hollow = false;
		
		boolean stretchedX = size.x % 2 == 0;
		boolean stretchedY = size.y % 2 == 0;
		boolean stretchedZ = size.z % 2 == 0;
		
		double centerX = size.x / 2;
		double centerY = size.y / 2;
		double centerZ = size.z / 2;
		
		min = box.getMinVec();
		
		for (int x = 0; x < size.x; x++) {
			for (int y = 0; y < size.y; y++) {
				for (int z = 0; z < size.z; z++) {
					
					double posX = x - centerX + (stretchedX ? 0.5 : 0);
					double posY = y - centerY + (stretchedY ? 0.5 : 0);
					double posZ = z - centerZ + (stretchedZ ? 0.5 : 0);
					
					double valueA = Math.pow(posX, 2) / a;
					double valueB = Math.pow(posY, 2) / b;
					double valueC = Math.pow(posZ, 2) / c;
					
					if (valueA + valueB + valueC <= 1) {
						double valueA2 = Math.pow(posX, 2) / a2;
						double valueB2 = Math.pow(posY, 2) / b2;
						double valueC2 = Math.pow(posZ, 2) / c2;
						if (!hollow || valueA2 + valueB2 + valueC2 > 1)
							boxes.add(new LittleBox(new LittleVec(min.x + x, min.y + y, min.z + z)));
					}
				}
			}
		}
		
		LittleBox.combineBoxesBlocks(boxes);
		
		if (preview && boxes.size() > LittlePreview.lowResolutionMode) {
			boxes.clear();
			boxes.add(box);
		}
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
		GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
		nbt.setInteger("thickness", (int) slider.value);
	}
	
	@Override
	public void rotate(NBTTagCompound nbt, Rotation rotation) {
		
	}
	
	@Override
	public void flip(NBTTagCompound nbt, Axis axis) {
		
	}
	
}
