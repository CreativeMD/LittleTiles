package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.ItemLittleGlove.GrabberMode;

public abstract class GuiGlove extends GuiConfigure {
    
    public final GrabberMode mode;
    public final int index;
    public final GrabberMode[] modes;
    public LittleGrid grid;
    
    public GuiGlove(GrabberMode mode, ContainerSlotView view, int width, int height, LittleGrid grid) {
        super("glove", width, height, view);
        this.mode = mode;
        this.modes = ItemLittleGlove.getModes();
        this.index = ItemLittleGlove.indexOf(mode);
        this.grid = grid;
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        ItemLittleGlove.setMode(tool.get(), mode);
        return null;
    }
    
    public void openNewGui(GrabberMode mode) {
        ItemLittleGlove.setMode(tool.get(), mode);
        GuiHandler.openGui("configure", new CompoundTag(), getPlayer());
    }
    
    @Override
    public void create() {
        add(new GuiButton("<<", x -> {
            int newIndex = index - 1;
            if (newIndex < 0)
                newIndex = modes.length - 1;
            openNewGui(modes[newIndex]);
        }).setTranslate("gui.previous"));
        
        add(new GuiButton(">>", x -> {
            int newIndex = index + 1;
            if (newIndex >= modes.length)
                newIndex = 0;
            openNewGui(modes[newIndex]);
        }).setTranslate("gui.previous"));
        
        add(new GuiLabel("name").setTranslate(mode.title));
    }
    
}
