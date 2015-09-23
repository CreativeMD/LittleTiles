package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemRecipe extends Item implements ITilesRenderer{
	
	public ItemRecipe(){
		setCreativeTab(CreativeTabs.tabTools);
		hasSubtypes = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    protected String getIconString()
    {
        return LittleTiles.modid + ":LTRecipe";
    }
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		if(stack.stackTagCompound != null)
			return false;
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles littleEntity = (TileEntityLittleTiles) tileEntity;
			saveTiles(world, littleEntity.tiles, stack);
		}
        return false;
    }
	
	public static ArrayList<LittleTilePreview> getPreview(ItemStack stack)
	{
		ArrayList<LittleTilePreview> result = new ArrayList<LittleTilePreview>();
		int tiles = stack.stackTagCompound.getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("tile" + i);
			LittleTilePreview preview = LittleTilePreview.getPreviewFromNBT(nbt);
			if(preview != null)
				result.add(preview);
		}
		return result;
	}
	
	public static LittleTileSize getSize(ItemStack stack)
	{
		ArrayList<LittleTilePreview> tiles = getPreview(stack);
		byte minX = LittleTile.maxPos;
		byte minY = LittleTile.maxPos;
		byte minZ = LittleTile.maxPos;
		byte maxX = LittleTile.minPos;
		byte maxY = LittleTile.minPos;
		byte maxZ = LittleTile.minPos;
		for (int i = 0; i < tiles.size(); i++) {
			LittleTilePreview tile = tiles.get(i);
			minX = (byte) Math.min(minX, tile.box.minX);
			minY = (byte) Math.min(minY, tile.box.minY);
			minZ = (byte) Math.min(minZ, tile.box.minZ);
			maxX = (byte) Math.max(maxX, tile.box.maxX);
			maxY = (byte) Math.max(maxY, tile.box.maxY);
			maxZ = (byte) Math.max(maxZ, tile.box.maxZ);
		}
		return new LittleTileSize(maxX-minX, maxY-minY, maxZ-minZ);
	}
	
	public static ArrayList<LittleTile> loadTiles(TileEntityLittleTiles te, ItemStack stack)
	{
		ArrayList<LittleTile> result = new ArrayList<LittleTile>();
		int tiles = stack.stackTagCompound.getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("tile" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(te, te.getWorldObj(), nbt);
			//if(tile != null && tile.isValid())
			result.add(tile);
		}
		return result;
	}
	
	public static void saveTiles(World world, ArrayList<LittleTile> tiles, ItemStack stack)
	{
		stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setInteger("tiles", tiles.size());
		for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			tiles.get(i).boundingBoxes.get(0).writeToNBT("bBox", nbt);
			tiles.get(i).saveTile(nbt);
			stack.stackTagCompound.setTag("tile" + i, nbt);
		}
	}
	
	public static ArrayList<CubeObject> getCubes(ItemStack stack)
	{
		ArrayList<LittleTilePreview> preview = getPreview(stack);
		ArrayList<CubeObject> cubes = new ArrayList<CubeObject>();
		for (int i = 0; i < preview.size(); i++) {
			cubes.add(preview.get(i).getCubeBlock());
		}
		return cubes;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(stack.stackTagCompound != null)
		{
			list.add("Contains " + stack.stackTagCompound.getInteger("tiles") + " tiles");
		}
	}

	@Override
	public boolean hasBackground(ItemStack stack) {
		return true;
	}

	@Override
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack) {
		return getCubes(stack);
	}
}
