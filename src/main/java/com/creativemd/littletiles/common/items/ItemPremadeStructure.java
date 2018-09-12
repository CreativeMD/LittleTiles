package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.creativetab.CreativeTabs;
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
		return super.getUnlocalizedName(stack) + "." + getPremadeID(stack);
	}

	@Override
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return LittleTilePreview.getCubes(stack);
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
		return ItemModelCache.getCache(stack, facing);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
		if (isInCreativeTab(tab)) {
			for (LittleStructurePremadeEntry entry : LittleStructurePremade.getPremadeStructures()) {
				list.add(entry.stack);
			}
		}
	}

	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}

	@Override
	public LittlePreviews getLittlePreview(ItemStack stack) {
		return LittleTilePreview.getPreview(stack, false);
	}

	@Override
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
		LittleTilePreview.savePreviewTiles(previews, stack);
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return ItemMultiTiles.getLTStructure(stack);
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
	public boolean snapToGridByDefault() {
		return true;
	}

	@Override
	public LittleTileSize getCachedSize(ItemStack stack) {
		if (stack.getTagCompound().hasKey("size"))
			return LittleTilePreview.getSize(stack);
		return null;
	}

	@Override
	public LittleTileVec getCachedOffset(ItemStack stack) {
		return LittleTilePreview.getOffset(stack);
	}

	public static String getPremadeID(ItemStack stack) {
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
