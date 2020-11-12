package com.creativemd.littletiles.common.structure.signal.logic.event;

import java.text.ParseException;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.SignalUtils;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.logic.SignalCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalCondition.Parser;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;

public abstract class SignalOutputCondition {
	
	public final SignalOutputConditionTarget target;
	
	public SignalOutputCondition(SignalOutputConditionTarget target) {
		this.target = target;
	}
	
	public abstract SignalMode getMode();
	
	public void setState(LittleStructure structure, boolean[] state) {
		ISignalComponent component = target.getTarget(structure);
		if (component != null)
			target.setState(component, state);
	}
	
	public int getBandwidth(LittleStructure structure) {
		return target.getBandwidth(structure);
	}
	
	public String write() {
		return target.write() + getMode().splitter;
	}
	
	public static SignalOutputCondition create(String pattern, SignalMode mode) throws ParseException {
		SignalOutputConditionTarget target = parseTarget(new Parser(pattern));
		switch (mode) {
		case EQUAL:
			return new SignalOutputConditionEqual(target);
		default:
			throw new ParseException("Invalid mode " + mode.name(), 0);
		}
	}
	
	public static SignalOutputConditionTarget parseTarget(Parser parser) throws ParseException {
		char begin = parser.next();
		if (begin == 'o' || begin == 'b') {
			boolean external = begin == 'o';
			
			int output = 0;
			if (Character.isDigit(parser.lookForNext()))
				output = SignalCondition.parseDigit(parser.next(), parser);
			
			if (parser.hasNext()) {
				char current = parser.next();
				if (current != '(')
					throw parser.throwException("Invalid char " + current);
				
				int start = SignalCondition.parseDigit(parser.next(), parser);
				current = parser.next();
				
				if (current == ')')
					return external ? new SignalOutputConditionTargetExternalIndex(output, start) : new SignalOutputConditionTargetInternalIndex(output, start);
				else if (current == '-') {
					int end = SignalCondition.parseDigit(parser.next(), parser);
					if (parser.next() != ')')
						throw parser.throwException("Invalid char " + current);
					if (end <= start)
						throw parser.throwException("Invalid second index " + start + " < " + end);
					int length = end - start;
					return external ? new SignalOutputConditionTargetExternalIndexLength(output, start, length) : new SignalOutputConditionTargetInternalIndexLength(output, start, length);
				} else
					throw parser.throwException("Invalid char " + current);
			} else
				return external ? new SignalOutputConditionTargetExternal(output) : new SignalOutputConditionTargetInternal(output);
		} else if (begin == 'c') {
			int output = 0;
			if (Character.isDigit(parser.lookForNext()))
				output = SignalCondition.parseDigit(parser.next(), parser);
			
			if (parser.lookForNext() == '.') {
				parser.next();
				return new SignalOutputConditionTargetChild(output, parseTarget(parser));
			} else
				throw parser.throwException("Missing dot after child input");
		} else
			throw parser.throwException("Invalid char " + begin);
	}
	
	public static abstract class SignalOutputConditionTarget {
		
		public abstract ISignalComponent getTarget(LittleStructure structure);
		
		public abstract String write();
		
		public int getBandwidth(LittleStructure structure) {
			ISignalComponent component = getTarget(structure);
			if (component != null)
				return component.getBandwidth();
			return 1;
		}
		
		public void setState(ISignalComponent component, boolean[] state) {
			component.updateState(state);
		}
		
	}
	
	public static class SignalOutputConditionTargetChild extends SignalOutputConditionTarget {
		
		public final int child;
		public final SignalOutputConditionTarget subTarget;
		
		public SignalOutputConditionTargetChild(int child, SignalOutputConditionTarget subTarget) {
			this.child = child;
			this.subTarget = subTarget;
		}
		
		@Override
		public ISignalComponent getTarget(LittleStructure structure) {
			if (child < structure.getChildren().size())
				try {
					return subTarget.getTarget(structure.getChild(child).getStructure());
				} catch (CorruptedConnectionException | NotYetConnectedException e) {}
			return null;
		}
		
		@Override
		public String write() {
			return "c" + child + "." + subTarget.write();
		}
		
	}
	
	public static class SignalOutputConditionTargetExternal extends SignalOutputConditionTarget {
		
		public final int output;
		
		public SignalOutputConditionTargetExternal(int output) {
			this.output = output;
		}
		
		@Override
		public ISignalComponent getTarget(LittleStructure structure) {
			return SignalUtils.getOutput(structure, output, true);
		}
		
		@Override
		public String write() {
			return "o" + output;
		}
		
	}
	
	public static class SignalOutputConditionTargetExternalIndex extends SignalOutputConditionTargetExternal {
		
		public final int index;
		
		public SignalOutputConditionTargetExternalIndex(int output, int index) {
			super(output);
			this.index = index;
		}
		
		@Override
		public ISignalComponent getTarget(LittleStructure structure) {
			return SignalUtils.getOutput(structure, output, true);
		}
		
		@Override
		public String write() {
			return super.write() + "{" + index + "}";
		}
		
		@Override
		public int getBandwidth(LittleStructure structure) {
			return 1;
		}
		
		@Override
		public void setState(ISignalComponent component, boolean[] state) {
			component.updateState(index, state);
		}
		
	}
	
	public static class SignalOutputConditionTargetExternalIndexLength extends SignalOutputConditionTargetExternal {
		
		public final int index;
		public final int length;
		
		public SignalOutputConditionTargetExternalIndexLength(int output, int index, int length) {
			super(output);
			this.index = index;
			this.length = length;
		}
		
		@Override
		public ISignalComponent getTarget(LittleStructure structure) {
			return SignalUtils.getOutput(structure, output, true);
		}
		
		@Override
		public String write() {
			return super.write() + "{" + index + "-" + index + length + "}";
		}
		
		@Override
		public int getBandwidth(LittleStructure structure) {
			return length;
		}
		
		@Override
		public void setState(ISignalComponent component, boolean[] state) {
			component.updateState(index, state);
		}
		
	}
	
	public static class SignalOutputConditionTargetInternal extends SignalOutputConditionTarget {
		
		public final int output;
		
		public SignalOutputConditionTargetInternal(int output) {
			this.output = output;
		}
		
		@Override
		public ISignalComponent getTarget(LittleStructure structure) {
			return SignalUtils.getOutput(structure, output, false);
		}
		
		@Override
		public String write() {
			return "b" + output;
		}
		
	}
	
	public static class SignalOutputConditionTargetInternalIndex extends SignalOutputConditionTargetInternal {
		
		public final int index;
		
		public SignalOutputConditionTargetInternalIndex(int output, int index) {
			super(output);
			this.index = index;
		}
		
		@Override
		public ISignalComponent getTarget(LittleStructure structure) {
			return SignalUtils.getOutput(structure, output, false);
		}
		
		@Override
		public String write() {
			return super.write() + "{" + index + "}";
		}
		
		@Override
		public int getBandwidth(LittleStructure structure) {
			return 1;
		}
		
		@Override
		public void setState(ISignalComponent component, boolean[] state) {
			component.updateState(index, state);
		}
		
	}
	
	public static class SignalOutputConditionTargetInternalIndexLength extends SignalOutputConditionTargetInternal {
		
		public final int index;
		public final int length;
		
		public SignalOutputConditionTargetInternalIndexLength(int output, int index, int length) {
			super(output);
			this.index = index;
			this.length = length;
		}
		
		@Override
		public ISignalComponent getTarget(LittleStructure structure) {
			return SignalUtils.getOutput(structure, output, false);
		}
		
		@Override
		public String write() {
			return super.write() + "{" + index + "-" + index + length + "}";
		}
		
		@Override
		public int getBandwidth(LittleStructure structure) {
			return length;
		}
		
		@Override
		public void setState(ISignalComponent component, boolean[] state) {
			component.updateState(index, state);
		}
		
	}
	
	public static class SignalOutputConditionEqual extends SignalOutputCondition {
		
		public SignalOutputConditionEqual(SignalOutputConditionTarget target) {
			super(target);
		}
		
		@Override
		public SignalMode getMode() {
			return SignalMode.EQUAL;
		}
		
	}
	
	public static class SignalOutputConditionToggle extends SignalOutputCondition {
		
		public SignalOutputConditionToggle(SignalOutputConditionTarget target) {
			super(target);
		}
		
		@Override
		public SignalMode getMode() {
			return SignalMode.TOGGLE;
		}
		
	}
	
	public static class SignalOutputConditionPulse extends SignalOutputCondition {
		
		public SignalOutputConditionPulse(SignalOutputConditionTarget target) {
			super(target);
		}
		
		@Override
		public SignalMode getMode() {
			return SignalMode.PULSE;
		}
		
	}
}
