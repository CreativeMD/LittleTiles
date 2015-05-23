package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMultiTiles extends Item implements ITilesRenderer, ILittleTile{
	
	public ItemMultiTiles()
	{
		//super(LittleTiles.blockTile);
		hasSubtypes = true;
		setCreativeTab(CreativeTabs.tabTools);
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
		return false;
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float offsetX, float offsetY, float offsetZ)
    {
		if(stack.stackTagCompound != null)
		{
			return Item.getItemFromBlock(LittleTiles.blockTile).onItemUse(stack, player, world, x, y, z, side, offsetX, offsetY, offsetZ);
		}
		return false;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconregister)
    {
        
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item stack, CreativeTabs tab, List list)
    {
        
    }

	@Override
	public LittleTilePreview getLittlePreview(ItemStack stack) {
		LittleTileSize size = ItemRecipe.getSize(stack);
		if(size != null)
		{
			LittleTilePreview preview = new LittleTilePreview(size);
			preview.subTiles.addAll(ItemRecipe.getPreview(stack));
			return preview;
		}
		return null;
	}

	@Override
	public ArrayList<LittleTile> getLittleTile(ItemStack stack, World world,
			int x, int y, int z) {
		return ItemRecipe.loadTiles(world, stack);
	}
	
	/*@Override
	@SideOnly(Side.CLIENT)
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack)
    {
		if(stack.stackTagCompound != null)
		{
			return super.func_150936_a(world, x, y, z, side, player, stack);
		}
		return false;
    }*/
	
}
