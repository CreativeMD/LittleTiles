package team.creative.littletiles.common.packet.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.component.SelectionComponent;

public class SelectionModePacket extends CreativePacket {
    
    public BlockPos pos;
    public boolean rightClick;
    
    public SelectionModePacket(BlockPos pos, boolean rightClick) {
        this.pos = pos;
        this.rightClick = rightClick;
    }
    
    public SelectionModePacket() {}
    
    @Override
    public void execute(Player player) {
        ItemStack stack = player.getMainHandItem();
        SelectionComponent sel = SelectionComponent.getOrDefault(stack);
        if (stack.getItem() instanceof ItemLittleBlueprint)
            if (rightClick)
                stack.set(LittleTilesRegistry.SELECTION, sel.mode.rightClick(player, sel, pos));
            else
                stack.set(LittleTilesRegistry.SELECTION, sel.mode.leftClick(player, sel, pos));
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
