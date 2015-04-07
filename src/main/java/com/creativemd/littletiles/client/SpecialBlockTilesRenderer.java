package com.creativemd.littletiles.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class SpecialBlockTilesRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler, IItemRenderer{
	
	/**Used for renderInventoryBlock*/
	public ItemStack currentRenderedStack = null;
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
		// TODO Add inventory render
		
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		// TODO Add a new RenderBlock renderer which can render blocks using custom metadata
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return LittleTilesClient.modelID;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		currentRenderedStack = item;
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {}

	@Override
	public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_,
			double p_147500_4_, double p_147500_6_, float p_147500_8_) {
		// TODO Auto-generated method stub
		
	}

}
