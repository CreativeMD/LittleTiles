package team.creative.littletiles.common.packet.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleTransformer;
import team.creative.littletiles.common.item.component.TransformMode;

public class TransformModePacket extends CreativePacket {
    
    public TransformMode component;
    
    public TransformModePacket(TransformMode component) {
        this.component = component;
    }
    
    public TransformModePacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ILittleTransformer)
            stack.set(LittleTilesRegistry.TRANSFORM_MODE, component);
    }
    
}
