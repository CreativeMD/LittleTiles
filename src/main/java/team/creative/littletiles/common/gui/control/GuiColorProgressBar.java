package team.creative.littletiles.common.gui.control;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.control.simple.GuiProgressbar;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.type.Color;

public class GuiColorProgressBar extends GuiProgressbar {
    
    public Color color;
    
    public GuiColorProgressBar(String name, double pos, double max, Color color) {
        super(name, pos, max);
        this.color = color;
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = super.getTooltip();
        if (tooltip != null)
            tooltip.add(Component.translatable("gui.color.rightclick"));
        return tooltip;
    }
    
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 1) {
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        return false;
    }
    
    @Override
    protected void renderProgress(GuiGraphics graphics, double percent) {
        GuiRenderHelper.colorRect(graphics, 0, 0, (int) (rect.getContentWidth() * percent), rect.getContentHeight(), color.toInt());
    }
    
}
