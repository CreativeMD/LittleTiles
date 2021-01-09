package com.creativemd.littletiles.common.structure.exception;

import java.util.UUID;

public class MissingAnimationException extends CorruptedConnectionException {
    
    public MissingAnimationException(UUID uuid) {
        super("Missing animation " + uuid);
    }
    
}
