package team.creative.littletiles.common.gui.tool;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiArraySlider;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.placement.selection.SelectionMode;
import team.creative.littletiles.common.placement.selection.SelectionMode.SelectionResult;

public class GuiRecipeSelection extends GuiConfigure {
    
    public SelectionResult result;
    
    public final GuiSyncLocal<CompoundTag> SAVE_SELECTION = getSyncHolder().register("save_selection", nbt -> {
        ItemStack stack = tool.get();
        SelectionMode mode = ItemLittleBlueprint.getSelectionMode(stack);
        try {
            LittleGroup previews = mode.getGroup(getPlayer().level, getPlayer(), stack, nbt.getBoolean("includeVanilla"), nbt.getBoolean("includeCB"), nbt
                    .getBoolean("includeLT"), nbt.getBoolean("remember_structure"));
            if (nbt.contains("grid")) {
                LittleGrid grid = LittleGrid.get(nbt.getInt("grid"));
                previews.convertTo(grid);
                LittleGrid aimedGrid = LittleGrid.get(nbt.getInt("aimedGrid"));
                if (aimedGrid.count > grid.count)
                    LittleGroup.setGridSecretly(previews, aimedGrid);
                else
                    LittleGroup.advancedScale(previews, aimedGrid.count, grid.count);
                previews.combineBlockwise();
            }
            
            previews.removeOffset();
            
            ((ItemLittleBlueprint) stack.getItem()).saveTiles(stack, previews);
            mode.clear(stack);
            
            tool.changed();
            GuiCreator.ITEM_OPENER.open(getPlayer(), InteractionHand.MAIN_HAND);
        } catch (LittleActionException e) {
            GuiDialogHandler.openDialog(getParent(), "info", Component.translatable("gui.ok"), (x, y) -> {}, DialogButton.OK);
            return;
        }
    });
    
    public GuiRecipeSelection(ContainerSlotView view) {
        super("recipe_selection", 200, 200, view);
        flow = GuiFlow.STACK_Y;
        
        registerEventChanged(x -> {
            if (!x.control.is("scale"))
                updateSlider();
        });
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        return null;
    }
    
    @Override
    public void create() {
        ItemStack stack = tool.get();
        SelectionMode mode = ItemLittleBlueprint.getSelectionMode(stack);
        GuiComboBoxMapped<SelectionMode> box = new GuiComboBoxMapped<>("selection_mode", new TextMapBuilder<SelectionMode>()
                .addEntrySet(SelectionMode.REGISTRY.entrySet(), x -> x.getValue().getTranslation()));
        box.select(mode);
        add(box.setExpandableX());
        
        result = mode.generateResult(getPlayer().level, stack);
        
        GuiCheckBox vanilla = new GuiCheckBox("includeVanilla", false).setTranslate("selection.include.vanilla");
        if (result != null && result.blocks > 0)
            vanilla.setTooltip(new TextBuilder().text(result.blocks + " ").translate("selection.blocks").build());
        else
            vanilla.enabled = false;
        add(vanilla);
        
        GuiCheckBox cb = new GuiCheckBox("includeCB", true).setTranslate("selection.include.cb");
        if (result != null && result.cbBlocks > 0)
            cb.setTooltip(new TextBuilder().text(result.cbBlocks + " ").translate("gui.blocks").text(", " + result.cbTiles + " ").translate("gui.tiles")
                    .text(", " + result.minCBGrid.count + " ").translate("gui.grid").build());
        else
            cb.enabled = false;
        add(cb);
        
        GuiCheckBox lt = new GuiCheckBox("includeLT", true).setTranslate("selection.include.lt");
        if (result != null && result.ltBlocks > 0)
            cb.setTooltip(new TextBuilder().text(result.ltBlocks + " ").translate("gui.blocks").text(", " + result.ltTiles + " ").translate("gui.tiles")
                    .text(", " + result.minLtGrid.count + " ").translate("gui.grid").build());
        else
            lt.enabled = false;
        add(lt);
        
        add(new GuiCheckBox("remember_structure", true).setTranslate("selection.include.structure"));
        // accurate
        GuiParent scale = new GuiParent(GuiFlow.STACK_X);
        add(scale);
        
        GuiLabel label = new GuiLabel("label_scale").setTitle(Component.translatable("selection.scale").append(": "));
        scale.add(label);
        scale.add(new GuiArraySlider("scale").setExpandableX());
        updateSlider();
        
        GuiParent bottom = new GuiParent(GuiFlow.STACK_X).setAlign(Align.RIGHT);
        add(bottom.setExpandableX());
        bottom.add(new GuiButton("save", x -> {
            boolean rememberStructure = ((GuiCheckBox) get("remember_structure")).value;
            boolean includeVanilla = ((GuiCheckBox) get("includeVanilla")).value;
            boolean includeCB = ((GuiCheckBox) get("includeCB")).value;
            boolean includeLT = ((GuiCheckBox) get("includeLT")).value;
            
            try {
                if (rememberStructure && mode.getGroup(getPlayer().level, getPlayer(), stack, includeVanilla, includeCB, includeLT, rememberStructure).isEmpty()) {
                    GuiDialogHandler.openDialog(this, "no_tiles", Component.translatable("selection.no_tiles"), (g, b) -> {}, DialogButton.OK);
                    return;
                }
            } catch (LittleActionException e) {
                GuiDialogHandler.openDialog(getParent(), "info", Component.translatable("gui.ok"), (g, b) -> {}, DialogButton.OK);
                return;
            }
            
            mode.save(stack);
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("save_selection", true);
            nbt.putBoolean("includeVanilla", includeVanilla);
            nbt.putBoolean("includeCB", includeCB);
            nbt.putBoolean("includeLT", includeLT);
            nbt.putBoolean("remember_structure", rememberStructure);
            
            LittleGrid minRequired = LittleGrid.min();
            if (nbt.getBoolean("includeCB") && result.minCBGrid != null)
                minRequired = LittleGrid.max(minRequired, result.minCBGrid);
            if (nbt.getBoolean("includeLT") && result.minLtGrid != null)
                minRequired = LittleGrid.max(minRequired, result.minLtGrid);
            LittleGrid selected = LittleGrid.getGrids()[LittleGrid.getGrids().length - 1 - ((GuiArraySlider) get("scale")).getValue()];
            if (minRequired != selected) {
                nbt.putInt("grid", minRequired.count);
                nbt.putInt("aimedGrid", selected.count);
            }
            
            SAVE_SELECTION.send(nbt);
        }).setTranslate("gui.save").setEnabled(result != null));
    }
    
    public void updateSlider() {
        GuiArraySlider slider = (GuiArraySlider) get("scale");
        boolean includeVanilla = ((GuiCheckBox) get("includeVanilla")).enabled && ((GuiCheckBox) get("includeVanilla")).value;
        boolean includeCB = ((GuiCheckBox) get("includeCB")).enabled && ((GuiCheckBox) get("includeCB")).value;
        boolean includeLT = ((GuiCheckBox) get("includeLT")).enabled && ((GuiCheckBox) get("includeLT")).value;
        
        if (result == null || (!includeVanilla && !includeCB && !includeLT))
            slider.setEnabled(false);
        else {
            LittleGrid minRequired = LittleGrid.min();
            if (includeCB && result.minCBGrid != null)
                minRequired = LittleGrid.max(minRequired, result.minCBGrid);
            if (includeLT && result.minLtGrid != null)
                minRequired = LittleGrid.max(minRequired, result.minLtGrid);
            
            String value = slider.get();
            
            String[] values = new String[LittleGrid.getGrids().length];
            for (LittleGrid context : LittleGrid.getGrids())
                values[values.length - 1 - context.getIndex()] = minRequired.count + ":" + context.count + " x" + (context.pixelLength / minRequired.pixelLength) + "";
            slider.setValues(values);
            if (ArrayUtils.contains(values, value))
                slider.select(value);
            else
                slider.select(values[values.length - 1 - minRequired.getIndex()]);
            slider.setEnabled(true);
        }
    }
}
