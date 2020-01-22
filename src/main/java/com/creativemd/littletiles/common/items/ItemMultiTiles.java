package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.client.render.cache.ItemModelCache;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.tiles.math.vec.LittleVec;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

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

public class ItemMultiTiles extends Item implements ICreativeRendered, ILittleTile {
	
	public static PlacementMode currentMode = PlacementMode.getDefault();
	public static LittleGridContext currentContext = LittleGridContext.get();
	
	public ItemMultiTiles() {
		hasSubtypes = true;
		setCreativeTab(LittleTiles.littleTab);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("structure") && stack.getTagCompound().getCompoundTag("structure").hasKey("name"))
			return stack.getTagCompound().getCompoundTag("structure").getString("name");
		return super.getItemStackDisplayName(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			String id = "none";
			if (stack.getTagCompound().hasKey("structure"))
				id = stack.getTagCompound().getCompoundTag("structure").getString("id");
			tooltip.add("structure: " + id);
			tooltip.add("contains " + stack.getTagCompound().getInteger("count") + " tiles");
		}
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		/* ItemStack stack = player.getHeldItem(hand); if(stack.hasTagCompound()) return
		 * Item.getItemFromBlock(LittleTiles.blockTile).onItemUse(player, world, pos,
		 * hand, facing, hitX, hitY, hitZ); */
		return EnumActionResult.PASS;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
		LittlePreview.savePreview(previews, stack);
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack) {
		return LittlePreview.getPreview(stack);
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack, boolean allowLowResolution, boolean marked) {
		return LittlePreview.getPreview(stack, allowLowResolution);
	}
	
	@Override
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return LittlePreview.getCubes(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
		if (stack.hasTagCompound()) {
			LittleVec size = LittlePreview.getSize(stack);
			LittleGridContext context = LittleGridContext.get(stack.getTagCompound());
			double scaler = 1 / Math.max(1, Math.max(1, Math.max(size.getPosX(context), Math.max(size.getPosY(context), size.getPosZ(context)))));
			GlStateManager.scale(scaler, scaler, scaler);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
		if (stack != null)
			ItemModelCache.cacheModel(stack, facing, cachedQuads);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
		if (stack == null)
			return null;
		return ItemModelCache.requestCache(stack, facing);
	}
	
	@Override
	public PlacementMode getPlacementMode(ItemStack stack) {
		if (!currentMode.canPlaceStructures() && stack.hasTagCompound() && stack.getTagCompound().hasKey("structure"))
			return PlacementMode.getStructureDefault();
		return currentMode;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
		return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
			
			@Override
			public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
				ItemMultiTiles.currentContext = context;
				ItemMultiTiles.currentMode = mode;
			}
			
		};
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack) {
		return true;
	}
	
	@Override
	public LittleGridContext getPositionContext(ItemStack stack) {
		return currentContext;
	}
	
	@Override
	public LittleVec getCachedSize(ItemStack stack) {
		if (stack.getTagCompound().hasKey("size"))
			return LittlePreview.getSize(stack);
		return null;
	}
	
	@Override
	public LittleVec getCachedOffset(ItemStack stack) {
		return LittlePreview.getOffset(stack);
	}
	
}
