package com.creativemd.littletiles.common.items.shapes;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ShapeSphere extends ChiselShape {

	public ShapeSphere() {
		super("sphere");
	}

	@Override
	public List<LittleTileBox> getBoxes(LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview) {
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
			
			a2 = Math.pow(Math.max(1, (size.sizeX-thickness*2)/2), 2);
			b2 = Math.pow(Math.max(1, (size.sizeY-thickness*2)/2), 2);
			c2 = Math.pow(Math.max(1, (size.sizeZ-thickness*2)/2), 2);
		}else
			hollow = false;
		
		for (int x = invCenter.x; x < center.x; x++) {
			for (int y = invCenter.y; y < center.y; y++) {
				for (int z = invCenter.z; z < center.z; z++) {
					if((Math.pow(x, 2))/a + (Math.pow(y, 2))/b + (Math.pow(z, 2))/c <= 1)
					{
						if(!hollow || (Math.pow(x, 2))/a2 + (Math.pow(y, 2))/b2 + (Math.pow(z, 2))/c2 > 1)
							boxes.add(new LittleTileBox(new LittleTileVec(x-invCenter.x+min.x, y-invCenter.y+min.y, z-invCenter.z+min.z)));
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
		
		controls.add(new GuiCheckBox("hollow", 0, 0, nbt.getBoolean("hollow")));			
		controls.add(new GuiSteppedSlider("thickness", 0, 20, 100, 14, nbt.getInteger("thickness"), 1, LittleTile.gridSize));
		
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
	
	

}
