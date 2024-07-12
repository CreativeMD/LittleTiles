package team.creative.littletiles.common.level.little;

import java.util.function.Consumer;

import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.server.player.LittleServerPlayerConnection;

public class LittlePlayerConnection {
    
    @OnlyIn(Dist.CLIENT)
    private static void runInContextClient(LittleLevel level, Player player, Consumer<PacketListener> consumer) {
        LittleTilesClient.PLAYER_CONNECTION.runInContext(level, x -> consumer.accept(x));
    }
    
    public static void runInContext(LittleLevel level, Player player, Consumer<PacketListener> consumer) {
        if (level.isClientSide())
            runInContextClient(level, player, consumer);
        else
            LittleServerPlayerConnection.runInContext(level, (ServerPlayer) player, x -> consumer.accept(x));
    }
    
}
