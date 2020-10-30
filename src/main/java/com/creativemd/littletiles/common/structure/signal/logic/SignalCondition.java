package com.creativemd.littletiles.common.structure.signal.logic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.ISignalStructureInternalInput;
import com.creativemd.littletiles.common.structure.signal.input.InternalSignalInput;

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
			return pattern.charAt(++pos);
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
			
			int type = Character.getType(current);
			
			if (current == '(')
				return parseExpression(parser, ')', PatternOperator.HIGHEST);
			else if (current == '!')
				return new SignalConditionNot(parseNextCondition(parser));
			else if (type == Character.LOWERCASE_LETTER)
				//else if (current <= 122 && current >= 97)
				return parseInputExternal(current, parser);
			else if (type == Character.UPPERCASE_LETTER)
				return parseInputInternal(current, parser);
			else
				throw new ParseException("Invalid signal pattern " + parser.pattern, parser.position());
		}
		
		throw new ParseException("Invalid signal pattern " + parser.pattern, parser.position());
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
	
	private static int parseInputIndex(char current, Parser parser) throws ParseException {
		int index = indexOf(Character.toLowerCase(current));
		if (Character.isDigit(parser.lookForNext()))
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
	
	private static SignalCondition parseInputExternal(char current, Parser parser) throws ParseException {
		int index = parseInputIndex(current, parser);
		if (parser.lookForNext() == '{')
			return new SignalConditionExternalIsExact(index, parseInputExact(parser));
		else if (parser.lookForNext() == '-') {
			parser.next();
			int secondIndex = parseInputIndex(current, parser);
			if (index >= secondIndex)
				throw new IllegalArgumentException("Second index cannot be equal or higher than first " + index + "-" + secondIndex);
			if (parser.lookForNext() == '{')
				return new SignalConditionExternalIsBetweenExact(index, secondIndex, parseInputExact(parser));
			else
				return new SignalConditionExternalIsBetween(index, secondIndex);
		} else
			return new SignalConditionExternalIs(index);
	}
	
	private static SignalCondition parseInputInternal(char current, Parser parser) throws ParseException {
		int index = parseInputIndex(current, parser);
		if (parser.lookForNext() == '{')
			return new SignalConditionInternalIsExact(index, parseInputExact(parser));
		else if (parser.lookForNext() == '-') {
			parser.next();
			int secondIndex = parseInputIndex(current, parser);
			if (index >= secondIndex)
				throw new IllegalArgumentException("Second index cannot be equal or higher than first " + index + "-" + secondIndex);
			if (parser.lookForNext() == '{')
				return new SignalConditionInternalIsBetweenExact(index, secondIndex, parseInputExact(parser));
			else
				return new SignalConditionInternalIsBetween(index, secondIndex);
		} else
			return new SignalConditionInternalIs(index);
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
	
	private static String indexToChar(int index, boolean external) {
		int digit = index / charAmount;
		char front = charIndex(index - digit * charAmount);
		if (!external)
			front = Character.toUpperCase(front);
		return front + "" + (digit > 0 ? digit : "");
	}
	
	private static InternalSignalInput getInternal(LittleStructure structure, int id) {
		if (id > 0 && id < structure.getChildren().size() && structure instanceof ISignalStructureInternalInput)
			return ((ISignalStructureInternalInput) structure).getInput(id);
		return null;
	}
	
	public static boolean[] getInternalState(LittleStructure structure, int id) {
		InternalSignalInput child = getInternal(structure, id);
		if (child != null)
			return child.getState();
		return null;
	}
	
	private static ISignalStructureComponent getExternal(LittleStructure structure, int id) {
		if (id > 0 && id < structure.getChildren().size())
			try {
				LittleStructure child = structure.getChild(id).getStructure();
				if (child instanceof ISignalStructureComponent && ((ISignalStructureComponent) child).getType() == SignalComponentType.INPUT)
					return (ISignalStructureComponent) child;
			} catch (CorruptedConnectionException | NotYetConnectedException e) {}
		return null;
	}
	
	public static boolean[] getExternalState(LittleStructure structure, int id) {
		ISignalComponent child = getExternal(structure, id);
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
	
	public static void combine(boolean[] state, boolean[] second) {
		int count = Math.min(state.length, second.length);
		for (int i = 0; i < count; i++)
			state[i] = second[i];
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
	
	public static class SignalConditionInternalIs extends SignalCondition {
		
		public int id;
		
		public SignalConditionInternalIs(int id) {
			this.id = id;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			combine(state, getInternalState(structure, id));
		}
		
		@Override
		public String write() {
			return indexToChar(id, false);
		}
		
	}
	
	public static class SignalConditionInternalIsBetween extends SignalCondition {
		
		public int startId;
		public int endId;
		
		public SignalConditionInternalIsBetween(int startId, int endId) {
			this.startId = startId;
			this.endId = endId;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			for (int i = startId; i <= endId; i++) {
				InternalSignalInput child = getInternal(structure, i);
				if (child != null)
					combine(state, child.getState());
			}
		}
		
		@Override
		public String write() {
			return indexToChar(startId, false) + "-" + indexToChar(endId, false);
		}
		
	}
	
	public static class SignalConditionInternalIsBetweenExact extends SignalCondition {
		
		public int startId;
		public int endId;
		public int[] indexes;
		
		public SignalConditionInternalIsBetweenExact(int startId, int endId, int[] indexes) {
			this.startId = startId;
			this.endId = endId;
			this.indexes = indexes;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			boolean found = false;
			for (int i = startId; i <= endId; i++) {
				InternalSignalInput child = getInternal(structure, i);
				if (child != null && is(child.getState(), indexes)) {
					found = true;
					break;
				}
			}
			for (int i = 0; i < state.length; i++)
				state[i] = found;
			
		}
		
		@Override
		public String write() {
			String result = indexToChar(startId, false) + "-" + indexToChar(endId, false) + "{";
			for (int i = 0; i < indexes.length; i++) {
				int index = indexes[i];
				if (index < 0)
					result += "!";
				result += (char) index;
			}
			return result + "}";
		}
		
	}
	
	public static class SignalConditionInternalIsExact extends SignalCondition {
		
		public int id;
		
		public int[] indexes;
		
		public SignalConditionInternalIsExact(int id, int[] indexes) {
			this.id = id;
			this.indexes = indexes;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			boolean found = is(getInternalState(structure, id), indexes);
			for (int i = 0; i < state.length; i++)
				state[i] = found;
		}
		
		@Override
		public String write() {
			String result = indexToChar(id, false) + "{";
			for (int i = 0; i < indexes.length; i++) {
				int index = indexes[i];
				if (index < 0)
					result += "!";
				result += (char) index;
			}
			return result + "}";
		}
		
	}
	
	public static class SignalConditionExternalIs extends SignalCondition {
		
		public int id;
		
		public SignalConditionExternalIs(int id) {
			this.id = id;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			combine(state, getExternalState(structure, id));
		}
		
		@Override
		public String write() {
			return indexToChar(id, true);
		}
		
	}
	
	public static class SignalConditionExternalIsBetween extends SignalCondition {
		
		public int startId;
		public int endId;
		
		public SignalConditionExternalIsBetween(int startId, int endId) {
			this.startId = startId;
			this.endId = endId;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			for (int i = startId; i <= endId; i++) {
				ISignalStructureComponent child = getExternal(structure, i);
				if (child != null)
					combine(state, child.getState());
			}
		}
		
		@Override
		public String write() {
			return indexToChar(startId, true) + "-" + indexToChar(endId, true);
		}
		
	}
	
	public static class SignalConditionExternalIsBetweenExact extends SignalCondition {
		
		public int startId;
		public int endId;
		public int[] indexes;
		
		public SignalConditionExternalIsBetweenExact(int startId, int endId, int[] indexes) {
			this.startId = startId;
			this.endId = endId;
			this.indexes = indexes;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			boolean found = false;
			for (int i = startId; i <= endId; i++) {
				ISignalStructureComponent child = getExternal(structure, i);
				if (child != null && is(child.getState(), indexes)) {
					found = true;
					break;
				}
			}
			for (int i = 0; i < state.length; i++)
				state[i] = found;
			
		}
		
		@Override
		public String write() {
			String result = indexToChar(startId, true) + "-" + indexToChar(endId, true) + "{";
			for (int i = 0; i < indexes.length; i++) {
				int index = indexes[i];
				if (index < 0)
					result += "!";
				result += (char) index;
			}
			return result + "}";
		}
		
	}
	
	public static class SignalConditionExternalIsExact extends SignalCondition {
		
		public int id;
		
		public int[] indexes;
		
		public SignalConditionExternalIsExact(int id, int[] indexes) {
			this.id = id;
			this.indexes = indexes;
		}
		
		@Override
		public void test(LittleStructure structure, boolean[] state) {
			boolean found = is(getExternalState(structure, id), indexes);
			for (int i = 0; i < state.length; i++)
				state[i] = found;
		}
		
		@Override
		public String write() {
			String result = indexToChar(id, true) + "{";
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
