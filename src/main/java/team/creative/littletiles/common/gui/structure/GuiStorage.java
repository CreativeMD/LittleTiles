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
        super("little_storage");
        this.size = StorageSize.getSizeFromInventory(storage.inventory);
        if (size.expanded)
            setDim(size.guiWidth, size.guiHeight);
        this.storage = storage;
        if (!player.level().isClientSide)
            this.storage.openContainer(this);
    }
    
    @Override
    public void create() {
        flow = GuiFlow.STACK_Y;
        align = Align.CENTER;
        
        GuiParent parent = this;
        if (size.scrollbox) {
            parent = new GuiScrollY();
            if (size.expanded)
                parent.setExpandableX();
            add(parent);
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
        
        if (size.sort)
            add(new GuiButton("sort", x -> SORT.send(EndTag.INSTANCE)).setTranslate("gui.sort"));
        
        GuiPlayerInventoryGrid playerGrid = new GuiPlayerInventoryGrid(getPlayer());
        if (!size.expanded)
            playerGrid.setUnexpandableX();
        add(playerGrid);
        
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
        
        SMALL(9, 0, 0, false, false),
        LARGE(13, 250, 250, true, false),
        INFINITE(13, 250, 250, true, true);
        
        public final boolean scrollbox;
        public final int cols;
        public final boolean sort;
        public final int guiWidth;
        public final int guiHeight;
        public final boolean expanded;
        
        StorageSize(int cols, int guiWidth, int guiHeight, boolean sort, boolean scrollbox) {
            this.cols = cols;
            this.guiWidth = guiWidth;
            this.guiHeight = guiHeight;
            this.scrollbox = scrollbox;
            this.sort = sort;
            this.expanded = guiWidth > 0;
        }
        
        public static StorageSize getSizeFromInventory(Container inventory) {
            if (inventory.getContainerSize() <= 27)
                return StorageSize.SMALL;
            else if (inventory.getContainerSize() <= 117)
                return LARGE;
            return StorageSize.INFINITE;
        }
        
    }
    
}
