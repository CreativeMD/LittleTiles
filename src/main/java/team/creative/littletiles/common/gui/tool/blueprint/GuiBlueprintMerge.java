package team.creative.littletiles.common.gui.tool.blueprint;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiFixedDimension;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents.GuiSignalEvent;

public class GuiBlueprintMerge extends GuiLayer {
    
    public GuiBlueprint blueprint;
    public GuiTreeItemStructure structure;
    
    public GuiBlueprintMerge() {
        super("gui.blueprint.merge");
        setDim(new GuiFixedDimension(150));
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(GuiBlueprint blueprint) {
        this.blueprint = blueprint;
        this.structure = (GuiTreeItemStructure) blueprint.tree.selected();
        clear();
        init();
    }
    
    public boolean isParent(GuiTreeItemStructure item, GuiTreeItemStructure possibleParent) {
        if (possibleParent.getLevel() >= item.getLevel())
            return false;
        if (item.getParentItem() == possibleParent)
            return true;
        if (item.getParentItem() instanceof GuiTreeItemStructure parent)
            return isParent(parent, possibleParent);
        return false;
    }
    
    @Override
    public void create() {
        if (structure == null)
            return;
        
        add(new GuiLabel("title").setTranslate("gui.blueprint.merge.title", structure.getTitle()));
        
        TextMapBuilder<GuiTreeItemStructure> map = new TextMapBuilder<GuiTreeItemStructure>();
        blueprint.actionOnAllItems(x -> {
            if (structure == x || isParent(structure, x))
                return;
            String prefix = "";
            for (int i = 1; i < x.getLevel(); i++)
                prefix += "-";
            map.addComponent(x, Component.literal(prefix + x.getTitle()));
        });
        boolean isEmpty = map.size() == 0;
        if (isEmpty)
            map.addComponent(null, Component.translatable("gui.blueprint.merge.not_found"));
        GuiComboBox<GuiTreeItemStructure> box = new GuiComboBox<>("box", map);
        add(box.setExpandableX().setEnabled(!isEmpty));
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("save", x -> {
            GuiTreeItemStructure selected = box.selected();
            if (selected == null)
                return;
            if (isParent(structure, selected))
                GuiDialogHandler.openDialog(getIntegratedParent(), "merge_failed", Component.translatable("gui.blueprint.dialog.merge.failed", selected.getTitle(), structure
                        .getTitle()), (g, b) -> {}, DialogButton.OK);
            
            blueprint.removeItem(selected);
            structure.group.add(selected.group);
            int size = structure.externalOutputCount();
            for (GuiSignalEvent event : selected.externalOutputs())
                structure.setExternalOutput(event.component.index() + size, event);
            for (GuiTreeItem child : selected.items())
                structure.addItem(child);
            structure.refreshAnimation();
            blueprint.tree.updateTree();
            closeThisLayer();
        }).setTranslate("gui.save").setEnabled(!isEmpty));
    }
    
}
