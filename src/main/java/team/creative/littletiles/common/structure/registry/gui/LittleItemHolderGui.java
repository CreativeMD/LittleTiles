package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.converation.ConfigTypeConveration;
import team.creative.creativecore.common.config.gui.GuiConfigSubControlNested;
import team.creative.creativecore.common.config.holder.ConfigHolderObject;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.config.holder.ICreativeConfigHolder;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.type.list.SortingList;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.LittleItemHolder;

public class LittleItemHolderGui extends LittleStructureGuiControl {
    
    public GuiCheckBox locked;
    public GuiConfigSubControlNested sortingControl;
    
    public LittleItemHolderGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
        flow = GuiFlow.STACK_Y;
    }
    
    @Override
    public void create(LittleStructure structure) {
        boolean locked;
        boolean whitelist;
        List<CreativeIngredient> filter;
        if (structure instanceof LittleItemHolder holder) {
            locked = holder.locked;
            whitelist = holder.whitelist;
            filter = holder.filter;
        } else {
            locked = false;
            whitelist = false;
            filter = null;
        }
        
        add(this.locked = new GuiCheckBox("locked", locked).setTranslate("gui.structure.locked"));
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
        LittleItemHolder holder = (LittleItemHolder) structure;
        if (item.group.getStructureTag() != null)
            structure.load(item.group.getStructureTag(), item.provider());
        
        holder.locked = locked.value;
        
        sortingControl.save();
        SortingList sorting = (SortingList) sortingControl.value;
        holder.whitelist = sorting.isWhitelist;
        if (sorting.entries.isEmpty())
            holder.filter = null;
        else
            holder.filter = new ArrayList<>(sorting.entries);
        return holder;
    }
    
}
