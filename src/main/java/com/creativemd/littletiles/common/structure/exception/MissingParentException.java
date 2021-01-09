package com.creativemd.littletiles.common.structure.exception;

import com.creativemd.littletiles.common.structure.connection.StructureChildConnection;

public class MissingParentException extends CorruptedConnectionException {
    
    public MissingParentException(StructureChildConnection connector, CorruptedConnectionException e) {
        super("Missing parent " + connector.getChildId() + " at " + connector.getStructurePosition(), e);
    }
}
