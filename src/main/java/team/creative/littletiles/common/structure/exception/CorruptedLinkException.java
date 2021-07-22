package team.creative.littletiles.common.structure.exception;

public class CorruptedLinkException extends CorruptedConnectionException {
    
    public CorruptedLinkException() {
        super("Link is invalid");
    }
    
}
