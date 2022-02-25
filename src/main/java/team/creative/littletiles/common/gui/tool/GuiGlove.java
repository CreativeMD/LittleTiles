package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.ItemLittleGlove.GloveMode;

public abstract class GuiGlove extends GuiConfigure {
    
    public final GloveMode mode;
    public final GloveMode before;
    public final GloveMode after;
    public LittleGrid grid;
    
    public GuiGlove(GloveMode mode, ContainerSlotView view, int width, int height, LittleGrid grid) {
        super("glove", width, height, view);
        this.mode = mode;
        this.grid = grid;
        List<GloveMode> modes = new ArrayList<>(ItemLittleGlove.MODES.values());
        int index = modes.indexOf(mode);
        this.before = index == 0 ? modes.get(modes.size() - 1) : modes.get(index - 1);
        this.after = index == modes.size() - 1 ? modes.get(0) : modes.get(index + 1);
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        ItemLittleGlove.setMode(tool.get(), mode);
        return null;
    }
    
    public void openNewGui(GloveMode mode) {
        ItemLittleGlove.setMode(tool.get(), mode);
        GuiHandler.openGui("configure", new CompoundTag(), getPlayer());
    }
    
    @Override
    public void create() {
        GuiParent upperBar = new GuiParent(GuiFlow.STACK_X);
        add(upperBar.setExpandableX());
        upperBar.add(new GuiButton("<<", x -> openNewGui(before)).setTranslate("gui.previous"));
        upperBar.add(new GuiLabel("name").setTranslate(mode.title).setExpandableX());
        upperBar.add(new GuiButton(">>", x -> openNewGui(after)).setTranslate("gui.previous"));
    }
    
}
