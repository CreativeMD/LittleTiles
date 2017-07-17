package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMultiTiles extends Item implements ICreativeRendered, ILittleTile{
	
	public ItemMultiTiles()
	{
		//super(LittleTiles.blockTile);
		hasSubtypes = true;
		setCreativeTab(LittleTiles.littleTab);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if(stack.hasTagCompound())
		{
			String id = "none";
			if(stack.getTagCompound().hasKey("structure"))
				id = stack.getTagCompound().getCompoundTag("structure").getString("id");
			tooltip.add("structure: " + id);
			tooltip.add("contains " + stack.getTagCompound().getInteger("count") + " tiles");
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		ItemStack stack = player.getHeldItem(hand);
		if(stack.hasTagCompound())
		{
			return Item.getItemFromBlock(LittleTiles.blockTile).onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
		}
		return EnumActionResult.PASS;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        
    }
	
	@Override
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews) {
		LittleTilePreview.savePreviewTiles(previews, stack);
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}

	@Override
	public List<LittleTilePreview> getLittlePreview(ItemStack stack) {
		return LittleTilePreview.getPreview(stack);
	}
	
	@Override
	public List<LittleTilePreview> getLittlePreview(ItemStack stack, boolean allowLowResolution)
	{
		return LittleTilePreview.getPreview(stack, allowLowResolution);
	}

	/*@Override
	public void rotateLittlePreview(ItemStack stack, EnumFacing direction) {
		ItemRecipe.rotatePreview(stack, direction);
	}*/

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return getLTStructure(stack);
	}
	
	public static LittleStructure getLTStructure(ItemStack stack) {
		return LittleStructure.createAndLoadStructure(stack.getTagCompound().getCompoundTag("structure"), null);
	}

	/*@Override
	public void flipLittlePreview(ItemStack stack, EnumFacing direction) {
		ItemRecipe.flipPreview(stack, direction);
	}*/

	@Override
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return LittleTilePreview.getCubes(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType)
	{
		if(stack.hasTagCompound())
		{
			LittleTileSize size = LittleTilePreview.getSize(stack);
			double scaler = 1/Math.max(1, Math.max(1, Math.max(size.getPosX(), Math.max(size.getPosY(), size.getPosZ()))));
			GlStateManager.scale(scaler, scaler, scaler);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded)
	{
		if(stack != null)
			ItemModelCache.cacheModel(stack, facing, cachedQuads);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded)
	{
		if(stack == null)
			return null;
		return ItemModelCache.getCache(stack, facing);
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
