package com.creativemd.littletiles.common.structure.exception;

public class MissingWorldException extends NotYetConnectedException {
    
    public MissingWorldException() {
        super("");
    }
    
    public MissingWorldException(String message) {
        super(message);
    }
    
}
