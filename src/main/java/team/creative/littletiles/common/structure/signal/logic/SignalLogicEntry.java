package team.creative.littletiles.common.structure.signal.logic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public interface SignalLogicEntry {
    
    public static final SignalLogicEntry HIGHEST_GENERAL = SignalLogicOperator.XOR;
    
    public static final SignalLogicEntry HIGHEST = SignalLogicComparator.SMALLER_EQUALS;
    
    public static final List<SignalLogicEntry> ALL = calculateAll();
    
    private static List<SignalLogicEntry> calculateAll() {
        List<SignalLogicEntry> entries = new ArrayList<>();
        var entry = HIGHEST;
        while (entry != null) {
            entries.add(entry);
            entry = entry.lower();
        }
        return Collections.unmodifiableList(entries);
    }
    
    public static SignalLogicEntry getHighest(boolean includeBitwise) {
        if (includeBitwise)
            return HIGHEST;
        return HIGHEST_GENERAL;
    }
    
    public SignalLogicEntry lower();
    
    public String operator();
    
    public int maxArgmumentCount();
    
    public default boolean is(SignalPatternParser parser) {
        String operator = operator();
        if (operator.length() == 1)
            return parser.lookForNext(true) == operator.charAt(0);
        return parser.lookForNext(operator.length(), true).equals(operator);
    }
    
    public default void consume(SignalPatternParser parser) {
        parser.skip(operator().length(), true);
    }
    
    public SignalInputCondition create(SignalInputCondition[] conditions, @Nullable SignalPosition position);
    
    public default boolean goOn(SignalPatternParser parser) throws ParseException {
        if (parser.hasNext()) {
            if (lower() == null) {
                char next = parser.lookForNext(true);
                return next == '(' || next == '!' || (next <= 122 && next >= 97);
            } else if (is(parser)) {
                consume(parser);
                return true;
            }
        }
        return false;
    }
}
