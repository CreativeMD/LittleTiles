package team.creative.littletiles.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.creativecore.common.util.math.utils.IntegerUtils;

public class MatrixDataComponent {
    
    public static final Codec<MatrixDataComponent> CODEC = new PrimitiveCodec<MatrixDataComponent>() {
        @Override
        public <T> DataResult<MatrixDataComponent> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(Number::intValue).map(MatrixDataComponent::new);
        }
        
        @Override
        public <T> T write(final DynamicOps<T> ops, final MatrixDataComponent value) {
            return ops.createInt(value.data);
        }
        
        @Override
        public String toString() {
            return "Matrix";
        }
    };
    
    public static final StreamCodec<ByteBuf, MatrixDataComponent> STREAM_CODEC = new StreamCodec<ByteBuf, MatrixDataComponent>() {
        
        @Override
        public MatrixDataComponent decode(ByteBuf buf) {
            return new MatrixDataComponent(buf.readInt());
        }
        
        @Override
        public void encode(ByteBuf buf, MatrixDataComponent matrix) {
            buf.writeInt(matrix.data);
        }
    };
    
    public static MatrixDataComponent of(IntMatrix3c matrix) {
        int[] array = matrix.getAsArray();
        int data = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > 0)
                data = IntegerUtils.set(data, i * 2);
            else if (array[i] < 0)
                data = IntegerUtils.set(data, i * 2 + 1);
        }
        return new MatrixDataComponent(data);
    }
    
    private final int data;
    
    private MatrixDataComponent(int data) {
        this.data = data;
    }
    
    public IntMatrix3 getMatrix() {
        int[] array = new int[9];
        for (int i = 0; i < array.length; i++) {
            if (IntegerUtils.bitIs(data, i * 2))
                array[i] = 1;
            else if (IntegerUtils.bitIs(data, i * 2 + 1))
                array[i] = -1;
        }
        return new IntMatrix3(array);
    }
    
    public boolean isEqual(MatrixDataComponent m) {
        return m.data == data;
    }
    
    @Override
    /** This always returns true because items should still stack if this component is different. Please use {@code isEqual()} instead if you want to check if the matrices are equal. */
    public boolean equals(Object obj) {
        if (obj instanceof MatrixDataComponent)
            return true;
        return super.equals(obj);
    }
    
}
