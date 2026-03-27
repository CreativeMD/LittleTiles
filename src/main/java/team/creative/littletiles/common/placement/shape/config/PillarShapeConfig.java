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
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig.GridRange;

public class PillarShapeConfig extends LittleShapeConfig {
    
    public boolean simple;
    @CreativeConfig.IntRangeSupplier(supplier = GridRange.class)
    public int width = 1;
    @CreativeConfig.IntRangeSupplier(supplier = GridRange.class)
    public int height = 1;
    
    public PillarShapeConfig() {
        this(false, 1, 1);
    }
    
    public PillarShapeConfig(boolean simple, int width, int height) {
        this.simple = simple;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public List<Component> information() {
        TextBuilder text = new TextBuilder();
        text.textColor(ChatFormatting.WHITE).translate("shape.config.simple").text(": ").bool(simple);
        text.newLine().textColor(ChatFormatting.WHITE).translate("shape.config.width").text(": ").textColor(ChatFormatting.GRAY).text("" + width);
        text.newLine().textColor(ChatFormatting.WHITE).translate("shape.config.height").text(": ").textColor(ChatFormatting.GRAY).text("" + height);
        return text.build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        if (LittleTilesClient.facingFromKeybind(player, key) != null) {
            int temp = width;
            this.width = height;
            this.height = temp;
            return true;
        }
        return false;
    }
    
}
