package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionColorBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.level.LittleLevelScanner;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.PlacementPreview;

public class GuiScrewdriver extends GuiConfigure {
    
    public static ItemStack lastSelectedSearchStack;
    public static ItemStack lastSelectedReplaceStack;
    
    public GuiScrewdriver(ContainerSlotView view) {
        super("screwdriver", 200, 205, view);
        flow = GuiFlow.STACK_Y;
    }
    
    @Override
    public void create() {
        tool.get().getOrCreateTag();
        
        add(new GuiCheckBox("any", false).setTranslate("gui.any"));
        GuiStackSelector selector = new GuiStackSelector("filter", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
        if (lastSelectedSearchStack != null)
            selector.setSelectedForce(lastSelectedSearchStack);
        add(selector.setExpandableX());
        
        add(new GuiCheckBox("remove", false).setTranslate("gui.remove"));
        
        add(new GuiCheckBox("replace", false).setTranslate("gui.replace_with"));
        
        selector = new GuiStackSelector("replacement", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
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
                getPlayer().sendMessage(new TextComponent(e.getLocalizedMessage()), Util.NIL_UUID);
            }
        }).setTranslate("gui.undo")).addLeft(new GuiButton("redo", x -> {
            try {
                LittleTilesClient.ACTION_HANDLER.redo();
            } catch (LittleActionException e) {
                getPlayer().sendMessage(new TextComponent(e.getLocalizedMessage()), Util.NIL_UUID);
            }
        }).setTranslate("gui.redo")).addRight(new GuiButton("run", x -> {
            LittleAction action = getDesiredAction();
            if (action != null)
                if (action.wasSuccessful(LittleTilesClient.ACTION_HANDLER.execute(action)))
                    playSound(SoundEvents.LEVER_CLICK);
        }).setTranslate("gui.run"));
        add(actions);
    }
    
    public LittleAction getDesiredAction() {
        int[] array = tool.get().getOrCreateTag().getIntArray("pos1");
        if (array.length != 3)
            return null;
        BlockPos pos = new BlockPos(array[0], array[1], array[2]);
        array = tool.get().getOrCreateTag().getIntArray("pos2");
        if (array.length != 3)
            return null;
        BlockPos pos2 = new BlockPos(array[0], array[1], array[2]);
        
        BiFilter<IParentCollection, LittleTile> filter;
        if (((GuiCheckBox) get("any")).value)
            filter = TileFilters.and();
        else {
            GuiStackSelector selector = (GuiStackSelector) get("filter");
            Block filterBlock = Block.byItem(selector.getSelected().getItem());
            if (filterBlock != null && !(filterBlock instanceof AirBlock))
                filter = TileFilters.block(filterBlock);
            else
                filter = TileFilters.and();
        }
        
        filter = TileFilters.and(TileFilters.noStructure(), filter);
        
        Level level = getPlayer().level;
        LittleBoxes boxes = LittleLevelScanner.scan(level, pos, pos2, filter);
        
        if (boxes.isEmpty())
            return null;
        
        boolean remove = ((GuiCheckBox) get("remove")).value;
        boolean replace = ((GuiCheckBox) get("replace")).value;
        boolean colorize = ((GuiCheckBox) get("colorize")).value;
        
        if (remove)
            return new LittleActionDestroyBoxes(level, boxes);
        else {
            List<LittleAction> actions = new ArrayList<>();
            
            if (replace) {
                GuiStackSelector replacement = (GuiStackSelector) get("replacement");
                ItemStack stackReplace = replacement.getSelected();
                if (stackReplace != null) {
                    Block replacementBlock = Block.byItem(stackReplace.getItem());
                    if (!LittleAction.isBlockValid(replacementBlock)) {
                        GuiDialogHandler.openDialog(this, "screwdriver_dialog", new TranslatableComponent("dialog.screwdriver.invalid_replacement"), (x, y) -> {}, DialogButton.OK);
                        return null;
                    }
                    actions.add(new LittleActionDestroyBoxes(level, boxes));
                    LittleGroupAbsolute previews = new LittleGroupAbsolute(pos, LittleGrid.min());
                    previews.add(new LittleElement(replacementBlock.defaultBlockState(), ColorUtils.WHITE), boxes);
                    
                    actions.add(new LittleActionDestroyBoxes(level, boxes));
                    actions.add(new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.absolute(level, ItemStack.EMPTY, previews, Facing.EAST)));
                }
            }
            
            if (colorize) {
                GuiColorPicker picker = (GuiColorPicker) get("picker");
                actions.add(new LittleActionColorBoxes(level, boxes, picker.color.toInt(), false));
            }
            
            if (!actions.isEmpty())
                return new LittleActions(actions.toArray(new LittleAction[0]));
        }
        
        if (!remove && !replace && !colorize)
            GuiDialogHandler.openDialog(this, "screwdriver_dialog", new TranslatableComponent("dialog.screwdriver.no_task"), (x, y) -> {}, DialogButton.OK);
        
        return null;
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        if (isClient()) {
            lastSelectedSearchStack = ((GuiStackSelector) get("filter")).getSelected();
            lastSelectedReplaceStack = ((GuiStackSelector) get("replacement")).getSelected();
        }
        return null;
    }
}
