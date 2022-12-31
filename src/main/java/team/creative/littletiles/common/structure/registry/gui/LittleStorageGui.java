package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraft.world.SimpleContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.type.LittleStorage;

@OnlyIn(Dist.CLIENT)
public class LittleStorageGui extends LittleStructureGuiControl {
    
    public LittleStorageGui(LittleStructureType type, GuiTreeItemStructure item) {
        super(type, item);
    }
    
    @Override
    public void createExtra(LittleGroup previews, @Nullable LittleStructure structure) {
        add(new GuiLabel("space").setTitle(new TextBuilder().text("space: " + LittleStorage.getSizeOfInventory(previews)).build()));
        boolean invisible = false;
        if (structure instanceof LittleStorage)
            invisible = ((LittleStorage) structure).invisibleStorageTiles;
        add(new GuiCheckBox("invisible", "invisible storage tiles", invisible));
    }
    
    @Override
    protected void saveExtra(LittleStructure structure, LittleGroup previews) {
        LittleStorage storage = (LittleStorage) structure;
        storage.invisibleStorageTiles = ((GuiCheckBox) get("invisible")).value;
        
        for (LittleTile tile : previews)
            if (tile.getBlock().is(LittleTiles.STORAGE_BLOCKS))
                tile.color = ColorUtils.setAlpha(tile.color, 0);
            
        storage.inventorySize = LittleStorage.getSizeOfInventory(previews);
        storage.stackSizeLimit = LittleStorage.maxSlotStackSize;
        storage.updateNumberOfSlots();
        storage.inventory = new SimpleContainer(storage.numberOfSlots);
    }
}