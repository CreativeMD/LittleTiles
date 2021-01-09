package com.creativemd.littletiles.common.structure.exception;

import com.creativemd.littletiles.common.structure.connection.StructureChildConnection;

public class MissingChildException extends CorruptedConnectionException {
    
    public MissingChildException(StructureChildConnection connector, CorruptedConnectionException e) {
        super("Missing child " + connector.getChildId() + " at " + connector.getStructurePosition(), e);
    }
    
}
