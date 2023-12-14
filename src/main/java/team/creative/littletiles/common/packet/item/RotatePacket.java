package team.creative.littletiles.common.packet.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.api.common.tool.ILittleTool;

public class RotatePacket extends CreativePacket {
    
    public RotatePacket() {
        
    }
    
    public Rotation rotation;
    
    public RotatePacket(Rotation rotation) {
        this.rotation = rotation;
    }
    
    @Override
    public void executeClient(Player player) {
        execute(player);
    }
    
    @Override
    public void execute(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ILittleTool tool)
            tool.rotate(player, stack, rotation, player.level().isClientSide);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        execute(player);
    }
    
}
