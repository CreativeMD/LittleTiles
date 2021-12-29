package com.creativemd.littletiles.common.structure.exception;

public class RemovedStructureException extends CorruptedConnectionException {
    
    public RemovedStructureException() {
        super("Structure has already been removed");
    }
    
}
