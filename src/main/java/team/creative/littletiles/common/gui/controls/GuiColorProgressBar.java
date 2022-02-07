package team.creative.littletiles.common.gui.controls;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.simple.GuiProgressbar;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.type.Color;

public class GuiColorProgressBar extends GuiProgressbar {
    
    public Color color;
    
    public GuiColorProgressBar(String name, double max, double pos, Color color) {
        super(name, max, pos);
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
    
    @Override
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        GuiRenderHelper.colorRect(pose, 0, 0, (int) rect.getWidth(), (int) rect.getHeight(), color.toInt());
        super.renderContent(pose, control, rect, mouseX, mouseY);
    }
    
}
