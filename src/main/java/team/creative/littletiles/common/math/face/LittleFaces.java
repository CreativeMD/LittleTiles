package team.creative.littletiles.common.math.face;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleFaces {
    
    private final ByteBuf buffer;
    
    public LittleFaces(byte[] initial) {
        buffer = ByteBufAllocator.DEFAULT.directBuffer(initial.length);
        buffer.writeBytes(initial);
    }
    
    public LittleFaces(int tiles) {
        buffer = ByteBufAllocator.DEFAULT.directBuffer(tiles * 3);
    }
    
    public void resetReader() {
        buffer.resetReaderIndex();
    }
    
    public boolean remaining() {
        return buffer.isReadable(3);
    }
    
    public synchronized void push(LittleFaceSideCache cache) {
        buffer.writeMedium(cache.value);
    }
    
    public void jumpWriter(int size) {
        buffer.writerIndex(buffer.writerIndex() + size * 3);
    }
    
    public void jumpReader(int size) {
        buffer.readerIndex(buffer.readerIndex() + size * 3);
    }
    
    public synchronized void pull(LittleFaceSideCache cache) {
        if (remaining())
            cache.value = buffer.readMedium();
        else
            cache.value = 0;
    }
    
    public synchronized byte[] array() {
        int index = buffer.readerIndex();
        resetReader();
        byte[] array = new byte[buffer.writerIndex()];
        buffer.readBytes(array);
        buffer.readerIndex(index);
        return array;
    }
    
    public synchronized void neighbourChanged(BETiles be, Facing facing) {
        LittleFaceSideCache cache = new LittleFaceSideCache();
        LittleServerFace face = new LittleServerFace(be);
        int index = buffer.readerIndex();
        resetReader();
        buffer.writerIndex(buffer.capacity());
        for (Pair<IParentCollection, LittleTile> entry : be.allTiles()) {
            for (LittleBox box : entry.getValue()) {
                cache.value = buffer.readMedium();
                if (cache.get(facing).outside()) {
                    face.set(entry.getKey(), entry.getValue(), box, facing);
                    cache.set(facing, face.calculate());
                }
                
                int readerIndex = buffer.readerIndex() - 3;
                buffer.resetReaderIndex();
                buffer.writerIndex(readerIndex);
                buffer.writeMedium(cache.value);
                buffer.readerIndex(buffer.writerIndex());
                buffer.writerIndex(buffer.capacity());
            }
        }
        buffer.readerIndex(index);
    }
    
    public static class LittleFaceSideCache {
        
        private int value;
        
        public void clear() {
            value = 0;
        }
        
        public void set(Facing facing, LittleFaceState state) {
            value |= state.ordinal() << (facing.ordinal() * 4);
        }
        
        public LittleFaceState get(Facing facing) {
            return LittleFaceState.values()[(value >> (facing.ordinal() * 4)) & 15];
        }
        
        public int data() {
            return value;
        }
    }
    
}
