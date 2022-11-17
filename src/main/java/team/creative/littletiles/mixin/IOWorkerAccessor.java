package team.creative.littletiles.mixin;

import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.chunk.storage.IOWorker;

@Mixin(IOWorker.class)
public interface IOWorkerAccessor {
    
    @Invoker(value = "<init>")
    static IOWorker create(Path path, boolean sync, String name) {
        throw new UnsupportedOperationException();
    }
    
}
