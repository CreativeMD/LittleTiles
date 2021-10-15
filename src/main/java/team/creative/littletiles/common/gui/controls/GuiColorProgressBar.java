package team.creative.littletiles.common.gui.controls;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.controls.gui.GuiProgressBar;

import net.minecraft.init.SoundEvents;

public class GuiColorProgressBar extends GuiProgressBar {
    
    public GuiColorProgressBar(String name, int x, int y, int width, int height, double max, double pos) {
        super(name, x, y, width, height, max, pos);
    }
    
    @Override
    public ArrayList<String> getTooltip() {
        ArrayList<String> tooltip = super.getTooltip();
        if (tooltip != null)
            tooltip.add(translate("gui.color.rightclick"));
        
        return tooltip;
    }
    
    @Override
    public boolean mousePressed(int x, int y, int button) {
        if (button == 1) {
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        return false;
    }
    
}
