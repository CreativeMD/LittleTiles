package team.creative.littletiles.server.level.handler;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.ActionEvent;
import team.creative.littletiles.client.action.ActionEvent.ActionType;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.packet.action.ActionMessagePacket;

public class LittleActionHandlerServer {
    
    public static void handleException(ServerPlayer player, LittleActionException e) {
        if (e.isHidden())
            return;
        
        List<Component> message = e.getActionMessage();
        if (message != null)
            LittleTiles.NETWORK.sendToClient(new ActionMessagePacket(message), player);
        else
            player.sendSystemMessage(Component.literal(e.getLocalizedMessage()));
    }
    
    public static <T> T execute(ServerPlayer player, LittleAction<T> action) {
        
        try {
            T result = action.action(player);
            if (action.wasSuccessful(result)) {
                MinecraftForge.EVENT_BUS.post(new ActionEvent(action, ActionType.normal, player));
                return result;
            }
        } catch (LittleActionException e) {
            handleException(player, e);
        }
        
        return action.failed();
    }
    
}
