package team.creative.littletiles.common.structure.signal.logic;

import java.text.ParseException;
import java.util.Iterator;

import team.creative.creativecore.common.util.type.itr.ArrayIterator;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;

public class SignalInputConditionGate extends SignalInputCondition {
    
    public static SignalInputConditionGate parseGate(SignalPatternParser parser, boolean invert, boolean includeBitwise, boolean insideVariable) throws ParseException {
        parser.next(true);
        SignalInputCondition gate = parseExpression(parser, new char[] { ',' }, includeBitwise, insideVariable);
        parser.next(true);
        SignalInputCondition through = parseExpression(parser, new char[] { ']' }, includeBitwise, insideVariable);
        parser.next(true);
        return new SignalInputConditionGate(gate, invert, through, SignalInputCondition.parsePosition(parser));
    }
    
    public final boolean invert;
    public final SignalInputCondition gate;
    public final SignalInputCondition through;
    
    public SignalInputConditionGate(SignalInputCondition gate, boolean invert, SignalInputCondition through, SignalPosition position) {
        super(position);
        this.invert = invert;
        this.gate = gate;
        this.through = through;
    }
    
    @Override
    public SignalState test(LittleStructure structure, boolean forceBitwise) {
        if (gate.test(structure, forceBitwise).any() != invert)
            return through.test(structure, forceBitwise);
        return SignalState.FALSE;
    }
    
    @Override
    public boolean testIndex(SignalState state) {
        return false;
    }
    
    @Override
    public String write() {
        return (invert ? "!" : "") + "[" + gate.write() + "," + through.write() + "]" + writePosition();
    }
    
    @Override
    protected double internalDelay() {
        if (invert)
            return LittleTiles.CONFIG.signal.nGate;
        return LittleTiles.CONFIG.signal.gate;
    }
    
    @Override
    public Iterator<SignalInputCondition> nested() {
        return new ArrayIterator<>(new SignalInputCondition[] { gate, through });
    }
    
    @Override
    public SignalTarget target() {
        return null;
    }
    
}
