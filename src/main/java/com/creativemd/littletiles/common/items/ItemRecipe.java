package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;

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
			saveTiles(littleEntity.tiles, stack);
		}
        return false;
    }
	
	public static ArrayList<LittleTile> loadTiles(ItemStack stack)
	{
		ArrayList<LittleTile> result = new ArrayList<LittleTile>();
		int tiles = stack.stackTagCompound.getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("tile" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(nbt);
			if(tile != null && tile.isValid())
				result.add(tile);
		}
		return result;
	}
	
	public static void saveTiles(ArrayList<LittleTile> tiles, ItemStack stack)
	{
		stack.stackTagCompound = new NBTTagCompound();
		stack.stackTagCompound.setInteger("tiles", tiles.size());
		for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			tiles.get(i).save(nbt);
			stack.stackTagCompound.setTag("tile" + i, nbt);
		}
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
}
