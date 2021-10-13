package team.creative.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.block.BlockStorageTile;
import com.creativemd.littletiles.common.container.SubContainerStorage;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.StackIngredient;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.group.LittleGroup;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;

public class LittleStorage extends LittleStructure {
    
    private List<SubContainerStorage> openContainers = new ArrayList<SubContainerStorage>();
    
    public static int maxSlotStackSize = 64;
    
    public int inventorySize = 0;
    public int stackSizeLimit = 0;
    public int numberOfSlots = 0;
    public int lastSlotStackSize = 0;
    
    public SimpleContainer inventory = null;
    
    public boolean invisibleStorageTiles = false;
    
    public LittleStorage(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    public void updateNumberOfSlots() {
        float slots = inventorySize / (float) stackSizeLimit;
        numberOfSlots = (int) Math.ceil(slots);
        lastSlotStackSize = (int) ((slots % 1) * stackSizeLimit);
    }
    
    @Override
    protected void loadFromNBTExtra(CompoundTag nbt) {
        inventorySize = nbt.getInt("inventorySize");
        stackSizeLimit = nbt.getInt("stackSizeLimit");
        numberOfSlots = nbt.getInt("numberOfSlots");
        lastSlotStackSize = nbt.getInt("lastSlot");
        if (nbt.contains("inventory"))
            inventory = InventoryUtils.loadInventoryBasic(nbt.getCompound("inventory"));
        else
            inventory = null;
        if (inventory != null)
            inventory.addListener(x -> onInventoryChanged());
        
        invisibleStorageTiles = nbt.getBoolean("invisibleStorage");
    }
    
    @Override
    protected void writeToNBTExtra(CompoundTag nbt) {
        if (inventory != null) {
            nbt.putInt("inventorySize", inventorySize);
            nbt.putInt("stackSizeLimit", stackSizeLimit);
            nbt.putInt("numberOfSlots", numberOfSlots);
            nbt.putInt("lastSlot", lastSlotStackSize);
            nbt.setTag("inventory", InventoryUtils.saveInventoryBasic(inventory));
        }
        nbt.putBoolean("invisibleStorage", invisibleStorageTiles);
    }
    
    @Override
    public void onStructureDestroyed() {
        super.onStructureDestroyed();
        if (!getLevel().isClientSide) {
            for (SubContainerStorage container : openContainers) {
                container.storage = null;
                NBTTagCompound nbt = new NBTTagCompound();
                PacketHandler.sendPacketToPlayer(new GuiLayerPacket(nbt, container.getLayerID(), true), (EntityPlayerMP) container.player);
                container.closeLayer(nbt, true);
            }
        }
    }
    
    public static int getSizeOfInventory(LittleGroup previews) {
        double size = 0;
        String name = LittleTiles.STORAGE_BLOCK.getRegistryName().toString();
        for (int i = 0; i < previews.size(); i++) {
            if (previews.get(i).getBlockName().equals(name))
                size += previews.get(i).box.getSize().getPercentVolume(previews.getGrid()) * LittleGrid.defaultGrid().count3d * LittleTiles.CONFIG.general.storagePerPixel;
        }
        return (int) size;
    }
    
    public boolean hasPlayerOpened(Player player) {
        for (SubContainerStorage container : openContainers)
            if (container.getPlayer() == player)
                return true;
        return false;
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide && !hasPlayerOpened(player))
            LittleStructureGuiHandler.openGui("littleStorageStructure", new CompoundTag(), player, this);
        return InteractionResult.SUCCESS;
    }
    
    protected void updateInput() {
        getInput(0).updateState(new boolean[] { !openContainers.isEmpty() });
    }
    
    public void onInventoryChanged() {
        if (getLevel().isClientSide)
            return;
        int used = 0;
        boolean allSlotsFilled = true;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
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
            for (IStructureParentCollection list : blocksList())
                for (LittleTile tile : list)
                    if (tile.getBlock() == LittleTiles.STORAGE_BLOCK)
                        volume += tile.getPercentVolume(list.getGrid());
                    
            volume *= LittleGrid.defaultGrid().count3d * LittleTiles.CONFIG.general.storagePerPixel;
            
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
        
        public LittleStorageType(String id, String category, Class<? extends LittleStructure> structureClass, LittleAttributeBuilder attribute) {
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
