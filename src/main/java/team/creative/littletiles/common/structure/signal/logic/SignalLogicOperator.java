package team.creative.littletiles.common.structure.signal.logic;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import team.creative.creativecore.common.util.type.itr.ArrayIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.signal.SignalContext;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.SignalState.SignalStateSize;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionOperator;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public enum SignalLogicOperator implements SignalLogicEntry {
    
    AND('\n', false, "and", "") {
        @Override
        public SignalLogicOperator lower() {
            return null;
        }
        
        @Override
        public boolean perform(boolean first, boolean second) {
            return first && second;
        }
        
        @Override
        public int perform(int first, int second) {
            return first & second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first & second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackable(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return AND;
                }
                
                @Override
                public boolean needsBrackets() {
                    return false;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.andDuration;
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
        public int perform(int first, int second) {
            return first | second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first | second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackable(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return OR;
                }
                
                @Override
                public boolean needsBrackets() {
                    return true;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.orDuration;
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
        public int perform(int first, int second) {
            return first ^ second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first ^ second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackable(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return XOR;
                }
                
                @Override
                public boolean needsBrackets() {
                    return true;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.xorDuration;
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
        public int perform(int first, int second) {
            return first & second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first & second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackableBitwise(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return BITWISE_AND;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.bandDuration;
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
        public int perform(int first, int second) {
            return first | second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first | second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackableBitwise(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return BITWISE_OR;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.borDuration;
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
        public int perform(int first, int second) {
            return first ^ second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first ^ second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackableBitwise(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return BITWISE_XOR;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.bxorDuration;
                }
            };
        }
        
    },
    ADD('#', true, "add") {
        @Override
        public SignalLogicOperator lower() {
            return SignalLogicOperator.BITWISE_XOR;
        }
        
        @Override
        public boolean perform(boolean first, boolean second) {
            return first | second;
        }
        
        @Override
        public int perform(int first, int second) {
            return first + second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first + second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackableMath(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.ADD;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.addDuration;
                }
                
            };
        }
    },
    SUB('-', true, "sub") {
        @Override
        public SignalLogicOperator lower() {
            return SignalLogicOperator.ADD;
        }
        
        @Override
        public boolean perform(boolean first, boolean second) {
            return first && !second;
        }
        
        @Override
        public int perform(int first, int second) {
            return first - second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first - second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackableMath(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.SUB;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.subDuration;
                }
            };
        }
    },
    MUL('*', true, "mul") {
        @Override
        public SignalLogicOperator lower() {
            return SignalLogicOperator.SUB;
        }
        
        @Override
        public boolean perform(boolean first, boolean second) {
            return first && second;
        }
        
        @Override
        public int perform(int first, int second) {
            return first * second;
        }
        
        @Override
        public long perform(long first, long second) {
            return first * second;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackableMath(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.MUL;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.mulDuration;
                }
            };
        }
    },
    DIV('/', true, "div") {
        @Override
        public SignalLogicOperator lower() {
            return SignalLogicOperator.MUL;
        }
        
        @Override
        public boolean perform(boolean first, boolean second) {
            return first && !second;
        }
        
        @Override
        public int perform(int first, int second) {
            try {
                return first / second;
            } catch (ArithmeticException e) {
                if (second == 0 && first != 0)
                    return 1;
                return 0;
            }
        }
        
        @Override
        public long perform(long first, long second) {
            try {
                return first / second;
            } catch (ArithmeticException e) {
                if (second == 0 && first != 0)
                    return 1;
                return 0;
            }
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions, SignalPosition position) {
            return new SignalInputConditionOperatorStackableMath(conditions, position) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.DIV;
                }
                
                @Override
                public double getModifier() {
                    return LittleTiles.CONFIG.signal.divDuration;
                }
            };
        }
    };
    
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
            case '#':
                return SignalLogicOperator.ADD;
            case '-':
                return SignalLogicOperator.SUB;
            case '*':
                return SignalLogicOperator.MUL;
            case '/':
                return SignalLogicOperator.DIV;
            default:
                return null;
        }
    }
    
    public static final List<SignalLogicOperator> NON_BITWISE_OPERATORS = Arrays.asList(SignalLogicOperator.AND, SignalLogicOperator.OR, SignalLogicOperator.XOR);
    
    public final char operator;
    public final boolean bitwise;
    public final String display;
    public final String seperator;
    
    private SignalLogicOperator(char operator, boolean bitwise, String display) {
        this(operator, bitwise, display, "" + operator);
    }
    
    private SignalLogicOperator(char operator, boolean bitwise, String display, String seperator) {
        this.operator = operator;
        this.bitwise = bitwise;
        this.display = display;
        this.seperator = seperator;
    }
    
    @Override
    public String operator() {
        return String.valueOf(operator);
    }
    
    @Override
    public int maxArgmumentCount() {
        return -1;
    }
    
    public abstract boolean perform(boolean first, boolean second);
    
    public abstract int perform(int first, int second);
    
    public abstract long perform(long first, long second);
    
    public static abstract class SignalInputConditionOperatorStackable extends SignalInputConditionOperator {
        
        public SignalInputCondition[] conditions;
        
        public SignalInputConditionOperatorStackable(SignalInputCondition[] conditions, SignalPosition position) {
            super(position);
            this.conditions = conditions;
        }
        
        @Override
        public SignalState test(SignalContext context) {
            SignalState[] state = new SignalState[conditions.length];
            SignalStateSize size = SignalStateSize.SINGLE;
            for (int i = 0; i < conditions.length; i++) {
                state[i] = conditions[i].test(context, false);
                size = size.max(state[i].size());
            }
            
            SignalState result = size.create();
            for (int i = 0; i < state.length; i++) {
                if (i == 0) {
                    switch (size) {
                        case SINGLE -> result = result.set(0, state[i].any());
                        case INT -> result = result.setNumber(state[i].number());
                        case LONG -> result = result.setLongNumber(state[i].longNumber());
                    }
                    continue;
                }
                switch (size) {
                    case SINGLE -> result = result.set(0, operator().perform(result.any(), state[i].any()));
                    case INT -> result = result.setNumber(operator().perform(result.number(), state[i].number()));
                    case LONG -> result = result.setLongNumber(operator().perform(result.longNumber(), state[i].longNumber()));
                }
            }
            return result;
        }
        
        @Override
        public boolean testIndex(SignalState state) {
            boolean result = false;
            for (int i = 0; i < conditions.length; i++) {
                if (i == 0)
                    result = conditions[i].testIndex(state);
                else
                    result = operator().perform(result, conditions[i].testIndex(state));
            }
            return result;
        }
        
        public abstract boolean needsBrackets();
        
        public abstract SignalLogicOperator operator();
        
        @Override
        public String write() {
            if (needsBrackets()) {
                String result = "(";
                for (int i = 0; i < conditions.length; i++) {
                    if (i > 0)
                        result += operator().seperator;
                    result += conditions[i].write();
                }
                return result + writePosition() + ")";
            }
            String result = "";
            for (int i = 0; i < conditions.length; i++) {
                if (i > 0)
                    result += operator().seperator;
                result += conditions[i].write();
            }
            return result + writePosition();
        }
        
        @Override
        protected double internalDelay() {
            double delay = conditions.length * getModifier();
            for (SignalInputCondition condition : conditions)
                delay += condition.calculateDelay();
            return delay;
        }
        
        public abstract double getModifier();
        
        @Override
        public Iterator<SignalInputCondition> nested() {
            return new ArrayIterator<>(conditions);
        }
        
        @Override
        public SignalTarget target() {
            return null;
        }
    }
    
    public static abstract class SignalInputConditionOperatorStackableBitwise extends SignalInputConditionOperatorStackable {
        
        public SignalInputConditionOperatorStackableBitwise(SignalInputCondition[] conditions, SignalPosition position) {
            super(conditions, position);
        }
        
        @Override
        public SignalState test(SignalContext context) {
            SignalState[] state = new SignalState[conditions.length];
            SignalStateSize size = SignalStateSize.SINGLE;
            for (int i = 0; i < conditions.length; i++) {
                state[i] = conditions[i].test(context, true);
                size = size.max(state[i].size());
            }
            
            SignalState result = size.create();
            for (int i = 0; i < state.length; i++) {
                if (i == 0) {
                    switch (size) {
                        case SINGLE -> result = result.set(0, state[i].any());
                        case INT -> result = result.setNumber(state[i].number());
                        case LONG -> result = result.setLongNumber(state[i].longNumber());
                    }
                    continue;
                }
                switch (size) {
                    case SINGLE -> result = result.set(0, operator().perform(result.any(), state[i].any()));
                    case INT -> result = result.setNumber(operator().perform(result.number(), state[i].number()));
                    case LONG -> result = result.setLongNumber(operator().perform(result.longNumber(), state[i].longNumber()));
                }
            }
            return result;
        }
        
        @Override
        public boolean needsBrackets() {
            return true;
        }
        
    }
    
    public static abstract class SignalInputConditionOperatorStackableMath extends SignalInputConditionOperatorStackable {
        
        public SignalInputConditionOperatorStackableMath(SignalInputCondition[] conditions, SignalPosition position) {
            super(conditions, position);
        }
        
        @Override
        public SignalState test(SignalContext context) {
            SignalState[] state = new SignalState[conditions.length];
            SignalStateSize size = SignalStateSize.SINGLE;
            for (int i = 0; i < conditions.length; i++) {
                state[i] = conditions[i].test(context, true);
                size = size.max(state[i].size());
            }
            
            SignalState result = size.create();
            switch (size) {
                case SINGLE -> {
                    for (int i = 0; i < state.length; i++) {
                        if (i == 0)
                            result = result.set(0, state[i].any());
                        else
                            result = result.set(0, operator().perform(result.any(), state[i].any()));
                    }
                }
                case INT -> {
                    for (int i = 0; i < state.length; i++) {
                        if (i == 0)
                            result = result.setNumber(state[i].number());
                        else
                            result = result.setNumber(operator().perform(result.number(), state[i].number()));
                    }
                }
                case LONG -> {
                    for (int i = 0; i < state.length; i++) {
                        if (i == 0)
                            result = result.setLongNumber(state[i].longNumber());
                        else
                            result = result.setLongNumber(operator().perform(result.longNumber(), state[i].longNumber()));
                    }
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
