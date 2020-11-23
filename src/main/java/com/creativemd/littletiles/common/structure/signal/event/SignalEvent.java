package com.creativemd.littletiles.common.structure.signal.event;

import java.text.ParseException;
import java.util.Arrays;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputCondition;

public class SignalEvent {
	
	public final SignalInputCondition condition;
	public final SignalOutputCondition output;
	
	public SignalEvent(LittleStructure structure, String pattern) throws ParseException {
		for (int i = SignalMode.values().length - 1; i >= 0; i--) {
			SignalMode mode = SignalMode.values()[i];
			if (pattern.contains(mode.splitter)) {
				String[] parts = pattern.split(mode.splitter);
				if (parts.length != 2)
					throw new ParseException("Invalid event " + pattern, 0);
				this.output = SignalOutputCondition.create(structure, parts[1], mode);
				this.condition = SignalInputCondition.parseInput(parts[0]);
				return;
			}
		}
		throw new ParseException("Invalid event pattern missing mode " + pattern, 0);
	}
	
	public void update(LittleStructure structure) {
		int bandwidth = output.getBandwidth(structure);
		if (bandwidth > 0) {
			boolean[] outputState = new boolean[bandwidth];
			boolean[] result = condition.test(structure, false);
			if (result.length == 1)
				Arrays.fill(result, result[0]);
			else
				for (int i = 0; i < result.length; i++)
					if (i < outputState.length)
						outputState[i] = result[i];
			output.schedule(structure, outputState);
		}
	}
	
	public String write() {
		return output.write() + condition.write();
	}
	
}
