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
import team.creative.littletiles.common.item.component.MeasurementTypeComponent;
import team.creative.littletiles.common.math.measure.LittleMeasurementType;
import team.creative.littletiles.common.packet.item.MeasurementTypePacket;

public class BuildingModeMeasureType extends BuildingModeFeature implements BuildingModeInfo {
    
    private static final Minecraft MC = Minecraft.getInstance();
    
    @CreativeConfig
    public KeyConfig key;
    
    private String title;
    private LittleMeasurementType lastType;
    
    @Nullable
    private GuiLabel label;
    
    public BuildingModeMeasureType(String title, int key, KeyModifier modifier) {
        this.key = new KeyConfig(key, modifier);
        this.title = title;
    }
    
    protected void changedLabel(LittleMeasurementType type) {
        if (label != null) {
            label.setTitle(Component.translatable(title, type.translatable(), key.getTranslatedKeyMessage()));
            lastType = type;
        }
    }
    
    protected void changed(MeasurementTypeComponent component) {
        changedLabel(component.type);
        
        LittleTiles.NETWORK.sendToServer(new MeasurementTypePacket(component));
    }
    
    @Override
    public void tick(PreviewRenderer renderer) {
        super.tick(renderer);
        var type = renderer.manager.tool().stack.get(LittleTilesRegistry.MEASUREMENT_TYPE);
        if (type != null && type.type != lastType)
            changed(type);
        
    }
    
    @Override
    public void createInfo(GuiParent parent) {
        parent.add(label = new GuiLabel(title));
        var stack = MC.player.getMainHandItem();
        if (stack.has(LittleTilesRegistry.MEASUREMENT_TYPE))
            changedLabel(stack.get(LittleTilesRegistry.MEASUREMENT_TYPE).type);
    }
    
    public void cycle() {
        var stack = MC.player.getMainHandItem();
        if (stack.has(LittleTilesRegistry.MEASUREMENT_TYPE)) {
            var type = stack.get(LittleTilesRegistry.MEASUREMENT_TYPE).type;
            
            type = LittleMeasurementType.REGISTRY.get(LittleMeasurementType.REGISTRY.next(LittleMeasurementType.REGISTRY.name(type)));
            var c = new MeasurementTypeComponent(type);
            stack.set(LittleTilesRegistry.MEASUREMENT_TYPE, c);
            changed(c);
        }
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
