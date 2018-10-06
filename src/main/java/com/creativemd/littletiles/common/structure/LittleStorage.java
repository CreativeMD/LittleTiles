package com.creativemd.littletiles.common.structure;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.blocks.BlockStorageTile;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleStorage extends LittleStructure {
	
	public static int maxSlotStackSize = 64;
	
	public int inventorySize = 0;
	public int stackSizeLimit = 0;
	public int numberOfSlots = 0;
	public int lastSlotStackSize = 0;
	
	public IInventory inventory = null;
	
	public boolean invisibleStorageTiles = false;
	
	public void updateNumberOfSlots() {
		float slots = inventorySize / (float) stackSizeLimit;
		numberOfSlots = (int) Math.ceil(slots);
		lastSlotStackSize = (int) ((slots % 1) * stackSizeLimit);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		inventorySize = nbt.getInteger("inventorySize");
		stackSizeLimit = nbt.getInteger("stackSizeLimit");
		numberOfSlots = nbt.getInteger("numberOfSlots");
		lastSlotStackSize = nbt.getInteger("lastSlot");
		if (nbt.hasKey("inventory"))
			inventory = InventoryUtils.loadInventoryBasic(nbt.getCompoundTag("inventory"));
		
		invisibleStorageTiles = nbt.getBoolean("invisibleStorage");
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		if (inventory != null) {
			nbt.setInteger("inventorySize", inventorySize);
			nbt.setInteger("stackSizeLimit", stackSizeLimit);
			nbt.setInteger("numberOfSlots", numberOfSlots);
			nbt.setInteger("lastSlot", lastSlotStackSize);
			nbt.setTag("inventory", InventoryUtils.saveInventoryBasic(inventory));
		}
		nbt.setBoolean("invisibleStorage", invisibleStorageTiles);
	}
	
	@Override
	public ItemStack getStructureDrop() {
		ItemStack stack = super.getStructureDrop();
		if (!stack.isEmpty())
			writeToNBTExtra(stack.getTagCompound().getCompoundTag("structure"));
		return stack;
	}
	
	public static int getSizeOfInventory(LittlePreviews previews) {
		double size = 0;
		String name = LittleTiles.storageBlock.getRegistryName().toString();
		for (int i = 0; i < previews.size(); i++) {
			if (previews.get(i).getPreviewBlockName().equals(name))
				size += previews.get(i).box.getSize().getPercentVolume(previews.context) * LittleGridContext.get().maxTilesPerBlock * SpecialServerConfig.storagePerPixel;
		}
		return (int) size;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (!worldIn.isRemote)
			LittleGuiHandler.openGui("littleStorageStructure", new NBTTagCompound(), playerIn, getMainTile());
		return true;
	}
	
	public static class LittleStorageParser extends LittleStructureParser<LittleStorage> {
		
		public LittleStorageParser(String id, GuiParent parent) {
			super(id, parent);
		}
		
		@Override
		public void createControls(ItemStack stack, LittleStructure structure) {
			parent.controls.add(new GuiLabel("space: " + getSizeOfInventory(LittleTilePreview.getPreview(stack)), 5, 30));
			boolean invisible = false;
			if (structure instanceof LittleStorage)
				invisible = ((LittleStorage) structure).invisibleStorageTiles;
			parent.controls.add(new GuiCheckBox("invisible", "invisible storage tiles", 5, 45, invisible));
		}
		
		@Override
		public LittleStorage parseStructure(ItemStack stack) {
			
			LittleStorage storage = new LittleStorage();
			storage.invisibleStorageTiles = ((GuiCheckBox) parent.get("invisible")).value;
			LittlePreviews previews = LittleTilePreview.getPreview(stack);
			for (int i = 0; i < previews.size(); i++) {
				if (previews.get(i).getPreviewBlock() instanceof BlockStorageTile)
					previews.get(i).setInvisibile(storage.invisibleStorageTiles);
			}
			LittleTilePreview.savePreviewTiles(previews, stack);
			storage.inventorySize = getSizeOfInventory(previews);
			storage.stackSizeLimit = maxSlotStackSize;
			storage.updateNumberOfSlots();
			storage.inventory = new InventoryBasic("basic", false, storage.numberOfSlots);
			
			return storage;
		}
	}
	
}
