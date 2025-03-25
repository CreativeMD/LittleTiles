package team.creative.littletiles.common.packet.action;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;

public class ChangedPosPacket extends CreativePacket {
    
    public boolean first;
    public BlockPos pos;
    
    public ChangedPosPacket(boolean first, BlockPos pos) {
        this.first = first;
        this.pos = pos;
    }
    
    public ChangedPosPacket() {}
    
    @Override
    public void execute(Player player) {
        player.getMainHandItem().set(first ? LittleTilesRegistry.FIRST_POS : LittleTilesRegistry.SECOND_POS, pos);
        player.inventoryMenu.broadcastChanges();
        if (!player.level().isClientSide)
            player.sendSystemMessage(Component.translatable("selection.mode.area.pos." + (first ? "first" : "second"), pos.getX(), pos.getY(), pos.getZ()));
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
