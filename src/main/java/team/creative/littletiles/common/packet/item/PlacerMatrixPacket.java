package team.creative.littletiles.common.packet.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.littletiles.api.common.tool.ILittlePlacer;

public class PlacerMatrixPacket extends CreativePacket {
    
    public PlacerMatrixPacket() {}
    
    public IntMatrix3c matrix;
    
    public PlacerMatrixPacket(IntMatrix3c matrix) {
        this.matrix = matrix;
    }
    
    @Override
    public void executeClient(Player player) {
        execute(player);
    }
    
    @Override
    public void execute(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ILittlePlacer p)
            p.transformMatrix(stack, matrix);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        execute(player);
    }
    
}
