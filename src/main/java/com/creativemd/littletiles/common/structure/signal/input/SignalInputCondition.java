package com.creativemd.littletiles.common.structure.signal.input;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator;
import com.creativemd.littletiles.common.structure.signal.logic.SignalPatternParser;

public abstract class SignalInputCondition {
    
    public static final float AND_DURATION = 0.4F;
    public static final float OR_DURATION = 0.1F;
    public static final float XOR_DURATION = 0.6F;
    public static final float BAND_DURATION = 0.6F;
    public static final float BOR_DURATION = 0.2F;
    public static final float BXOR_DURATION = 0.7F;
    public static final float NOT_DURATION = 0.05F;
    public static final float BNOT_DURATION = 0.1F;
    public static final float VARIABLE_DURATION = 0.05F;
    
    public static SignalInputCondition parseInput(String pattern) throws ParseException {
        return parseExpression(new SignalPatternParser(pattern), new char[0], true, false);
    }
    
    public static SignalInputCondition parseNextCondition(SignalPatternParser parser, boolean includeBitwise, boolean insideVariable) throws ParseException {
        while (parser.hasNext()) {
            char next = parser.lookForNext(true);
            int type = Character.getType(next);
            
            if (next == '(') {
                parser.next(true);
                SignalInputCondition condition = parseExpression(parser, new char[] { ')' }, SignalLogicOperator.getHighest(includeBitwise), includeBitwise, insideVariable);
                parser.next(true);
                return condition;
            } else if (next == '!') {
                parser.next(true);
                return new SignalInputConditionNot(parseNextCondition(parser, includeBitwise, insideVariable));
            } else if (next == '~') {
                parser.next(true);
                return new SignalInputConditionNotBitwise(parseNextCondition(parser, includeBitwise, insideVariable));
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
                return new SignalInputVirtualVariable(array.toArray(new SignalInputCondition[array.size()]));
            } else if (type == Character.LOWERCASE_LETTER)
                return SignalInputVariable.parseInput(parser, insideVariable);
            else if (type == Character.UPPERCASE_LETTER)
                return SignalInputVariable.parseInput(parser, insideVariable);
            else if (next == '0' || next == '1') {
                parser.next(true);
                return new SignalInputBit(next == '1');
            } else
                throw parser.exception("Invalid signal pattern");
        }
        
        throw parser.exception("Invalid signal pattern");
    }
    
    private static SignalInputCondition parseLowerExpression(SignalPatternParser parser, char[] until, SignalLogicOperator operator, boolean includeBitwise, boolean insideVariable) throws ParseException {
        if (operator.lower() != null)
            return parseExpression(parser, until, operator.lower(), includeBitwise, insideVariable);
        return parseNextCondition(parser, includeBitwise, insideVariable);
    }
    
    public static SignalInputCondition parseExpression(SignalPatternParser parser, char[] until, boolean includeBitwise, boolean insideVariable) throws ParseException {
        return parseExpression(parser, until, SignalLogicOperator.getHighest(includeBitwise), includeBitwise, insideVariable);
    }
    
    public static SignalInputCondition parseExpression(SignalPatternParser parser, char[] until, SignalLogicOperator operator, boolean includeBitwise, boolean insideVariable) throws ParseException {
        SignalInputCondition first = parseLowerExpression(parser, until, operator, includeBitwise, insideVariable);
        
        if (!parser.hasNext() || ArrayUtils.contains(until, parser.lookForNext(true)))
            return first;
        
        if (operator.goOn(parser)) {
            List<SignalInputCondition> conditions = new ArrayList<>();
            conditions.add(first);
            conditions.add(parseLowerExpression(parser, until, operator, includeBitwise, insideVariable));
            while (operator.goOn(parser))
                conditions.add(parseLowerExpression(parser, until, operator, includeBitwise, insideVariable));
            return operator.create(conditions.toArray(new SignalInputCondition[conditions.size()]));
        }
        return first;
    }
    
    public abstract boolean[] test(LittleStructure structure, boolean forceBitwise);
    
    /** only used for sub equation (inside a variable), only has indexes */
    public abstract boolean testIndex(boolean[] state);
    
    public abstract String write();
    
    public abstract float calculateDelay();
    
    @Override
    public String toString() {
        return write();
    }
    
    public static abstract class SignalInputConditionOperator extends SignalInputCondition {
        
        @Override
        public boolean[] test(LittleStructure structure, boolean forceBitwise) {
            return test(structure);
        }
        
        public abstract boolean[] test(LittleStructure structure);
        
    }
    
    public static class SignalInputConditionNotBitwise extends SignalInputConditionOperator {
        
        public SignalInputCondition condition;
        
        public SignalInputConditionNotBitwise(SignalInputCondition condition) {
            this.condition = condition;
        }
        
        @Override
        public boolean[] test(LittleStructure structure) {
            boolean[] state = condition.test(structure, true);
            for (int i = 0; i < state.length; i++)
                state[i] = !state[i];
            return state;
        }
        
        @Override
        public boolean testIndex(boolean[] state) {
            return !condition.testIndex(state);
        }
        
        @Override
        public String write() {
            return "~" + condition.write();
        }
        
        @Override
        public float calculateDelay() {
            return BNOT_DURATION + condition.calculateDelay();
        }
    }
    
    public static class SignalInputConditionNot extends SignalInputConditionOperator {
        
        public SignalInputCondition condition;
        
        public SignalInputConditionNot(SignalInputCondition condition) {
            this.condition = condition;
        }
        
        @Override
        public boolean[] test(LittleStructure structure) {
            boolean[] state = condition.test(structure, false);
            for (int i = 0; i < state.length; i++)
                state[i] = !state[i];
            return state;
        }
        
        @Override
        public boolean testIndex(boolean[] state) {
            return !condition.testIndex(state);
        }
        
        @Override
        public String write() {
            return "!" + condition.write();
        }
        
        @Override
        public float calculateDelay() {
            return NOT_DURATION + condition.calculateDelay();
        }
        
    }
    
    public static class SignalInputBit extends SignalInputCondition {
        
        public boolean bit;
        
        public SignalInputBit(boolean bit) {
            this.bit = bit;
        }
        
        @Override
        public boolean[] test(LittleStructure structure, boolean forceBitwise) {
            return BooleanUtils.asArray(bit);
        }
        
        @Override
        public boolean testIndex(boolean[] state) {
            return false;
        }
        
        @Override
        public String write() {
            return bit ? "1" : "0";
        }
        
        @Override
        public float calculateDelay() {
            return 0;
        }
        
    }
    
    public static class SignalInputVirtualVariable extends SignalInputCondition {
        
        public SignalInputCondition[] conditions;
        
        public SignalInputVirtualVariable(SignalInputCondition[] conditions) {
            this.conditions = conditions;
        }
        
        @Override
        public boolean[] test(LittleStructure structure, boolean forceBitwise) {
            boolean[] state = new boolean[conditions.length];
            for (int i = 0; i < state.length; i++)
                state[i] = BooleanUtils.any(conditions[i].test(structure, false));
            if (forceBitwise)
                return state;
            return BooleanUtils.asArray(BooleanUtils.any(state));
        }
        
        @Override
        public boolean testIndex(boolean[] state) {
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
            return result + "]";
        }
        
        @Override
        public float calculateDelay() {
            float delay = AND_DURATION * conditions.length;
            for (int i = 0; i < conditions.length; i++)
                delay += conditions[i].calculateDelay();
            return delay;
        }
        
    }
    
}
