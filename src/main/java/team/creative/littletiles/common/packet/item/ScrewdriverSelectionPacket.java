package team.creative.littletiles.common.packet.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.item.ItemLittleScrewdriver;

public class ScrewdriverSelectionPacket extends CreativePacket {
    
    public BlockPos pos;
    public boolean rightClick;
    
    public ScrewdriverSelectionPacket(BlockPos pos, boolean rightClick) {
        this.pos = pos;
        this.rightClick = rightClick;
    }
    
    public ScrewdriverSelectionPacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemLittleScrewdriver)
            ((ItemLittleScrewdriver) stack.getItem()).onClick(player, rightClick, pos, stack);
    }
    
}
