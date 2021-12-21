package team.creative.littletiles.common.gui.configure;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public abstract class GuiModeSelector extends GuiConfigure {
    
    public LittleGrid grid;
    public PlacementMode mode;
    
    public GuiModeSelector(ItemStack stack, LittleGrid grid, PlacementMode mode) {
        super("mode-selector", 150, 150, stack);
        this.grid = grid;
        this.mode = mode;
        registerEventChanged(x -> {
            if (x.control.is("mode")) {
                TextBuilder builder = new TextBuilder();
                if (getMode().canPlaceStructures())
                    builder.text("" + ChatFormatting.BOLD).translate("placement.mode.placestructure").text("" + ChatFormatting.WHITE).newLine();
                builder.translate(getMode().name + ".tooltip");
                ((GuiLabel) get("text")).setTitle(builder.build());
            }
        });
    }
    
    @Override
    public void create() {
        GuiComboBoxMapped<PlacementMode> box = new GuiComboBoxMapped<>("mode", PlacementMode.map());
        box.select(mode);
        add(box);
        add(new GuiLabel("text"));
        GuiComboBoxMapped<LittleGrid> gridBox = new GuiComboBoxMapped<>("grid", LittleGrid.mapBuilder());
        gridBox.select(grid);
        add(gridBox);
        raiseEvent(new GuiControlChangedEvent(box));
    }
    
    public PlacementMode getMode() {
        GuiComboBoxMapped<PlacementMode> box = (GuiComboBoxMapped<PlacementMode>) get("mode");
        return box.getSelected(PlacementMode.getDefault());
    }
    
    public abstract CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, PlacementMode mode);
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        mode = getMode();
        GuiComboBoxMapped<LittleGrid> gridBox = (GuiComboBoxMapped<LittleGrid>) get("grid");
        grid = gridBox.getSelected(LittleGrid.defaultGrid());
        return saveConfiguration(nbt, grid, mode);
    }
    
}
