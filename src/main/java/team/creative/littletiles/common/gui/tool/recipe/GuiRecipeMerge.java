package team.creative.littletiles.common.gui.tool.recipe;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiFixedDimension;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents.GuiSignalEvent;

public class GuiRecipeMerge extends GuiLayer {
    
    public GuiRecipe recipe;
    public GuiTreeItemStructure structure;
    
    public GuiRecipeMerge() {
        super("gui.recipe.merge");
        setDim(new GuiFixedDimension(150));
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(GuiRecipe recipe) {
        this.recipe = recipe;
        this.structure = (GuiTreeItemStructure) recipe.tree.selected();
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
        
        add(new GuiLabel("title").setTranslate("gui.recipe.merge.title", structure.getTitle()));
        
        TextMapBuilder<GuiTreeItemStructure> map = new TextMapBuilder<GuiTreeItemStructure>();
        recipe.actionOnAllItems(x -> {
            if (structure == x || isParent(structure, x))
                return;
            String prefix = "";
            for (int i = 1; i < x.getLevel(); i++)
                prefix += "-";
            map.addComponent(x, Component.literal(prefix + x.getTitle()));
        });
        boolean isEmpty = map.size() == 0;
        if (isEmpty)
            map.addComponent(null, Component.translatable("gui.recipe.merge.not_found"));
        GuiComboBoxMapped<GuiTreeItemStructure> box = new GuiComboBoxMapped<>("box", map);
        add(box.setExpandableX().setEnabled(!isEmpty));
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("save", x -> {
            GuiTreeItemStructure selected = box.getSelected();
            if (selected == null)
                return;
            if (isParent(structure, selected))
                GuiDialogHandler.openDialog(getIntegratedParent(), "merge_failed", Component
                        .translatable("gui.recipe.dialog.merge.failed", selected.getTitle(), structure.getTitle()), (g, b) -> {}, DialogButton.OK);
            
            recipe.removeItem(selected);
            structure.group.add(selected.group);
            int size = structure.externalOutputCount();
            for (GuiSignalEvent event : selected.externalOutputs())
                structure.setExternalOutput(event.component.index() + size, event);
            for (GuiTreeItem child : selected.items())
                structure.addItem(child);
            structure.refreshAnimation();
            recipe.tree.updateTree();
            closeThisLayer();
        }).setTranslate("gui.save").setEnabled(!isEmpty));
    }
    
}
