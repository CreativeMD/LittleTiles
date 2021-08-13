package team.creative.littletiles.client.action;

import com.creativemd.littletiles.common.action.LittleAction;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class ActionEvent extends Event {
    
    public final LittleAction action;
    
    public final ActionType type;
    
    public final Player player;
    
    public ActionEvent(LittleAction action, ActionType type, Player player) {
        this.action = action;
        this.type = type;
        this.player = player;
    }
    
    public static enum ActionType {
        normal,
        undo,
        redo;
    }
}
