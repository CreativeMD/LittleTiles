package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
			String id = "none";
			if(stack.stackTagCompound.hasKey("structure"))
				id = stack.stackTagCompound.getCompoundTag("structure").getString("id");
			list.add("structure: " + id);
			list.add("contains " + stack.stackTagCompound.getInteger("tiles") + " tiles");
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
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		return ItemRecipe.getPreview(stack);
	}

	@Override
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack) {
		return ItemRecipe.getCubes(stack);
	}

	@Override
	public void rotateLittlePreview(ItemStack stack, ForgeDirection direction) {
		ItemRecipe.rotatePreview(stack, direction);
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return getLTStructure(stack);
	}
	
	public static LittleStructure getLTStructure(ItemStack stack) {
		return LittleStructure.createAndLoadStructure(stack.stackTagCompound.getCompoundTag("structure"), null);
	}

	@Override
	public void flipLittlePreview(ItemStack stack, ForgeDirection direction) {
		ItemRecipe.flipPreview(stack, direction);
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
