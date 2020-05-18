package com.creativemd.littletiles.common.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.client.render.cache.ItemModelCache;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructureTypePremade;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPremadeStructure extends Item implements ICreativeRendered, ILittleTile {
	
	public ItemPremadeStructure() {
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + getPremadeId(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		LittleStructureTypePremade premade = (LittleStructureTypePremade) LittleStructureRegistry.getStructureType(stack.getTagCompound().getCompoundTag("structure").getString("id"));
		LittlePreviews previews = getLittlePreview(stack);
		List<RenderCubeObject> cubes = premade.getRenderingCubes(previews);
		if (cubes == null) {
			cubes = new ArrayList<>();
			
			for (LittlePreview preview : previews.allPreviews())
				cubes.add(preview.getCubeBlock(previews.getContext()));
		}
		return cubes;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public LittleGridContext getPositionContext(ItemStack stack) {
		return ItemMultiTiles.currentContext;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
		return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemLittleChisel.currentMode) {
			
			@Override
			public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
				ItemLittleChisel.currentMode = mode;
				ItemMultiTiles.currentContext = context;
			}
		};
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
		if (stack != null)
			ItemModelCache.cacheModel(getPremade(stack).stack, facing, cachedQuads);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
		if (stack == null)
			return null;
		LittleStructurePremadeEntry entry = getPremade(stack);
		if (entry == null)
			return null;
		return ItemModelCache.requestCache(entry.stack, facing);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		if (isInCreativeTab(tab))
			for (LittleStructurePremadeEntry entry : LittleStructurePremade.getPremadeStructures())
				list.add(entry.stack);
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}
	
	public void removeUnnecessaryData(ItemStack stack) {
		if (stack.hasTagCompound()) {
			stack.getTagCompound().removeTag("tiles");
			stack.getTagCompound().removeTag("size");
			stack.getTagCompound().removeTag("min");
		}
	}
	
	private static HashMap<String, LittlePreviews> cachedPreviews = new HashMap<>();
	
	public static void clearCache() {
		cachedPreviews.clear();
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack) {
		String id = getPremadeId(stack);
		if (cachedPreviews.containsKey(id))
			return cachedPreviews.get(id).copy();
		return LittleStructurePremade.getPreviews(id).copy();
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
		cachedPreviews.put(getPremadeId(stack), previews);
	}
	
	@Override
	public boolean sendTransformationUpdate() {
		return false;
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack) {
		return true;
	}
	
	@Override
	public PlacementMode getPlacementMode(ItemStack stack) {
		if (!ItemMultiTiles.currentMode.canPlaceStructures())
			return PlacementMode.getStructureDefault();
		return ItemMultiTiles.currentMode;
	}
	
	@Override
	public boolean shouldCache() {
		return false;
	}
	
	@Override
	public boolean snapToGridByDefault() {
		return true;
	}
	
	public static String getPremadeId(ItemStack stack) {
		if (stack.hasTagCompound())
			return stack.getTagCompound().getCompoundTag("structure").getString("id");
		return null;
	}
	
	public static LittleStructurePremadeEntry getPremade(ItemStack stack) {
		if (stack.hasTagCompound())
			return LittleStructurePremade.getStructurePremadeEntry(stack.getTagCompound().getCompoundTag("structure").getString("id"));
		return null;
	}
	
}
