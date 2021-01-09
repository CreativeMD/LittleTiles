package com.creativemd.littletiles.common.structure.exception;

public class CorruptedConnectionException extends StructureException {
    
    protected CorruptedConnectionException(String message) {
        super(message);
    }
    
    protected CorruptedConnectionException(String message, CorruptedConnectionException e) {
        super(message, e);
    }
    
}
