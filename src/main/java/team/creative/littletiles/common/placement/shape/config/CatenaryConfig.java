package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.CreativeConfig.IntRange;
import team.creative.creativecore.common.config.api.CreativeConfig.IntRangeSupplier;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig.GridRange;

public class CatenaryConfig extends LittleShapeConfig {

    public enum Mode {
        BETWEEN,
        BEYOND
    }

    @CreativeConfig
    @IntRangeSupplier(supplier = GridRange.class)
    public int thickness = 1;

    @CreativeConfig
    @IntRange(min = 0, max = 128)
    public int drop = 16;

    @CreativeConfig
    public Mode mode = Mode.BETWEEN;

    @Override
    public List<Component> information() {
        return new TextBuilder()
                .textColor(ChatFormatting.WHITE)
                .translate("shape.config.thickness").text(": ").textColor(ChatFormatting.GRAY).text("" + thickness).newLine()
                .textColor(ChatFormatting.WHITE)
                .translate("shape.config.drop").text(": ").textColor(ChatFormatting.GRAY).text("" + drop).newLine()
                .textColor(ChatFormatting.WHITE)
                .translate("shape.config.catenary_mode").text(": ").textColor(ChatFormatting.GRAY).translate("shape.config.catenary_mode." + mode.name().toLowerCase())
                .build();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        return false;
    }
}