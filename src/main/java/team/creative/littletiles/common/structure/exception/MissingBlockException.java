package team.creative.littletiles.common.structure.exception;

import net.minecraft.core.BlockPos;

public class MissingBlockException extends CorruptedConnectionException {
    
    public MissingBlockException(BlockPos pos) {
        super("Block is missing/ replaced " + pos);
    }
    
}
