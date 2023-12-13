package team.creative.littletiles.common.structure.signal;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import team.creative.creativecore.common.util.math.utils.IntegerUtils;

public abstract class SignalState {
    
    public static SignalState of(boolean value) {
        return value ? TRUE : FALSE;
    }
    
    public static SignalState of(int number) {
        return new IntegerState(number);
    }
    
    public static SignalState of(long number) {
        throw new UnsupportedOperationException();
    }
    
    public static SignalState create(int bandwidth) {
        if (bandwidth == 1)
            return FALSE;
        if (bandwidth <= 32)
            return new IntegerState(0);
        
        throw new UnsupportedOperationException();
    }
    
    public static SignalState loadFromTag(Tag tag) {
        if (tag instanceof NumericTag number) {
            if (tag instanceof LongTag)
                throw new UnsupportedOperationException();
            if (tag instanceof ByteTag byteT)
                return of(number.getAsByte() != 0);
            return of(number.getAsInt());
        }
        return FALSE;
    }
    
    public static SignalState read(FriendlyByteBuf buffer) {
        int length = buffer.readInt();
        if (length == 0)
            return of(buffer.readBoolean());
        if (length == 1)
            return new IntegerState(buffer.readInt());
        
        throw new UnsupportedOperationException();
    }
    
    public static SignalState copy(SignalState state) {
        if (state instanceof PrimitveState)
            return state;
        if (state instanceof IntegerState iState)
            return new IntegerState(iState.value);
        throw new UnsupportedOperationException();
    }
    
    public static int getRequiredBandwidth(int number) {
        int digit = 0;
        while (number != 0) {
            digit++;
            number = number / 10;
        }
        return digit;
    }
    
    public static final SignalState TRUE = new PrimitveState(true);
    public static final SignalState FALSE = new PrimitveState(false);
    
    @Override
    public abstract boolean equals(Object object);
    
    public abstract SignalState overwrite(SignalState state);
    
    public abstract SignalState set(int index, boolean value);
    
    public abstract SignalState setNumber(int value);
    
    public abstract SignalState setLongNumber(long value);
    
    public abstract SignalState fill(boolean value);
    
    public abstract SignalState fill(SignalState other);
    
    public abstract SignalState reset();
    
    public abstract boolean equals(int bandwidth, SignalState state);
    
    public abstract boolean any();
    
    public abstract SignalState or(SignalState other);
    
    public abstract boolean is(int index);
    
    public boolean is(int[] indexes) {
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] == 2)
                continue;
            if (indexes[i] != (is(i) ? 1 : 0))
                return false;
        }
        return true;
    }
    
    public abstract SignalState load(Tag tag);
    
    public abstract SignalState load(int number);
    
    public abstract Tag save();
    
    public abstract int number();
    
    public abstract long longNumber();
    
    @Override
    public abstract String toString();
    
    public abstract String print(int length);
    
    public abstract void write(FriendlyByteBuf buffer);
    
    public abstract SignalState invert();
    
    public abstract SignalStateSize size();
    
    public abstract void shrinkTo(int bandwidth);
    
    private static class PrimitveState extends SignalState {
        
        private final boolean value;
        
        private PrimitveState(boolean value) {
            this.value = value;
        }
        
        @Override
        public boolean is(int index) {
            if (index == 0)
                return value;
            return false;
        }
        
        @Override
        public String toString() {
            return "[" + value + "]";
        }
        
        @Override
        public String print(int length) {
            return toString();
        }
        
        @Override
        public SignalState overwrite(SignalState state) {
            return of(state.any());
        }
        
        @Override
        public SignalState set(int index, boolean value) {
            if (index == 0)
                return of(value);
            return this;
        }
        
        @Override
        public SignalState reset() {
            return FALSE;
        }
        
        @Override
        public boolean equals(int bandwidth, SignalState state) {
            return value == state.any();
        }
        
        @Override
        public boolean any() {
            return value;
        }
        
        @Override
        public SignalState load(Tag tag) {
            if (tag instanceof NumericTag num)
                return num.getAsByte() == 0 ? FALSE : TRUE;
            else
                return FALSE;
        }
        
        @Override
        public Tag save() {
            return ByteTag.valueOf(value);
        }
        
        @Override
        public int number() {
            return value ? 1 : 0;
        }
        
        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeInt(0);
            buffer.writeBoolean(value);
        }
        
        @Override
        public SignalState setNumber(int value) {
            return of(value != 0);
        }
        
        @Override
        public SignalState setLongNumber(long value) {
            return of(value != 0);
        }
        
        @Override
        public SignalState fill(boolean value) {
            return of(value);
        }
        
        @Override
        public SignalState fill(SignalState other) {
            return of(other.any());
        }
        
        @Override
        public SignalState or(SignalState other) {
            return of(value || other.any());
        }
        
        @Override
        public SignalState load(int number) {
            return of(number != 0);
        }
        
        @Override
        public long longNumber() {
            return value ? 1 : 0;
        }
        
        @Override
        public SignalState invert() {
            return of(!value);
        }
        
        @Override
        public SignalStateSize size() {
            return SignalStateSize.SINGLE;
        }
        
        @Override
        public void shrinkTo(int bandwidth) {}
        
        @Override
        public boolean equals(Object object) {
            if (object instanceof PrimitveState prim)
                return this == object;
            else if (object instanceof IntegerState integer)
                return integer.number() == number();
            else if (object instanceof SignalState state)
                return state.longNumber() == longNumber();
            return false;
        }
        
    }
    
    static class IntegerState extends SignalState {
        
        private int value;
        
        public IntegerState(int value) {
            this.value = value;
        }
        
        @Override
        public boolean is(int index) {
            if (index < 32)
                return IntegerUtils.bitIs(value, index);
            return false;
        }
        
        @Override
        public String toString() {
            return "[" + value + "]";
        }
        
        @Override
        public String print(int length) {
            String result = "[";
            for (int i = 0; i < length; i++)
                result += is(i) ? "1" : "0";
            return result + "]";
        }
        
        @Override
        public SignalState load(Tag tag) {
            if (tag instanceof NumericTag num)
                value = num.getAsInt();
            else
                value = 0;
            return this;
        }
        
        @Override
        public Tag save() {
            return IntTag.valueOf(value);
        }
        
        @Override
        public SignalState overwrite(SignalState state) {
            this.value = state.number();
            return this;
        }
        
        @Override
        public SignalState set(int index, boolean value) {
            this.value = IntegerUtils.set(this.value, index, value);
            return this;
        }
        
        @Override
        public SignalState setNumber(int value) {
            this.value = value;
            return this;
        }
        
        @Override
        public SignalState setLongNumber(long value) {
            this.value = (int) value;
            return this;
        }
        
        @Override
        public SignalState fill(boolean value) {
            this.value = value ? -1 : 0;
            return this;
        }
        
        @Override
        public SignalState fill(SignalState other) {
            this.value = other.number();
            return this;
        }
        
        @Override
        public SignalState reset() {
            this.value = 0;
            return this;
        }
        
        @Override
        public boolean equals(int bandwidth, SignalState state) {
            return shrinkTo(state.number(), bandwidth) == shrinkTo(value, bandwidth);
        }
        
        @Override
        public boolean any() {
            return value != 0;
        }
        
        @Override
        public SignalState or(SignalState other) {
            this.value |= other.number();
            return this;
        }
        
        @Override
        public SignalState load(int number) {
            this.value = number;
            return this;
        }
        
        @Override
        public int number() {
            return value;
        }
        
        @Override
        public long longNumber() {
            return value;
        }
        
        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeInt(1);
            buffer.writeInt(value);
        }
        
        @Override
        public SignalState invert() {
            value = ~value;
            return this;
        }
        
        @Override
        public SignalStateSize size() {
            return SignalStateSize.INT;
        }
        
        @Override
        public void shrinkTo(int bandwidth) {
            value = shrinkTo(value, bandwidth);
        }
        
        @Override
        public boolean equals(Object object) {
            if (object instanceof IntegerState integer)
                return this.value == integer.value;
            else if (object instanceof SignalState state)
                return state.longNumber() == longNumber();
            return false;
        }
        
        private static int shrinkTo(int number, int bandwidth) {
            bandwidth--;
            return number & (-1 >>> (31 - bandwidth));
        }
    }
    
    public static enum SignalStateSize {
        
        SINGLE(1) {
            @Override
            public SignalState create() {
                return FALSE;
            }
        },
        INT(32) {
            @Override
            public SignalState create() {
                return new IntegerState(0);
            }
        },
        LONG(64) {
            @Override
            public SignalState create() {
                throw new UnsupportedOperationException();
            }
        };
        
        public final int bandwidth;
        
        private SignalStateSize(int bandwidth) {
            this.bandwidth = bandwidth;
        }
        
        public abstract SignalState create();
        
        public SignalStateSize max(SignalStateSize size) {
            if (ordinal() > size.ordinal())
                return this;
            return size;
        }
        
    }
    
}
