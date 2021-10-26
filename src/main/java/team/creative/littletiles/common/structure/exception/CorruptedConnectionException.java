package team.creative.littletiles.common.structure.exception;

public class CorruptedConnectionException extends StructureException {
    
    public CorruptedConnectionException(String message) {
        super(message);
    }
    
    public CorruptedConnectionException(String message, CorruptedConnectionException e) {
        super(message, e);
    }
    
}
