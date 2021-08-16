package team.creative.littletiles.common.permission;

import team.creative.littletiles.common.action.LittleActionException;

public class PermissionException extends LittleActionException {
    
    public PermissionException(String msg) {
        super(msg);
    }
    
}
