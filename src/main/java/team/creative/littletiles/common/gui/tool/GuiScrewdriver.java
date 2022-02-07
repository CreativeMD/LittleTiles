package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.selection.selector.AndSelector;
import com.creativemd.littletiles.common.util.selection.selector.AnySelector;
import com.creativemd.littletiles.common.util.selection.selector.NoStructureSelector;
import com.creativemd.littletiles.common.util.selection.selector.StateSelector;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;
import com.creativemd.littletiles.common.util.selection.selector.TileSelectorBlock;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionColorBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.mode.PlacementMode;

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
                if (LittleTilesClient.ACTION_HANDLER.execute(action))
                    playSound(SoundEvents.LEVER_CLICK);
        }).setTranslate("gui.run"));
        add(actions);
    }
    
    public LittleAction getDesiredAction() {
        int[] array = stack.getTagCompound().getIntArray("pos1");
        if (array.length != 3)
            return null;
        BlockPos pos = new BlockPos(array[0], array[1], array[2]);
        array = stack.getTagCompound().getIntArray("pos2");
        if (array.length != 3)
            return null;
        BlockPos pos2 = new BlockPos(array[0], array[1], array[2]);
        
        TileSelector selector;
        if (((GuiCheckBox) get("any")).value)
            selector = new AnySelector();
        else {
            GuiStackSelector filter = (GuiStackSelector) get("filter");
            ItemStack stackFilter = filter.getSelected();
            Block filterBlock = Block.getBlockFromItem(stackFilter.getItem());
            boolean meta = ((GuiCheckBox) get("meta")).value;
            selector = meta ? new StateSelector(BlockUtils.getState(filterBlock, stackFilter.getMetadata())) : new TileSelectorBlock(filterBlock);
        }
        
        selector = new AndSelector(new NoStructureSelector(), selector);
        
        LittleBoxes boxes = TileSelector.getAbsoluteBoxes(getPlayer().world, pos, pos2, selector);
        
        if (boxes.isEmpty())
            return null;
        
        boolean remove = ((GuiCheckBox) get("remove")).value;
        boolean replace = ((GuiCheckBox) get("replace")).value;
        boolean colorize = ((GuiCheckBox) get("colorize")).value;
        
        if (remove)
            return new LittleActionDestroyBoxes(boxes);
        else {
            List<LittleAction> actions = new ArrayList<>();
            
            if (replace) {
                GuiStackSelectorAll replacement = (GuiStackSelectorAll) get("replacement");
                ItemStack stackReplace = replacement.getSelected();
                if (stackReplace != null) {
                    Block replacementBlock = Block.getBlockFromItem(stackReplace.getItem());
                    if (!LittleAction.isBlockValid(BlockUtils.getState(replacementBlock, stackReplace.getMetadata()))) {
                        openButtonDialogDialog("Invalid replacement block!", "ok");
                        return null;
                    }
                    actions.add(new LittleActionDestroyBoxes(boxes));
                    LittleAbsolutePreviews previews = new LittleAbsolutePreviews(pos, LittleGridContext.getMin());
                    for (LittleBox box : boxes.all()) {
                        LittleTile tile = new LittleTile(replacementBlock, stackReplace.getMetadata());
                        tile.setBox(box);
                        previews.addPreview(pos, tile.getPreviewTile(), boxes.context);
                    }
                    actions.add(new LittleActionDestroyBoxes(boxes));
                    actions.add(new LittleActionPlaceAbsolute(previews, PlacementMode.all, false));
                }
            }
            
            if (colorize) {
                GuiColorPicker picker = (GuiColorPicker) get("picker");
                actions.add(new LittleActionColorBoxes(boxes, ColorUtils.RGBAToInt(picker.color), false));
            }
            
            if (!actions.isEmpty())
                return new LittleActions(actions.toArray(new LittleAction[0]));
        }
        
        if (!remove && !replace && !colorize)
            openButtonDialogDialog("You have to select a task!", "ok");
        
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
