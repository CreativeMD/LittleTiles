package team.creative.littletiles.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.ForgeEventFactory;
import team.creative.littletiles.server.level.little.LittleChunkMap;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    
    @Shadow
    private ServerLevel level;
    
    @Shadow
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;
    
    @Shadow
    private boolean modified;
    
    @Shadow
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads;
    
    @Shadow
    private ThreadedLevelLightEngine lightEngine;
    
    @Shadow
    private ChunkTaskPriorityQueueSorter queueSorter;
    
    @Shadow
    private LongSet toDrop;
    
    public ChunkMap as() {
        return (ChunkMap) (Object) this;
    }
    
    @Inject(at = @At("HEAD"), method = "updateChunkScheduling(JILnet/minecraft/server/level/ChunkHolder;I)Lnet/minecraft/server/level/ChunkHolder;", cancellable = true,
            require = 1)
    public void updateChunkScheduling(long pos, int oldTicketLevel, @Nullable ChunkHolder holder, int newTicketLevel, CallbackInfoReturnable<ChunkHolder> callback) {
        if (as() instanceof LittleChunkMap) {
            
            //if (newTicketLevel > ChunkMap.MAX_CHUNK_DISTANCE && oldTicketLevel > ChunkMap.MAX_CHUNK_DISTANCE)
            //    callback.setReturnValue(holder);
            
            if (holder != null)
                holder.setTicketLevel(newTicketLevel);
            
            if (holder != null)
                //if (newTicketLevel > ChunkMap.MAX_CHUNK_DISTANCE)
                //    this.toDrop.add(pos);
                //else
                this.toDrop.remove(pos);
            
            if (/*newTicketLevel <= ChunkMap.MAX_CHUNK_DISTANCE &&*/ holder == null) {
                holder = this.pendingUnloads.remove(pos);
                if (holder != null)
                    holder.setTicketLevel(newTicketLevel);
                else
                    holder = new ChunkHolder(new ChunkPos(pos), newTicketLevel, this.level, this.lightEngine, this.queueSorter, as());
                
                this.updatingChunkMap.put(pos, holder);
                this.modified = true;
            }
            
            ForgeEventFactory.fireChunkTicketLevelUpdated(this.level, pos, oldTicketLevel, newTicketLevel, holder);
            callback.setReturnValue(holder);
        }
    }
    
}
