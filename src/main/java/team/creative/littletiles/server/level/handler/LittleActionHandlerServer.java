package team.creative.littletiles.server.level.handler;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.ActionEvent;
import team.creative.littletiles.client.action.ActionEvent.ActionType;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.packet.action.ActionMessagePacket;
import team.creative.littletiles.common.packet.action.PlacementPlayerSettingPacket;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

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
                MinecraftForge.EVENT_BUS.post(new ActionEvent(action, ActionType.NORMAL, player));
                return result;
            }
        } catch (LittleActionException e) {
            handleException(player, e);
        }
        
        return action.failed();
    }
    
    public static void playerLoggedIn(PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        LittleTiles.NETWORK.sendToClient(new PlacementPlayerSettingPacket(new PlacementPlayerSetting(player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(
            PlacementPlayerSetting.SETTING_KEY))), player);
    }
}
