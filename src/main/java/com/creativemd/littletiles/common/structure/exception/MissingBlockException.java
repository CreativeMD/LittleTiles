package com.creativemd.littletiles.common.structure.exception;

import net.minecraft.util.math.BlockPos;

public class MissingBlockException extends CorruptedConnectionException {
	
	public MissingBlockException(BlockPos pos) {
		super("Block is missing/ replaced " + pos);
	}
	
}
