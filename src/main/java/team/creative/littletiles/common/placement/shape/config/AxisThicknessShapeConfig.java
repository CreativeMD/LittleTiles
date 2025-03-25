package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig.GridRange;

public class AxisThicknessShapeConfig extends AxisShapeConfig {
    
    @CreativeConfig.IntRangeSupplier(supplier = GridRange.class)
    public int thickness = 1;
    
    public AxisThicknessShapeConfig() {}
    
    @Override
    public List<Component> information() {
        return new TextBuilder(super.information()).newLine().color(ColorUtils.WHITE).translate("shape.config.thickness").text(": ").color(ColorUtils.GRAY).text("" + thickness)
                .build();
    }
    
}
