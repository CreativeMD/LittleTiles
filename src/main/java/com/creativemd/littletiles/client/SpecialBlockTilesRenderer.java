package com.creativemd.littletiles.client;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
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
		LittleTile tile = ItemBlockTiles.getLittleTile(currentRenderedStack);
		if(tile != null)
		{
			double x = (double)tile.size.sizeX/2D/16D;
			double y = (double)tile.size.sizeY/2D/16D;
			double z = (double)tile.size.sizeZ/2D/16D;
			renderer.setRenderBounds(0.5-x, 0.5-y, 0.5-z, 0.5+x, 0.5+y, 0.5+z);
			renderer.lockBlockBounds = true;
			renderer.renderBlockAsItem(tile.block, currentRenderedStack.getItemDamage(), 1F);
			renderer.lockBlockBounds = false;
		}
		/*Tessellator tesselator = Tessellator.instance;
		block = Blocks.stone;
		int meta = 0;
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tesselator.startDrawingQuads();
        tesselator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, meta));
        tesselator.draw();
        tesselator.startDrawingQuads();
        tesselator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, meta));
        tesselator.draw();
        tesselator.startDrawingQuads();
        tesselator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, meta));
        tesselator.draw();
        tesselator.startDrawingQuads();
        tesselator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, meta));
        tesselator.draw();
        tesselator.startDrawingQuads();
        tesselator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, meta));
        tesselator.draw();
        tesselator.startDrawingQuads();
        tesselator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, meta));
        tesselator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);*/
        //TODO Add Inventory render
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles little = (TileEntityLittleTiles) tileEntity;
			for (int i = 0; i < little.tiles.size(); i++) {
				renderer.lockBlockBounds = true;
				renderer.setRenderBounds(little.tiles.get(i).minX, little.tiles.get(i).minY, little.tiles.get(i).minZ, little.tiles.get(i).maxX, little.tiles.get(i).maxY, little.tiles.get(i).maxZ);
				renderer.renderBlockAllFaces(little.tiles.get(i).block, x, y, z);
				renderer.lockBlockBounds = false;
				// TODO Add a new RenderBlock renderer which can render blocks using custom metadata
			}
		}
		
		return true;
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
