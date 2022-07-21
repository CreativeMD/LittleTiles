package team.creative.littletiles.common.action;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class LittleActions extends LittleAction<Boolean> {
    
    public LittleAction[] actions;
    
    public LittleActions(LittleAction... actions) {
        this.actions = actions;
    }
    
    public LittleActions() {}
    
    @Override
    public boolean canBeReverted() {
        for (int i = 0; i < actions.length; i++) {
            if (!actions[i].canBeReverted())
                return false;
        }
        return true;
    }
    
    @Override
    public LittleAction revert(Player player) throws LittleActionException {
        LittleAction[] newActions = new LittleAction[actions.length];
        for (int i = 0; i < newActions.length; i++) {
            if (actions[actions.length - 1 - i] != null)
                newActions[i] = actions[actions.length - 1 - i].revert(player);
        }
        return new LittleActions(newActions);
    }
    
    @Override
    public Boolean action(Player player) throws LittleActionException {
        if (actions.length == 0)
            return true;
        boolean success = false;
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] != null && actions[i].wasSuccessful(actions[i].action(player)))
                success = true;
        }
        return success;
    }
    
    @Override
    public LittleAction mirror(Axis axis, LittleBoxAbsolute box) {
        LittleAction[] newActions = new LittleAction[actions.length];
        for (int i = 0; i < actions.length; i++)
            newActions[i] = actions[i].mirror(axis, box);
        return new LittleActions(newActions);
    }
    
    @Override
    public boolean wasSuccessful(Boolean result) {
        return result;
    }
    
    @Override
    public Boolean failed() {
        return false;
    }
    
}
