package com.creativemd.littletiles.common.structure.exception;

import com.creativemd.littletiles.common.action.LittleActionException;

public class StructureException extends LittleActionException {
    
    public StructureException(String message) {
        super(message);
    }
    
    public StructureException(String message, Exception e) {
        super(message, e);
    }
    
}
