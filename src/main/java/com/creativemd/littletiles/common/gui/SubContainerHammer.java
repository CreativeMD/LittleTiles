package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.creativemd.creativecore.common.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
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
				LittleTileVec size = new LittleTileVec(Integer.parseInt(sizeS[0]), Integer.parseInt(sizeS[1]), Integer.parseInt(sizeS[2]));
				if(basic.getStackInSlot(0) != null && basic.getStackInSlot(0).getItem() instanceof ItemBlock)
				{
					Block block = Block.getBlockFromItem(basic.getStackInSlot(0).getItem());
					if(block.isNormalCube())
					{
						LittleTile tile = new LittleTile(block, basic.getStackInSlot(0).getItemDamage(), size);
						ItemStack stack = new ItemStack(LittleTiles.blockTile);
						ItemBlockTiles.saveLittleTile(stack, tile);
						player.inventory.addItemStackToInventory(stack);
					}
				}
			}
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
