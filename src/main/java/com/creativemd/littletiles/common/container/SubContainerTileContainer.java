package com.creativemd.littletiles.common.container;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.controls.container.SlotControl;
import com.creativemd.creativecore.gui.event.container.SlotChangeEvent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.gui.controls.SlotControlBlockEntry;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerTileContainer extends SubContainer{
	
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
		if(event.source instanceof SlotControlBlockEntry)
		{
			SlotControlBlockEntry control = (SlotControlBlockEntry) event.source;
			Slot slot = ((SlotControl) event.source).slot;
			ItemStack input = slot.getStack();
			Block block = Block.getBlockFromItem(input.getItem());
			int meta = input.getItemDamage();
			if(control.entry != null)
			{
				block = control.entry.block;
				meta = control.entry.meta;
			}
			if((!(block instanceof BlockAir) && SubContainerHammer.isBlockValid(block)) || PlacementHelper.isLittleBlock(input) || input.getItem() instanceof ItemTileContainer)
			{
				if(control.entry == null)
				{
					if(!input.isEmpty())
					{
						if(input.getItem() instanceof ItemTileContainer)
						{
							if(stack != input)
							{
								ArrayList<BlockEntry> map = ItemTileContainer.loadMap(input);
								for (int i = 0; i < map.size(); i++) {
									ItemTileContainer.addBlock(stack, map.get(i).block, map.get(i).meta, map.get(i).value);
								}
								ItemTileContainer.saveMap(input, new ArrayList<BlockEntry>());
							}
							slot.putStack(ItemStack.EMPTY);
							if(player.inventory.addItemStackToInventory(input))
								player.dropItem(input, false);
						}else{
							if(PlacementHelper.isLittleBlock(input))
							{
								List<LittleTilePreview> previews = PlacementHelper.getLittleInterface(input).getLittlePreview(input);
								for (int i = 0; i < previews.size(); i++) {
									if(previews.get(i).isOrdinaryTile())
										ItemTileContainer.addBlock(stack, previews.get(i).getPreviewBlock(), previews.get(i).getPreviewBlockMeta(), previews.get(i).size.getPercentVolume()*input.getCount());
									else
									{
										ItemStack unmergeable = ItemBlockTiles.getStackFromPreview(previews.get(i));
										if(player.inventory.addItemStackToInventory(unmergeable))
											player.dropItem(unmergeable, false);
									}
								}
								input.setCount(0);
							}else{
								ItemTileContainer.addBlock(stack, block, meta, input.getCount());
								input.setCount(0);
							}
						}
					}
				}else{
					
					if(control.entry.value < 1)
					{
						int countBefore = (int) (control.entry.value/LittleTile.minimumTileSize);
						if(countBefore > input.getCount())
							ItemTileContainer.drainBlock(stack, control.entry.block, control.entry.meta, (countBefore-input.getCount()) * LittleTile.minimumTileSize);
						else if(countBefore < input.getCount())
							ItemTileContainer.addBlock(stack, control.entry.block, control.entry.meta, (input.getCount()-countBefore) * LittleTile.minimumTileSize);
					}else{
						int countBefore = (int) control.entry.value;
						if(countBefore > input.getCount())
							ItemTileContainer.drainBlock(stack, control.entry.block, control.entry.meta, (countBefore-input.getCount()));
						else if(countBefore < input.getCount())
							ItemTileContainer.addBlock(stack, control.entry.block, control.entry.meta, (input.getCount()-countBefore));
					}
				}
			}
			reloadControls();
		}
	}
	
	public void reloadControls()
	{
		controls.clear();
		createControls();
		refreshControls();
		NBTTagCompound nbt = stack.getTagCompound().copy();
		nbt.setBoolean("reload", true);
		sendNBTToGui(nbt);
	}

	@Override
	public void createControls() {
		int index = 0;
		ArrayList<BlockEntry> map = ItemTileContainer.loadMap(stack);
		InventoryBasic basic = new InventoryBasic("item", false, map.size()+1)
		{
			@Override
			public int getInventoryStackLimit()
		    {
		        return 4098;
		    }
		};
		int cols = 2;
		for (BlockEntry entry : map) {
			if(!(entry.block instanceof BlockAir) && entry.block != null)
			{
				ItemStack stack = entry.getItemStack();
				if(entry.value >= 1)
					stack.setCount((int) entry.value);
				else
					stack = entry.getTileItemStack();
				basic.setInventorySlotContents(index, stack);
				controls.add(new SlotControlBlockEntry(new Slot(basic, index, 8 + (index % cols) * 110, 8+(index/cols)*24){
					@Override
					public boolean isItemValid(ItemStack stack)
					{
						return SubContainerHammer.isBlockValid(Block.getBlockFromItem(stack.getItem())) || PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemTileContainer;
					}
					
				}, entry));
				index++;
			}
		}
		basic.setInventorySlotContents(index, ItemStack.EMPTY);
		SlotControl control = new SlotControlBlockEntry(new Slot(basic, index, 8 + (index % cols) * 110, 8+(index/cols)*24), null);
		control.name = "item-last" + index;
		controls.add(control);
		addPlayerSlotsToContainer(player, 50, 170, this.index);
	}
	
	@Override
	public void onClosed()
	{
		player.inventory.mainInventory.set(index, stack);
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		
	}

}
