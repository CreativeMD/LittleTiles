package team.creative.littletiles.common.structure.exception;

public class RemovedStructureException extends CorruptedConnectionException {
    
    public RemovedStructureException() {
        super("Structure has already been removed");
    }
    
}
