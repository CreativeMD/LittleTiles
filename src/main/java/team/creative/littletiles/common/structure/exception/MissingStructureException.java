package team.creative.littletiles.common.structure.exception;

import net.minecraft.core.BlockPos;

public class MissingStructureException extends CorruptedConnectionException {
    
    public MissingStructureException(BlockPos pos) {
        super("Structure inside " + pos + " is missing");
    }
    
}
