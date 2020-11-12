package com.creativemd.littletiles.common.structure.signal.logic;

public enum SignalMode {
	
	EQUAL("signal.mode.equal", "="), TOGGLE("signal.mode.toggle", "|="), PULSE("signal.mode.pulse", "~=");
	
	public final String translateKey;
	public final String splitter;
	
	private SignalMode(String translateKey, String splitter) {
		this.translateKey = translateKey;
		this.splitter = splitter;
	}
	
}
