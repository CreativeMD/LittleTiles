package team.creative.littletiles.common.packet.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.api.common.tool.ILittleSelector;
import team.creative.littletiles.common.item.component.SelectionComponent;

public class SelectionModePacket extends CreativePacket {
    
    public SelectionComponent component;
    
    public SelectionModePacket(SelectionComponent component) {
        this.component = component;
    }
    
    public SelectionModePacket() {}
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ILittleSelector selector)
            selector.setSelection(stack, component);
    }
    
}
