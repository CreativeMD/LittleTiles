package com.creativemd.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.block.BlockStorageTile;
import com.creativemd.littletiles.common.container.SubContainerStorage;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.StackIngredient;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleStorage extends LittleStructure {
    
    private List<SubContainerStorage> openContainers = new ArrayList<SubContainerStorage>();
    
    public static int maxSlotStackSize = 64;
    
    public int inventorySize = 0;
    public int stackSizeLimit = 0;
    public int numberOfSlots = 0;
    public int lastSlotStackSize = 0;
    
    public InventoryBasic inventory = null;
    
    public boolean invisibleStorageTiles = false;
    
    public LittleStorage(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
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
        else
            inventory = null;
        if (inventory != null)
            inventory.addInventoryChangeListener((x) -> onInventoryChanged());
        
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
                size += previews.get(i).box.getSize()
                    .getPercentVolume(previews.getContext()) * LittleGridContext.get().maxTilesPerBlock * LittleTiles.CONFIG.general.storagePerPixel;
        }
        return (int) size;
    }
    
    public boolean hasPlayerOpened(EntityPlayer player) {
        for (SubContainerStorage container : openContainers)
            if (container.getPlayer() == player)
                return true;
        return false;
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
        if (!worldIn.isRemote && !hasPlayerOpened(playerIn))
            LittleStructureGuiHandler.openGui("littleStorageStructure", new NBTTagCompound(), playerIn, this);
        return true;
    }
    
    protected void updateInput() {
        getInput(0).updateState(new boolean[] { !openContainers.isEmpty() });
    }
    
    public void onInventoryChanged() {
        if (getWorld().isRemote)
            return;
        int used = 0;
        boolean allSlotsFilled = true;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty())
                allSlotsFilled = false;
            else
                used += stack.getCount();
        }
        if (allSlotsFilled)
            used = inventorySize;
        int filled = (int) (Math.ceil((double) used / inventorySize * 65535));
        getInput(1).updateState(BooleanUtils.toBits(filled, 16));
    }
    
    public void openContainer(SubContainerStorage container) {
        openContainers.add(container);
        updateInput();
    }
    
    public void closeContainer(SubContainerStorage container) {
        openContainers.remove(container);
        updateInput();
        onInventoryChanged();
    }
    
    @Override
    protected void afterPlaced() {
        super.afterPlaced();
        double volume = 0;
        try {
            for (IStructureTileList list : blocksList())
                for (LittleTile tile : list)
                    if (tile.getBlock() instanceof BlockStorageTile)
                        volume += tile.getPercentVolume(list.getContext());
                    
            volume *= LittleGridContext.get().maxTilesPerBlock * LittleTiles.CONFIG.general.storagePerPixel;
            
            inventorySize = (int) volume;
            stackSizeLimit = maxSlotStackSize;
            updateNumberOfSlots();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
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
            
            LittleStorage storage = createStructure(LittleStorage.class, null);
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
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleStorage.class);
        }
    }
    
    public static class LittleStorageType extends LittleStructureType {
        
        public LittleStorageType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
            super(id, category, structureClass, attribute);
        }
        
        @Override
        public void addIngredients(LittlePreviews previews, LittleIngredients ingredients) {
            super.addIngredients(previews, ingredients);
            
            IInventory inventory = InventoryUtils.loadInventoryBasic(previews.structureNBT.getCompoundTag("inventory"));
            if (inventory != null)
                ingredients.add(new StackIngredient(inventory));
        }
        
    }
    
}
