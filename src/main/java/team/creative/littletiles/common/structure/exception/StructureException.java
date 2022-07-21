package team.creative.littletiles.common.structure.exception;

import team.creative.littletiles.common.action.LittleActionException;

public class StructureException extends LittleActionException {
    
    public StructureException(String message) {
        super(message);
    }
    
    public StructureException(String message, Exception e) {
        super(message, e);
    }
    
}
