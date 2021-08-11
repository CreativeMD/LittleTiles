package team.creative.littletiles.common.structure.exception;

import team.creative.littletiles.common.structure.connection.StructureChildConnection;

public class MissingParentException extends CorruptedConnectionException {
    
    public MissingParentException(StructureChildConnection connector, CorruptedConnectionException e) {
        super("Missing parent " + connector.getChildId() + " at " + connector.getStructurePosition(), e);
    }
}
