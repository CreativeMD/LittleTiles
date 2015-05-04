package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

public class SubContainerHammer extends SubContainer{
	
	public InventoryBasic basic = new InventoryBasic("Hammer", false, 1);
	
	@Override
	public void onGuiPacket(int control, String value, EntityPlayer player) {
		if(control == 6)
		{
			String[] sizeS = value.split(";");
			if(sizeS.length == 3)
			{
				LittleTileSize size = new LittleTileSize(Integer.parseInt(sizeS[0]), Integer.parseInt(sizeS[1]), Integer.parseInt(sizeS[2]));
				ItemStack stack = basic.getStackInSlot(0);
				if(stack != null && stack.getItem() instanceof ItemBlock)
				{
					Block block = Block.getBlockFromItem(stack.getItem());
					if(block.isNormalCube() || block.isOpaqueCube() || block.renderAsNormalBlock() || block instanceof BlockGlass || block instanceof BlockStainedGlass)
					{
						int tiles = (int) (1/size.getPercentVolume()*stack.stackSize);
						if(tiles > 64)
							tiles = 64;
						int blocks = (int) Math.ceil(tiles*size.getPercentVolume());
						stack.stackSize -= blocks;
						if(stack.stackSize <= 0)
							basic.setInventorySlotContents(0, null);
						if(block.hasTileEntity(stack.getItemDamage()))
							return ;
						LittleTile tile = new LittleTile(block, stack.getItemDamage(), size);
						ItemStack dropstack = new ItemStack(LittleTiles.blockTile);
						dropstack.stackSize = tiles;
						ItemBlockTiles.saveLittleTile(dropstack, tile);
						player.inventory.addItemStackToInventory(dropstack);
					}
				}
			}
		}
	}
	
	@Override
	public void onGuiClosed(EntityPlayer player)
	{
		if(basic.getStackInSlot(0) != null)
		{
			player.dropPlayerItemWithRandomChoice(basic.getStackInSlot(0), false);
		}
	}
	
	@Override
	public ArrayList<Slot> getSlots(EntityPlayer player) {
		ArrayList<Slot> slots = new ArrayList<Slot>();
		slots.add(new Slot(basic, 0, 10, 10));
		slots.addAll(getPlayerSlots(player));
		return slots;
	}

	@Override
	public boolean doesGuiNeedUpdate() {
		return false;
	}

}
