package team.creative.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.inventory.InventoryUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiCreator;
import team.creative.littletiles.common.gui.structure.GuiStorage;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.StackIngredient;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;

public class LittleStorage extends LittleStructure {
    
    public static final LittleStructureGuiCreator GUI = GuiCreator
            .register("storage", new LittleStructureGuiCreator((nbt, player, structure) -> new GuiStorage((LittleStorage) structure, player)));
    
    private List<GuiStorage> openContainers = new ArrayList<GuiStorage>();
    
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
    protected void loadExtra(CompoundTag nbt) {
        inventorySize = nbt.getInt("inventorySize");
        stackSizeLimit = nbt.getInt("stackSizeLimit");
        numberOfSlots = nbt.getInt("numberOfSlots");
        lastSlotStackSize = nbt.getInt("lastSlot");
        if (nbt.contains("inventory"))
            inventory = InventoryUtils.load(nbt.getCompound("inventory"));
        else
            inventory = null;
        if (inventory != null)
            inventory.addListener(x -> onInventoryChanged());
        
        invisibleStorageTiles = nbt.getBoolean("invisibleStorage");
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        if (inventory != null) {
            nbt.putInt("inventorySize", inventorySize);
            nbt.putInt("stackSizeLimit", stackSizeLimit);
            nbt.putInt("numberOfSlots", numberOfSlots);
            nbt.putInt("lastSlot", lastSlotStackSize);
            nbt.put("inventory", InventoryUtils.save(inventory));
        }
        nbt.putBoolean("invisibleStorage", invisibleStorageTiles);
    }
    
    @Override
    public void onStructureDestroyed() {
        super.onStructureDestroyed();
        if (!getLevel().isClientSide) {
            for (GuiStorage container : openContainers) {
                container.storage = null;
                container.closeThisLayer();
            }
        }
    }
    
    public static int getSizeOfInventory(LittleGroup previews) {
        double size = 0;
        String name = LittleTilesRegistry.STORAGE_BLOCK.get().builtInRegistryHolder().key().location().toString();
        for (LittleTile tile : previews)
            if (tile.getBlock().blockName().equals(name))
                size += tile.getPercentVolume(previews.getGrid()) * LittleGrid.defaultGrid().count3d * LittleTiles.CONFIG.general.storagePerPixel;
        return (int) size;
    }
    
    public boolean hasPlayerOpened(Player player) {
        for (GuiStorage container : openContainers)
            if (container.getPlayer() == player)
                return true;
        return false;
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide && !hasPlayerOpened(player))
            GUI.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
    protected void updateInput() {
        getInput(0).updateState(SignalState.of(!openContainers.isEmpty()));
    }
    
    public void onInventoryChanged() {
        if (getLevel().isClientSide)
            return;
        if (!openContainers.isEmpty())
            for (GuiStorage gui : openContainers)
                gui.inventoryChanged();
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
        getInput(1).updateState(SignalState.of(filled));
    }
    
    public void openContainer(GuiStorage container) {
        openContainers.add(container);
        updateInput();
    }
    
    public void closeContainer(GuiStorage container) {
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
                    if (tile.getBlock() == LittleTilesRegistry.STORAGE_BLOCK.get())
                        volume += tile.getPercentVolume(list.getGrid());
                    
            volume *= LittleGrid.defaultGrid().count3d * LittleTiles.CONFIG.general.storagePerPixel;
            
            inventorySize = (int) volume;
            stackSizeLimit = maxSlotStackSize;
            updateNumberOfSlots();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    public static class LittleStorageType extends LittleStructureType {
        
        public <T extends LittleStructure> LittleStorageType(String id, Class<T> structureClass, BiFunction<LittleStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
            super(id, structureClass, factory, attribute);
        }
        
        @Override
        public void addIngredients(LittleGroup previews, LittleIngredients ingredients) {
            super.addIngredients(previews, ingredients);
            
            Container inventory = InventoryUtils.load(previews.getStructureTag().getCompound("inventory"));
            if (inventory != null)
                ingredients.add(new StackIngredient(inventory));
        }
        
    }
    
}
