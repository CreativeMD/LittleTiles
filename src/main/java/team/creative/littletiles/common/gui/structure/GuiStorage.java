package team.creative.littletiles.common.gui.structure;

import net.minecraft.nbt.EndTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.InventoryUtils;
import team.creative.littletiles.common.structure.type.LittleStorage;

public class GuiStorage extends GuiLayer {
    
    public LittleStorage storage;
    public final StorageSize size;
    public final GuiSyncLocal<EndTag> SORT = getSyncHolder().register("sort", x -> {
        InventoryUtils.sortInventory(storage.inventory, false);
        get("storage", GuiInventoryGrid.class).setChanged();
    });
    
    public GuiStorage(LittleStorage storage, Player player) {
        super("little_storage", 250, 250);
        this.size = StorageSize.getSizeFromInventory(storage.inventory);
        this.storage = storage;
        if (!player.level.isClientSide)
            this.storage.openContainer(this);
    }
    
    @Override
    public void create() {
        flow = GuiFlow.STACK_Y;
        align = Align.CENTER;
        
        GuiParent parent = this;
        if (size.scrollbox) {
            parent = new GuiScrollY();
            add(parent.setExpandableX());
        }
        
        GuiInventoryGrid inv = new GuiInventoryGrid("storage", storage.inventory, size.cols, (int) Math.ceil(storage.inventory.getContainerSize() / (double) size.cols), (c, i) -> {
            if (i + 1 == storage.numberOfSlots && storage.lastSlotStackSize > 0)
                return new Slot(c, i, 0, 0) {
                    
                    @Override
                    public int getMaxStackSize() {
                        return storage.lastSlotStackSize;
                    }
                };
            return new Slot(c, i, 0, 0);
        });
        
        parent.add(inv);
        inv.setChanged();
        
        add(new GuiPlayerInventoryGrid(getPlayer()));
        
        add(new GuiButton("sort", x -> SORT.send(EndTag.INSTANCE)));
    }
    
    public void inventoryChanged() {
        if (!isClient())
            get("storage", GuiInventoryGrid.class).setChanged();
    }
    
    @Override
    public void closed() {
        super.closed();
        if (storage != null && !isClient())
            storage.closeContainer(this);
    }
    
    public static enum StorageSize {
        
        SMALL(9, false),
        Large(13, false),
        INFINITE(13, true);
        
        public final boolean scrollbox;
        public final int cols;
        
        StorageSize(int cols, boolean scrollbox) {
            this.cols = cols;
            this.scrollbox = scrollbox;
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
