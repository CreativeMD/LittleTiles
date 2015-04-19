package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.omg.CORBA.REBIND;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpecialBlockTilesRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler, IItemRenderer{
	
	/**Used for renderInventoryBlock*/
	public ItemStack currentRenderedStack = null;
	
	public void renderLittleTileInventory(LittleTile tile, RenderBlocks renderer, boolean usePlace)
	{
		if(!usePlace)
		{
			double x = (double)tile.size.sizeX/2D/16D;
			double y = (double)tile.size.sizeY/2D/16D;
			double z = (double)tile.size.sizeZ/2D/16D;
			renderer.setRenderBounds(0.5-x, 0.5-y, 0.5-z, 0.5+x, 0.5+y, 0.5+z);
		}else{
			AxisAlignedBB box = tile.getBox();
			renderer.setRenderBounds(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		}
		renderer.lockBlockBounds = true;
		renderer.renderBlockAsItem(tile.block, tile.meta, 1F);
		renderer.lockBlockBounds = false;
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
		LittleTile tile = ItemBlockTiles.getLittleTile(currentRenderedStack);
		if(tile != null)
			renderLittleTileInventory(tile, renderer, false);
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
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles little = (TileEntityLittleTiles) tileEntity;
			for (int i = 0; i < little.tiles.size(); i++) {
				double minX = (double)(little.tiles.get(i).minX+8)/16D;
				double minY = (double)(little.tiles.get(i).minY+8)/16D;
				double minZ = (double)(little.tiles.get(i).minZ+8)/16D;
				double maxX = (double)(little.tiles.get(i).maxX+8)/16D;
				double maxY = (double)(little.tiles.get(i).maxY+8)/16D;
				double maxZ = (double)(little.tiles.get(i).maxZ+8)/16D;
				
				RenderHelper3D.renderBlocks.blockAccess = renderer.blockAccess;
				RenderHelper3D.renderBlocks.clearOverrideBlockTexture();
				RenderHelper3D.renderBlocks.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
				RenderHelper3D.renderBlocks.meta = little.tiles.get(i).meta;
				RenderHelper3D.renderBlocks.lockBlockBounds = true;
				RenderHelper3D.renderBlocks.renderBlockAllFaces(little.tiles.get(i).block, x, y, z);
				RenderHelper3D.renderBlocks.lockBlockBounds = false;
				//RenderHelper3D.renderBlocks.renderStandardBlock(little.tiles.get(i).block, x, y, z);
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
		if(item.getItem() instanceof ITilesRenderer && item.stackTagCompound != null)
			return true;
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		if(item.getItem() instanceof ITilesRenderer)
		{
			Minecraft mc = Minecraft.getMinecraft();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			if(type == ItemRenderType.INVENTORY)
			{
				if(((ITilesRenderer)item.getItem()).hasBackground(item))
					RenderItem.getInstance().renderItemIntoGUI(mc.fontRenderer, mc.renderEngine, item, 0, 0);
	            
				GL11.glTranslatef(7.5F, 7.5F, 10);
				GL11.glScalef(10F, 10F, 10F);
				GL11.glScalef(1.0F, 1.0F, -1F);
	            GL11.glRotatef(210F, 1.0F, 0.0F, 0.0F);
	            GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
			}else{
				GL11.glTranslatef(0.5F, 0.5F, 0);
			}
			mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			ArrayList<LittleTile> tiles = ItemRecipe.loadTiles(item);
			for (int i = 0; i < tiles.size(); i++) {
				
				renderLittleTileInventory(tiles.get(i), (RenderBlocks) data[0], true);
				GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
			}
		}
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x,
			double y, double z, float p_147500_8_) {
		
	}

}
