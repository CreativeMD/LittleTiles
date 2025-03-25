package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.client.LittleTilesClient;

public class AxisShapeConfig extends LittleShapeConfig {
    
    public Axis axis = Axis.Y;
    
    public AxisShapeConfig() {}
    
    @Override
    public List<Component> information() {
        return new TextBuilder().translate("gui.axis").text(": ").translate("gui.axis." + axis.name()).build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        var newAxis = axis;
        if (key == LittleTilesClient.KEY_UP || key == LittleTilesClient.KEY_DOWN)
            newAxis = Axis.Y;
        if (key == LittleTilesClient.KEY_RIGHT || key == LittleTilesClient.KEY_LEFT)
            if (axis == Axis.X)
                newAxis = Axis.Z;
            else
                newAxis = Axis.X;
        axis = newAxis;
        return true;
    }
    
}
