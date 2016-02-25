package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.Constants.NBT;
import scala.tools.nsc.backend.icode.Primitives.Shift;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.InsideShiftHandler;
import com.creativemd.littletiles.utils.ShiftHandler;

public final class LittleTilePreview {
	
	public boolean canSplit = true;
	public LittleTileSize size = null;
	
	public NBTTagCompound nbt;
	///**Used for multiblocks**/
	//public ArrayList<LittleTilePreview> subTiles = new ArrayList<LittleTilePreview>(); 
	
	public LittleTileBox box;
	
	public ArrayList<ShiftHandler> shifthandlers = new ArrayList<ShiftHandler>();
	
	public LittleTilePreview(LittleTileBox box, NBTTagCompound nbt)
	{
		this(box.getSize(), nbt);
		this.box = box;
	}
	
	public LittleTilePreview(LittleTileSize size, NBTTagCompound nbt)
	{
		this.size = size;
		this.nbt = nbt;
	}
	
	public void updateSize()
	{
		size = box.getSize();
	}
	
	public LittleTile getLittleTile(TileEntityLittleTiles te)
	{
		return LittleTile.CreateandLoadTile(te, te.getWorldObj(), nbt);
	}
	
	/*public ArrayList<LittleTilePreview> getAllTiles()
	{
		ArrayList<LittleTilePreview> tiles = new ArrayList<LittleTilePreview>();
		if(box != null || subTiles.size() == 0)
			tiles.add(this);
		for (int i = 0; i < subTiles.size(); i++) {
			tiles.addAll(subTiles.get(i).getAllTiles());
		}
		return tiles;
	}*/
	
	public CubeObject getCubeBlock()
	{
		CubeObject cube = box.getCube();
		if(nbt.hasKey("block"))
		{
			cube.block = Block.getBlockFromName(nbt.getString("block"));
			cube.meta = nbt.getInteger("meta");
		}else{
			cube.block = Blocks.stone;
		}
		if(nbt.hasKey("color"))
			cube.color = nbt.getInteger("color");
		return cube;
	}

	public LittleTilePreview copy() {
		LittleTilePreview preview = new LittleTilePreview(size != null ? size.copy() : null, (NBTTagCompound)nbt.copy());
		preview.canSplit = this.canSplit;
		preview.shifthandlers = new ArrayList<ShiftHandler>(this.shifthandlers);
		if(box != null)
			preview.box = box.copy();
		return preview;
	}
	
	public static void flipPreview(NBTTagCompound nbt, ForgeDirection direction)
	{
		if(nbt.hasKey("bBoxminX"))
		{
			LittleTileBox box = new LittleTileBox("bBox", nbt);
			box.flipBoxWithCenter(direction, null);
			box.writeToNBT("bBox", nbt);
		}
		if(nbt.hasKey("bSize"))
		{
			int count = nbt.getInteger("bSize");
			for (int i = 0; i < count; i++) {
				LittleTileBox box = new LittleTileBox("bBox" + i, nbt);
				box.flipBoxWithCenter(direction, null);
				//box.rotateBox(direction.getRotation(ForgeDirection.UP));
				box.writeToNBT("bBox" + i, nbt);
			}
		}
	}
	
	public static void rotatePreview(NBTTagCompound nbt, ForgeDirection direction)
	{
		if(nbt.hasKey("sizex"))
		{
			LittleTileSize size = new LittleTileSize("size", nbt);
			size.rotateSize(direction);
			size.writeToNBT("size", nbt);
		}
		if(nbt.hasKey("bBoxminX"))
		{
			LittleTileBox box = new LittleTileBox("bBox", nbt);
			box.rotateBox(direction);
			box.writeToNBT("bBox", nbt);
		}
		if(nbt.hasKey("bSize"))
		{
			int count = nbt.getInteger("bSize");
			for (int i = 0; i < count; i++) {
				LittleTileBox box = new LittleTileBox("bBox" + i, nbt);
				box.rotateBox(direction);
				//box.rotateBox(direction.getRotation(ForgeDirection.UP));
				box.writeToNBT("bBox" + i, nbt);
			}
		}
	}
	
	
	public static LittleTilePreview getPreviewFromNBT(NBTTagCompound nbt)
	{
		if(nbt == null)
			return null;
		LittleTileSize size = null;
		LittleTileBox box = null;
		if(nbt.hasKey("sizex"))
			size = new LittleTileSize("size", nbt);
		if(nbt.hasKey("bBoxminX"))
		{
			box = new LittleTileBox("bBox", nbt);
			if(size == null)
				size = box.getSize();
		}
		
		if(size != null)
		{
			LittleTilePreview preview = new LittleTilePreview(size, nbt);
			preview.box = box;
			return preview;
		}else{
			return null;
		}
	}
	
}
