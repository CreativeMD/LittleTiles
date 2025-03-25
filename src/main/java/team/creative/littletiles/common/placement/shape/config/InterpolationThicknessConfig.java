package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig.GridRange;
import team.creative.littletiles.common.placement.shape.type.LittleShapeCurve.ShapeInterpolation;

public class InterpolationThicknessConfig extends LittleShapeConfig {
    
    public ShapeInterpolation interpolation = ShapeInterpolation.HERMITE;
    @CreativeConfig.IntRangeSupplier(supplier = GridRange.class)
    public int thickness = 1;
    
    @Override
    public List<Component> information() {
        TextBuilder text = new TextBuilder().color(ColorUtils.WHITE).translate("gui.interpolation").text(": ").color(ColorUtils.GRAY).add(interpolation.translatable()).newLine()
                .color(ColorUtils.WHITE).translate("shape.config.thickness").text(": ").color(ColorUtils.GRAY).text("" + thickness);
        return text.build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        return false;
    }
}
