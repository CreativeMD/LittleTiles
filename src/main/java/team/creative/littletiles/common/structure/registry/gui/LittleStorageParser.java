package team.creative.littletiles.common.structure.registry.gui;

import net.minecraft.world.SimpleContainer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.LittleStorage;

public class LittleStorageParser extends LittleStructureGuiControl {
    
    public LittleStorageParser(GuiParent parent, AnimationGuiHandler handler) {
        super(parent, handler);
    }
    
    @Override
    public void createControls(LittleGroup previews, LittleStructure structure) {
        parent.add(new GuiLabel("space").setTitle(new TextBuilder().text("space: " + LittleStorage.getSizeOfInventory(previews)).build()));
        boolean invisible = false;
        if (structure instanceof LittleStorage)
            invisible = ((LittleStorage) structure).invisibleStorageTiles;
        parent.add(new GuiCheckBox("invisible", "invisible storage tiles", invisible));
    }
    
    @Override
    public LittleStorage parseStructure(LittleGroup previews) {
        LittleStorage storage = createStructure(LittleStorage.class, null);
        storage.invisibleStorageTiles = ((GuiCheckBox) parent.get("invisible")).value;
        
        for (LittleTile tile : previews)
            if (tile.getBlock().is(LittleTiles.STORAGE_BLOCKS))
                tile.color = ColorUtils.setAlpha(tile.color, 0);
            
        storage.inventorySize = LittleStorage.getSizeOfInventory(previews);
        storage.stackSizeLimit = LittleStorage.maxSlotStackSize;
        storage.updateNumberOfSlots();
        storage.inventory = new SimpleContainer(storage.numberOfSlots);
        
        return storage;
    }
    
    @Override
    protected LittleStructureType getStructureType() {
        return LittleStructureRegistry.getStructureType(LittleStorage.class);
    }
}