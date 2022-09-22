package team.creative.littletiles.common.structure.signal.input;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;
import team.creative.littletiles.common.structure.signal.logic.SignalPatternParser;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;

public class SignalInputVariable extends SignalInputCondition {
    
    private static int[] parseInputExact(SignalPatternParser parser) throws ParseException {
        List<Integer> indexes = new ArrayList<>();
        while (parser.hasNext()) {
            char next = parser.lookForNext(true);
            if (next == '}') {
                parser.next(true);
                break;
            } else if (Character.isDigit(next) || next == '*') {
                if (next == '*') {
                    indexes.add(2);
                    next = parser.next(true);
                    if (next == '}')
                        break;
                    continue;
                } else {
                    indexes.add(Integer.parseInt("" + next));
                    next = parser.next(true);
                    if (next == '}')
                        break;
                    continue;
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
        return parseInput(parser, insideVariable, false);
    }
    
    public static SignalInputVariable parseInput(SignalPatternParser parser, boolean insideVariable, boolean forceBitwise) throws ParseException {
        SignalTarget target = SignalTarget.parseTarget(parser, false, insideVariable);
        if (!insideVariable && parser.lookForNext(false) == '{') {
            parser.next(false);
            char next = parser.lookForNext(true);
            if (Character.isDigit(next) || next == '*') {
                int[] indexes = parseInputExact(parser);
                if (indexes != null)
                    return new SignalInputVariablePattern(target, indexes);
                return forceBitwise ? new SignalInputVariableBitwise(target) : new SignalInputVariable(target);
            }
            
            SignalLogicOperator operator = SignalLogicOperator.getOperator(next);
            if (operator != null) {
                parser.next(true);
                if (parser.next(true) == '}')
                    return new SignalInputVariableOperator(target, operator);
                else
                    throw parser.invalidChar(parser.current());
            }
            
            return new SignalInputVariableEquation(target, SignalInputCondition.parseExpression(parser, new char[] { '}' }, false, true));
        } else
            return forceBitwise ? new SignalInputVariableBitwise(target) : new SignalInputVariable(target);
    }
    
    public final SignalTarget target;
    
    public SignalInputVariable(SignalTarget target) {
        this.target = target;
    }
    
    @Override
    public SignalState test(LittleStructure structure, boolean forceBitwise) {
        SignalState state = target.getState(structure);
        if (forceBitwise)
            return state;
        return SignalState.of(state.any());
        
    }
    
    @Override
    public boolean testIndex(SignalState state) {
        if (target.isIndexVariable())
            return state.is(target.child);
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
    
    public static class SignalInputVariableBitwise extends SignalInputVariable {
        
        public SignalInputVariableBitwise(SignalTarget target) {
            super(target);
        }
        
        @Override
        public SignalState test(LittleStructure structure, boolean forceBitwise) {
            return target.getState(structure);
        }
        
    }
    
    public static class SignalInputVariableOperator extends SignalInputVariable {
        
        public final SignalLogicOperator operator;
        
        public SignalInputVariableOperator(SignalTarget target, SignalLogicOperator operator) {
            super(target);
            this.operator = operator;
        }
        
        @Override
        public SignalState test(LittleStructure structure, boolean forceBitwise) {
            SignalState state = target.getState(structure);
            boolean result = false;
            int bandwidth = target.bandwidth(structure);
            for (int i = 0; i < bandwidth; i++)
                if (i == 0)
                    result = state.is(i);
                else
                    result = operator.perform(result, state.is(i));
            return SignalState.of(result);
        }
        
        @Override
        public String write() {
            return super.write() + "{" + (operator == SignalLogicOperator.AND ? "&" : operator.operator) + "}";
        }
    }
    
    public static class SignalInputVariablePattern extends SignalInputVariable {
        
        public final int[] indexes;
        
        public SignalInputVariablePattern(SignalTarget target, int[] indexes) {
            super(target);
            this.indexes = indexes;
        }
        
        @Override
        public SignalState test(LittleStructure structure, boolean forceBitwise) {
            return SignalState.of(target.getState(structure).is(indexes));
        }
        
        @Override
        public String write() {
            String result = super.write() + "{";
            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];
                result += "" + (index >= 2 ? "*" : index);
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
        public SignalState test(LittleStructure structure, boolean forceBitwise) {
            return SignalState.of(condition.testIndex(target.getState(structure)));
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
