package com.creativemd.littletiles.client.gui.controls;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.littletiles.client.tooltip.CompiledActionMessage;

import net.minecraft.client.renderer.GlStateManager;

public class GuiActionDisplay extends GuiControl {
    
    protected List<CompiledActionMessage> messages = new ArrayList<>();
    
    private int maxActions;
    private int displayTime;
    private int fadeTime;
    private int totalTime;
    protected int lines = 0;
    
    public GuiActionDisplay(String name, int x, int y, int width) {
        super(name, x, y, width, 10);
        setTimer(5, 1);
        setMessageCount(6);
    }
    
    public GuiActionDisplay setMessageCount(int count) {
        this.maxActions = count;
        List<CompiledActionMessage> newMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (i >= count)
                break;
            newMessages.add(messages.get(i));
        }
        this.messages = newMessages;
        this.height = (font.FONT_HEIGHT + 3) * maxActions;
        return this;
    }
    
    public GuiActionDisplay setLinesCount(int count) {
        this.height = (font.FONT_HEIGHT + 3) * count;
        return this;
    }
    
    public GuiActionDisplay setTimer(int displaySeconds, int fadeSeconds) {
        this.displayTime = displaySeconds * 1000;
        this.fadeTime = fadeSeconds * 1000;
        this.totalTime = displayTime + fadeTime;
        return this;
    }
    
    public void addMessage(CompiledActionMessage message) {
        messages.add(0, message);
        if (messages.size() > maxActions)
            for (int i = maxActions; i < messages.size(); i++)
                messages.remove(i);
            
        updateLineCount();
    }
    
    public void addMessage(String text, Object... objects) {
        addMessage(new CompiledActionMessage(text, objects));
    }
    
    protected void removeMessage(int index) {
        messages.remove(index);
        updateLineCount();
    }
    
    protected void updateLineCount() {
        lines = 0;
        for (int i = 0; i < messages.size(); i++)
            if (messages.get(i) != null)
                lines += messages.get(i).lines.size();
    }
    
    @Override
    protected void renderContent(GuiRenderHelper helper, Style style, int width, int height) {
        int i = 0;
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        
        int requiredHeight = (font.FONT_HEIGHT + 3) * (lines - 1);
        GlStateManager.translate(width / 2, height - requiredHeight, 0);
        
        while (i < messages.size()) {
            CompiledActionMessage message = messages.get(i);
            
            long timer = System.currentTimeMillis() - message.timestamp;
            if (timer >= totalTime)
                removeMessage(i);
            else {
                GlStateManager.pushMatrix();
                float alpha = timer > displayTime ? (1 - Math.max(0F, timer - displayTime) / fadeTime) : 1;
                message.render(helper, Math.max(0.05F, alpha));
                i++;
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
        
    }
    
}
