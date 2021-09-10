package team.creative.littletiles.client.action;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.util.tooltip.ActionMessage;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.ActionEvent.ActionType;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.level.LevelHandler;

@OnlyIn(Dist.CLIENT)
public class LittleActionHandlerClient extends LevelHandler {
    
    private static final Minecraft mc = Minecraft.getInstance();
    private List<LittleAction> lastActions = new ArrayList<>();
    private int index = 0;
    
    public LittleActionHandlerClient(Level level) {
        super(level);
    }
    
    protected void rememberAction(LittleAction action) {
        if (!action.canBeReverted())
            return;
        
        if (index > 0) {
            if (index < lastActions.size())
                lastActions = lastActions.subList(index, lastActions.size());
            else
                lastActions = new ArrayList<>();
        }
        
        index = 0;
        
        if (lastActions.size() == LittleTiles.CONFIG.building.maxSavedActions)
            lastActions.remove(LittleTiles.CONFIG.building.maxSavedActions - 1);
        
        lastActions.add(0, action);
    }
    
    public static boolean canUseUndoOrRedo(Player player) {
        GameType type = PlayerUtils.getGameType(player);
        return type == GameType.CREATIVE || type == GameType.SURVIVAL;
    }
    
    public static boolean isUsingSecondMode(Player player) {
        if (player == null)
            return false;
        if (LittleTiles.CONFIG.building.useALTForEverything)
            return Screen.hasAltDown();
        if (LittleTiles.CONFIG.building.useAltWhenFlying)
            return player.getAbilities().flying ? Screen.hasAltDown() : player.isCrouching();
        return player.isCrouching();
    }
    
    public boolean execute(LittleAction action) {
        Player player = Minecraft.getInstance().player;
        
        try {
            if (action.action(player)) {
                rememberAction(action);
                MinecraftForge.EVENT_BUS.post(new ActionEvent(action, ActionType.normal, player));
                
                LittleTiles.NETWORK.sendToServer(action);
                
                return true;
            }
        } catch (LittleActionException e) {
            handleException(e);
        }
        
        return false;
    }
    
    public boolean undo() throws LittleActionException {
        if (lastActions.size() > index) {
            Player player = mc.player;
            
            if (!canUseUndoOrRedo(player))
                return false;
            
            LittleAction reverted = lastActions.get(index).revert(player);
            
            if (reverted == null)
                throw new LittleActionException("action.revert.notavailable");
            
            if (reverted.action(player)) {
                MinecraftForge.EVENT_BUS.post(new ActionEvent(reverted, ActionType.undo, player));
                LittleTiles.NETWORK.sendToServer(reverted);
                
                lastActions.set(index, reverted);
                index++;
                return true;
            }
        }
        return false;
    }
    
    public boolean redo() throws LittleActionException {
        if (index > 0 && index <= lastActions.size()) {
            Player player = mc.player;
            
            if (!canUseUndoOrRedo(player))
                return false;
            
            index--;
            
            LittleAction reverted = lastActions.get(index).revert(player);
            
            if (reverted == null)
                throw new LittleActionException("action.revert.notavailable");
            
            if (reverted.action(player)) {
                MinecraftForge.EVENT_BUS.post(new ActionEvent(reverted, ActionType.redo, player));
                LittleTiles.NETWORK.sendToServer(reverted);
                
                lastActions.set(index, reverted);
                
                return true;
            }
        }
        return false;
    }
    
    public static void handleException(LittleActionException e) {
        if (e.isHidden())
            return;
        
        ActionMessage message = e.getActionMessage();
        if (message != null)
            LittleTilesClient.displayActionMessage(message);
        else
            mc.player.sendMessage(new TextComponent(e.getLocalizedMessage()), Util.NIL_UUID);
    }
}