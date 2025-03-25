package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.util.text.TextBuilder;

public class CornerShapeConfig extends MatrixShapeConfig {
    
    public boolean secondMode;
    
    @Override
    public List<Component> information() {
        return new TextBuilder().translate("shape.config.second_type").text(": ").bool(secondMode).build();
    }
}
