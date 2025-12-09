package team.creative.littletiles.common.structure.signal.input;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import team.creative.creativecore.common.util.type.itr.ArrayIterator;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicEntry;
import team.creative.littletiles.common.structure.signal.logic.SignalPatternParser;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;

public abstract class SignalInputCondition {
    
    public static SignalInputCondition parseInput(String pattern) throws ParseException {
        SignalPatternParser parser = new SignalPatternParser(pattern);
        SignalInputCondition input = tryParseNextCondition(parser, true, false, true);
        if (input != null && !parser.hasNext())
            return input;
        return parseExpression(new SignalPatternParser(pattern), new char[0], true, false);
    }
    
    public static SignalInputCondition parseNextCondition(SignalPatternParser parser, boolean includeBitwise, boolean insideVariable) throws ParseException {
        return parseNextCondition(parser, includeBitwise, insideVariable, false);
    }
    
    public static SignalInputCondition parseNextCondition(SignalPatternParser parser, boolean includeBitwise, boolean insideVariable, boolean forceBitwise) throws ParseException {
        SignalInputCondition condition = tryParseNextCondition(parser, includeBitwise, insideVariable, forceBitwise);
        if (condition == null)
            throw parser.exception("Invalid signal pattern");
        return condition;
    }
    
    public static SignalInputCondition tryParseNextCondition(SignalPatternParser parser, boolean includeBitwise, boolean insideVariable,
            boolean forceBitwise) throws ParseException {
        while (parser.hasNext()) {
            char next = parser.lookForNext(true);
            int type = Character.getType(next);
            
            if (next == '(') {
                parser.next(true);
                SignalInputCondition condition = parseExpression(parser, new char[] { ')' }, SignalLogicEntry.getHighest(includeBitwise), includeBitwise, insideVariable);
                parser.next(true);
                return condition;
            } else if (next == '!') {
                parser.next(true);
                SignalPosition position = parsePosition(parser);
                return new SignalInputConditionNot(parseNextCondition(parser, includeBitwise, insideVariable, forceBitwise), position);
            } else if (next == '~') {
                parser.next(true);
                SignalPosition position = parsePosition(parser);
                return new SignalInputConditionNotBitwise(parseNextCondition(parser, includeBitwise, insideVariable, forceBitwise), position);
            } else if (next == 'v') {
                parser.next(true);
                next = parser.next(true);
                if (next != '[')
                    throw parser.invalidChar(next);
                List<SignalInputCondition> array = new ArrayList<>();
                while (true) {
                    if (parser.lookForNext(true) != ']')
                        array.add(parseExpression(parser, new char[] { ',', ']' }, includeBitwise, insideVariable));
                    char current = parser.next(true);
                    if (current == ',')
                        continue;
                    else if (current == ']')
                        break;
                    else
                        throw parser.exception("Invalid signal pattern");
                }
                return new SignalInputVirtualVariable(array.toArray(new SignalInputCondition[array.size()]), parsePosition(parser));
            } else if (next == 'n') {
                parser.next(true);
                return new SignalInputVirtualNumber(parser.parseNumber(), parsePosition(parser));
            } else if (type == Character.LOWERCASE_LETTER)
                return SignalInputVariable.parseInput(parser, insideVariable, forceBitwise);
            else if (type == Character.UPPERCASE_LETTER)
                return SignalInputVariable.parseInput(parser, insideVariable, forceBitwise);
            else
                return null;
        }
        return null;
    }
    
    private static SignalInputCondition parseLowerExpression(SignalPatternParser parser, char[] until, SignalLogicEntry operator, boolean includeBitwise,
            boolean insideVariable) throws ParseException {
        if (operator.lower() != null)
            return parseExpression(parser, until, operator.lower(), includeBitwise, insideVariable);
        return parseNextCondition(parser, includeBitwise, insideVariable);
    }
    
    public static SignalInputCondition parseExpression(SignalPatternParser parser, char[] until, boolean includeBitwise, boolean insideVariable) throws ParseException {
        return parseExpression(parser, until, SignalLogicEntry.getHighest(includeBitwise), includeBitwise, insideVariable);
    }
    
    public static SignalInputCondition parseExpression(SignalPatternParser parser, char[] until, SignalLogicEntry operator, boolean includeBitwise,
            boolean insideVariable) throws ParseException {
        SignalInputCondition first = parseLowerExpression(parser, until, operator, includeBitwise, insideVariable);
        
        if (!parser.hasNext() || ArrayUtils.contains(until, parser.lookForNext(true)))
            return first;
        
        if (operator.goOn(parser)) {
            List<SignalInputCondition> conditions = new ArrayList<>();
            conditions.add(first);
            conditions.add(parseLowerExpression(parser, until, operator, includeBitwise, insideVariable));
            while (operator.goOn(parser))
                conditions.add(parseLowerExpression(parser, until, operator, includeBitwise, insideVariable));
            if (operator.maxArgmumentCount() >= 0 && conditions.size() > operator.maxArgmumentCount())
                throw parser.exception("Operator " + operator.operator() + " does not except " + conditions.size() + " arguments");
            return operator.create(conditions.toArray(new SignalInputCondition[conditions.size()]), parsePosition(parser));
        }
        return first;
    }
    
    public static SignalPosition parsePosition(SignalPatternParser parser) throws ParseException {
        if (parser.lookForNext(true) == ':') {
            parser.next(true);
            int x = parser.parseNumber();
            if (parser.next(true) != ',')
                throw parser.exception("Invalid coordinates");
            int y = parser.parseNumber();
            if (parser.next(true) != ';')
                throw parser.exception("Invalid coordinates");
            return new SignalPosition(x, y);
        }
        return null;
    }
    
    public final SignalPosition position;
    
    public SignalInputCondition(SignalPosition position) {
        this.position = position;
    }
    
    public abstract SignalState test(LittleStructure structure, boolean forceBitwise);
    
    /** only used for sub equation (inside a variable), only has indexes */
    public abstract boolean testIndex(SignalState state);
    
    public abstract String write();
    
    protected String writePosition() {
        if (position == null)
            return "";
        return ":" + position.x + "," + position.y + ";";
    }
    
    protected abstract double internalDelay();
    
    public double calculateDelay() {
        return internalDelay() * LittleTiles.CONFIG.signal.overallDurationScale;
    }
    
    @Override
    public String toString() {
        return write();
    }
    
    public abstract Iterator<SignalInputCondition> nested();
    
    public abstract SignalTarget target();
    
    public static abstract class SignalInputConditionOperator extends SignalInputCondition {
        
        public SignalInputConditionOperator(SignalPosition position) {
            super(position);
        }
        
        @Override
        public SignalState test(LittleStructure structure, boolean forceBitwise) {
            return test(structure);
        }
        
        public abstract SignalState test(LittleStructure structure);
        
    }
    
    public static class SignalInputConditionNotBitwise extends SignalInputConditionOperator {
        
        public SignalInputCondition condition;
        
        public SignalInputConditionNotBitwise(SignalInputCondition condition, SignalPosition position) {
            super(position);
            this.condition = condition;
        }
        
        @Override
        public SignalState test(LittleStructure structure) {
            return SignalState.copy(condition.test(structure, true)).invert();
        }
        
        @Override
        public boolean testIndex(SignalState state) {
            return !condition.testIndex(state);
        }
        
        @Override
        public String write() {
            return "~" + writePosition() + "(" + condition.write() + ")";
        }
        
        @Override
        protected double internalDelay() {
            return LittleTiles.CONFIG.signal.bnotDuration + condition.calculateDelay();
        }
        
        @Override
        public Iterator<SignalInputCondition> nested() {
            return new SingleIterator<>(condition);
        }
        
        @Override
        public SignalTarget target() {
            return null;
        }
    }
    
    public static class SignalInputConditionNot extends SignalInputConditionOperator {
        
        public SignalInputCondition condition;
        
        public SignalInputConditionNot(SignalInputCondition condition, SignalPosition position) {
            super(position);
            this.condition = condition;
        }
        
        @Override
        public SignalState test(LittleStructure structure) {
            return SignalState.copy(condition.test(structure, false)).invert();
        }
        
        @Override
        public boolean testIndex(SignalState state) {
            return !condition.testIndex(state);
        }
        
        @Override
        public String write() {
            return "!" + writePosition() + "(" + condition.write() + ")";
        }
        
        @Override
        protected double internalDelay() {
            return LittleTiles.CONFIG.signal.notDuration + condition.calculateDelay();
        }
        
        @Override
        public Iterator<SignalInputCondition> nested() {
            return new SingleIterator<>(condition);
        }
        
        @Override
        public SignalTarget target() {
            return null;
        }
    }
    
    public static class SignalInputVirtualNumber extends SignalInputCondition {
        
        public int number;
        
        public SignalInputVirtualNumber(int number, SignalPosition position) {
            super(position);
            this.number = number;
        }
        
        @Override
        public SignalState test(LittleStructure structure, boolean forceBitwise) {
            return SignalState.of(number);
        }
        
        @Override
        public boolean testIndex(SignalState state) {
            return false;
        }
        
        @Override
        public String write() {
            return "n" + number + writePosition();
        }
        
        @Override
        protected double internalDelay() {
            return 0;
        }
        
        @Override
        public Iterator<SignalInputCondition> nested() {
            return Collections.emptyIterator();
        }
        
        @Override
        public SignalTarget target() {
            return null;
        }
        
    }
    
    public static class SignalInputVirtualVariable extends SignalInputCondition {
        
        public SignalInputCondition[] conditions;
        
        public SignalInputVirtualVariable(SignalInputCondition[] conditions, SignalPosition position) {
            super(position);
            this.conditions = conditions;
        }
        
        @Override
        public SignalState test(LittleStructure structure, boolean forceBitwise) {
            SignalState state = SignalState.create(conditions.length);
            for (int i = 0; i < conditions.length; i++)
                state = state.set(i, conditions[i].test(structure, false).any());
            return state;
        }
        
        @Override
        public boolean testIndex(SignalState state) {
            return false;
        }
        
        @Override
        public String write() {
            String result = "v[";
            for (int i = 0; i < conditions.length; i++) {
                if (i > 0)
                    result += ",";
                result += conditions[i].write();
            }
            return result + "]" + writePosition();
        }
        
        @Override
        protected double internalDelay() {
            double delay = LittleTiles.CONFIG.signal.andDuration * conditions.length;
            for (int i = 0; i < conditions.length; i++)
                delay += conditions[i].calculateDelay();
            return delay;
        }
        
        @Override
        public Iterator<SignalInputCondition> nested() {
            return new ArrayIterator<>(conditions);
        }
        
        @Override
        public SignalTarget target() {
            return null;
        }
    }
    
    public static record SignalPosition(int x, int y) {}
    
}
