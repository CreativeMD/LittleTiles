package com.creativemd.littletiles.common.container;

import java.awt.Color;
import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.place.PlacePreviewTile;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;

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
						volumePerItem += previews.get(i).size.getPercentVolume();
					}
					availableVolume = volumePerItem*stack.getCount();
				}
				
				if(block.hasTileEntity(block.getStateFromMeta(meta)))
					return ;
				int alltiles = (int) (availableVolume/size.getPercentVolume());
				int tiles = Math.min(alltiles, 64);
				if(alltiles == 0 || block == null)
					return ;
				int blocks = (int) Math.ceil((tiles*size.getPercentVolume()/volumePerItem));
				stack.shrink(blocks);
				if(stack.isEmpty())
					basic.setInventorySlotContents(0, ItemStack.EMPTY);
				
				//LittleTile tile = new LittleTile(block, stack.getItemDamage(), size);
				ItemStack dropstack = new ItemStack(LittleTiles.blockTile);
				dropstack.setCount(tiles);
				dropstack.setTagCompound(new NBTTagCompound());
				size.writeToNBT("size", dropstack.getTagCompound());
				
				LittleTile tile = null;
				if(nbt.hasKey("color") && nbt.getInteger("color") != ColorUtils.WHITE)
					tile = new LittleTileBlockColored(block, meta, ColorUtils.IntToRGB(nbt.getInteger("color")));
				else
					tile = new LittleTileBlock(block, meta);
				tile.saveTileExtra(dropstack.getTagCompound());
				if(tile instanceof LittleTileBlockColored)
					dropstack.getTagCompound().setString("tID", "BlockTileColored");
				else
					dropstack.getTagCompound().setString("tID", "BlockTileBlock");
				
				double missingTiles = blocks-tiles*size.getPercentVolume();
				if(missingTiles > 0)
					ItemTileContainer.addBlock(player, block, meta, missingTiles);
				//dropstack.stackTagCompound.setString("block", Block.blockRegistry.getNameForObject(block));
				//dropstack.stackTagCompound.setInteger("meta", stack.getItemDamage());
				//ItemBlockTiles.saveLittleTile(player.worldObj, dropstack, tile);
				player.inventory.addItemStackToInventory(dropstack);
			}
		}
	}

}
