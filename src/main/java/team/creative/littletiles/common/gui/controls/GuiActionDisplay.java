package team.creative.littletiles.common.gui.controls;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;

public class GuiActionDisplay extends GuiControl {
    
    protected List<ActionMessage> messages = new ArrayList<>();
    
    private int maxActions;
    private int displayTime;
    private int fadeTime;
    private int totalTime;
    
    public GuiActionDisplay(String name) {
        super(name);
        setTimer(5, 1);
        setMessageCount(6);
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.TRANSPARENT;
    }
    
    @Override
    public void flowX(int width, int preferred) {
        for (int i = 0; i < messages.size(); i++)
            messages.get(i).setDimension(width, Integer.MAX_VALUE);
    }
    
    @Override
    public void flowY(int width, int height, int preferred) {}
    
    @Override
    public int getMinWidth() {
        return 10;
    }
    
    @Override
    public int preferredWidth() {
        int max = 0;
        for (int i = 0; i < messages.size(); i++)
            max = Math.max(max, messages.get(i).getTotalWidth());
        return max;
    }
    
    @Override
    public int getMinHeight(int width) {
        return Minecraft.getInstance().font.lineHeight;
    }
    
    @Override
    public int preferredHeight(int width) {
        int height = 0;
        for (int i = 0; i < messages.size(); i++)
            height += messages.get(i).getTotalHeight();
        return height;
    }
    
    public GuiActionDisplay setMessageCount(int count) {
        this.maxActions = count;
        List<ActionMessage> newMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (i >= count)
                break;
            newMessages.add(messages.get(i));
        }
        this.messages = newMessages;
        return this;
    }
    
    public GuiActionDisplay setTimer(int displaySeconds, int fadeSeconds) {
        this.displayTime = displaySeconds * 1000;
        this.fadeTime = fadeSeconds * 1000;
        this.totalTime = displayTime + fadeTime;
        return this;
    }
    
    public void addMessage(List<Component> message) {
        messages.add(0, new ActionMessage(message));
        if (messages.size() > maxActions)
            for (int i = maxActions; i < messages.size(); i++)
                messages.remove(i);
        reflow();
    }
    
    protected void removeMessage(int index) {
        messages.remove(index);
        reflow();
    }
    
    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void renderContent(PoseStack matrix, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        int i = 0;
        matrix.pushPose();
        while (i < messages.size()) {
            ActionMessage message = messages.get(i);
            
            long timer = System.currentTimeMillis() - message.timestamp;
            if (timer >= totalTime)
                removeMessage(i);
            else {
                
                float alpha = timer > displayTime ? (1 - Math.max(0F, timer - displayTime) / fadeTime) : 1;
                message.render(matrix, Math.max(0.05F, alpha));
                i++;
                matrix.translate(0, message.getTotalHeight(), 0);
            }
        }
        matrix.popPose();
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void init() {}
    
    @Override
    public void tick() {}
    
    private static class ActionMessage extends CompiledText {
        
        long timestamp;
        
        public ActionMessage(List<Component> message) {
            super(Integer.MAX_VALUE, Integer.MAX_VALUE);
            setText(message);
            this.timestamp = System.currentTimeMillis();
        }
        
        public void render(PoseStack stack, float alpha) {
            RenderSystem.setShaderColor(1, 1, 1, alpha);
            super.render(stack);
        }
        
    }
    
}
