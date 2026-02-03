package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.text.TextBuilder;

public class BrushSizeShapeConfig extends LittleShapeConfig {
    
    public static final int BRUSH_MAX = 8;
    
    @CreativeConfig.IntRange(min = 1, max = BRUSH_MAX)
    public int size = 4;
    
    public BrushSizeShapeConfig() {}
    
    public BrushSizeShapeConfig(int thickness) {
        this.size = thickness;
    }
    
    @Override
    public List<Component> information() {
        return new TextBuilder().newLine().textColor(ChatFormatting.WHITE).translate("shape.config.size").text(": ").textColor(ChatFormatting.GRAY).text("" + size).build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        return false;
    }
    
}
