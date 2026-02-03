package team.creative.littletiles.common.placement.shape.config;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.client.LittleTilesClient;

public class FacingShapeConfig extends LittleShapeConfig {
    
    public Facing facing = Facing.EAST;
    
    public FacingShapeConfig() {}
    
    @Override
    public List<Component> information() {
        return new TextBuilder().translate("gui.facing").text(": ").textColor(ChatFormatting.GRAY).add(facing.translate()).build();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        var transform = LittleTilesClient.fromKeybind(player, key);
        if (transform == null)
            return false;
        facing = facing.transform(transform);
        return true;
    }
    
}
