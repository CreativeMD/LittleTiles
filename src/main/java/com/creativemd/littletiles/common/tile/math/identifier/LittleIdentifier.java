package com.creativemd.littletiles.common.tile.math.identifier;

import java.util.Arrays;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

public class LittleIdentifier {
	
	public final int[] identifier;
	public final LittleGridContext context;
	
	public LittleIdentifier(LittleGridContext context, int[] identifier) {
		this.context = context;
		this.identifier = identifier;
	}
	
	public LittleIdentifier(LittleTile tile) {
		this(tile.getContext(), tile.getIdentifier());
	}
	
	public LittleIdentifier(LittleIdentifierRelative identifier) {
		this(identifier.context, identifier.identifier);
	}
	
	public LittleIdentifier(LittleIdentifierAbsolute identifier) {
		this(identifier.context, identifier.identifier);
	}
	
	@Override
	public int hashCode() {
		return identifier.hashCode();
	}
	
	public boolean is(LittleGridContext context, int[] iddentifier) {
		return Arrays.equals(this.identifier, LittleIdentifierAbsolute.convertTo(identifier, context, this.context));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LittleIdentifier)
			return Arrays.equals(identifier, LittleIdentifierAbsolute.convertTo(((LittleIdentifier) obj).identifier, ((LittleIdentifier) obj).context, context));
		return false;
	}
	
}
