package team.creative.littletiles.common.structure.signal.logic;

import java.util.Iterator;

import javax.annotation.Nullable;

import team.creative.creativecore.common.util.type.itr.ArrayIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.SignalState.SignalStateSize;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionOperator;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public enum SignalLogicComparator implements SignalLogicEntry {
    
    GREATER(">") {
        
        @Override
        public SignalLogicEntry lower() {
            return SignalLogicOperator.DIV;
        }
        
        @Override
        public double delayModifier() {
            return LittleTiles.CONFIG.signal.greater;
        }
        
        @Override
        public boolean compare(boolean first, boolean second) {
            return first && !second;
        }
        
        @Override
        public boolean compare(int first, int second) {
            return first > second;
        }
        
        @Override
        public boolean compare(long first, long second) {
            return first > second;
        }
        
    },
    GREATER_EQUALS(">=") {
        
        @Override
        public SignalLogicEntry lower() {
            return SignalLogicComparator.GREATER;
        }
        
        @Override
        public double delayModifier() {
            return LittleTiles.CONFIG.signal.greaterEquals;
        }
        
        @Override
        public boolean compare(boolean first, boolean second) {
            return first || !second;
        }
        
        @Override
        public boolean compare(int first, int second) {
            return first >= second;
        }
        
        @Override
        public boolean compare(long first, long second) {
            return first >= second;
        }
        
    },
    SMALLER("<") {
        
        @Override
        public SignalLogicEntry lower() {
            return SignalLogicComparator.GREATER_EQUALS;
        }
        
        @Override
        public double delayModifier() {
            return LittleTiles.CONFIG.signal.smaller;
        }
        
        @Override
        public boolean compare(boolean first, boolean second) {
            return !first && second;
        }
        
        @Override
        public boolean compare(int first, int second) {
            return first < second;
        }
        
        @Override
        public boolean compare(long first, long second) {
            return first < second;
        }
        
    },
    SMALLER_EQUALS("<=") {
        
        @Override
        public SignalLogicEntry lower() {
            return SignalLogicComparator.SMALLER;
        }
        
        @Override
        public double delayModifier() {
            return LittleTiles.CONFIG.signal.smallerEquals;
        }
        
        @Override
        public boolean compare(boolean first, boolean second) {
            return !first || second;
        }
        
        @Override
        public boolean compare(int first, int second) {
            return first <= second;
        }
        
        @Override
        public boolean compare(long first, long second) {
            return first <= second;
        }
        
    };
    
    public final String operator;
    
    private SignalLogicComparator(String operator) {
        this.operator = operator;
    }
    
    @Override
    public String operator() {
        return operator;
    }
    
    @Override
    public int maxArgmumentCount() {
        return 2;
    }
    
    public abstract double delayModifier();
    
    public abstract boolean compare(boolean first, boolean second);
    
    public abstract boolean compare(int first, int second);
    
    public abstract boolean compare(long first, long second);
    
    @Override
    public SignalInputCondition create(SignalInputCondition[] conditions, @Nullable SignalPosition position) {
        return new SignalInputConditionComparator(conditions, this, position);
    }
    
    public static class SignalInputConditionComparator extends SignalInputConditionOperator {
        
        public final SignalLogicComparator comparator;
        public final SignalInputCondition[] conditions;
        
        public SignalInputConditionComparator(SignalInputCondition[] conditions, SignalLogicComparator comparator, @Nullable SignalPosition position) {
            super(position);
            this.conditions = conditions;
            this.comparator = comparator;
        }
        
        @Override
        public SignalState test(LittleStructure structure) {
            SignalState[] state = new SignalState[conditions.length];
            SignalStateSize size = SignalStateSize.SINGLE;
            for (int i = 0; i < conditions.length; i++) {
                state[i] = conditions[i].test(structure, false);
                size = size.max(state[i].size());
            }
            
            return SignalState.of(switch (size) {
                case SINGLE -> comparator.compare(state[0].any(), state[1].any());
                case INT -> comparator.compare(state[0].number(), state[1].number());
                case LONG -> comparator.compare(state[0].longNumber(), state[1].longNumber());
            });
        }
        
        @Override
        public boolean testIndex(SignalState state) {
            boolean result = false;
            for (int i = 0; i < conditions.length; i++) {
                if (i == 0)
                    result = conditions[i].testIndex(state);
                else
                    result = comparator.compare(result, conditions[i].testIndex(state));
            }
            return result;
        }
        
        @Override
        public String write() {
            String result = "(";
            for (int i = 0; i < conditions.length; i++) {
                if (i > 0)
                    result += comparator.operator;
                result += conditions[i].write();
            }
            return result + writePosition() + ")";
        }
        
        @Override
        protected double internalDelay() {
            double delay = conditions.length * comparator.delayModifier();
            for (SignalInputCondition condition : conditions)
                delay += condition.calculateDelay();
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
    
}
