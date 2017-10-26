package com.creativemd.littletiles.common.items.geo;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapeSphere extends DragShape {

	public DragShapeSphere() {
		super("sphere");
	}

	@Override
	public List<LittleTileBox> getBoxes(LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTileVec originalMin, LittleTileVec originalMax) {
		ArrayList<LittleTileBox> boxes = new ArrayList<>();
		LittleTileBox box = new LittleTileBox(min, max);
		
		boolean hollow = nbt.getBoolean("hollow");
		LittleTileSize size = box.getSize();
		if(preview && size.getPercentVolume() > 4)
		{
			boxes.add(box);
			return boxes;
		}
		
		LittleTileVec center = size.calculateCenter();
		LittleTileVec invCenter = size.calculateInvertedCenter();
		invCenter.invert();
		
		double a = Math.pow(Math.max(1, size.sizeX/2), 2);
		double b = Math.pow(Math.max(1, size.sizeY/2), 2);
		double c = Math.pow(Math.max(1, size.sizeZ/2), 2);
		
		double a2 = 1;
		double b2 = 1;
		double c2 = 1;
		
		int thickness = nbt.getInteger("thickness");
		
		if(hollow && size.sizeX > thickness*2 && size.sizeY > thickness*2 && size.sizeZ > thickness*2)
		{
			int all = size.sizeX+size.sizeY+size.sizeZ;
			
			double sizeXValue = (double)size.sizeX/all;
			double sizeYValue = (double)size.sizeY/all;
			double sizeZValue = (double)size.sizeZ/all;
			
			if(sizeXValue > 0.5)
				sizeXValue = 0.5;
			if(sizeYValue > 0.5)
				sizeYValue = 0.5;
			if(sizeZValue > 0.5)
				sizeZValue = 0.5;
			
			a2 = Math.pow(Math.max(1, (sizeXValue*all-thickness*2)/2), 2);
			b2 = Math.pow(Math.max(1, (sizeYValue*all-thickness*2)/2), 2);
			c2 = Math.pow(Math.max(1, (sizeZValue*all-thickness*2)/2), 2);
		}else
			hollow = false;
		
		boolean stretchedX = size.sizeX % 2 == 0;
		boolean stretchedY = size.sizeY % 2 == 0;
		boolean stretchedZ = size.sizeZ % 2 == 0;
		
		double centerX = size.sizeX/2;
		double centerY = size.sizeY/2;
		double centerZ = size.sizeZ/2;
		
		min = box.getMinVec();
		
		for (int x = 0; x < size.sizeX; x++) {
			for (int y = 0; y < size.sizeY; y++) {
				for (int z = 0; z < size.sizeZ; z++) {
					
					double posX = x - centerX + (stretchedX ? 0.5 : 0);
					double posY = y - centerY + (stretchedY ? 0.5 : 0);
					double posZ = z - centerZ + (stretchedZ ? 0.5 : 0);
					
					double valueA = Math.pow(posX, 2)/a;
					double valueB = Math.pow(posY, 2)/b;
					double valueC = Math.pow(posZ, 2)/c;
					
					if(valueA + valueB + valueC <= 1)
					{
						double valueA2 = Math.pow(posX, 2)/a2;
						double valueB2 = Math.pow(posY, 2)/b2;
						double valueC2 = Math.pow(posZ, 2)/c2;
						if(!hollow || valueA2 + valueB2 + valueC2 > 1)
							boxes.add(new LittleTileBox(new LittleTileVec(min.x + x, min.y + y, min.z + z)));
					}
				}
			}
		}
		
		LittleTileBox.combineBoxesBlocks(boxes);
		
		return boxes;
	}

	@Override
	public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
		if(nbt.getBoolean("hollow"))
		{
			list.add("type: hollow");
			list.add("thickness: " + nbt.getInteger("thickness") + " tiles");
		}else
			list.add("type: solid");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<GuiControl> getCustomSettings(NBTTagCompound nbt) {
		List<GuiControl> controls = new ArrayList<>();
		
		controls.add(new GuiCheckBox("hollow", 5, 0, nbt.getBoolean("hollow")));			
		controls.add(new GuiSteppedSlider("thickness", 5, 20, 100, 14, nbt.getInteger("thickness"), 1, LittleTile.gridSize));
		
		return controls;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt) {
		
		GuiCheckBox box = (GuiCheckBox) gui.get("hollow");
		nbt.setBoolean("hollow", box.value);
		if(box.value)
		{
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
