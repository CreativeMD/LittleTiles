package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PlacePreviewTile;
import com.creativemd.littletiles.utils.ShiftHandler;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTilePreview {
	
	public boolean canSplit = true;
	public LittleTileSize size = null;
	
	protected NBTTagCompound nbt;
	
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
		return LittleTile.CreateandLoadTile(te, te.getWorld(), nbt);
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
	
	public String getPreviewBlockName()
	{
		return nbt.getString("block");
	}
	
	public Block getPreviewBlock()
	{
		if(nbt.hasKey("block"))
			return Block.getBlockFromName(getPreviewBlockName());
		return Blocks.AIR;
	}
	
	public int getPreviewBlockMeta()
	{
		return nbt.getInteger("meta");
	}
	
	public boolean hasColor()
	{
		return nbt.hasKey("color");
	}
	
	public int getColor()
	{
		return nbt.getInteger("color");
	}
	
	@SideOnly(Side.CLIENT)
	public RenderCubeObject getCubeBlock()
	{
		RenderCubeObject cube = new RenderCubeObject(box.getCube(), null);
		if(nbt.hasKey("block"))
		{
			cube.block = getPreviewBlock();
			cube.meta = getPreviewBlockMeta();
		}else{
			cube.block = Blocks.STONE;
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
	
	public PlacePreviewTile getPreviewTile(LittleTileBox box, boolean canPlaceNormal, LittleTileVec offset)
	{
		if(this.box == null)
		{
			return new PlacePreviewTile(box.copy(), this);
		}else{
			if(!canPlaceNormal)
				this.box.addOffset(offset);
			return new PlacePreviewTile(this.box, this);
		}
	}
	
	public static void flipPreview(NBTTagCompound nbt, EnumFacing direction)
	{
		if(nbt.hasKey("bBoxminX") || nbt.hasKey("bBox"))
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
	
	public static void rotatePreview(NBTTagCompound nbt, EnumFacing direction)
	{
		if(nbt.hasKey("sizex") || nbt.hasKey("size"))
		{
			LittleTileSize size = new LittleTileSize("size", nbt);
			size.rotateSize(direction);
			size.writeToNBT("size", nbt);
		}
		if(nbt.hasKey("bBoxminX") || nbt.hasKey("bBox"))
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
		
	public static LittleTilePreview loadPreviewFromNBT(NBTTagCompound nbt)
	{
		if(nbt == null)
			return null;
		LittleTileSize size = null;
		LittleTileBox box = null;
		if(nbt.hasKey("sizex") || nbt.hasKey("size"))
			size = new LittleTileSize("size", nbt);
		if(nbt.hasKey("bBoxminX") || nbt.hasKey("bBox"))
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
