package com.creativemd.littletiles.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.creativecore.common.utils.Rotation;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTilePreview {
	
	private static HashMap<String, Class<? extends LittleTilePreview>> previewTypes = new HashMap<>();
	
	public static void registerPreviewType(String id, Class<? extends LittleTilePreview> type)
	{
		previewTypes.put(id, type);
	}
	
	public String getTypeID()
	{
		if(!isCustomPreview())
			return "";
		for (Entry<String, Class<? extends LittleTilePreview>> type : previewTypes.entrySet())
			if(type.getValue() == this.getClass())
				return type.getKey();
		return "";
	}
	
	public boolean canSplit = true;
	public LittleTileSize size = null;
	
	protected NBTTagCompound tileData;
	
	public LittleTileBox box;
	
	public ArrayList<ShiftHandler> shifthandlers = new ArrayList<ShiftHandler>();
	
	/**This constructor needs to be implemented in every subclass**/
	public LittleTilePreview(NBTTagCompound nbt) {
		if(nbt.hasKey("sizex") || nbt.hasKey("size"))
			size = new LittleTileSize("size", nbt);
		if(nbt.hasKey("bBoxminX") || nbt.hasKey("bBox"))
		{
			box = new LittleTileBox("bBox", nbt);
			if(size == null)
				size = box.getSize();
		}
		if(nbt.hasKey("tile"))
			tileData = nbt.getCompoundTag("tile");
		else{
			tileData = nbt.copy();
			tileData.removeTag("bBox");
			tileData.removeTag("size");
		}
	}
	
	public LittleTilePreview(LittleTileBox box, NBTTagCompound tileData)
	{
		this(box.getSize(), tileData);
		this.box = box;
	}
	
	public LittleTilePreview(LittleTileSize size, NBTTagCompound tileData)
	{
		this.size = size;
		this.tileData = tileData;
	}
	
	/*public void updateSize()
	{
		size = box.getSize();
	}*/
	
	/**Used for placing the tile**/
	public LittleTile getLittleTile(TileEntityLittleTiles te)
	{
		return LittleTile.CreateandLoadTile(te, te.getWorld(), tileData);
	}
	
	/**Rendering inventory**/
	public String getPreviewBlockName()
	{
		return tileData.getString("block");
	}
	
	/**Rendering inventory**/
	public Block getPreviewBlock()
	{
		if(tileData.hasKey("block"))
			return Block.getBlockFromName(getPreviewBlockName());
		return Blocks.AIR;
	}
	
	/**Rendering inventory**/
	public int getPreviewBlockMeta()
	{
		return tileData.getInteger("meta");
	}
	
	/**Rendering inventory**/
	public boolean hasColor()
	{
		return tileData.hasKey("color");
	}
	
	/**Rendering inventory**/
	public int getColor()
	{
		return tileData.getInteger("color");
	}
	
	/**Rendering inventory**/
	@SideOnly(Side.CLIENT)
	public RenderCubeObject getCubeBlock()
	{
		RenderCubeObject cube = new RenderCubeObject(box.getCube(), null);
		if(tileData.hasKey("block"))
		{
			cube.block = getPreviewBlock();
			cube.meta = getPreviewBlockMeta();
		}else{
			cube.block = Blocks.STONE;
		}
		if(tileData.hasKey("color"))
			cube.color = tileData.getInteger("color");
		return cube;
	}
	
	public LittleTilePreview copy() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		LittleTilePreview preview = loadPreviewFromNBT(nbt);
		
		if(preview == null)
		{
			if(this.box != null)
				preview = new LittleTilePreview(box, tileData.copy());
			else
				preview = new LittleTilePreview(size != null ? size.copy() : null, tileData.copy());
		}
		preview.canSplit = this.canSplit;
		preview.shifthandlers = new ArrayList<ShiftHandler>(this.shifthandlers);
		return preview;
	}
	
	public PlacePreviewTile getPlaceableTile(LittleTileBox box, boolean canPlaceNormal, LittleTileVec offset)
	{
		if(this.box == null)
			return new PlacePreviewTile(box.copy(), this);
		else{
			LittleTileBox newBox = this.box.copy();
			if(!canPlaceNormal)
				newBox.addOffset(offset);
			return new PlacePreviewTile(newBox, this);
		}
	}
	
	public void flipPreview(EnumFacing direction)
	{
		if(box != null)
			box.flipBoxWithCenter(direction, null);
		/*if(tileData.hasKey("bBoxminX") || tileData.hasKey("bBox"))
		{
			LittleTileBox box = new LittleTileBox("bBox", tileData);
			box.flipBoxWithCenter(direction, null);
			box.writeToNBT("bBox", tileData);
		}
		if(tileData.hasKey("bSize"))
		{
			int count = tileData.getInteger("bSize");
			for (int i = 0; i < count; i++) {
				LittleTileBox box = new LittleTileBox("bBox" + i, tileData);
				box.flipBoxWithCenter(direction, null);
				//box.rotateBox(direction.getRotation(ForgeDirection.UP));
				box.writeToNBT("bBox" + i, tileData);
			}
		}*/
	}
	
	public void rotatePreview(Rotation direction)
	{
		size.rotateSize(direction);
		if(box != null)
		{
			box.rotateBoxWithCenter(direction, new Vec3d(1/32D, 1/32D, 1/32D));
			size = box.getSize();
		}
	}
	
	public void rotatePreview(EnumFacing direction)
	{
		/*if(tileData.hasKey("sizex") || tileData.hasKey("size"))
		{
			LittleTileSize size = new LittleTileSize("size", tileData);
			size.rotateSize(direction);
			size.writeToNBT("size", tileData);
		}
		if(tileData.hasKey("bBoxminX") || tileData.hasKey("bBox"))
		{
			LittleTileBox box = new LittleTileBox("bBox", tileData);
			box.rotateBox(direction);
			box.writeToNBT("bBox", tileData);
		}*/
		size.rotateSize(direction);
		if(box != null)
			box.rotateBox(direction);
		/*if(tileData.hasKey("bSize"))
		{
			int count = tileData.getInteger("bSize");
			for (int i = 0; i < count; i++) {
				LittleTileBox box = new LittleTileBox("bBox" + i, tileData);
				box.rotateBox(direction);
				//box.rotateBox(direction.getRotation(ForgeDirection.UP));
				box.writeToNBT("bBox" + i, tileData);
			}
		}*/
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		if(box != null)
			box.writeToNBT("bBox", nbt);
		size.writeToNBT("size", nbt);
		nbt.setTag("tile", tileData);
		if(isCustomPreview() && !getTypeID().equals(""))
			nbt.setString("type", getTypeID());
	}
	
	public boolean isCustomPreview()
	{
		return this.getClass() != LittleTilePreview.class;
	}
	
	public static LittleTilePreview loadPreviewFromNBT(NBTTagCompound nbt)
	{
		if(nbt == null)
			return null;
		if(nbt.hasKey("type"))
		{
			Class<? extends LittleTilePreview> type = previewTypes.get(nbt.getString("type"));
			if(type != null)
			{
				LittleTilePreview preview = null;
				try {
					preview = type.getConstructor(NBTTagCompound.class).newInstance(nbt);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				return preview;
			}
		}else
			return new LittleTilePreview(nbt);
		/*LittleTileSize size = null;
		LittleTileBox box = null;
		if(tileData.hasKey("sizex") || tileData.hasKey("size"))
			size = new LittleTileSize("size", tileData);
		if(tileData.hasKey("bBoxminX") || tileData.hasKey("bBox"))
		{
			box = new LittleTileBox("bBox", tileData);
			if(size == null)
				size = box.getSize();
		}
		
		if(size != null)
		{ 
			LittleTilePreview preview = new LittleTilePreview(size, tileData);
			preview.box = box;
			return preview;
		}else{
			return null;
		}*/
		return null;
	}
	
}
