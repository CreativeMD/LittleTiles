package team.creative.littletiles.common.packet.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.item.ItemLittleBlueprint;

public class SelectionModePacket extends CreativePacket {
    
    public BlockPos pos;
    public boolean rightClick;
    
    public SelectionModePacket(BlockPos pos, boolean rightClick) {
        this.pos = pos;
        this.rightClick = rightClick;
    }
    
    public SelectionModePacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemLittleBlueprint)
            if (rightClick)
                ItemLittleBlueprint.getSelectionMode(stack).rightClick(player, stack.getOrCreateTagElement(ItemLittleBlueprint.SELECTION_KEY), pos);
            else
                ItemLittleBlueprint.getSelectionMode(stack).leftClick(player, stack.getOrCreateTagElement(ItemLittleBlueprint.SELECTION_KEY), pos);
    }
    
}
