package com.creativemd.littletiles.common.structure.signal.logic.event;

import java.text.ParseException;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.logic.SignalCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;

public class SignalEvent {
	
	public final SignalCondition condition;
	public final SignalOutputCondition output;
	
	public SignalEvent(String pattern) throws ParseException {
		for (SignalMode mode : SignalMode.values()) {
			if (pattern.contains(mode.splitter)) {
				String[] parts = pattern.split(mode.splitter);
				if (parts.length != 2)
					throw new ParseException("Invalid event " + pattern, 0);
				this.output = SignalOutputCondition.create(parts[0], mode);
				this.condition = SignalCondition.parse(parts[1]);
				return;
			}
		}
		throw new ParseException("Invalid event pattern missing mode " + pattern, 0);
	}
	
	public void update(LittleStructure structure) {
		int bandwidth = output.getBandwidth(structure);
		if (bandwidth > 0) {
			boolean[] state = new boolean[bandwidth];
			condition.test(structure, state);
			output.setState(structure, state);
		}
	}
	
	public String write() {
		return output.write() + condition.write();
	}
	
}
