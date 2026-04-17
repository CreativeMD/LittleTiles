package team.creative.littletiles.client.tool.mode;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.settings.KeyModifier;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.config.premade.KeyConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.util.registry.ICreativeRegistry;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo;

public class BuildingModeCycle<T> extends BuildingModeFeature implements BuildingModeInfo, ICreativeConfig {
    
    private final ICreativeRegistry<T> registry;
    private final Function<T, Component> translate;
    
    @CreativeConfig
    public KeyConfig key;
    
    private String title;
    private T selected;
    
    @CreativeConfig
    public String defaultSelected;
    
    @Nullable
    private GuiLabel label;
    
    public BuildingModeCycle(String title, String defaultSelected, ICreativeRegistry<T> registry, Function<T, Component> translate, int key, KeyModifier modifier) {
        this.key = new KeyConfig(key, modifier);
        this.title = title;
        this.defaultSelected = defaultSelected;
        this.registry = registry;
        this.translate = translate;
        this.selected = null;
    }
    
    protected void changed() {
        if (label != null)
            label.setTitle(Component.translatable(title, translate.apply(selected), key.getTranslatedKeyMessage()));
    }
    
    public void set(T selected) {
        this.selected = selected;
        changed();
    }
    
    public T selected() {
        return selected;
    }
    
    @Override
    public void createInfo(GuiParent parent) {
        parent.add(label = new GuiLabel(title));
        changed();
    }
    
    public void cycle() {
        selected = registry.get(registry.next(registry.name(selected)));
        changed();
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
    
    public void reset() {
        selected = registry.get(defaultSelected);
        changed();
    }
    
    @Override
    public void configured(Side side) {
        reset();
    }
}
