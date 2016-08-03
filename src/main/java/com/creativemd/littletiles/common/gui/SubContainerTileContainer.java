package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.event.container.SlotChangeEvent;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerTileContainer extends SubContainer{
	
	public InventoryBasic basic = new InventoryBasic("basic", false, 1);
	public ItemStack stack;
	public int index;
	
	public SubContainerTileContainer(EntityPlayer player, ItemStack stack, int index) {
		super(player);
		this.stack = stack;
		this.index = index;
	}
	
	@CustomEventSubscribe
	public void onSlotChange(SlotChangeEvent event)
	{
		ItemStack slot = basic.getStackInSlot(0); 
		if(slot != null)
		{
			Block block = Block.getBlockFromItem(slot.getItem());
			if((!(block instanceof BlockAir) && SubContainerHammer.isBlockValid(block)) || PlacementHelper.isLittleBlock(slot) || slot.getItem() instanceof ItemTileContainer)
			{
				
				if(PlacementHelper.isLittleBlock(slot))
				{
					float ammount = slot.stackSize;
					ArrayList<LittleTilePreview> tiles = null;
					if(slot.getItem() instanceof ILittleTile)
					{
						tiles = ((ILittleTile)slot.getItem()).getLittlePreview(slot);
					}else if(Block.getBlockFromItem(slot.getItem()) instanceof ILittleTile){
						tiles = ((ILittleTile)Block.getBlockFromItem(slot.getItem())).getLittlePreview(slot);
					}
					if(tiles != null)
					{
						for (int i = 0; i < tiles.size(); i++) {
							if(tiles.get(i).nbt.hasKey("block"))
							{
								Block block2 = Block.getBlockFromName(tiles.get(i).nbt.getString("block"));
								if(block2 != null && !(block2 instanceof BlockAir))
								{
										
									ItemTileContainer.addBlock(stack, block2, tiles.get(i).nbt.getInteger("meta"), ammount*tiles.get(i).size.getPercentVolume());
								}
							}
						}
						
					}
				}else if(slot.getItem() instanceof ItemTileContainer){
					ArrayList<BlockEntry> map = ItemTileContainer.loadMap(slot);
					for (int i = 0; i < map.size(); i++) {
						ItemTileContainer.addBlock(stack, map.get(i).block, map.get(i).meta, map.get(i).value);
					}
					ItemTileContainer.saveMap(slot, new ArrayList<BlockEntry>());
				}else
					ItemTileContainer.addBlock(stack, block, slot.getItemDamage(), basic.getStackInSlot(0).stackSize);
				
				NBTTagCompound nbt = stack.getTagCompound();
				nbt.setBoolean("needUpdate", true);
				sendNBTUpdate(nbt);
				if(!(slot.getItem() instanceof ItemTileContainer))
					basic.setInventorySlotContents(0, null);
			}
		}
	}

	@Override
	public void createControls() {
		addSlotToContainer(new Slot(basic, 0, 150, 35));
		addPlayerSlotsToContainer(player);
	}
	
	@Override
	public void onClosed()
	{
		player.inventory.mainInventory[player.inventory.currentItem] = stack;
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		ItemStack dropStack = ItemStack.loadItemStackFromNBT(nbt);
		Block block = Block.getBlockFromItem(dropStack.getItem());
		int meta = dropStack.getItemDamage();
		ArrayList<BlockEntry> entries = ItemTileContainer.loadMap(stack);
		for (int i = 0; i < entries.size(); i++) {
			if(entries.get(i).block == block && entries.get(i).meta == meta)
			{
				if(entries.get(i).value > 1)
				{
					int drain = Math.min((int)entries.get(i).value, dropStack.stackSize);
					entries.get(i).value -= drain;
					dropStack.stackSize = drain;
					WorldUtils.dropItem(player, dropStack);
				}
				if(entries.get(i).value == 0)
					entries.remove(i);
				break;
			}
		}
		ItemTileContainer.saveMap(stack, entries);
		

		NBTTagCompound nbt2 = stack.getTagCompound();
		nbt2.setBoolean("needUpdate", true);
		sendNBTUpdate(nbt2);
	}

}
