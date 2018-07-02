package com.creativemd.littletiles.common.utils.shape;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragShapeCylinder extends DragShape {

	public DragShapeCylinder() {
		super("cylinder");
	}

	@Override
	public LittleBoxes getBoxes(LittleBoxes boxes, LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTilePos originalMin, LittleTilePos originalMax) {
		LittleTileBox box = new LittleTileBox(min, max);
		
		boolean hollow = nbt.getBoolean("hollow");
		
		int direction = nbt.getInteger("direction");
		
		LittleTileSize size = box.getSize();
		
		int sizeA = size.sizeX;
		int sizeB = size.sizeZ;
		
		if(direction == 1){
			sizeA = size.sizeY;
			sizeB = size.sizeZ;
		}else if(direction == 2){
			sizeA = size.sizeX;
			sizeB = size.sizeY;
		}
		
		double a = Math.pow(Math.max(1, sizeA/2), 2);
		double b = Math.pow(Math.max(1, sizeB/2), 2);
		
		double a2 = 1;
		double b2 = 1;
		double c2 = 1;
		
		int thickness = nbt.getInteger("thickness");
		
		if(hollow && sizeA > thickness*2 && sizeB > thickness*2)
		{
			int all = sizeA+sizeB;
			
			double sizeAValue = (double)sizeA/all;
			double sizeBValue = (double)sizeB/all;
			
			if(sizeAValue > 0.5)
				sizeAValue = 0.5;
			if(sizeBValue > 0.5)
				sizeBValue = 0.5;
			
			a2 = Math.pow(Math.max(1, (sizeAValue*all-thickness*2)/2), 2);
			b2 = Math.pow(Math.max(1, (sizeBValue*all-thickness*2)/2), 2);
		}else
			hollow = false;
		
		boolean stretchedA = sizeA % 2 == 0;
		boolean stretchedB = sizeB % 2 == 0;
		
		double centerA = sizeA/2;
		double centerB = sizeB/2;
		
		min = box.getMinVec();
		max = box.getMaxVec();
		
		for (int incA = 0; incA < sizeA; incA++) {
			for (int incB = 0; incB < sizeB; incB++) {
				double posA = incA - centerA + (stretchedA ? 0.5 : 0);
				double posB = incB - centerB + (stretchedB ? 0.5 : 0);
				
				double valueA = Math.pow(posA, 2)/a;
				double valueB = Math.pow(posB, 2)/b;
				
				if(valueA + valueB <= 1)
				{
					double valueA2 = Math.pow(posA, 2)/a2;
					double valueB2 = Math.pow(posB, 2)/b2;
					if(!hollow || valueA2 + valueB2 > 1)
					{
						LittleTileBox toAdd = null;
						switch(direction)
						{
						case 0:
							toAdd = new LittleTileBox(min.x + incA, min.y, min.z + incB, min.x + incA + 1, max.y, min.z + incB + 1);
							break;
						case 1:
							toAdd = new LittleTileBox(min.x, min.y + incA, min.z + incB, max.x, min.y + incA + 1, min.z + incB + 1);
							break;
						case 2:
							toAdd = new LittleTileBox(min.x + incA, min.y + incB, min.z, min.x + incA + 1, min.y + incB + 1, max.z);
							break;
						}
						boxes.add(toAdd);
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
		
		int facing = nbt.getInteger("direction");
		String text = "facing: ";
		switch(facing)
		{
		case 0:
			text += "y";
			break;
		case 1:
			text += "x";
			break;
		case 2:
			text += "z";
			break;
		}
		list.add(text);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
		List<GuiControl> controls = new ArrayList<>();
		
		controls.add(new GuiCheckBox("hollow", 5, 0, nbt.getBoolean("hollow")));			
		controls.add(new GuiSteppedSlider("thickness", 5, 20, 100, 14, nbt.getInteger("thickness"), 1, context.size));
		controls.add(new GuiStateButton("direction", nbt.getInteger("direction"), 5, 42, "facing: y", "facing: x", "facing: z"));
		return controls;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
		
		GuiCheckBox box = (GuiCheckBox) gui.get("hollow");
		nbt.setBoolean("hollow", box.value);
		if(box.value)
		{
			GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
			nbt.setInteger("thickness", (int) slider.value);
		}
		
		GuiStateButton state = (GuiStateButton) gui.get("direction");
		nbt.setInteger("direction", state.getState());
		
	}

	@Override
	public void rotate(NBTTagCompound nbt, Rotation rotation) {
		int direction = nbt.getInteger("direction");
		if(rotation.axis != Axis.Y)
			direction = 0;
		else{
			if(direction == 1)
				direction = 2;
			else
				direction = 1;
		}
		
		nbt.setInteger("direction", direction);
	}

	@Override
	public void flip(NBTTagCompound nbt, Axis axis) {
		
	}

}
