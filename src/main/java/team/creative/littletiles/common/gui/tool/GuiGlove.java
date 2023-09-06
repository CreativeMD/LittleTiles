package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.glove.GloveMode;

public class GuiGlove extends GuiConfigure {
    
    private GloveMode mode;
    private GloveMode before;
    private GloveMode after;
    public LittleGrid grid;
    
    public GuiGlove(GloveMode mode, ContainerSlotView view, int width, int height, LittleGrid grid) {
        super("glove", width, height, view);
        this.grid = grid;
        loadMode(mode);
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        ItemLittleGlove.setMode(nbt, mode);
        mode.saveGui(this, nbt);
        return nbt;
    }
    
    public void loadMode(GloveMode mode) {
        List<GloveMode> modes = new ArrayList<>(GloveMode.REGISTRY.values());
        this.mode = mode;
        int index = modes.indexOf(mode);
        this.before = index == 0 ? modes.get(modes.size() - 1) : modes.get(index - 1);
        this.after = index == modes.size() - 1 ? modes.get(0) : modes.get(index + 1);
        
        if (getParent() != null) {
            clear();
            clearEvents();
            create();
            reflow();
        }
    }
    
    @Override
    public void create() {
        flow = GuiFlow.STACK_Y;
        GuiParent upperBar = new GuiParent(GuiFlow.STACK_X);
        add(upperBar.setVAlign(VAlign.CENTER).setExpandableX());
        upperBar.add(new GuiButton("<<", x -> loadMode(before)).setTranslate("gui.previous"));
        upperBar.add(new GuiLabel("name").setTitle(mode.translatable()).setAlign(Align.CENTER).setExpandableX());
        upperBar.add(new GuiButton(">>", x -> loadMode(after)).setTranslate("gui.next"));
        
        mode.loadGui(this);
    }
    
}
