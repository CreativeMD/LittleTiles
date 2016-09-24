package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMultiTiles extends Item implements ICreativeRendered, ILittleTile{
	
	public ItemMultiTiles()
	{
		//super(LittleTiles.blockTile);
		hasSubtypes = true;
		setCreativeTab(CreativeTabs.TOOLS);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(stack.hasTagCompound())
		{
			String id = "none";
			if(stack.getTagCompound().hasKey("structure"))
				id = stack.getTagCompound().getCompoundTag("structure").getString("id");
			list.add("structure: " + id);
			list.add("contains " + stack.getTagCompound().getInteger("tiles") + " tiles");
		}
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if(stack.hasTagCompound())
		{
			return Item.getItemFromBlock(LittleTiles.blockTile).onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
		}
		return EnumActionResult.PASS;
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
	public void rotateLittlePreview(ItemStack stack, EnumFacing direction) {
		ItemRecipe.rotatePreview(stack, direction);
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return getLTStructure(stack);
	}
	
	public static LittleStructure getLTStructure(ItemStack stack) {
		return LittleStructure.createAndLoadStructure(stack.getTagCompound().getCompoundTag("structure"), null);
	}

	@Override
	public void flipLittlePreview(ItemStack stack, EnumFacing direction) {
		ItemRecipe.flipPreview(stack, direction);
	}

	@Override
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return ItemRecipe.getCubes(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack)
	{
		if(stack.hasTagCompound())
		{
			if(!stack.getTagCompound().hasKey("size"))
				ItemRecipe.updateSize(ItemRecipe.getCubes(stack), stack);
			double scaler = 1/Math.max(1, stack.getTagCompound().getDouble("size"));
			GlStateManager.scale(scaler, scaler, scaler);
		}
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
