package com.creativemd.littletiles.common.structure.signal.logic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.logic.SignalConditionExternal.SignalConditionExternalExact;
import com.creativemd.littletiles.common.structure.signal.logic.SignalConditionInternal.SignalConditionInternalExact;

public abstract class SignalCondition {
	
	public static class Parser {
		
		public final String pattern;
		private int pos = -1;
		
		public Parser(String pattern) {
			this.pattern = pattern;
		}
		
		public boolean hasNext() {
			return pos < pattern.length() - 1;
		}
		
		public char next() throws ParseException {
			try {
				return pattern.charAt(++pos);
			} catch (IndexOutOfBoundsException e) {
				throw throwException("Invalid end of pattern");
			}
		}
		
		public char lookForNext() {
			if (hasNext())
				return pattern.charAt(pos + 1);
			return '\n';
		}
		
		public int position() {
			return pos;
		}
		
		public ParseException throwException(String text) {
			return new ParseException(text + " '" + pattern + "'", pos);
		}
		
	}
	
	public static SignalCondition parse(String pattern) throws ParseException {
		return parseExpression(new Parser(pattern), '\n', PatternOperator.HIGHEST);
	}
	
	public static SignalCondition parseNextCondition(Parser parser) throws ParseException {
		while (parser.hasNext()) {
			char current = parser.next();
			
			if (current == ' ')
				continue;
			
			int type = Character.getType(current);
			
			if (current == '(')
				return parseExpression(parser, ')', PatternOperator.HIGHEST);
			else if (current == '!')
				return new SignalConditionNot(parseNextCondition(parser));
			else if (type == Character.LOWERCASE_LETTER)
				return parseInput(current, parser);
			else if (type == Character.UPPERCASE_LETTER)
				return parseInput(Character.toLowerCase(current), parser);
			else
				throw parser.throwException("Invalid signal pattern");
		}
		
		throw parser.throwException("Invalid signal pattern");
	}
	
	private static SignalCondition parseLower(Parser parser, char until, PatternOperator operator) throws ParseException {
		if (operator.lower() != null)
			return parseExpression(parser, until, operator.lower());
		return parseNextCondition(parser);
	}
	
	public static SignalCondition parseExpression(Parser parser, char until, PatternOperator operator) throws ParseException {
		SignalCondition first = parseLower(parser, until, operator);
		
		if (!parser.hasNext() || parser.lookForNext() == until)
			return first;
		
		if (operator.goOn(parser)) {
			List<SignalCondition> conditions = new ArrayList<>();
			conditions.add(first);
			conditions.add(parseLower(parser, until, operator));
			while (operator.goOn(parser))
				conditions.add(parseLower(parser, until, operator));
			return operator.create(conditions.toArray(new SignalCondition[conditions.size()]));
		}
		return first;
	}
	
	public static int parseDigit(char current, Parser parser) throws ParseException {
		String digit = "" + current;
		while (Character.isDigit(parser.lookForNext()))
			digit += parser.next();
		try {
			return Integer.parseInt(digit);
		} catch (NumberFormatException e) {
			throw parser.throwException("Invalid number " + digit);
		}
	}
	
	private static int[] parseInputExact(Parser parser) throws ParseException {
		List<Integer> indexes = new ArrayList<>();
		int index = -1;
		boolean first = true;
		while (parser.hasNext()) {
			char current = parser.next();
			if (current == ' ')
				continue;
			if (current == '}')
				break;
			else if (Character.isDigit(current) || ((first || index != -1) && current == '*')) {
				if (current == '*') {
					if (first)
						first = false;
					else if (index == -1)
						throw parser.throwException("* is not allowed when index prefix are used");
					index++;
					char next = parser.next();
					while (next == ' ')
						next = parser.next();
					if (next == ',')
						continue;
					else
						throw parser.throwException("Invalid input index=" + index);
				} else {
					int number = parseDigit(current, parser);
					char next = parser.next();
					if (next == ',') {
						if (number != '0' && number != '1')
							throw parser.throwException("Invalid bit value " + number);
						
						if (first)
							first = false;
						else if (index == -1)
							throw parser.throwException("All entries have to have an index prefix");
						index++;
						indexes.add(number == '0' ? -index : index);
						
						next = parser.next();
						while (next == ' ')
							next = parser.next();
						if (next == ',')
							continue;
						else
							throw parser.throwException("Invalid input index=" + index);
					} else if (next == ':') {
						int value = parseDigit(next, parser);
						if (value != '0' && value != '1')
							throw parser.throwException("Invalid bit value " + value);
						if (first)
							first = false;
						else if (index != -1)
							throw parser.throwException("No index prefix allowed when they are missing before");
						indexes.add(number == '0' ? -number : number);
						
						next = parser.next();
						while (next == ' ')
							next = parser.next();
						if (next == ',')
							continue;
						else
							throw parser.throwException("Invalid input index=" + index);
					} else
						throw parser.throwException("Invalid input index=" + index);
				}
			} else
				throw parser.throwException("Invalid character in input sequence '" + current + "' " + parser.pattern);
		}
		
		int[] result = new int[indexes.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = indexes.get(i);
		return result;
	}
	
	private static SignalConditionInput parseInput(char current, Parser parser) throws ParseException {
		int index = 0;
		if (Character.isDigit(parser.lookForNext()))
			index = parseDigit(parser.next(), parser);
		if (current == 'a' || current == 'i') {
			boolean external = current == 'i';
			if (parser.lookForNext() == '{')
				return external ? new SignalConditionExternalExact(index, parseInputExact(parser)) : new SignalConditionInternalExact(index, parseInputExact(parser));
			else
				return external ? new SignalConditionExternal(index) : new SignalConditionInternal(index);
		} else if (current == 'c') {
			if (parser.lookForNext() == '.') {
				parser.next();
				current = parser.next();
				int type = Character.getType(current);
				
				if (type == Character.LOWERCASE_LETTER)
					return new SignalConditionChild(index, parseInput(current, parser));
				else if (type == Character.UPPERCASE_LETTER)
					return new SignalConditionChild(index, parseInput(Character.toLowerCase(current), parser));
			} else
				throw parser.throwException("Missing dot after child input");
		}
		throw parser.throwException("Invalid input type " + current);
	}
	
	public abstract void test(LittleStructure structure, boolean[] state);
	
	public abstract String write();
	
	@Override
	public String toString() {
		return write();
	}
	
	public static class SignalConditionAND extends SignalCondition {
		
		public SignalCondition[] conditions;
		
		public SignalConditionAND(SignalCondition[] conditions) {
			this.conditions = conditions;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			for (int i = 0; i < conditions.length; i++) {
				boolean[] newState = new boolean[state.length];
				conditions[i].test(structure, newState);
				for (int j = 0; j < newState.length; j++)
					state[j] = state[j] && newState[j];
			}
		}
		
		@Override
		public String write() {
			String result = "";
			for (int i = 0; i < conditions.length; i++)
				result += conditions[i].write();
			return result;
		}
		
	}
	
	public static class SignalConditionOR extends SignalCondition {
		
		public SignalCondition[] conditions;
		
		public SignalConditionOR(SignalCondition[] conditions) {
			this.conditions = conditions;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			for (int i = 0; i < conditions.length; i++) {
				boolean[] newState = new boolean[state.length];
				conditions[i].test(structure, newState);
				for (int j = 0; j < newState.length; j++)
					state[j] = state[j] || newState[j];
			}
		}
		
		@Override
		public String write() {
			String result = "(";
			for (int i = 0; i < conditions.length; i++) {
				if (i > 0)
					result += "+";
				result += conditions[i].write();
			}
			return result + ")";
		}
		
	}
	
	public static class SignalConditionXOR extends SignalCondition {
		
		public SignalCondition[] conditions;
		
		public SignalConditionXOR(SignalCondition[] conditions) {
			this.conditions = conditions;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			boolean[][] newStates = new boolean[conditions.length][state.length];
			
			for (int i = 0; i < conditions.length; i++)
				conditions[i].test(structure, newStates[i]);
			
			for (int i = 0; i < state.length; i++) {
				boolean oneTrue = false;
				
				for (int j = 0; j < newStates.length; j++) {
					if (newStates[j][i])
						if (!oneTrue)
							oneTrue = true;
						else {
							oneTrue = false;
							break;
						}
					newStates[j][i] = oneTrue;
				}
			}
		}
		
		@Override
		public String write() {
			String result = "(";
			for (int i = 0; i < conditions.length; i++) {
				if (i > 0)
					result += "^";
				result += conditions[i].write();
			}
			return result + ")";
		}
		
	}
	
	public static class SignalConditionNot extends SignalCondition {
		
		public SignalCondition condition;
		
		public SignalConditionNot(SignalCondition condition) {
			this.condition = condition;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			condition.test(structure, state);
			for (int i = 0; i < state.length; i++)
				state[i] = !state[i];
		}
		
		@Override
		public String write() {
			return "!" + condition.write();
		}
		
	}
	
	public static abstract class SignalConditionInput extends SignalCondition {
		
		public final int childId;
		
		public SignalConditionInput(int id) {
			this.childId = id;
		}
	}
	
	private static enum PatternOperator {
		
		AND('\n') {
			@Override
			public PatternOperator lower() {
				return null;
			}
			
			@Override
			public SignalCondition create(SignalCondition[] conditions) {
				return new SignalConditionAND(conditions);
			}
		},
		OR('+') {
			
			@Override
			public PatternOperator lower() {
				return PatternOperator.AND;
			}
			
			@Override
			public SignalCondition create(SignalCondition[] conditions) {
				return new SignalConditionOR(conditions);
			}
			
		},
		XOR('^') {
			
			@Override
			public PatternOperator lower() {
				return PatternOperator.OR;
			}
			
			@Override
			public SignalCondition create(SignalCondition[] conditions) {
				return new SignalConditionXOR(conditions);
			}
		};
		
		public static final PatternOperator HIGHEST = XOR;
		
		public final char operator;
		
		private PatternOperator(char operator) {
			this.operator = operator;
		}
		
		public abstract PatternOperator lower();
		
		public boolean goOn(Parser parser) throws ParseException {
			if (parser.hasNext()) {
				while (parser.lookForNext() == ' ')
					parser.next();
				
				if (this == AND) {
					char next = parser.lookForNext();
					return next == '(' || next == '!' || next <= 122 & next >= 97;
				} else if (parser.lookForNext() == operator) {
					parser.next();
					return true;
				}
			}
			return false;
		}
		
		public abstract SignalCondition create(SignalCondition[] conditions);
		
	}
	
}
