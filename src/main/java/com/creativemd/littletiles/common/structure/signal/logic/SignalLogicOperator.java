package com.creativemd.littletiles.common.structure.signal.logic;

import java.text.ParseException;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionOperator;

public enum SignalLogicOperator {
    
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
        public SignalInputCondition create(SignalInputCondition[] conditions) {
            return new SignalInputConditionOperatorStackable(conditions) {
                
                @Override
                public boolean perform(boolean first, boolean second) {
                    return first && second;
                }
                
                @Override
                public SignalLogicOperator operator() {
                    return AND;
                }
                
                @Override
                public boolean needsBrackets() {
                    return false;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.AND_DURATION;
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
                public SignalLogicOperator operator() {
                    return OR;
                }
                
                @Override
                public boolean needsBrackets() {
                    return true;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.OR_DURATION;
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
                public SignalLogicOperator operator() {
                    return XOR;
                }
                
                @Override
                public boolean needsBrackets() {
                    return true;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.XOR_DURATION;
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
                public SignalLogicOperator operator() {
                    return BITWISE_AND;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.BAND_DURATION;
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
                public SignalLogicOperator operator() {
                    return BITWISE_OR;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.BOR_DURATION;
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
                public SignalLogicOperator operator() {
                    return BITWISE_XOR;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.BXOR_DURATION;
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
            return false;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions) {
            return new SignalInputConditionOperatorStackableMath(conditions) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.ADD;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.ADD_DURATION;
                }
                
                @Override
                public int perform(int first, int second) {
                    return first + second;
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
            return false;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions) {
            return new SignalInputConditionOperatorStackableMath(conditions) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.SUB;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.SUB_DURATION;
                }
                
                @Override
                public int perform(int first, int second) {
                    return first - second;
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
            return false;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions) {
            return new SignalInputConditionOperatorStackableMath(conditions) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.MUL;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.MUL_DURATION;
                }
                
                @Override
                public int perform(int first, int second) {
                    return first * second;
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
            return false;
        }
        
        @Override
        public SignalInputCondition create(SignalInputCondition[] conditions) {
            return new SignalInputConditionOperatorStackableMath(conditions) {
                
                @Override
                public SignalLogicOperator operator() {
                    return SignalLogicOperator.DIV;
                }
                
                @Override
                public float getModifier() {
                    return SignalInputCondition.DIV_DURATION;
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
            };
        }
    };
    
    public static final SignalLogicOperator HIGHEST_GENERAL = XOR;
    
    public static final SignalLogicOperator HIGHEST = DIV;
    
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
            for (int i = 0; i < conditions.length; i++) {
                if (i == 0)
                    result = conditions[i].testIndex(state);
                else
                    result = perform(result, conditions[i].testIndex(state));
            }
            return result;
        }
        
        public abstract boolean perform(boolean first, boolean second);
        
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
                return result + ")";
            }
            String result = "";
            for (int i = 0; i < conditions.length; i++) {
                if (i > 0)
                    result += operator().seperator;
                result += conditions[i].write();
            }
            return result;
        }
        
        @Override
        public float calculateDelay() {
            float delay = conditions.length * getModifier();
            for (SignalInputCondition condition : conditions)
                delay += condition.calculateDelay();
            return delay;
        }
        
        public abstract float getModifier();
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
    
    public static abstract class SignalInputConditionOperatorStackableMath extends SignalInputConditionOperatorStackable {
        
        public SignalInputConditionOperatorStackableMath(SignalInputCondition[] conditions) {
            super(conditions);
        }
        
        @Override
        public boolean perform(boolean first, boolean second) {
            return false;
        }
        
        public abstract int perform(int first, int second);
        
        @Override
        public boolean[] test(LittleStructure structure) {
            boolean[][] state = new boolean[conditions.length][];
            int size = 0;
            for (int i = 0; i < conditions.length; i++) {
                state[i] = conditions[i].test(structure, true);
                size = Math.max(size, state[i].length);
            }
            
            int result = 0;
            for (int i = 0; i < state.length; i++) {
                if (i == 0)
                    result = BooleanUtils.toNumber(state[i]);
                else
                    result = perform(result, BooleanUtils.toNumber(state[i]));
            }
            return BooleanUtils.toBits(result, Math.max(size, BooleanUtils.getRequiredBandwidth(result)));
        }
        
        @Override
        public boolean needsBrackets() {
            return true;
        }
        
    }
    
}
