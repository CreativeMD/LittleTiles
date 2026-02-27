package team.creative.littletiles.client.tool.mode;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.config.premade.KeyConfig;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public class BuildingModeGridScroll extends BuildingModeFeature implements ICreativeConfig, BuildingModeInfo {
    
    private static final Minecraft MC = Minecraft.getInstance();
    private boolean active;
    private double scrollPosition;
    
    @CreativeConfig
    private double scrollSpeed = 0.25;
    
    @CreativeConfig
    public KeyConfig key = new KeyConfig(InputConstants.KEY_V, KeyModifier.NONE);
    public GuiLabel label;
    
    private boolean keyDown = false;
    
    public BuildingModeGridScroll() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {
        active = true;
        scrollPosition = PlacementPlayerSetting.grid(MC.player).getIndex();
    }
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        active = false;
        keyDown = false;
    }
    
    private boolean active() {
        return active && MC.player != null && MC.screen == null && keyDown;
    }
    
    @Override
    public void keyPressed(Key key) {
        if (this.key.matches(key))
            if (key.getAction() == InputConstants.PRESS)
                keyDown = true;
            else if (key.getAction() == InputConstants.RELEASE)
                keyDown = false;
    }
    
    public boolean isKeyDown() {
        return keyDown;
    }
    
    @SubscribeEvent
    public void scroll(InputEvent.MouseScrollingEvent event) {
        if (!active())
            return;
        
        int gridBefore = PlacementPlayerSetting.grid(MC.player).getIndex();
        scrollPosition = Math.clamp(scrollPosition + event.getScrollDeltaY() * scrollSpeed, 0, LittleTiles.CONFIG.build.get(MC.player).maxGrid());
        int gridIndex = (int) scrollPosition;
        
        if (gridBefore != gridIndex) {
            var grid = LittleGrid.gridByIndex(gridIndex);
            LittleTilesClient.grid(grid);
            MC.player.displayClientMessage(Component.translatable("building.grid.message", grid.count), true);
            updateTitle();
        }
        event.setCanceled(true);
    }
    
    @Override
    public void configured(Side side) {
        keyDown = false;
    }
    
    protected void updateTitle() {
        if (label != null)
            label.setTitle(Component.translatable("building.grid.info", key.getTranslatedKeyMessage(), PlacementPlayerSetting.grid(MC.player).count));
    }
    
    @Override
    public void createInfo(GuiParent parent) {
        parent.add(label = new GuiLabel("grid"));
        updateTitle();
    }
    
}
