package com.creativemd.littletiles.common.structure.signal.logic;

import java.text.ParseException;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionOperator;

public enum SignalLogicOperator {
	
	AND('\n', false, "and") {
		@Override
		public SignalLogicOperator lower() {
			return null;
		}
		
		@Override
		public boolean perform(boolean first, boolean second) {
			return first && second;
		}
		
		@Override
		public SignalInputCondition create(SignalInputCondition[] conditions) {
			return new SignalInputConditionOperatorStackable(conditions) {
				
				@Override
				public boolean perform(boolean first, boolean second) {
					return first && second;
				}
				
				@Override
				public String operator() {
					return "";
				}
				
				@Override
				public boolean needsBrackets() {
					return false;
				}
			};
		}
	},
	OR('+', false, "or") {
		
		@Override
		public SignalLogicOperator lower() {
			return AND;
		}
		
		@Override
		public boolean perform(boolean first, boolean second) {
			return first || second;
		}
		
		@Override
		public SignalInputCondition create(SignalInputCondition[] conditions) {
			return new SignalInputConditionOperatorStackable(conditions) {
				
				@Override
				public boolean perform(boolean first, boolean second) {
					return first || second;
				}
				
				@Override
				public String operator() {
					return operator + "";
				}
				
				@Override
				public boolean needsBrackets() {
					return true;
				}
			};
		}
		
	},
	XOR('V', false, "xor") {
		
		@Override
		public SignalLogicOperator lower() {
			return OR;
		}
		
		@Override
		public boolean perform(boolean first, boolean second) {
			return first ^ second;
		}
		
		@Override
		public SignalInputCondition create(SignalInputCondition[] conditions) {
			return new SignalInputConditionOperatorStackable(conditions) {
				
				@Override
				public boolean perform(boolean first, boolean second) {
					return first ^ second;
				}
				
				@Override
				public String operator() {
					return operator + "";
				}
				
				@Override
				public boolean needsBrackets() {
					return true;
				}
			};
		}
	},
	BITWISE_AND('&', true, "b-and") {
		
		@Override
		public SignalLogicOperator lower() {
			return XOR;
		}
		
		@Override
		public boolean perform(boolean first, boolean second) {
			return first && second;
		}
		
		@Override
		public SignalInputCondition create(SignalInputCondition[] conditions) {
			return new SignalInputConditionOperatorStackableBitwise(conditions) {
				
				@Override
				public boolean perform(boolean first, boolean second) {
					return first && second;
				}
				
				@Override
				public String operator() {
					return operator + "";
				}
			};
		}
		
	},
	BITWISE_OR('|', true, "b-or") {
		
		@Override
		public SignalLogicOperator lower() {
			return BITWISE_AND;
		}
		
		@Override
		public boolean perform(boolean first, boolean second) {
			return first || second;
		}
		
		@Override
		public SignalInputCondition create(SignalInputCondition[] conditions) {
			return new SignalInputConditionOperatorStackableBitwise(conditions) {
				
				@Override
				public boolean perform(boolean first, boolean second) {
					return first || second;
				}
				
				@Override
				public String operator() {
					return operator + "";
				}
			};
		}
		
	},
	BITWISE_XOR('^', true, "b-xor") {
		
		@Override
		public SignalLogicOperator lower() {
			return BITWISE_OR;
		}
		
		@Override
		public boolean perform(boolean first, boolean second) {
			return first ^ second;
		}
		
		@Override
		public SignalInputCondition create(SignalInputCondition[] conditions) {
			return new SignalInputConditionOperatorStackableBitwise(conditions) {
				
				@Override
				public boolean perform(boolean first, boolean second) {
					return first ^ second;
				}
				
				@Override
				public String operator() {
					return operator + "";
				}
			};
		}
		
	};
	
	public static final SignalLogicOperator HIGHEST_GENERAL = XOR;
	
	public static final SignalLogicOperator HIGHEST = BITWISE_XOR;
	
	public static SignalLogicOperator getHighest(boolean includeBitwise) {
		if (includeBitwise)
			return HIGHEST;
		return HIGHEST_GENERAL;
	}
	
	public static SignalLogicOperator getOperator(char character) {
		switch (character) {
		case '&':
			return SignalLogicOperator.BITWISE_AND;
		case '+':
			return SignalLogicOperator.OR;
		case '|':
			return SignalLogicOperator.BITWISE_OR;
		case 'V':
			return SignalLogicOperator.XOR;
		case '^':
			return SignalLogicOperator.BITWISE_XOR;
		default:
			return null;
		}
	}
	
	public final char operator;
	public final boolean bitwise;
	public final String display;
	
	private SignalLogicOperator(char operator, boolean bitwise, String display) {
		this.operator = operator;
		this.bitwise = bitwise;
		this.display = display;
	}
	
	public abstract SignalLogicOperator lower();
	
	public boolean goOn(SignalPatternParser parser) throws ParseException {
		if (parser.hasNext()) {
			if (this == AND) {
				char next = parser.lookForNext(true);
				return next == '(' || next == '!' || next <= 122 & next >= 97;
			} else if (parser.lookForNext(true) == operator) {
				parser.next(true);
				return true;
			}
		}
		return false;
	}
	
	public abstract boolean perform(boolean first, boolean second);
	
	public abstract SignalInputCondition create(SignalInputCondition[] conditions);
	
	public static abstract class SignalInputConditionOperatorStackable extends SignalInputConditionOperator {
		
		public SignalInputCondition[] conditions;
		
		public SignalInputConditionOperatorStackable(SignalInputCondition[] conditions) {
			this.conditions = conditions;
		}
		
		@Override
		public boolean[] test(LittleStructure structure) {
			boolean[][] state = new boolean[conditions.length][];
			int size = 0;
			for (int i = 0; i < conditions.length; i++) {
				state[i] = conditions[i].test(structure, false);
				size = Math.max(size, state[i].length);
			}
			
			boolean[] result = new boolean[size];
			for (int i = 0; i < state.length; i++) {
				for (int j = 0; j < result.length; j++) {
					boolean value;
					if (state[i].length > j)
						value = state[i][j];
					else if (state[i].length == 1) // single sized arrays will be treated as infinite sized arrays (with one value)
						value = state[i][0];
					else
						value = false;
					if (i == 0)
						result[j] = value;
					else
						result[j] = perform(result[j], value);
				}
			}
			return result;
		}
		
		@Override
		public boolean testIndex(boolean[] state) {
			boolean result = false;
			for (int i = 0; i < state.length; i++) {
				if (i == 0)
					result = state[i];
				else
					result = perform(result, state[i]);
			}
			return result;
		}
		
		public abstract boolean perform(boolean first, boolean second);
		
		public abstract boolean needsBrackets();
		
		public abstract String operator();
		
		@Override
		public String write() {
			if (needsBrackets()) {
				String result = "(";
				for (int i = 0; i < conditions.length; i++) {
					if (i > 0)
						result += operator();
					result += conditions[i].write();
				}
				return result + ")";
			}
			String result = "";
			for (int i = 0; i < conditions.length; i++) {
				if (i > 0)
					result += operator();
				result += conditions[i].write();
			}
			return result;
		}
		
	}
	
	public static abstract class SignalInputConditionOperatorStackableBitwise extends SignalInputConditionOperatorStackable {
		
		public SignalInputConditionOperatorStackableBitwise(SignalInputCondition[] conditions) {
			super(conditions);
		}
		
		@Override
		public boolean[] test(LittleStructure structure) {
			boolean[][] state = new boolean[conditions.length][];
			int size = 0;
			for (int i = 0; i < conditions.length; i++) {
				state[i] = conditions[i].test(structure, true);
				size = Math.max(size, state[i].length);
			}
			
			boolean[] result = new boolean[size];
			for (int i = 0; i < state.length; i++) {
				for (int j = 0; j < result.length; j++) {
					boolean value;
					if (state[i].length > j)
						value = state[i][j];
					else
						value = false;
					if (i == 0)
						result[j] = value;
					else
						result[j] = perform(result[j], value);
				}
			}
			return result;
		}
		
		@Override
		public boolean needsBrackets() {
			return true;
		}
		
	}
	
}
