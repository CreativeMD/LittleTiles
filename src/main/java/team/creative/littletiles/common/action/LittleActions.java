package team.creative.littletiles.common.action;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.action.cancel.ActionCancelContext;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;

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
    public LittleAction revert(LittleActionSource source) throws LittleActionException {
        LittleAction[] newActions = new LittleAction[actions.length];
        for (int i = 0; i < newActions.length; i++) {
            if (actions[actions.length - 1 - i] != null)
                newActions[i] = actions[actions.length - 1 - i].revert(source);
        }
        return new LittleActions(newActions);
    }
    
    @Override
    public void cancel(ActionCancelContext context) throws LittleActionException {
        for (int i = 0; i < actions.length; i++)
            if (actions[i] != null)
                actions[i].cancel(context);
    }
    
    @Override
    public Boolean action(LittleActionSource source) throws LittleActionException {
        if (actions.length == 0)
            return true;
        boolean success = false;
        for (int i = 0; i < actions.length; i++) {
            try {
                if (actions[i] != null && actions[i].wasSuccessful(actions[i].action(source)))
                    success = true;
            } catch (LittleActionException e) {
                // Make sure all actions that have run before are cancelled
                if (!e.hasCancelContext())
                    e.setCancelContext(new ActionCancelContext(actions[i]));
                var context = e.getCancelContext();
                for (int j = 0; j < i; j++)
                    if (actions[j] != null)
                        actions[j].cancel(context);
                throw e;
            }
        }
        return success;
    }
    
    @Override
    public LittleAction mirror(Axis axis, LittleBoxAbsolute box) {
        LittleAction[] newActions = new LittleAction[actions.length];
        for (int i = 0; i < actions.length; i++)
            if (actions[i] != null)
                newActions[i] = actions[i].mirror(axis, box);
        return new LittleActions(newActions);
    }
    
    @Override
    public void include(LittleBoxes boxes) {
        for (int i = 0; i < actions.length; i++)
            if (actions[i] != null)
                actions[i].include(boxes);
    }
    
    @Override
    public void exclude(LittleBoxes boxes) {
        for (int i = 0; i < actions.length; i++)
            if (actions[i] != null)
                actions[i].exclude(boxes);
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
