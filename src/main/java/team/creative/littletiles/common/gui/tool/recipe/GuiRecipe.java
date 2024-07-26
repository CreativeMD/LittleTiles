package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTree.GuiTreeSelectionChanged;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRatioRules;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.gui.sync.GuiSyncLocalLayer;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiComboxMappedFlexible;
import team.creative.littletiles.common.gui.controls.animation.GuiAnimationPanel;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.gui.tool.recipe.test.GuiRecipeTest;
import team.creative.littletiles.common.gui.tool.recipe.test.RecipeTest;
import team.creative.littletiles.common.gui.tool.recipe.test.RecipeTestError;
import team.creative.littletiles.common.gui.tool.recipe.test.RecipeTestResults;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGui;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGuiControl;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGuiRegistry;

public class GuiRecipe extends GuiConfigure {
    
    public final GuiSyncLocal<EndTag> CLEAR_CONTENT = getSyncHolder().register("clear_content", tag -> {
        CompoundTag content = new CompoundTag();
        LittleGrid.MIN.set(content);
        ItemLittleBlueprint.setContent(tool.get(), content);
        tool.changed();
        LittleTilesGuiRegistry.OPEN_CONFIG.open(getPlayer());
    });
    
    public final GuiSyncLocal<EndTag> REMOVE_CONTENT = getSyncHolder().register("remove_content", tag -> {
        var data = ILittleTool.getData(tool.get());
        data.remove(ItemLittleBlueprint.CONTENT_KEY);
        ILittleTool.setData(tool.get(), data);
        tool.changed();
        LittleTilesGuiRegistry.OPEN_CONFIG.open(getPlayer());
    });
    
    public final GuiSyncLocal<CompoundTag> SAVE = getSyncHolder().register("save", tag -> {
        ItemLittleBlueprint.setContent(tool.get(), tag);
        tool.changed();
        GuiRecipe.super.closeThisLayer();
    });
    
    public final GuiSyncLocalLayer<GuiRecipeTest> OPEN_TEST = getSyncHolder().layer("test", tag -> new GuiRecipeTest());
    public final GuiSyncLocalLayer<GuiRecipeAdd> OPEN_ADD = getSyncHolder().layer("add", tag -> new GuiRecipeAdd());
    public final GuiSyncLocalLayer<GuiRecipeMove> OPEN_MOVE = getSyncHolder().layer("move", tag -> new GuiRecipeMove());
    public final GuiSyncLocalLayer<GuiRecipeMerge> OPEN_MERGE = getSyncHolder().layer("merge", tag -> new GuiRecipeMerge());
    
    public GuiTree tree;
    public GuiComboxMappedFlexible<LittleStructureGui> types;
    public GuiParent config;
    public LittleStructureGuiControl control;
    public GuiLabel testReport;
    public GuiParent sidebarButtons;
    @OnlyIn(Dist.CLIENT)
    public GuiRecipeAnimationStorage storage;
    public GuiRecipeAnimationHandler animation = new GuiRecipeAnimationHandler();
    private boolean selectedBefore = true;
    
    public GuiRecipe(ContainerSlotView view) {
        super("recipe", view);
        flow = GuiFlow.STACK_X;
        valign = VAlign.STRETCH;
        setDim(new GuiSizeRules().minWidth(500).minHeight(300));
        registerEventChanged(x -> {
            if (x.control.is("type") && tree.selected() != null)
                ((GuiTreeItemStructure) tree.selected()).load();
            else if (x instanceof GuiTreeSelectionChanged sel)
                if (selectedBefore != (sel.selected != null)) {
                    selectedBefore = sel.selected != null;
                    for (GuiChildControl child : sidebarButtons)
                        if (!child.control.is("add"))
                            child.control.setEnabled(selectedBefore);
                }
        });
    }
    
    @Override
    public void becameTopLayer() {
        super.becameTopLayer();
        if (isClient())
            get("animation", GuiAnimationPanel.class).refresh();
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        return null;
    }
    
    public void buildStructureTree(GuiTree tree, GuiTreeItem parent, LittleGroup group, int index) {
        if (group.isEmpty()) {
            if (!group.children.hasChildren())
                return;
            for (LittleGroup child : group.children.children()) {
                buildStructureTree(tree, parent, child, index);
                index++;
            }
            return;
        }
        
        LittleGroup copy = new LittleGroup(group.hasStructure() ? group.getStructureTag().copy() : null, Collections.EMPTY_LIST);
        copy.addAll(group.getGrid(), new FunctionIterator<>(group, x -> x.copy()));
        for (Entry<String, LittleGroup> extension : group.children.extensionEntries())
            copy.children.addExtension(extension.getKey(), extension.getValue().copy());
        GuiTreeItemStructure item = new GuiTreeItemStructure(this, tree, copy, index);
        parent.addItem(item);
        
        int i = 0;
        for (LittleGroup child : group.children.children()) {
            buildStructureTree(tree, item, child, i);
            i++;
        }
    }
    
    @Override
    public void closeThisLayer() {
        closeWithDialog();
    }
    
    @Override
    public void closeTopLayer() {
        closeWithDialog();
    }
    
    @Override
    public void closed() {
        if (isClient())
            storage.unload();
    }
    
    private void closeWithDialog() {
        if (runTest()) {
            CompoundTag nbt = LittleGroup.save(reconstructBlueprint());
            
            if (ItemLittleBlueprint.getContent(tool.get()).equals(nbt)) { // No need to save anything
                super.closeThisLayer();
                return;
            }
            
            GuiDialogHandler.openDialog(getIntegratedParent(), "cancel", translatable("gui.recipe.cancel.dialog"), (g, b) -> {
                if (b == DialogButton.CANCEL)
                    return;
                if (b == DialogButton.YES)
                    SAVE.send(LittleGroup.save(reconstructBlueprint()));
                GuiRecipe.super.closeThisLayer();
            }, DialogButton.CANCEL, DialogButton.NO, DialogButton.YES);
        } else {
            GuiDialogHandler.openDialog(getIntegratedParent(), "cancel", translatable("gui.recipe.cancel.dialog.failed"), (g, b) -> {
                if (b == DialogButton.CONFIRM)
                    GuiRecipe.super.closeThisLayer();
            }, DialogButton.ABORT, DialogButton.CONFIRM);
        }
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
        
        // Load recipe content
        LittleGroup group = LittleGroup.load(ItemLittleBlueprint.getContent(tool.get()));
        
        GuiParent top = new GuiParent(GuiFlow.STACK_X);
        add(top.setExpandableY());
        
        tree = new GuiTree("overview", false) {
            
            @Override
            public void updateTree() {
                actionOnAllItems(x -> x.updateTitle());
                super.updateTree();
            }
            
        }.setRootVisibility(false).keepSelected();
        
        if (storage == null)
            storage = new GuiRecipeAnimationStorage(tree);
        
        buildStructureTree(tree, tree.root(), group, 0);
        tree.root().setTitle(Component.literal("root"));
        tree.updateTree();
        
        GuiParent sidebar = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        top.add(sidebar.setDim(new GuiSizeRatioRules().widthRatio(0.2F).maxWidth(100)));
        sidebar.add(tree.setExpandableY());
        
        sidebarButtons = new GuiParent(GuiFlow.FIT_X);
        sidebar.add(sidebarButtons.setAlign(Align.CENTER));
        
        sidebarButtons.add(new GuiButton("add", x -> OPEN_ADD.open(new CompoundTag()).init(this)).setTranslate("gui.plus").setAlign(Align.CENTER).setVAlign(VAlign.CENTER).setDim(
            12, 12).setTooltip(new TextBuilder().translate("gui.recipe.add").build()));
        sidebarButtons.add(new GuiButtonIcon("duplicate", Icon.DUPLICATE, x -> {
            if (tree.selected() == null)
                return;
            tree.selected().getParentItem().addItem(((GuiTreeItemStructure) tree.selected()).duplicate());
            tree.updateTree();
        }).setTooltip(new TextBuilder().translate("gui.recipe.duplicate").build()));
        sidebarButtons.add(new GuiButton("del", x -> {
            if (tree.selected() == null)
                return;
            GuiDialogHandler.openDialog(getIntegratedParent(), "delete_item", Component.translatable("gui.recipe.dialog.delete", ((GuiTreeItemStructure) tree.selected())
                    .getTitle()), (g, b) -> {
                        if (b == DialogButton.YES)
                            removeItem((GuiTreeItemStructure) tree.selected());
                    }, DialogButton.NO, DialogButton.YES);
        }).setTranslate("gui.del").setAlign(Align.CENTER).setVAlign(VAlign.CENTER).setDim(12, 12).setTooltip(new TextBuilder().translate("gui.recipe.delete").build()));
        sidebarButtons.add(new GuiButtonIcon("up", Icon.ARROW_UP, x -> tree.moveUp()).setTooltip(new TextBuilder().translate("gui.recipe.moveup").build()));
        sidebarButtons.add(new GuiButtonIcon("down", Icon.ARROW_DOWN, x -> tree.moveDown()).setTooltip(new TextBuilder().translate("gui.recipe.movedown").build()));
        
        sidebarButtons.add(new GuiButtonIcon("move", Icon.MOVE, x -> OPEN_MOVE.open(new CompoundTag()).init(this)).setTooltip(new TextBuilder().translate("gui.recipe.move")
                .build()));
        sidebarButtons.add(new GuiButtonIcon("merge", Icon.MERGE, x -> OPEN_MERGE.open(new CompoundTag()).init(this)).setTooltip(new TextBuilder().translate("gui.recipe.merge")
                .build()));
        
        GuiParent topCenter = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        top.add(topCenter.setDim(new GuiSizeRatioRules().widthRatio(0.4F).maxWidth(400)).setExpandableY());
        
        // Actual recipe configuration
        types = new GuiComboxMappedFlexible<>("type", new TextMapBuilder<LittleStructureGui>().addComponent(LittleStructureGuiRegistry.registered(), x -> x.translatable()), x -> x
                .translatable());
        topCenter.add(types);
        config = new GuiParent("config", GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        topCenter.add(config.setExpandableY());
        config.registerEventChanged(x -> {
            if (x.control.is("name") && tree.selected() instanceof GuiTreeItemStructure item)
                item.onNameChanged((GuiTextfield) x.control);
        });
        
        top.add(new GuiAnimationPanel(tree, storage, true, animation));
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom.setVAlign(VAlign.CENTER).setExpandableX());
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addLeft(new GuiButton("selection", x -> {
            GuiDialogHandler.openDialog(getIntegratedParent(), "remove_content", Component.translatable("gui.recipe.dialog.clear"), (g, b) -> {
                if (b == DialogButton.YES)
                    REMOVE_CONTENT.send(EndTag.INSTANCE);
            }, DialogButton.NO, DialogButton.YES);
        }).setTranslate("gui.recipe.selection"));
        bottom.addLeft(new GuiButton("clear", x -> {
            GuiDialogHandler.openDialog(getIntegratedParent(), "clear_content", Component.translatable("gui.recipe.dialog.clear"), (g, b) -> {
                if (b == DialogButton.YES)
                    CLEAR_CONTENT.send(EndTag.INSTANCE);
            }, DialogButton.NO, DialogButton.YES);
        }).setTranslate("gui.recipe.clear"));
        
        bottom.addRight(testReport = new GuiLabel("report").setTitle(Component.empty()));
        bottom.addRight(new GuiButton("check", x -> OPEN_TEST.open(new CompoundTag()).init(this)).setTranslate("gui.recipe.test"));
        bottom.addRight(new GuiButton("save", x -> {
            if (runTest())
                SAVE.send(LittleGroup.save(reconstructBlueprint()));
        }).setTranslate("gui.save"));
        
        tree.selectFirst();
    }
    
    @Override
    public void tick() {
        if (!isClient())
            return;
        
        super.tick();
        animation.tick();
        if (storage != null)
            storage.tick();
    }
    
    @Override
    public void render(GuiGraphics graphics, GuiChildControl control, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        storage.renderTick();
        super.render(graphics, control, controlRect, realRect, scale, mouseX, mouseY);
    }
    
    public void removeItem(GuiTreeItemStructure item) {
        if (item == null)
            return;
        item.getParentItem().removeItem(item);
        tree.updateTree();
        tree.selectFirst();
    }
    
    public void actionOnAllItems(Consumer<GuiTreeItemStructure> con) {
        for (GuiTreeItem item : tree.allItems())
            if (item instanceof GuiTreeItemStructure s)
                con.accept(s);
    }
    
    public boolean runTest() {
        if (tree.selected() != null)
            ((GuiTreeItemStructure) tree.selected()).save();
        RecipeTestResults results = RecipeTest.STANDARD.test(this);
        actionOnAllItems(x -> x.clearErrors());
        
        if (results.success()) {
            testReport.setTitle(translatable("gui.recipe.test.result.success"));
            get("check", GuiButton.class).setTranslate("gui.recipe.test");
        } else {
            for (RecipeTestError error : results)
                for (GuiTreeItemStructure item : error)
                    item.addError(error);
                
            String title = translate("gui.recipe.test.result.fail") + " ";
            if (results.errorCount() == 1)
                title += translate("gui.recipe.test.error.single");
            else
                title += translate("gui.recipe.test.error.multiple", results.errorCount());
            testReport.setTitle(Component.literal(title));
            get("check", GuiButton.class).setTranslate("gui.recipe.solve");
        }
        
        actionOnAllItems(x -> {
            x.updateTitle();
            x.updateTooltip();
        });
        
        reflow();
        
        return results.success();
    }
    
    protected LittleGroup reconstructBlueprint(GuiTreeItemStructure item) {
        Consumer<GuiTreeItemStructure> finalizer = LittleStructureGuiRegistry.getFinalizer(item.getStructureType());
        if (finalizer != null)
            finalizer.accept(item);
        
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
        return new LittleGroup(nbt, item.group.copyExceptChildren(), children);
    }
    
    protected LittleGroup reconstructBlueprint() {
        if (tree.root().itemsCount() == 1)
            return reconstructBlueprint((GuiTreeItemStructure) tree.root().items().iterator().next());
        List<LittleGroup> children = new ArrayList<>();
        for (GuiTreeItem child : tree.root().items())
            children.add(reconstructBlueprint((GuiTreeItemStructure) child));
        return new LittleGroup((CompoundTag) null, children);
    }
    
}
