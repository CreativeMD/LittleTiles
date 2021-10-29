package team.creative.littletiles.common.gui;

import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.ItemLittleGlove.GrabberMode;

public abstract class SubGuiGrabber extends GuiConfigure {
    
    public final GrabberMode mode;
    public final int index;
    public final GrabberMode[] modes;
    public LittleGridContext context;
    
    public SubGuiGrabber(GrabberMode mode, ItemStack stack, int width, int height, LittleGridContext context) {
        super(width, height, stack);
        this.mode = mode;
        this.modes = ItemLittleGlove.getModes();
        this.index = ItemLittleGlove.indexOf(mode);
        this.context = context;
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        ItemLittleGlove.setMode(stack, mode);
        saveConfiguration();
        sendPacketToServer(stack.getTagCompound());
    }
    
    public void openNewGui(GrabberMode mode) {
        ItemLittleGlove.setMode(stack, mode);
        GuiHandler.openGui("grabber", new NBTTagCompound(), getPlayer());
    }
    
    @Override
    public void createControls() {
        controls.add(new GuiButton("<<", 0, 0, 10) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                int newIndex = index - 1;
                if (newIndex < 0)
                    newIndex = modes.length - 1;
                openNewGui(modes[newIndex]);
            }
            
        });
        
        controls.add(new GuiButton(">>", 124, 0, 10) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                int newIndex = index + 1;
                if (newIndex >= modes.length)
                    newIndex = 0;
                openNewGui(modes[newIndex]);
            }
            
        });
        
        controls.add(new GuiLabel(mode.getLocalizedName(), 20, 3));
    }
    
}
