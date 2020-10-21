package com.creativemd.littletiles.common.structure.signal.logic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.ISignalInput;

public abstract class SignalCondition {
	
	public static final int charAmount = 26;
	
	private static class Parser {
		
		public final String pattern;
		private int pos = -1;
		
		public Parser(String pattern) {
			this.pattern = pattern;
		}
		
		public boolean hasNext() {
			return pos < pattern.length() - 1;
		}
		
		public char next() {
			return pattern.charAt(pos++);
		}
		
		public char lookForNext() {
			if (hasNext())
				return pattern.charAt(pos + 1);
			return '\n';
		}
		
		public int position() {
			return pos;
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
			
			if (current == '(')
				return parseExpression(parser, ')', PatternOperator.HIGHEST);
			else if (current == '!')
				return new SignalConditionNot(parseNextCondition(parser));
			else if (current <= 122 && current >= 97)
				return parseInput(current, parser);
			else
				throw new ParseException("Invalid signal pattern " + parser.pattern, parser.position());
		}
		
		throw new ParseException("Invalid signal pattern " + parser.pattern, parser.position());
	}
	
	public static SignalCondition parseExpression(Parser parser, char until, PatternOperator operator) throws ParseException {
		SignalCondition first;
		if (operator.lower() != null)
			first = parseExpression(parser, until, operator);
		else
			first = parseNextCondition(parser);
		
		if (!parser.hasNext() || parser.lookForNext() == until)
			return first;
		
		if (operator.goOn(parser)) {
			List<SignalCondition> conditions = new ArrayList<>();
			conditions.add(first);
			conditions.add(parseNextCondition(parser));
			while (operator.goOn(parser))
				conditions.add(parseNextCondition(parser));
			return operator.create(conditions.toArray(new SignalCondition[conditions.size()]));
		}
		return first;
	}
	
	private static int parseInputIndex(char current, Parser parser) throws ParseException {
		int index = indexOf(current);
		index += charAmount * parseDigit(parser.next(), parser);
		return index;
	}
	
	private static int parseDigit(char current, Parser parser) throws ParseException {
		String digit = "";
		while (Character.isDigit(parser.lookForNext()))
			digit += parser.next();
		try {
			return Integer.parseInt(digit);
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid number " + digit, parser.position());
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
						throw new ParseException("* is not allowed when index prefix are used", parser.position());
					index++;
					char next = parser.next();
					while (next == ' ')
						next = parser.next();
					if (next == ',')
						continue;
					else
						throw new ParseException("Invalid input index=" + index, parser.position());
				} else {
					int number = parseDigit(current, parser);
					char next = parser.next();
					if (next == ',') {
						if (number != '0' && number != '1')
							throw new ParseException("Invalid bit value " + number, parser.position());
						
						if (first)
							first = false;
						else if (index == -1)
							throw new ParseException("All entries have to have an index prefix", parser.position());
						index++;
						indexes.add(number == '0' ? -index : index);
						
						next = parser.next();
						while (next == ' ')
							next = parser.next();
						if (next == ',')
							continue;
						else
							throw new ParseException("Invalid input index=" + index, parser.position());
					} else if (next == ':') {
						int value = parseDigit(next, parser);
						if (value != '0' && value != '1')
							throw new ParseException("Invalid bit value " + value, parser.position());
						if (first)
							first = false;
						else if (index != -1)
							throw new ParseException("No index prefix allowed when they are missing before", parser.position());
						indexes.add(number == '0' ? -number : number);
						
						next = parser.next();
						while (next == ' ')
							next = parser.next();
						if (next == ',')
							continue;
						else
							throw new ParseException("Invalid input index=" + index, parser.position());
					} else
						throw new ParseException("Invalid input index=" + index, parser.position());
				}
			} else
				throw new ParseException("Invalid character in input sequence '" + current + "' " + parser.pattern, parser.position());
		}
		
		int[] result = new int[indexes.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = indexes.get(i);
		return result;
	}
	
	private static SignalCondition parseInput(char current, Parser parser) throws ParseException {
		int index = parseInputIndex(current, parser);
		if (parser.lookForNext() == '{')
			return new SignalConditionIsExact(index, parseInputExact(parser));
		else if (parser.lookForNext() == '-') {
			parser.next();
			int secondIndex = parseInputIndex(current, parser);
			if (index >= secondIndex)
				throw new IllegalArgumentException("Second index cannot be equal or higher than first " + index + "-" + secondIndex);
			if (parser.lookForNext() == '{')
				return new SignalConditionIsBetweenExact(index, secondIndex, parseInputExact(parser));
			else
				return new SignalConditionIsBetween(index, secondIndex);
		} else
			return new SignalConditionIs(index);
	}
	
	private static int indexOf(char letter) {
		switch (letter) {
		case 'a':
			return 0;
		case 'b':
			return 1;
		case 'c':
			return 2;
		case 'd':
			return 3;
		case 'e':
			return 4;
		case 'f':
			return 5;
		case 'g':
			return 6;
		case 'h':
			return 7;
		case 'i':
			return 8;
		case 'j':
			return 9;
		case 'k':
			return 10;
		case 'l':
			return 11;
		case 'm':
			return 12;
		case 'n':
			return 13;
		case 'o':
			return 14;
		case 'p':
			return 15;
		case 'q':
			return 16;
		case 'r':
			return 17;
		case 's':
			return 18;
		case 't':
			return 19;
		case 'u':
			return 20;
		case 'v':
			return 21;
		case 'w':
			return 22;
		case 'x':
			return 23;
		case 'y':
			return 24;
		case 'z':
			return 25;
		default:
			return -1;
		}
	}
	
	private static char charIndex(int index) {
		switch (index) {
		case 0:
			return 'a';
		case 1:
			return 'b';
		case 2:
			return 'c';
		case 3:
			return 'd';
		case 4:
			return 'e';
		case 5:
			return 'f';
		case 6:
			return 'g';
		case 7:
			return 'h';
		case 8:
			return 'i';
		case 9:
			return 'j';
		case 10:
			return 'k';
		case 11:
			return 'l';
		case 12:
			return 'm';
		case 13:
			return 'n';
		case 14:
			return 'o';
		case 15:
			return 'p';
		case 16:
			return 'q';
		case 17:
			return 'r';
		case 18:
			return 's';
		case 19:
			return 't';
		case 20:
			return 'u';
		case 21:
			return 'v';
		case 22:
			return 'w';
		case 23:
			return 'x';
		case 24:
			return 'y';
		case 25:
			return 'z';
		default:
			return '\n';
		}
	}
	
	private static String indexToChar(int index) {
		int digit = index / charAmount;
		return charIndex(index - digit * charAmount) + "" + digit;
	}
	
	private static ISignalInput get(LittleStructure structure, int id) {
		if (id > 0 && id < structure.getChildren().size())
			try {
				LittleStructure child = structure.getChild(id).getStructure();
				if (child instanceof ISignalInput)
					return (ISignalInput) child;
			} catch (CorruptedConnectionException | NotYetConnectedException e) {}
		return null;
	}
	
	public static boolean is(LittleStructure structure, int id) {
		ISignalInput child = get(structure, id);
		if (child != null)
			return BooleanUtils.any(child.getState());
		return false;
	}
	
	public static boolean is(LittleStructure structure, int id, int index) {
		ISignalInput child = get(structure, id);
		if (child != null) {
			boolean[] state = child.getState();
			if (index < state.length)
				return state[index];
		}
		return false;
	}
	
	public static boolean[] getState(LittleStructure structure, int id) {
		ISignalInput child = get(structure, id);
		if (child != null)
			return child.getState();
		return null;
	}
	
	public static boolean is(boolean[] state, int[] indexes) {
		for (int i = 0; i < indexes.length; i++) {
			int index = indexes[i];
			if (index > 0 != (Math.abs(index) < state.length && state[Math.abs(index)]))
				return false;
		}
		return true;
	}
	
	public abstract boolean test(LittleStructure structure);
	
	public abstract String write();
	
	public static class SignalConditionAND extends SignalCondition {
		
		public SignalCondition[] conditions;
		
		public SignalConditionAND(SignalCondition[] conditions) {
			this.conditions = conditions;
		}
		
		@Override
		public boolean test(LittleStructure structure) {
			for (int i = 0; i < conditions.length; i++)
				if (!conditions[i].test(structure))
					return false;
			return true;
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
		public boolean test(LittleStructure structure) {
			for (int i = 0; i < conditions.length; i++)
				if (conditions[i].test(structure))
					return true;
			return false;
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
		public boolean test(LittleStructure structure) {
			boolean oneTrue = false;
			for (int i = 0; i < conditions.length; i++)
				if (conditions[i].test(structure)) {
					if (oneTrue)
						return false;
					oneTrue = true;
				}
			return oneTrue;
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
	
	public static class SignalConditionIs extends SignalCondition {
		
		public int id;
		
		public SignalConditionIs(int id) {
			this.id = id;
		}
		
		@Override
		public boolean test(LittleStructure structure) {
			return is(structure, id);
		}
		
		@Override
		public String write() {
			return indexToChar(id);
		}
		
	}
	
	public static class SignalConditionIsBetween extends SignalCondition {
		
		public int startId;
		public int endId;
		
		public SignalConditionIsBetween(int startId, int endId) {
			this.startId = startId;
			this.endId = endId;
		}
		
		@Override
		public boolean test(LittleStructure structure) {
			for (int i = startId; i <= endId; i++) {
				ISignalInput child = get(structure, i);
				if (child != null)
					return BooleanUtils.any(child.getState());
			}
			return false;
		}
		
		@Override
		public String write() {
			return indexToChar(startId) + "-" + indexToChar(endId);
		}
		
	}
	
	public static class SignalConditionIsBetweenExact extends SignalCondition {
		
		public int startId;
		public int endId;
		public int[] indexes;
		
		public SignalConditionIsBetweenExact(int startId, int endId, int[] indexes) {
			this.startId = startId;
			this.endId = endId;
			this.indexes = indexes;
		}
		
		@Override
		public boolean test(LittleStructure structure) {
			for (int i = startId; i <= endId; i++) {
				ISignalInput child = get(structure, i);
				if (child != null)
					return is(child.getState(), indexes);
			}
			return false;
		}
		
		@Override
		public String write() {
			String result = indexToChar(startId) + "-" + indexToChar(endId) + "{";
			for (int i = 0; i < indexes.length; i++) {
				int index = indexes[i];
				if (index < 0)
					result += "!";
				result += (char) index;
			}
			return result + "}";
		}
		
	}
	
	public static class SignalConditionIsExact extends SignalCondition {
		
		public int id;
		
		public int[] indexes;
		
		public SignalConditionIsExact(int id, int[] indexes) {
			this.id = id;
			this.indexes = indexes;
		}
		
		@Override
		public boolean test(LittleStructure structure) {
			return is(getState(structure, id), indexes);
		}
		
		@Override
		public String write() {
			String result = indexToChar(id) + "{";
			for (int i = 0; i < indexes.length; i++) {
				int index = indexes[i];
				if (index < 0)
					result += "!";
				result += (char) index;
			}
			return result + "}";
		}
		
	}
	
	public static class SignalConditionNot extends SignalCondition {
		
		public SignalCondition condition;
		
		public SignalConditionNot(SignalCondition condition) {
			this.condition = condition;
		}
		
		@Override
		public boolean test(LittleStructure structure) {
			return !condition.test(structure);
		}
		
		@Override
		public String write() {
			return "!" + condition.write();
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
		
		public boolean goOn(Parser parser) {
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
