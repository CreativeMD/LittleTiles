package com.creativemd.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.block.BlockStorageTile;
import com.creativemd.littletiles.common.container.SubContainerStorage;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.StackIngredient;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleStorage extends LittleStructure {
	
	public LittleStorage(LittleStructureType type) {
		super(type);
	}
	
	public List<SubContainerStorage> openContainers = new ArrayList<SubContainerStorage>();
	
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
	
	@Override
	public void addIngredients(LittleIngredients ingredients) {
		super.addIngredients(ingredients);
		if (inventory != null)
			ingredients.add(new StackIngredient(inventory));
	}
	
	@Override
	public void onStructureDestroyed() {
		super.onStructureDestroyed();
		if (!getWorld().isRemote) {
			for (SubContainerStorage container : openContainers) {
				container.storage = null;
				NBTTagCompound nbt = new NBTTagCompound();
				PacketHandler.sendPacketToPlayer(new GuiLayerPacket(nbt, container.getLayerID(), true), (EntityPlayerMP) container.player);
				container.closeLayer(nbt, true);
			}
		}
	}
	
	public static int getSizeOfInventory(LittlePreviews previews) {
		double size = 0;
		String name = LittleTiles.storageBlock.getRegistryName().toString();
		for (int i = 0; i < previews.size(); i++) {
			if (previews.get(i).getBlockName().equals(name))
				size += previews.get(i).box.getSize().getPercentVolume(previews.context) * LittleGridContext.get().maxTilesPerBlock * LittleTiles.CONFIG.general.storagePerPixel;
		}
		return (int) size;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (!worldIn.isRemote)
			LittleGuiHandler.openGui("littleStorageStructure", new NBTTagCompound(), playerIn, getMainTile());
		return true;
	}
	
	public static class LittleStorageParser extends LittleStructureGuiParser {
		
		public LittleStorageParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@Override
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			parent.controls.add(new GuiLabel("space: " + getSizeOfInventory(previews), 5, 0));
			boolean invisible = false;
			if (structure instanceof LittleStorage)
				invisible = ((LittleStorage) structure).invisibleStorageTiles;
			parent.controls.add(new GuiCheckBox("invisible", "invisible storage tiles", 5, 18, invisible));
		}
		
		@Override
		public LittleStorage parseStructure(LittlePreviews previews) {
			
			LittleStorage storage = createStructure(LittleStorage.class);
			storage.invisibleStorageTiles = ((GuiCheckBox) parent.get("invisible")).value;
			for (int i = 0; i < previews.size(); i++) {
				if (previews.get(i).getBlock() instanceof BlockStorageTile)
					previews.get(i).setInvisibile(storage.invisibleStorageTiles);
			}
			storage.inventorySize = getSizeOfInventory(previews);
			storage.stackSizeLimit = maxSlotStackSize;
			storage.updateNumberOfSlots();
			storage.inventory = new InventoryBasic("basic", false, storage.numberOfSlots);
			
			return storage;
		}
	}
	
}
