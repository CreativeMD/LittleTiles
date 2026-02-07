package team.creative.littletiles.common.gui.tool.blueprint;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.simple.GuiArraySlider;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.placement.selection.SelectionMode;
import team.creative.littletiles.common.placement.selection.SelectionParameters;
import team.creative.littletiles.common.placement.selection.SelectionScanResult;

public class GuiBlueprintSelection extends GuiConfigure {
    
    public SelectionScanResult result;
    
    public final GuiSyncLocal<CompoundTag> SAVE_SELECTION = getSyncHolder().register("save_selection", nbt -> {
        
        try {
            SelectionMode mode = SelectionMode.REGISTRY.get(nbt.getString("mode"));
            SelectionParameters selection = new SelectionParameters(nbt.getBoolean("includeVanilla"), nbt.getBoolean("includeCB"), nbt.getBoolean("includeLT"), nbt.getBoolean(
                "remember_structure"));
            SelectionComponent component = SelectionComponent.of(mode, nbt.getCompound("sel_config"));
            tool.get().set(LittleTilesRegistry.SELECTION, component);
            LittleGroup previews = mode.select(getPlayer().level(), (LittleActionSource) getPlayer(), selection, tool.get(), component);
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
            
            ((ItemLittleBlueprint) tool.get().getItem()).saveTiles(tool.get(), previews);
            tool.get().remove(LittleTilesRegistry.SELECTION);
            tool.get().remove(LittleTilesRegistry.MATRIX);
            tool.changed();
            LittleTilesGuiRegistry.OPEN_CONFIG.open(getPlayer());
        } catch (LittleActionException e) {
            GuiDialogHandler.openDialog(getIntegratedParent(), "info", Component.translatable("gui.ok"), (x, y) -> {}, DialogButton.OK);
            return;
        }
    });
    
    public final GuiSyncLocal<EndTag> CLEAR_SELECTION = getSyncHolder().register("clear_selection", x -> {
        tool.get().remove(LittleTilesRegistry.SELECTION);
        tool.changed();
        LittleTilesGuiRegistry.OPEN_CONFIG.open(getPlayer());
    });
    
    public GuiBlueprintSelection(ContainerSlotView view) {
        super("blueprint_selection", 200, 200, view);
        flow = GuiFlow.STACK_Y;
        
        registerEventChanged(x -> {
            if (!x.control.is("scale"))
                updateSlider();
        });
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        GuiComboBox<SelectionMode> box = get("selection_mode");
        data.set(LittleTilesRegistry.SELECTION.get(), SelectionComponent.getOrDefault(tool.get()).withMode(box.selected(SelectionMode.REGISTRY.getDefault())));
        return false; // Needs to be changed to true for more selection modes
    }
    
    @Override
    public void create() {
        ItemStack stack = tool.get();
        SelectionComponent component = SelectionComponent.getOrDefault(stack);
        GuiComboBox<SelectionMode> box = new GuiComboBox<>("selection_mode", new TextMapBuilder<SelectionMode>().addEntrySet(SelectionMode.REGISTRY.entrySet(), x -> x.getValue()
                .getTranslation()));
        box.select(component.mode);
        add(box.setExpandableX());
        
        result = component.mode.scan(getPlayer().level(), stack, component);
        
        GuiCheckBox vanilla = new GuiCheckBox("includeVanilla", false).setTranslate("selection.include.vanilla");
        if (result != null && result.hasBlocks())
            vanilla.setTooltip(result.blockInfo());
        else
            vanilla.enabled = false;
        add(vanilla);
        
        GuiCheckBox cb = new GuiCheckBox("includeCB", true).setTranslate("selection.include.cb");
        if (result != null && result.hasCB())
            cb.setTooltip(result.cbInfo());
        else
            cb.enabled = false;
        add(cb);
        
        GuiCheckBox lt = new GuiCheckBox("includeLT", true).setTranslate("selection.include.lt");
        if (result != null && result.hasTiles())
            cb.setTooltip(result.ltInfo());
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
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom.setAlign(Align.RIGHT).setExpandableX());
        bottom.addRight(new GuiButton("clear", x -> {
            GuiDialogHandler.openDialog(getIntegratedParent(), "clear_sekection", Component.translatable("gui.selection.dialog.clear"), (g, b) -> {
                if (b == DialogButton.YES)
                    CLEAR_SELECTION.send(EndTag.INSTANCE);
            }, DialogButton.NO, DialogButton.YES);
        }).setTranslate("selection.clear"));
        bottom.addRight(new GuiButton("save", x -> {
            SelectionParameters selection = new SelectionParameters(get("includeVanilla", GuiCheckBox.class).value, get("includeCB", GuiCheckBox.class).value, get("includeLT",
                GuiCheckBox.class).value, get("remember_structure", GuiCheckBox.class).value);
            
            SelectionMode mode = box.selected(SelectionMode.REGISTRY.getDefault());
            
            try {
                if (selection.rememberStructure() && mode.select(getPlayer().level(), (LittleActionSource) getPlayer(), selection, stack, component).isEmptyIncludeChildren()) {
                    GuiDialogHandler.openDialog(getIntegratedParent(), "no_tiles", Component.translatable("selection.no_tiles"), (g, b) -> {}, DialogButton.OK);
                    return;
                }
            } catch (LittleActionException e) {
                GuiDialogHandler.openDialog(getIntegratedParent(), "info", Component.translatable("gui.ok"), (g, b) -> {}, DialogButton.OK);
                return;
            }
            
            CompoundTag nbt = new CompoundTag();
            nbt.putString("mode", mode.getName());
            nbt.put("sel_config", component.getConfig());
            nbt.putBoolean("includeVanilla", selection.includeVanilla());
            nbt.putBoolean("includeCB", selection.includeCB());
            nbt.putBoolean("includeLT", selection.includeLT());
            nbt.putBoolean("remember_structure", selection.rememberStructure());
            
            LittleGrid minRequired = result.minGrid(selection.includeLT(), selection.includeCB());
            LittleGrid selected = LittleGrid.gridByIndex(LittleGrid.gridCount() - 1 - ((GuiArraySlider) get("scale")).getIntValue());
            if (minRequired != selected) {
                nbt.putInt("grid", minRequired.count);
                nbt.putInt("aimedGrid", selected.count);
            }
            
            SAVE_SELECTION.send(nbt);
        }).setTranslate("gui.save").setEnabled(result != null));
    }
    
    public void updateSlider() {
        GuiArraySlider slider = (GuiArraySlider) get("scale");
        boolean includeVanilla = get("includeVanilla", GuiCheckBox.class).enabled && get("includeVanilla", GuiCheckBox.class).value;
        boolean includeCB = get("includeCB", GuiCheckBox.class).enabled && get("includeCB", GuiCheckBox.class).value;
        boolean includeLT = get("includeLT", GuiCheckBox.class).enabled && get("includeLT", GuiCheckBox.class).value;
        
        if (result == null || (!includeVanilla && !includeCB && !includeLT))
            slider.setEnabled(false);
        else {
            LittleGrid minRequired = result.minGrid(includeLT, includeCB);
            String value = slider.get();
            
            String[] values = new String[LittleGrid.gridCount()];
            for (LittleGrid context : LittleGrid.grids())
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
