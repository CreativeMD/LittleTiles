package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRatioRules;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiAnimationViewer;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.LittleToolHandler;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGui;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGuiControl;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGuiRegistry;

public class GuiRecipe extends GuiConfigure {
    
    public final GuiSyncLocal<EndTag> CLEAR_CONTENT = getSyncHolder().register("clear_content", tag -> {
        tool.get().getOrCreateTag().remove(ItemLittleBlueprint.CONTENT_KEY);
        tool.changed();
        LittleToolHandler.OPEN_CONFIG.open(getPlayer());
    });
    
    public final GuiSyncLocal<CompoundTag> SAVE = getSyncHolder().register("save", tag -> {
        tool.get().getOrCreateTag().put(ItemLittleBlueprint.CONTENT_KEY, tag);
        tool.changed();
        closeThisLayer();
    });
    
    private GuiTree tree;
    public GuiComboBoxMapped<LittleStructureGui> types;
    public GuiParent config;
    public LittleStructureGuiControl control;
    
    public LinkedHashMap<GuiTreeItemStructure, AnimationPreview> availablePreviews = new LinkedHashMap<>();
    
    public GuiRecipe(ContainerSlotView view) {
        super("recipe", view);
        flow = GuiFlow.STACK_X;
        valign = VAlign.STRETCH;
        setDim(new GuiSizeRules().minWidth(500).minHeight(300));
        registerEventChanged(x -> {
            if (x.control.is("type"))
                ((GuiTreeItemStructure) tree.selected()).load();
        });
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        return null;
    }
    
    private void buildStructureTree(GuiTree tree, GuiTreeItem parent, LittleGroup group, int index) {
        if (group.isEmpty()) {
            if (!group.children.hasChildren())
                return;
            for (LittleGroup child : group.children.children()) {
                buildStructureTree(tree, parent, child, index);
                index++;
            }
            return;
        }
        
        LittleGroup copy = new LittleGroup(group.hasStructure() ? group.getStructureTag().copy() : null, group.getGrid(), Collections.EMPTY_LIST);
        for (LittleTile tile : group)
            copy.addDirectly(tile.copy());
        for (Entry<String, LittleGroup> extension : group.children.extensionEntries())
            copy.children.addExtension(extension.getKey(), extension.getValue().copy());
        GuiTreeItemStructure item = new GuiTreeItemStructure(name, this, tree, copy, index);
        availablePreviews.put(item, null);
        parent.addItem(item);
        
        int i = 0;
        for (LittleGroup child : group.children.children()) {
            buildStructureTree(tree, item, child, i);
            i++;
        }
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        
        // Load recipe content
        LittleGroup group = LittleGroup.load(tool.get().getOrCreateTagElement(ItemLittleBlueprint.CONTENT_KEY));
        
        GuiParent topBottom = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        add(topBottom.setExpandable());
        
        GuiParent top = new GuiParent(GuiFlow.STACK_X);
        topBottom.add(top.setExpandableY());
        
        tree = new GuiTree("overview", false).setRootVisibility(false).keepSelected();
        buildStructureTree(tree, tree.root(), group, 0);
        tree.root().setTitle(Component.literal("root"));
        tree.updateTree();
        
        top.add(tree.setDim(new GuiSizeRatioRules().widthRatio(0.2F).maxWidth(100)).setExpandableY());
        
        GuiParent topCenter = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        top.add(topCenter.setDim(new GuiSizeRatioRules().widthRatio(0.4F).maxWidth(300)).setExpandableY());
        
        // Actual recipe configuration
        types = new GuiComboBoxMapped<>("type", new TextMapBuilder<LittleStructureGui>().addComponent(LittleStructureGuiRegistry.registered(), x -> x.translatable()));
        topCenter.add(types);
        config = new GuiParent("config", GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        topCenter.add(config.setExpandableY());
        
        top.add(new GuiAnimationViewer("viewer", () -> availablePreviews, () -> (GuiTreeItemStructure) tree.selected()).setExpandable());
        
        GuiParent bottom = new GuiParent(GuiFlow.STACK_X).setVAlign(VAlign.STRETCH);
        topBottom.add(bottom);
        
        GuiParent leftBottom = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        bottom.add(leftBottom.setDim(new GuiSizeRatioRules().widthRatio(0.2F).maxWidth(100)));
        leftBottom.add(new GuiIconButton("up", GuiIcon.ARROW_UP, x -> tree.moveUp()));
        leftBottom.add(new GuiIconButton("down", GuiIcon.ARROW_DOWN, x -> tree.moveDown()));
        
        GuiLeftRightBox rightBottom = new GuiLeftRightBox();
        bottom.add(rightBottom.setVAlign(VAlign.CENTER).setExpandableX());
        rightBottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        rightBottom.addLeft(new GuiButton("clear", x -> {
            GuiDialogHandler.openDialog(getIntegratedParent(), "clear_content", Component.translatable("gui.recipe.dialog.clear"), (g, b) -> {
                if (b == DialogButton.YES)
                    CLEAR_CONTENT.send(EndTag.INSTANCE);
            }, DialogButton.NO, DialogButton.YES);
        }).setTranslate("gui.recipe.clear"));
        rightBottom.addLeft(new GuiButton("selection", x -> {}).setTranslate("gui.recipe.selection").setEnabled(false));
        rightBottom.addRight(new GuiButton("check", x -> {}).setTranslate("gui.check").setEnabled(false));
        rightBottom.addRight(new GuiButton("save", x -> {
            ((GuiTreeItemStructure) tree.selected()).save();
            SAVE.send(LittleGroup.save(reconstructBlueprint()));
        }).setTranslate("gui.save"));
        
        tree.selectFirst();
    }
    
    protected LittleGroup reconstructBlueprint(GuiTreeItemStructure item) {
        List<LittleGroup> children = new ArrayList<>();
        for (GuiTreeItem child : item.items())
            children.add(reconstructBlueprint((GuiTreeItemStructure) child));
        CompoundTag nbt;
        if (item.structure == null)
            nbt = null;
        else {
            nbt = new CompoundTag();
            item.structure.save(nbt);
        }
        return new LittleGroup(nbt, item.group.copy(), children);
    }
    
    protected LittleGroup reconstructBlueprint() {
        if (tree.root().itemsCount() == 1)
            return reconstructBlueprint((GuiTreeItemStructure) tree.root().items().iterator().next());
        List<LittleGroup> children = new ArrayList<>();
        for (GuiTreeItem child : tree.root().items())
            children.add(reconstructBlueprint((GuiTreeItemStructure) child));
        return new LittleGroup(null, LittleGrid.min(), children);
    }
    
}
