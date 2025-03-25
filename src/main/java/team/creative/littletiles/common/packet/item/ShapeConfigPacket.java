package team.creative.littletiles.common.packet.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.placement.shape.LittleShapeInstance;

public class ShapeConfigPacket extends CreativePacket {
    
    public LittleShapeInstance instance;
    
    public ShapeConfigPacket() {}
    
    public ShapeConfigPacket(LittleShapeInstance instance) {
        this.instance = instance;
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        player.getMainHandItem().set(LittleTilesRegistry.SHAPE, instance);
    }
    
}
