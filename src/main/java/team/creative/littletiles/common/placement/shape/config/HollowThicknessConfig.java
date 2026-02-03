package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.IntRangeSupplier;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.client.LittleTilesClient;

public class HollowThicknessConfig extends LittleShapeConfig {
    
    public boolean hollow;
    @CreativeConfig.IntRangeSupplier(supplier = GridRange.class)
    public int thickness = 1;
    
    public HollowThicknessConfig() {
        this(false, 1);
    }
    
    public HollowThicknessConfig(boolean hollow, int thickness) {
        this.hollow = hollow;
        this.thickness = thickness;
    }
    
    @Override
    public List<Component> information() {
        TextBuilder text = new TextBuilder();
        text.textColor(ChatFormatting.WHITE).translate("shape.config.hollow").text(": ").bool(hollow);
        if (hollow)
            text.newLine().textColor(ChatFormatting.WHITE).translate("shape.config.thickness").text(": ").textColor(ChatFormatting.GRAY).text("" + thickness);
        return text.build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        return false;
    }
    
    public static class GridRange implements IntRangeSupplier {
        
        @Override
        public int getMin() {
            return 1;
        }
        
        @Override
        public int getMax() {
            return LittleTilesClient.ACTION_HANDLER.setting.grid().count;
        }
        
    }
    
}
