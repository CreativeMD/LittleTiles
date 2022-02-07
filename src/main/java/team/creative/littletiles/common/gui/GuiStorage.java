package team.creative.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.ContainerControl;
import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.util.inventory.InventoryUtils;
import team.creative.littletiles.common.structure.type.LittleStorage;

public class GuiStorage extends GuiLayer {
    
    public LittleStorage storage;
    public final StorageSize size;
    
    public GuiStorage(LittleStorage storage, Player player) {
        super("little-storage", 250, 250);
        this.size = StorageSize.getSizeFromInventory(storage.inventory);
        this.storage = storage;
        if (!player.level.isClientSide)
            this.storage.openContainer(this);
        rect.maxX = size.width;
        rect.maxY = size.height;
    }
    
    @Override
    public void create() {
        
        if (!size.scrollbox) {
            super.addContainerControls();
            return;
        }
        
        if (storage.inventory != null) {
            int slotsPerRow = size.width / 18;
            int rows = (int) Math.ceil(storage.inventory.getContainerSize() / (double) slotsPerRow);
            int rowWidth = Math.min(slotsPerRow, storage.inventory.getContainerSize()) * 18;
            int offsetX = (size.width - rowWidth) / 2;
            
            for (int i = 0; i < storage.inventory.getContainerSize(); i++) {
                int row = i / slotsPerRow;
                int rowIndex = i - row * slotsPerRow;
                int stackSize = storage.stackSizeLimit;
                if (i + 1 == storage.numberOfSlots && storage.lastSlotStackSize > 0)
                    stackSize = storage.lastSlotStackSize;
                addSlotToContainer(new SlotStackLimit(storage.inventory, i, offsetX + rowIndex * 18, 5 + row * 18, stackSize));
            }
            
            addPlayerSlotsToContainer(player, size.playerOffsetX, size.playerOffsetY);
        }
        
        GuiScrollBox box = new GuiScrollBox("box", 0, 0, 244, 150);
        controls.add(box);
        for (int i = 0; i < container.controls.size(); i++) {
            ContainerControl control = container.controls.get(i);
            control.onOpened();
            
            if (control instanceof SlotControl && ((SlotControl) control).slot.inventory == storage.inventory) {
                ((SlotControl) control).slot.xPos -= 4;
                box.addControl(control.getGuiControl());
            } else
                controls.add(control.getGuiControl());
        }
        
        add(new GuiPlayerInventoryGrid(getPlayer()));
        
        add(new GuiButton("sort", x -> {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("sort", true);
            sendPacketToServer(nbt);
        }));
    }
    
    @Override
    public void writeOpeningNBT(NBTTagCompound nbt) {
        nbt.setTag("inventory", InventoryUtils.saveInventoryBasic(storage.inventory));
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        if (isRemote()) {
            if (nbt.hasKey("inventory")) {
                ItemStack[] stacks = InventoryUtils.loadInventory(nbt.getCompoundTag("inventory"));
                for (int i = 0; i < stacks.length; i++)
                    storage.inventory.setInventorySlotContents(i, stacks[i]);
            }
        }
        if (nbt.getBoolean("sort")) {
            InventoryUtils.sortInventory(storage.inventory, false);
            NBTTagCompound packet = new NBTTagCompound();
            packet.setTag("inventory", InventoryUtils.saveInventoryBasic(storage.inventory));
            sendNBTUpdate(packet);
        }
    }
    
    @Override
    public void closed() {
        super.closed();
        if (storage != null && !isClient())
            storage.closeContainer(this);
    }
    
    public static enum StorageSize {
        
        SMALL(176, 166, 8, 84, false),
        Large(250, 250, 45, 170, false),
        INFINITE(250, 250, 45, 170, true);
        
        public final int height;
        public final int width;
        public final int playerOffsetX;
        public final int playerOffsetY;
        public final boolean scrollbox;
        
        StorageSize(int width, int height, int playerOffsetX, int playerOffsetY, boolean scrollbox) {
            this.scrollbox = scrollbox;
            this.height = height;
            this.width = width;
            this.playerOffsetX = playerOffsetX;
            this.playerOffsetY = playerOffsetY;
        }
        
        public static StorageSize getSizeFromInventory(Container inventory) {
            if (inventory.getContainerSize() <= 27)
                return StorageSize.SMALL;
            else if (inventory.getContainerSize() <= 117)
                return Large;
            return StorageSize.INFINITE;
        }
        
    }
    
}
