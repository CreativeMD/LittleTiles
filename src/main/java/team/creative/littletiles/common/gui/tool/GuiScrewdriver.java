package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionColorBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.control.filter.GuiElementFilter;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.selection.SelectionMode;

public class GuiScrewdriver extends GuiConfigure {
    
    public static BiFilter<IParentCollection, LittleTile> lastSelectedFilter = TileFilters.and();
    public static ItemStack lastSelectedReplaceStack;
    
    protected GuiElementFilter filter;
    
    public GuiScrewdriver(ContainerSlotView view) {
        super("screwdriver", 200, 205, view);
        flow = GuiFlow.STACK_Y;
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        
        var stack = tool.get();
        SelectionComponent component = SelectionComponent.getOrDefault(stack);
        
        GuiComboBox<SelectionMode> box = new GuiComboBox<>("selection_mode", new TextMapBuilder<SelectionMode>().addEntrySet(SelectionMode.REGISTRY.entrySet(), x -> x.getValue()
                .getTranslation()));
        box.select(component.mode);
        add(box.setExpandableX());
        
        add(new GuiLabel("filter_label").setTranslate("gui.filter"));
        add(filter = (GuiElementFilter) GuiElementFilter.ofGroup(getPlayer(), lastSelectedFilter).setExpandableX().setDim(new GuiSizeRules().prefHeight(100)));
        add(new GuiCheckBox("remove", false).setTranslate("gui.remove"));
        
        add(new GuiCheckBox("replace", false).setTranslate("gui.replace_with"));
        
        GuiStackSelector selector = new GuiStackSelector("replacement", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
        if (lastSelectedReplaceStack != null)
            selector.setSelectedForce(lastSelectedReplaceStack);
        add(selector.setExpandableX());
        
        Color color = new Color(255, 255, 255, 255);
        add(new GuiCheckBox("colorize", false).setTranslate("gui.colorize"));
        
        add(new GuiColorPicker("picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())).setExpandableX());
        
        GuiLeftRightBox actions = new GuiLeftRightBox().addLeft(new GuiButton("undo", x -> {
            try {
                LittleTilesClient.ACTION_HANDLER.undo();
            } catch (LittleActionException e) {
                getPlayer().sendSystemMessage(Component.literal(e.getLocalizedMessage()));
            }
        }).setTranslate("gui.undo")).addLeft(new GuiButton("redo", x -> {
            try {
                LittleTilesClient.ACTION_HANDLER.redo();
            } catch (LittleActionException e) {
                getPlayer().sendSystemMessage(Component.literal(e.getLocalizedMessage()));
            }
        }).setTranslate("gui.redo")).addRight(new GuiButton("run", x -> {
            try {
                LittleAction action = getDesiredAction();
                if (action != null)
                    if (action.wasSuccessful(LittleTilesClient.ACTION_HANDLER.execute(action)))
                        playSound(SoundEvents.LEVER_CLICK);
            } catch (LittleActionException e) {
                LittleActionHandlerClient.handleException(e);
            }
            
        }).setTranslate("gui.run"));
        add(actions);
    }
    
    public LittleAction getDesiredAction() throws LittleActionException {
        var stack = tool.get();
        SelectionComponent component = SelectionComponent.getOrDefault(stack);
        
        BiFilter<IParentCollection, LittleTile> filter = this.filter.get();
        
        var player = getPlayer();
        Level level = player.level();
        var selection = component.mode.selectionBuilder(level, (LittleActionSource) player, stack, component).build(filter);
        
        if (selection.isEmpty())
            return null;
        
        LittleBoxes boxes = LittleBoxes.of(selection);
        
        boolean remove = get("remove", GuiCheckBox.class).value;
        boolean replace = get("replace", GuiCheckBox.class).value;
        boolean colorize = get("colorize", GuiCheckBox.class).value;
        
        if (remove)
            return new LittleActionDestroyBoxes(level, boxes);
        else {
            List<LittleAction> actions = new ArrayList<>();
            
            if (replace) {
                GuiStackSelector replacement = get("replacement");
                ItemStack stackReplace = replacement.getSelected();
                if (stackReplace != null) {
                    Block replacementBlock = Block.byItem(stackReplace.getItem());
                    if (!LittleAction.isBlockValid(replacementBlock.defaultBlockState())) {
                        GuiDialogHandler.openDialog(getIntegratedParent(), "screwdriver_dialog", Component.translatable("dialog.screwdriver.invalid_replacement"), (x, y) -> {},
                            DialogButton.OK);
                        return null;
                    }
                    actions.add(new LittleActionDestroyBoxes(level, boxes));
                    LittleGroupAbsolute previews = new LittleGroupAbsolute(boxes.pos);
                    previews.add(boxes.grid, new LittleElement(replacementBlock.defaultBlockState(), ColorUtils.WHITE), boxes);
                    
                    actions.add(new LittleActionDestroyBoxes(level, boxes));
                    actions.add(new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.absolute(level, PlacementMode.FILL, previews)));
                }
            }
            
            if (colorize) {
                GuiColorPicker picker = get("picker");
                actions.add(new LittleActionColorBoxes(level, boxes, picker.color.toInt(), false));
            }
            
            if (!actions.isEmpty())
                return new LittleActions(actions.toArray(new LittleAction[0]));
        }
        
        if (!remove && !replace && !colorize)
            GuiDialogHandler.openDialog(getIntegratedParent(), "screwdriver_dialog", Component.translatable("dialog.screwdriver.no_task"), (x, y) -> {}, DialogButton.OK);
        
        return null;
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        if (isClient()) {
            lastSelectedFilter = filter.get();
            lastSelectedReplaceStack = ((GuiStackSelector) get("replacement")).getSelected();
        }
        GuiComboBox<SelectionMode> box = get("selection_mode");
        data.set(LittleTilesRegistry.SELECTION.get(), SelectionComponent.getOrDefault(tool.get()).withMode(box.selected(SelectionMode.REGISTRY.getDefault())));
        return true;
    }
}
