package team.creative.littletiles.client.action;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.ActionEvent.ActionType;
import team.creative.littletiles.common.level.LevelHandler;

@OnlyIn(Dist.CLIENT)
public class LittleActionHandlerClient extends LevelHandler {
    
    private static final Minecraft mc = Minecraft.getInstance();
    private List<LittleAction> lastActions = new ArrayList<>();
    private int index = 0;
    
    public LittleActionHandlerClient(Level level) {
        super(level);
    }
    
    public void rememberAction(LittleAction action) {
        if (!action.canBeReverted())
            return;
        
        if (index > 0) {
            if (index < lastActions.size())
                lastActions = lastActions.subList(index, lastActions.size() - 1);
            else
                lastActions = new ArrayList<>();
        }
        
        index = 0;
        
        if (lastActions.size() == LittleTiles.CONFIG.building.maxSavedActions)
            lastActions.remove(LittleTiles.CONFIG.building.maxSavedActions - 1);
        
        lastActions.add(0, action);
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
    
    public boolean undo() throws LittleActionException {
        if (lastActions.size() > index) {
            Player player = mc.player;
            
            LittleAction reverted = lastActions.get(index).revert(player);
            
            if (reverted == null)
                throw new LittleActionException("action.revert.notavailable");
            
            reverted.furtherActions = lastActions.get(index).revertFurtherActions();
            
            if (reverted.action(player)) {
                MinecraftForge.EVENT_BUS.post(new ActionEvent(reverted, ActionType.undo, player));
                if (reverted.sendToServer())
                    PacketHandler.sendPacketToServer(reverted);
                
                if (reverted.furtherActions != null && !reverted.furtherActions.isEmpty()) {
                    for (int i = 0; i < reverted.furtherActions.size(); i++) {
                        LittleAction subAction = reverted.furtherActions.get(i);
                        
                        if (subAction == null)
                            continue;
                        
                        try {
                            subAction.action(player);
                            
                            if (subAction.sendToServer())
                                PacketHandler.sendPacketToServer(subAction);
                            
                        } catch (LittleActionException e) {
                            handleExceptionClient(e);
                        }
                    }
                }
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
            
            index--;
            
            LittleAction reverted = lastActions.get(index).revert(player);
            
            if (reverted == null)
                throw new LittleActionException("action.revert.notavailable");
            
            reverted.furtherActions = lastActions.get(index).revertFurtherActions();
            
            if (reverted.action(player)) {
                MinecraftForge.EVENT_BUS.post(new ActionEvent(reverted, ActionType.redo, player));
                if (reverted.sendToServer())
                    PacketHandler.sendPacketToServer(reverted);
                
                if (reverted.furtherActions != null && !reverted.furtherActions.isEmpty()) {
                    for (int i = 0; i < reverted.furtherActions.size(); i++) {
                        LittleAction subAction = reverted.furtherActions.get(i);
                        
                        if (subAction == null)
                            continue;
                        
                        try {
                            subAction.action(player);
                            
                            if (subAction.sendToServer())
                                PacketHandler.sendPacketToServer(subAction);
                            
                        } catch (LittleActionException e) {
                            handleExceptionClient(e);
                        }
                    }
                }
                lastActions.set(index, reverted);
                
                return true;
            }
        }
        return false;
    }
    
}
