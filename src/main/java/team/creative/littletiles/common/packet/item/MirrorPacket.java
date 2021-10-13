package team.creative.littletiles.common.packet.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.api.tool.ILittleTool;

public class MirrorPacket extends CreativePacket {
    
    public MirrorPacket() {
        
    }
    
    public Axis axis;
    
    public MirrorPacket(Axis axis) {
        this.axis = axis;
    }
    
    @Override
    public void executeClient(Player player) {
        execute(player);
    }
    
    @Override
    public void execute(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ILittleTool)
            ((ILittleTool) stack.getItem()).flip(player, stack, axis, player.level.isClientSide);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        execute(player);
    }
    
}
