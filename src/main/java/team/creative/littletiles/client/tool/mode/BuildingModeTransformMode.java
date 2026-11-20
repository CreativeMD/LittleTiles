package team.creative.littletiles.client.tool.mode;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.premade.KeyConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo;
import team.creative.littletiles.common.item.component.TransformMode;
import team.creative.littletiles.common.packet.item.TransformModePacket;

public class BuildingModeTransformMode extends BuildingModeFeature implements BuildingModeInfo {
    
    private static final Minecraft MC = Minecraft.getInstance();
    
    @CreativeConfig
    public KeyConfig key;
    
    private String title;
    private TransformMode lastMode;
    
    @Nullable
    private GuiLabel label;
    
    public BuildingModeTransformMode(String title, int key, KeyModifier modifier) {
        this.key = new KeyConfig(key, modifier);
        this.title = title;
    }
    
    protected void changedLabel(TransformMode mode) {
        if (label != null) {
            label.setTitle(Component.translatable(title, mode.translatable(), key.getTranslatedKeyMessage()));
            lastMode = mode;
        }
    }
    
    protected void changed(TransformMode mode) {
        changedLabel(mode);
        
        LittleTiles.NETWORK.sendToServer(new TransformModePacket(mode));
    }
    
    @Override
    public void tick(PreviewRenderer renderer) {
        super.tick(renderer);
        var mode = renderer.manager.tool().stack.getOrDefault(LittleTilesRegistry.TRANSFORM_MODE, TransformMode.DEFAULT);
        if (mode != null && mode != lastMode)
            changed(mode);
        
    }
    
    @Override
    public void createInfo(GuiParent parent) {
        parent.add(label = new GuiLabel(title));
        var stack = MC.player.getMainHandItem();
        changedLabel(stack.getOrDefault(LittleTilesRegistry.TRANSFORM_MODE, TransformMode.DEFAULT));
    }
    
    public void cycle() {
        var stack = MC.player.getMainHandItem();
        var mode = stack.getOrDefault(LittleTilesRegistry.TRANSFORM_MODE, TransformMode.DEFAULT);
        
        int index = mode.ordinal() + 1;
        if (index >= TransformMode.values().length)
            index = 0;
        mode = TransformMode.values()[index];
        
        stack.set(LittleTilesRegistry.TRANSFORM_MODE, mode);
        changed(mode);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        if (this.key.matchesPress(keyCode, action)) {
            cycle();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, action, modifiers);
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {}
    
    @Override
    public void remove(OverlayGuiLayer gui) {}
}
