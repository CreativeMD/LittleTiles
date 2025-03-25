package team.creative.littletiles.common.placement.shape.config;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3;
import team.creative.littletiles.client.LittleTilesClient;

public class MatrixShapeConfig extends LittleShapeConfig {
    
    @CreativeConfig(hideFromGUI = true)
    public IntMatrix3 matrix = new IntMatrix3();
    
    @Override
    public List<Component> information() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean react(Player player, KeyMapping key) {
        var transform = LittleTilesClient.fromKeybind(player, key);
        if (transform == null)
            return false;
        matrix.mul(transform);
        return true;
    }
}
