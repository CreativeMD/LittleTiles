package team.creative.littletiles.common.structure.signal.logic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalUtils;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;

public abstract class SignalTarget {
    
    public static SignalTarget parseTarget(SignalPatternParser parser, boolean onlyOutputs, boolean insideVariable) throws ParseException {
        char begin = parser.next(true);
        if (begin == 'a' || begin == 'b' || begin == 'i' || begin == 'o') {
            if (insideVariable)
                throw parser.exception("Invalid usuage of variables in sub equation, only d<index> are allowed");
            boolean external = begin == 'i' || begin == 'o';
            boolean input = begin == 'a' || begin == 'i';
            if (onlyOutputs && input)
                throw parser.exception("Input cannot be defined in output condition");
            
            int child = parser.parseNumber();
            
            if (parser.hasNext() && parser.lookForNext(false) == '[') {
                parser.next(false);
                if (parser.lookForNext(true) == ']')
                    return new SignalTargetChild(input, child, external);
                SignalCustomIndex first = parseIndex(parser);
                char next = parser.lookForNext(true);
                
                if (next == ']') {
                    parser.next(true);
                    if (first instanceof SignalCustomIndexSingle)
                        return new SignalTargetChildIndex(input, child, external, ((SignalCustomIndexSingle) first).index);
                    else
                        return new SignalTargetChildIndexRange(input, child, external, ((SignalCustomIndexRange) first).index, ((SignalCustomIndexRange) first).length);
                } else if (next == ',') {
                    List<SignalCustomIndex> indexes = new ArrayList<>();
                    indexes.add(first);
                    while ((next = parser.next(true)) == ',')
                        indexes.add(parseIndex(parser));
                    if (next == ']')
                        return new SignalTargetChildCustomIndex(input, child, external, indexes.toArray(new SignalCustomIndex[indexes.size()]));
                    throw parser.invalidChar(next);
                } else
                    throw parser.invalidChar(next);
            } else
                return new SignalTargetChild(input, child, external);
        } else if (begin == 'c') {
            int child = parser.parseNumber();
            if (parser.next(false) == '.')
                return new SignalTargetNested(child, parseTarget(parser, onlyOutputs, insideVariable));
            else
                throw parser.exception("Missing dot after child input");
        } else if (begin == 'p') {
            if (parser.next(false) == '.')
                return new SignalTargetParent(parseTarget(parser, onlyOutputs, insideVariable));
            else
                throw parser.exception("Missing dot after child input");
        } else if (begin == 'd') {
            if (!insideVariable)
                throw parser.exception("Invalid usage of index variable");
            return new SignalTargetIndex(parser.parseNumber());
        } else
            throw parser.invalidChar(begin);
    }
    
    public static SignalCustomIndex parseIndex(SignalPatternParser parser) throws ParseException {
        int start = parser.parseNumber();
        if (parser.lookForNext(true) != '-')
            return new SignalCustomIndexSingle(start);
        parser.next(true);
        int end = parser.parseNumber();
        if (end <= start)
            throw parser.exception("Invalid second index " + start + " < " + end);
        return new SignalCustomIndexRange(start, end);
    }
    
    public final int child;
    
    public SignalTarget(int child) {
        this.child = child;
    }
    
    public final boolean[] getState(LittleStructure structure) {
        try {
            return getState(getTarget(structure));
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            return BooleanUtils.asArray(false);
        }
    }
    
    protected boolean[] getState(ISignalComponent component) throws CorruptedConnectionException, NotYetConnectedException {
        if (component == null)
            throw new NotYetConnectedException();
        return component.getState();
    }
    
    public abstract ISignalComponent getTarget(LittleStructure structure);
    
    public abstract String writeBase();
    
    public abstract String write();
    
    public abstract boolean isIndexVariable();
    
    public int getBandwidth(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException {
        ISignalComponent component = getTarget(structure);
        if (component != null)
            return component.getBandwidth();
        return 1;
    }
    
    public SignalTarget getNestedTarget() {
        return this;
    }
    
    public static class SignalTargetNested extends SignalTarget {
        
        public final SignalTarget subTarget;
        
        public SignalTargetNested(int child, SignalTarget subTarget) {
            super(child);
            this.subTarget = subTarget;
        }
        
        @Override
        protected boolean[] getState(ISignalComponent component) throws CorruptedConnectionException, NotYetConnectedException {
            return subTarget.getState(component);
        }
        
        @Override
        public ISignalComponent getTarget(LittleStructure structure) {
            try {
                return subTarget.getTarget(structure.children.getChild(child).getStructure());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            return null;
        }
        
        @Override
        public String writeBase() {
            return "c" + child + "." + subTarget.writeBase();
        }
        
        @Override
        public String write() {
            return "c" + child + "." + subTarget.write();
        }
        
        @Override
        public boolean isIndexVariable() {
            return subTarget.isIndexVariable();
        }
        
        @Override
        public SignalTarget getNestedTarget() {
            return subTarget;
        }
    }
    
    public static class SignalTargetParent extends SignalTarget {
        
        public final SignalTarget subTarget;
        
        public SignalTargetParent(SignalTarget subTarget) {
            super(-1);
            this.subTarget = subTarget;
        }
        
        @Override
        public ISignalComponent getTarget(LittleStructure structure) {
            if (structure.getParent() != null)
                try {
                    
                    return subTarget.getTarget(structure.getParent().getStructure());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            return null;
        }
        
        @Override
        protected boolean[] getState(ISignalComponent component) throws CorruptedConnectionException, NotYetConnectedException {
            return subTarget.getState(component);
        }
        
        @Override
        public String writeBase() {
            return "p." + subTarget.writeBase();
        }
        
        @Override
        public String write() {
            return "p." + subTarget.write();
        }
        
        @Override
        public boolean isIndexVariable() {
            return subTarget.isIndexVariable();
        }
        
        @Override
        public SignalTarget getNestedTarget() {
            return subTarget;
        }
    }
    
    public static class SignalTargetIndex extends SignalTarget {
        
        public SignalTargetIndex(int index) {
            super(index);
        }
        
        @Override
        public ISignalComponent getTarget(LittleStructure structure) {
            return null;
        }
        
        @Override
        public String writeBase() {
            return "d" + child;
        }
        
        @Override
        public String write() {
            return "d" + child;
        }
        
        @Override
        public boolean isIndexVariable() {
            return true;
        }
        
    }
    
    public static class SignalTargetChild extends SignalTarget {
        
        public final boolean input;
        
        public final boolean external;
        
        public SignalTargetChild(boolean input, int child, boolean external) {
            super(child);
            this.input = input;
            this.external = external;
        }
        
        @Override
        public ISignalComponent getTarget(LittleStructure structure) {
            return input ? SignalUtils.getInput(structure, child, external) : SignalUtils.getOutput(structure, child, external);
        }
        
        @Override
        public String writeBase() {
            if (external)
                return (input ? "i" : "o") + child;
            return (input ? "a" : "b") + child;
        }
        
        @Override
        public String write() {
            if (external)
                return (input ? "i" : "o") + child;
            return (input ? "a" : "b") + child;
        }
        
        @Override
        public boolean isIndexVariable() {
            return false;
        }
        
    }
    
    public static class SignalTargetChildIndex extends SignalTargetChild {
        
        public final int index;
        
        public SignalTargetChildIndex(boolean input, int child, boolean external, int index) {
            super(input, child, external);
            this.index = index;
        }
        
        @Override
        protected boolean[] getState(ISignalComponent component) throws CorruptedConnectionException, NotYetConnectedException {
            boolean[] state = component.getState();
            if (state.length > index)
                return BooleanUtils.asArray(state[index]);
            return BooleanUtils.asArray(false);
        }
        
        @Override
        public String write() {
            return super.write() + "[" + index + "]";
        }
        
        @Override
        public int getBandwidth(LittleStructure structure) {
            return 1;
        }
        
    }
    
    public static class SignalTargetChildIndexRange extends SignalTargetChild {
        
        public final int index;
        public final int length;
        
        public SignalTargetChildIndexRange(boolean input, int output, boolean external, int index, int length) {
            super(input, output, external);
            this.index = index;
            this.length = length;
        }
        
        @Override
        protected boolean[] getState(ISignalComponent component) throws CorruptedConnectionException, NotYetConnectedException {
            boolean[] state = component.getState();
            boolean[] newState = new boolean[length];
            for (int i = 0; i < newState.length; i++)
                if (state.length > i + index)
                    newState[i] = state[index + 1];
            return newState;
        }
        
        @Override
        public String write() {
            return super.write() + "[" + index + "-" + index + length + "]";
        }
        
        @Override
        public int getBandwidth(LittleStructure structure) {
            return length;
        }
        
    }
    
    public static class SignalTargetChildCustomIndex extends SignalTargetChild {
        
        public final SignalCustomIndex[] indexes;
        
        public SignalTargetChildCustomIndex(boolean input, int child, boolean external, SignalCustomIndex[] indexes) {
            super(input, child, external);
            this.indexes = indexes;
        }
        
        @Override
        protected boolean[] getState(ISignalComponent component) throws CorruptedConnectionException, NotYetConnectedException {
            boolean[] state = component.getState();
            boolean[] newState = new boolean[component.getBandwidth()];
            int index = 0;
            for (int i = 0; i < indexes.length; i++)
                index += indexes[i].set(newState, index, state);
            return newState;
        }
        
        @Override
        public String write() {
            String result = super.write() + "[";
            for (int i = 0; i < indexes.length; i++) {
                if (i > 0)
                    result += ",";
                result += indexes[i].write();
            }
            return result + "]";
        }
        
        @Override
        public int getBandwidth(LittleStructure structure) {
            int length = 0;
            for (int i = 0; i < indexes.length; i++)
                length += indexes[i].length();
            return length;
        }
        
    }
    
    public static abstract class SignalCustomIndex {
        
        public abstract String write();
        
        public abstract int length();
        
        public abstract int set(boolean[] toModify, int index, boolean[] value);
        
    }
    
    public static class SignalCustomIndexSingle extends SignalCustomIndex {
        
        public final int index;
        
        public SignalCustomIndexSingle(int index) {
            this.index = index;
        }
        
        @Override
        public int set(boolean[] toModify, int index, boolean[] value) {
            if (toModify.length <= index)
                return 0;
            toModify[index] = value[index];
            return 1;
        }
        
        @Override
        public String write() {
            return index + "";
        }
        
        @Override
        public int length() {
            return 1;
        }
    }
    
    public static class SignalCustomIndexRange extends SignalCustomIndex {
        
        public final int index;
        public final int length;
        
        public SignalCustomIndexRange(int index, int length) {
            this.index = index;
            this.length = length - index + 1;
        }
        
        @Override
        public int set(boolean[] toModify, int index, boolean[] value) {
            int added = 0;
            for (int i = 0; i < length; i++) {
                if (toModify.length <= index + i)
                    break;
                toModify[index + i] = value[this.index + i];
                added++;
            }
            return added;
        }
        
        @Override
        public String write() {
            return index + "-" + (index + length - 1);
        }
        
        @Override
        public int length() {
            return length;
        }
    }
    
}
