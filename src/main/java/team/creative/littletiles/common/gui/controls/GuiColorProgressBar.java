package team.creative.littletiles.common.gui.controls;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.common.gui.controls.simple.GuiProgressbar;
import team.creative.creativecore.common.util.math.geo.Rect;

public class GuiColorProgressBar extends GuiProgressbar {
    
    public GuiColorProgressBar(String name, double max, double pos) {
        super(name, max, pos);
    }
    
    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = super.getTooltip();
        if (tooltip != null)
            tooltip.add(new TranslatableComponent("gui.color.rightclick"));
        return tooltip;
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (button == 1) {
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        return false;
    }
    
}
