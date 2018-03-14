package com.creativemd.littletiles.common.utils.geo;

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

public class DragShapeWall extends DragShape {

	public DragShapeWall() {
		super("wall");
	}

	@Override
	public LittleBoxes getBoxes(LittleBoxes boxes, LittleTileVec min, LittleTileVec max, EntityPlayer player, NBTTagCompound nbt, boolean preview, LittleTilePos originalMin, LittleTilePos originalMax) {
		LittleTileBox box = new LittleTileBox(min, max);
		
		int direction = nbt.getInteger("direction");
		
		LittleTileSize size = box.getSize();
		
		int thicknessXInv = 0;
		int thicknessX = 0;
		int thicknessYInv = nbt.getInteger("thickness") > 1 ? (int) Math.ceil((nbt.getInteger("thickness")-1)/2D) : 0;
		int thicknessY = nbt.getInteger("thickness") > 1 ? (int) Math.floor((nbt.getInteger("thickness")-1)/2D) : 0;
		
		int w = originalMax.contextVec.vec.x - originalMin.contextVec.vec.x;
	    int h = originalMax.contextVec.vec.z - originalMin.contextVec.vec.z;
	    
	    int x = originalMin.contextVec.vec.x;
	    int y = originalMin.contextVec.vec.z;
	    
	    if(direction == 1){
	    	w = originalMax.contextVec.vec.y - originalMin.contextVec.vec.y;
	    	h = originalMax.contextVec.vec.z - originalMin.contextVec.vec.z;
	    	x = originalMin.contextVec.vec.y;
	    	y = originalMin.contextVec.vec.z;
		}else if(direction == 2){
			w = originalMax.contextVec.vec.x - originalMin.contextVec.vec.x;
	    	h = originalMax.contextVec.vec.y - originalMin.contextVec.vec.y;
	    	x = originalMin.contextVec.vec.x;
	    	y = originalMin.contextVec.vec.y;
		}
	    
	    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
	    if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
	    if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
	    int longest = Math.abs(w) ;
	    int shortest = Math.abs(h) ;
	    if (!(longest>shortest)) {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
	        dx2 = 0 ; 
	        
	        thicknessX = thicknessY;
	        thicknessXInv = thicknessYInv;
	        thicknessY = 0;
	        thicknessYInv = 0;
	    }
	    int numerator = longest >> 1 ;
	    for (int i=0;i<=longest;i++) {
	    	
	    	LittleTileBox toAdd = null;
			switch(direction)
			{
			case 0:
				toAdd = new LittleTileBox(x-thicknessXInv, box.minY, y-thicknessYInv, x + thicknessX + 1, box.maxY, y + thicknessY + 1);
				break;
			case 1:
				toAdd = new LittleTileBox(box.minX, x-thicknessXInv, y-thicknessYInv, box.maxX, x + thicknessX + 1, y + thicknessY + 1);
				break;
			case 2:
				toAdd = new LittleTileBox(x-thicknessXInv, y-thicknessYInv, box.minZ, x + thicknessX + 1, y + thicknessY + 1, box.maxZ);
				break;
			}
			boxes.add(toAdd);
			
	        numerator += shortest ;
	        if (!(numerator<longest)) {
	            numerator -= longest ;
	            x += dx1 ;
	            y += dy1 ;
	        } else {
	            x += dx2 ;
	            y += dy2 ;
	        }
	    }
		
		LittleTileBox.combineBoxesBlocks(boxes);
		
		return boxes;
	}

	@Override
	public void addExtraInformation(NBTTagCompound nbt, List<String> list) {
		list.add("thickness: " + nbt.getInteger("thickness") + " tiles");
		
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
			
		controls.add(new GuiSteppedSlider("thickness", 5, 5, 100, 14, nbt.getInteger("thickness"), 1, context.size));
		controls.add(new GuiStateButton("direction", nbt.getInteger("direction"), 5, 27, "facing: y", "facing: x", "facing: z"));
		return controls;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
		
		GuiSteppedSlider slider = (GuiSteppedSlider) gui.get("thickness");
		nbt.setInteger("thickness", (int) slider.value);
		
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
