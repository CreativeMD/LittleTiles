package team.creative.littletiles.client.tool.mode;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.premade.KeyConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo;

public class BuildingModeToggle extends BuildingModeFeature implements BuildingModeInfo {
    
    @CreativeConfig
    public KeyConfig key;
    
    private String title;
    private boolean enabled;
    
    @CreativeConfig
    public boolean defaultValue = false;
    
    @Nullable
    private GuiCheckBox box;
    
    public BuildingModeToggle(String title, int key, KeyModifier modifier, boolean enabled) {
        this.key = new KeyConfig(key, modifier);
        this.title = title;
        this.enabled = enabled;
    }
    
    protected void changed() {
        if (box != null)
            box.value = enabled;
    }
    
    public void set(boolean enabled) {
        this.enabled = enabled;
        changed();
    }
    
    public boolean enabled() {
        return enabled;
    }
    
    @Override
    public void createInfo(GuiParent parent) {
        parent.add(box = new GuiCheckBox(title, enabled).setTitle(Component.translatable(title).append(" (").append(key.getTranslatedKeyMessage()).append(")")));
    }
    
    public void toggle() {
        enabled = !enabled;
        changed();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        if (this.key.matchesPress(keyCode, action)) {
            toggle();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, action, modifiers);
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {}
    
    @Override
    public void remove(OverlayGuiLayer gui) {}
    
    public void reset() {
        enabled = defaultValue;
        changed();
    }
    
}
