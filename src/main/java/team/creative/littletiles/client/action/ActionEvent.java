package team.creative.littletiles.client.action;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import team.creative.littletiles.common.action.LittleAction;

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
        NORMAL,
        UNDO,
        REDO;
    }
}
