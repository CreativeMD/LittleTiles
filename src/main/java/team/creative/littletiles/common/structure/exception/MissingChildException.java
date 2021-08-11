package team.creative.littletiles.common.structure.exception;

import team.creative.littletiles.common.structure.connection.StructureChildConnection;

public class MissingChildException extends CorruptedConnectionException {
    
    public MissingChildException(StructureChildConnection connector, CorruptedConnectionException e) {
        super("Missing child " + connector.getChildId() + " at " + connector.getStructurePosition(), e);
    }
    
    public MissingChildException(int index) {
        super("There is no child at position " + index);
    }
    
}
