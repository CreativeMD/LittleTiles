package team.creative.littletiles.common.packet.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;

public class ChangedElementPacket extends CreativePacket {
    
    public LittleElement element;
    
    public ChangedElementPacket(LittleElement element) {
        this.element = element;
    }
    
    public ChangedElementPacket() {}
    
    @Override
    public void execute(Player player) {
        if (LittleAction.isBlockValid(element.getState())) {
            player.getMainHandItem().set(LittleTilesRegistry.ELEMENT, element);
            player.inventoryMenu.broadcastChanges();
        }
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
