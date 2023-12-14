package team.creative.littletiles.common.gui.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiGridConfig;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public abstract class GuiModeSelector extends GuiConfigure {
    
    public LittleGrid grid;
    public PlacementMode mode;
    
    public GuiModeSelector(ContainerSlotView view, LittleGrid grid, PlacementMode mode) {
        super("mode-selector", 150, 150, view);
        this.grid = grid;
        this.mode = mode;
        registerEventChanged(x -> {
            if (x.control.is("mode")) {
                TextBuilder builder = new TextBuilder();
                if (getMode().canPlaceStructures())
                    builder.text("" + ChatFormatting.BOLD).translate("placement.mode.placestructure").text("" + ChatFormatting.WHITE).newLine();
                builder.translate(getMode().translatableKey() + ".tooltip");
                ((GuiLabel) get("text")).setTitle(builder.build());
            }
        });
    }
    
    @Override
    public void create() {
        GuiParent place = new GuiParent(GuiFlow.STACK_Y);
        add(place);
        GuiComboBoxMapped<PlacementMode> box = new GuiComboBoxMapped<>("mode", PlacementMode.map());
        box.select(mode);
        place.add(box);
        place.add(new GuiLabel("text"));
        GuiGridConfig gridBox = new GuiGridConfig("grid", getPlayer(), grid, null);
        gridBox.select(grid);
        add(gridBox);
        raiseEvent(new GuiControlChangedEvent(box));
    }
    
    public PlacementMode getMode() {
        GuiComboBoxMapped<PlacementMode> box = get("mode");
        return box.getSelected(PlacementMode.getDefault());
    }
    
    public abstract CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, PlacementMode mode);
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        mode = getMode();
        grid = get("grid", GuiGridConfig.class).get();
        return saveConfiguration(nbt, grid, mode);
    }
    
}
