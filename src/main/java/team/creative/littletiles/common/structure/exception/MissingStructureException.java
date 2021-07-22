package team.creative.littletiles.common.structure.exception;

import net.minecraft.util.math.BlockPos;

public class MissingStructureException extends CorruptedConnectionException {
    
    public MissingStructureException(BlockPos pos) {
        super("Structure inside " + pos + " is missing");
    }
    
}
