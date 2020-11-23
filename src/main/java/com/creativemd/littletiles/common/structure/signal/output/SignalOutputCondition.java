package com.creativemd.littletiles.common.structure.signal.output;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.logic.SignalPatternParser;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget;

public abstract class SignalOutputCondition {
	
	public final SignalTarget target;
	public final int delay;
	
	public SignalOutputCondition(int delay, SignalTarget target) {
		this.target = target;
		this.delay = delay;
	}
	
	public abstract SignalMode getMode();
	
	public abstract void schedule(LittleStructure structure, boolean[] state);
	
	public void performStateChange(LittleStructure structure, boolean[] state) {
		ISignalComponent component = target.getTarget(structure);
		if (component != null)
			target.setState(component, state);
	}
	
	public int getBandwidth(LittleStructure structure) {
		return target.getBandwidth(structure);
	}
	
	public String write() {
		int[] data = getExtraData();
		if (data.length == 0)
			return target.write() + getMode().splitter;
		String bracket = "[";
		for (int i = 0; i < data.length; i++) {
			if (i > 0)
				bracket += ",";
			bracket += "" + data[i];
		}
		return bracket + "]" + target.write() + getMode().splitter;
	}
	
	public abstract int[] getExtraData();
	
	public static SignalOutputCondition create(LittleStructure structure, String pattern, SignalMode mode) throws ParseException {
		SignalPatternParser parser = new SignalPatternParser(pattern);
		SignalTarget target = SignalTarget.parseTarget(parser, true, false);
		List<Integer> numbers = new ArrayList<>();
		if (parser.lookForNext(true) == '{') {
			parser.next(true);
			if (parser.lookForNext(true) == '}') {
				while (numbers.add(parser.parseNumber())) {
					char current = parser.next(true);
					if (current == ',')
						continue;
					else if (current == '}')
						break;
					else
						throw parser.invalidChar(current);
				}
			}
		}
		if (numbers.isEmpty())
			numbers.add(1);
		return mode.create(structure, numbers, target);
	}
	
}
