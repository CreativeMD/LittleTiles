package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig.GridRange;

public class AxisHollowThicknessShapeConfig extends AxisShapeConfig {
    
    public boolean hollow;
    @CreativeConfig.IntRangeSupplier(supplier = GridRange.class)
    public int thickness = 1;
    
    public AxisHollowThicknessShapeConfig() {}
    
    @Override
    public List<Component> information() {
        TextBuilder text = new TextBuilder(super.information());
        text.textColor(ChatFormatting.WHITE).translate("shape.config.hollow").text(": ").bool(hollow);
        if (hollow)
            text.newLine().textColor(ChatFormatting.WHITE).translate("shape.config.thickness").text(": ").textColor(ChatFormatting.GRAY).text("" + thickness);
        return text.build();
    }
    
}
