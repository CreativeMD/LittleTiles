package com.creativemd.littletiles.common.container;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SubContainerHammer extends SubContainer{
	
	public SubContainerHammer(EntityPlayer player) {
		super(player);
	}

	public InventoryBasic basic = new InventoryBasic("Hammer", false, 1);
	
	@SideOnly(Side.CLIENT)
	public static boolean doesBlockSupportedTranslucent(Block block)
	{
		return block.getBlockLayer() == BlockRenderLayer.SOLID || block.getBlockLayer() == BlockRenderLayer.TRANSLUCENT;
	}
	
	public static boolean isBlockValid(Block block)
	{
		return block.isNormalCube(block.getDefaultState()) || block.isFullyOpaque(block.getDefaultState()) || block.isFullBlock(block.getDefaultState()) || block instanceof BlockGlass || block instanceof BlockStainedGlass || block instanceof BlockBreakable;
	}
	
	@Override
	public void onClosed()
	{
		if(!basic.getStackInSlot(0).isEmpty())
		{
			player.dropItem(basic.getStackInSlot(0), false);
		}
	}
	
	@Override
	public void createControls() {
		addSlotToContainer(new Slot(basic, 0, 10, 10));
		addPlayerSlotsToContainer(player, 20, 120);
	}
	
	@Override
	public void sendUpdate() {
		
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		LittleTileSize size = new LittleTileSize(nbt.getInteger("sizeX"), nbt.getInteger("sizeY"), nbt.getInteger("sizeZ"));
		ItemStack stack = basic.getStackInSlot(0);
		if(!stack.isEmpty() && stack.getItem() instanceof ItemBlock)
		{
			double volumePerItem = 1;
			Block block = Block.getBlockFromItem(stack.getItem());
			int meta = stack.getItemDamage();
			if(isBlockValid(block) || stack.getItem() instanceof ItemBlockTiles)
			{
				double availableVolume = stack.getCount();
				if(stack.getItem() instanceof ItemBlockTiles)
				{
					block = null;
					volumePerItem = 0;
					ArrayList<LittleTilePreview> previews = ((ItemBlockTiles) stack.getItem()).getLittlePreview(stack);
					for (int i = 0; i < previews.size(); i++) {
						if(block == null)
						{
							block = previews.get(i).getPreviewBlock();
							meta = previews.get(i).getPreviewBlockMeta();
						}
						volumePerItem += previews.get(i).getPercentVolume();
					}
					availableVolume = volumePerItem*stack.getCount();
				}
				
				if(block.hasTileEntity(block.getStateFromMeta(meta)))
					return ;
				
				LittleTileBox box = null;
				if(nbt.getBoolean("sliced"))
					box = new LittleTileSlicedOrdinaryBox(0, 0, 0, size.sizeX, size.sizeY, size.sizeZ, LittleSlice.X_DS_UN_LEFT);
				else
					box = new LittleTileBox(0, 0, 0, size.sizeX, size.sizeY, size.sizeZ);
				
				int alltiles = (int) (availableVolume/box.getPercentVolume());
				int tiles = Math.min(alltiles, 64);
				if(alltiles == 0 || block == null)
					return ;
				int blocks = (int) Math.ceil((tiles*box.getPercentVolume()/volumePerItem));
				
				LittleTile tile = null;
				if(nbt.hasKey("color") && nbt.getInteger("color") != ColorUtils.WHITE)
					tile = new LittleTileBlockColored(block, meta, ColorUtils.IntToRGB(nbt.getInteger("color")));
				else
					tile = new LittleTileBlock(block, meta);
				
				tile.box = box;
				
				ItemStack dropstack = ItemBlockTiles.getStackFromPreview(tile.getPreviewTile());
				dropstack.setCount(tiles);
				
				double missingTiles = blocks-tiles*box.getPercentVolume();
				
				try {
					LittleAction.addPreviewToInventory(player, ((ILittleTile) dropstack.getItem()).getLittlePreview(dropstack, false, false));
				} catch (NotEnoughIngredientsException e) {
					e.printStackTrace();
				}
				
				stack.shrink(blocks);
				if(stack.isEmpty())
					basic.setInventorySlotContents(0, ItemStack.EMPTY);
				
				player.inventory.addItemStackToInventory(dropstack);
				
			}
		}
	}

}
