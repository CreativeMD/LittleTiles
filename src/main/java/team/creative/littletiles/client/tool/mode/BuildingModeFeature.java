package team.creative.littletiles.client.tool.mode;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;

public abstract class BuildingModeFeature {
    
    public abstract void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures);
    
    public abstract void remove(OverlayGuiLayer gui);
    
    public void tick(Level level, Player player, @Nullable BlockHitResult blockHit) {}
    
    public void render(Level level, Player player, PoseStack pose, Vec3 cam, boolean lines) {}
    
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        return false;
    }
    
    public Component tooltip() {
        return null;
    }
}
