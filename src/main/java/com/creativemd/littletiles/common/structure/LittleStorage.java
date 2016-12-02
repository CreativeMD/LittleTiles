package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.InventoryUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockStorageTile;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

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

public class LittleStorage extends LittleStructure  {
	
	/**tile-size: 1x1x1**/
	public static int slotsPerTileSize = 1;
	public static int maxSlotStackSize = 64;
	
	public int inventorySize = 0;
	public int stackSizeLimit = 0;
	public int numberOfSlots = 0;
	public int lastSlotStackSize = 0;
	
	public IInventory inventory = null;
	
	public boolean invisibleStorageTiles = false;
	
	public void updateNumberOfSlots()
	{
		float slots = inventorySize/(float) stackSizeLimit;
		numberOfSlots = (int) Math.ceil(slots);
		lastSlotStackSize = (int) ((slots%1)*stackSizeLimit);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		inventorySize = nbt.getInteger("inventorySize");
		stackSizeLimit = nbt.getInteger("stackSizeLimit");
		numberOfSlots = nbt.getInteger("numberOfSlots");
		lastSlotStackSize = nbt.getInteger("lastSlot");
		if(nbt.hasKey("inventory"))
			inventory = InventoryUtils.loadInventoryBasic(nbt.getCompoundTag("inventory"));
		
		invisibleStorageTiles = nbt.getBoolean("invisible");
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		if(inventory != null)
		{
			nbt.setInteger("inventorySize", inventorySize);
			nbt.setInteger("stackSizeLimit", stackSizeLimit);
			nbt.setInteger("numberOfSlots", numberOfSlots);
			nbt.setInteger("lastSlot", lastSlotStackSize);
			nbt.setTag("inventory", InventoryUtils.saveInventoryBasic(inventory));
		}
		nbt.setBoolean("invisible", invisibleStorageTiles);
	}
	
	@Override
	public ItemStack getStructureDrop()
	{
		ItemStack stack = super.getStructureDrop();
		writeToNBTExtra(stack.getTagCompound().getCompoundTag("structure"));
		return stack;
	}
	
	public static int getSizeOfInventory(ArrayList<LittleTilePreview> previews)
	{
		int size = 0;
		String name = LittleTiles.storageBlock.getRegistryName().toString();
		for (int i = 0; i < previews.size(); i++) {
			if(previews.get(i).getPreviewBlockName().equals(name))
				size += previews.get(i).box.getSize().getVolume() * slotsPerTileSize;
		}
		return size;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!worldIn.isRemote)
			LittleGuiHandler.openGui("littleStorageStructure", new NBTTagCompound(), playerIn, getMainTile());
		return true;
	}
	
	@Override
	public void createControls(SubGui gui, LittleStructure structure) {
		gui.controls.add(new GuiLabel("space: " + getSizeOfInventory(ItemRecipe.getPreview(((SubGuiStructure) gui).stack)), 5, 30));
		boolean invisible = false;
		if(structure instanceof LittleStorage)
			invisible = ((LittleStorage) structure).invisibleStorageTiles;
		gui.controls.add(new GuiCheckBox("invisible", "invisible storage tiles", 5, 45, invisible));
	}

	@Override
	public LittleStructure parseStructure(SubGui gui) {
		
		LittleStorage storage = new LittleStorage();
		storage.invisibleStorageTiles = ((GuiCheckBox) gui.get("invisible")).value;
		ArrayList<LittleTilePreview> previews = ItemRecipe.getPreview(((SubGuiStructure) gui).stack);
		for (int i = 0; i < previews.size(); i++) {
			if(previews.get(i).getPreviewBlock() instanceof BlockStorageTile)
				previews.get(i).setInvisibile(storage.invisibleStorageTiles);
		}
		ItemRecipe.savePreviewTiles(previews, ((SubGuiStructure) gui).stack);
		storage.inventorySize = getSizeOfInventory(previews);
		storage.stackSizeLimit = maxSlotStackSize;
		storage.updateNumberOfSlots();
		storage.inventory = new InventoryBasic("basic", false, storage.numberOfSlots);
		
		return storage;
	}

}
