package com.creativemd.littletiles.common.structure.signal.input;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.SignalUtils;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator;
import com.creativemd.littletiles.common.structure.signal.logic.SignalPatternParser;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget;

public class SignalInputVariable extends SignalInputCondition {
	
	private static int[] parseInputExact(SignalPatternParser parser) throws ParseException {
		List<Integer> indexes = new ArrayList<>();
		int index = -1;
		boolean first = true;
		while (parser.hasNext()) {
			char next = parser.lookForNext(true);
			if (next == '}') {
				parser.next(true);
				break;
			} else if (Character.isDigit(next) || ((first || index != -1) && next == '*')) {
				if (next == '*') {
					if (first)
						first = false;
					else if (index == -1)
						throw parser.exception("* is not allowed when index prefix are used");
					index++;
					next = parser.next(true);
					if (next == ',')
						continue;
					else if (next == '}')
						break;
					else
						throw parser.exception("Invalid input index=" + index);
				} else {
					int number = parser.parseNumber();
					next = parser.next(true);
					if (next == ',') {
						if (number != '0' && number != '1')
							throw parser.exception("Invalid bit value " + number);
						
						if (first)
							first = false;
						else if (index == -1)
							throw parser.exception("All entries have to have an index prefix");
						index++;
						indexes.add(number == '0' ? -index : index);
						
						next = parser.next(true);
						if (next == ',')
							continue;
						else if (next == '}')
							break;
						else
							throw parser.exception("Invalid input index=" + index);
					} else if (next == ':') {
						int value = parser.parseNumber();
						if (value != '0' && value != '1')
							throw parser.exception("Invalid bit value " + value);
						if (first)
							first = false;
						else if (index != -1)
							throw parser.exception("No index prefix allowed when they are missing before");
						indexes.add(number == '0' ? -number : number);
						
						next = parser.next(true);
						if (next == ',')
							continue;
						else if (next == '}')
							break;
						else
							throw parser.exception("Invalid input index=" + index);
					} else
						throw parser.exception("Invalid input index=" + index);
				}
			} else
				throw parser.invalidChar(next);
		}
		if (indexes.isEmpty())
			return null;
		int[] result = new int[indexes.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = indexes.get(i);
		return result;
	}
	
	public static SignalInputVariable parseInput(SignalPatternParser parser, boolean insideVariable) throws ParseException {
		SignalTarget target = SignalTarget.parseTarget(parser, false, insideVariable);
		if (!insideVariable && parser.lookForNext(false) == '{') {
			char next = parser.lookForNext(true);
			if (Character.isDigit(next)) {
				int[] indexes = parseInputExact(parser);
				if (indexes != null)
					return new SignalInputVariablePattern(target, indexes);
				return new SignalInputVariable(target);
			}
			
			SignalLogicOperator operator = SignalLogicOperator.getOperator(next);
			if (operator != null) {
				parser.next(true);
				if (parser.next(true) == '}')
					return new SignalInputVariableOperator(target, operator);
				else
					throw parser.invalidChar(parser.current());
			}
			
			return new SignalInputVariableEquation(target, SignalInputCondition.parseExpression(parser, '}', false, true));
		} else
			return new SignalInputVariable(target);
	}
	
	public final SignalTarget target;
	
	public SignalInputVariable(SignalTarget target) {
		this.target = target;
	}
	
	@Override
	public boolean[] test(LittleStructure structure, boolean forceBitwise) {
		boolean[] state = target.getState(structure);
		if (forceBitwise)
			return state;
		return BooleanUtils.asArray(BooleanUtils.any(state));
		
	}
	
	@Override
	public boolean testIndex(boolean[] state) {
		if (target.isIndexVariable() && target.child < state.length)
			return state[target.child];
		return false;
	}
	
	@Override
	public String write() {
		return target.write();
	}
	
	@Override
	public float calculateDelay() {
		return VARIABLE_DURATION;
	}
	
	public static class SignalInputVariableOperator extends SignalInputVariable {
		
		public final SignalLogicOperator operator;
		
		public SignalInputVariableOperator(SignalTarget target, SignalLogicOperator operator) {
			super(target);
			this.operator = operator;
		}
		
		@Override
		public boolean[] test(LittleStructure structure, boolean forceBitwise) {
			boolean[] state = target.getState(structure);
			boolean result = false;
			for (int i = 0; i < state.length; i++)
				if (i == 0)
					result = state[i];
				else
					result = operator.perform(result, state[i]);
			return BooleanUtils.asArray(result);
		}
		
		@Override
		public String write() {
			return super.write() + "{" + operator.operator + "}";
		}
	}
	
	public static class SignalInputVariablePattern extends SignalInputVariable {
		
		public final int[] indexes;
		
		public SignalInputVariablePattern(SignalTarget target, int[] indexes) {
			super(target);
			this.indexes = indexes;
		}
		
		@Override
		public boolean[] test(LittleStructure structure, boolean forceBitwise) {
			return BooleanUtils.asArray(SignalUtils.is(target.getState(structure), indexes));
		}
		
		@Override
		public String write() {
			String result = super.write() + "{";
			for (int i = 0; i < indexes.length; i++) {
				int index = indexes[i];
				if (index < 0)
					result += ",";
				result += (char) index;
			}
			return result + "}";
		}
		
	}
	
	public static class SignalInputVariableEquation extends SignalInputVariable {
		
		public final SignalInputCondition condition;
		
		public SignalInputVariableEquation(SignalTarget target, SignalInputCondition condition) {
			super(target);
			this.condition = condition;
		}
		
		@Override
		public boolean[] test(LittleStructure structure, boolean forceBitwise) {
			return BooleanUtils.asArray(condition.testIndex(target.getState(structure)));
		}
		
		@Override
		public String write() {
			return super.write() + "{" + condition.write() + "}";
		}
		
		@Override
		public float calculateDelay() {
			return super.calculateDelay() + condition.calculateDelay();
		}
		
	}
	
}
