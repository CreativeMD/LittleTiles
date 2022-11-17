package team.creative.littletiles.server.level.little;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;

public class LittleIOWorker extends IOWorker {
    
    private HashMap<ChunkPos, CompoundTag> data = new HashMap<>();
    
    public LittleIOWorker(Path path, boolean sync, String name) {
        super(path, sync, name);
    }
    
    @Override
    public boolean isOldChunkAround(ChunkPos pos, int distance) {
        return false;
    }
    
    @Override
    public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos pos) {
        return CompletableFuture.supplyAsync(() -> {
            CompoundTag tag = data.get(pos);
            if (tag == null)
                tag = new CompoundTag();
            return Optional.of(tag);
        });
    }
    
    @Override
    public CompletableFuture<Void> store(ChunkPos pos, @Nullable CompoundTag tag) {
        return CompletableFuture.runAsync(() -> {
            if (tag == null)
                data.remove(pos);
            else
                data.put(pos, tag);
        });
    }
    
    public void add(ChunkPos pos, CompoundTag tag) {
        data.put(pos, tag);
    }
    
}
