package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.SimpleContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.gui.GuiConfigSubControlNested;
import team.creative.creativecore.common.config.holder.ConfigHolderObject;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.config.holder.ICreativeConfigHolder;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.type.list.SortingList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.LittleStorage;

@OnlyIn(Dist.CLIENT)
public class LittleStorageGui extends LittleStructureGuiControl {
    
    public GuiCheckBox locked;
    public GuiConfigSubControlNested sortingControl;
    
    public LittleStorageGui(LittleStructureGui type, GuiTreeItemStructure item) {
        super(type, item);
        flow = GuiFlow.STACK_Y;
    }
    
    @Override
    public void create(@Nullable LittleStructure structure) {
        add(new GuiLabel("space").setTitle(new TextBuilder().text("space: " + LittleStorage.getSizeOfInventory(item.group)).build()));
        boolean invisible = false;
        boolean whitelist = false;
        List<CreativeIngredient> filter = null;
        if (structure instanceof LittleStorage s) {
            invisible = s.invisibleStorageTiles;
            whitelist = s.whitelist;
            filter = s.filter;
        }
        add(new GuiCheckBox("invisible", invisible).setTranslate("gui.blueprint.storage.invisible"));
        
        SortingList sortingList = new SortingList(whitelist);
        if (filter != null)
            sortingList.entries.addAll(filter);
        
        ICreativeConfigHolder holder = ConfigHolderObject.createUnrelated(CreativeConfigRegistry.ROOT, Side.SERVER, sortingList, new SortingList());
        add(sortingControl = new GuiConfigSubControlNested("filter", ConfigTypeConveration.FAKE_PARENT, null, Side.SERVER, null, true));
        sortingControl.setDim(new GuiSizeRules().maxWidth(200).maxHeight(200));
        sortingControl.load(holder, sortingList);
        sortingControl.createControls();
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleStorage storage = (LittleStorage) structure;
        storage.invisibleStorageTiles = ((GuiCheckBox) get("invisible")).value;
        
        for (LittleTile tile : item.group)
            if (tile.getBlock().is(LittleTiles.STORAGE_BLOCKS))
                tile.color = ColorUtils.setAlpha(tile.color, storage.invisibleStorageTiles ? 0 : 255);
            
        storage.inventorySize = LittleStorage.getSizeOfInventory(item.group);
        storage.stackSizeLimit = LittleStorage.MAX_SLOT_STACK_SIZE;
        storage.updateNumberOfSlots();
        storage.inventory = new SimpleContainer(storage.numberOfSlots);
        
        sortingControl.save();
        SortingList sorting = (SortingList) sortingControl.value;
        storage.whitelist = sorting.isWhitelist;
        if (!sorting.entries.isEmpty())
            storage.filter = new ArrayList<>(sorting.entries);
        else
            storage.filter = null;
        
        return structure;
    }
}