package team.creative.littletiles.server.level.little;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTicks;
import team.creative.littletiles.common.level.little.LittleLevel;

public abstract class LittleServerLevel extends LittleLevel {
    
    private final MinecraftServer server;
    final List<ServerPlayer> players = Lists.newArrayList();
    final EntityTickList entityTickList = new EntityTickList();
    private final PersistentEntitySectionManager<Entity> entityManager;
    public boolean noSave;
    private boolean handlingTick;
    private final LevelTicks<Block> blockTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
    private final LevelTicks<Fluid> fluidTicks = new LevelTicks<>(this::isPositionTickingWithEntitiesLoaded, this.getProfilerSupplier());
    
    protected LittleServerLevel(MinecraftServer server, WritableLevelData worldInfo, int radius, ResourceKey<Level> dimension, Supplier<ProfilerFiller> supplier, boolean debug, long seed, RegistryAccess access) {
        super(worldInfo, radius, dimension, supplier, false, debug, seed, access);
        this.server = server;
        this.entityManager = new PersistentEntitySectionManager<>(Entity.class, new LittleServerLevel.EntityCallbacks(), new EntityPersistentStorage<>() {
            
            @Override
            public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos pos) {
                return CompletableFuture.completedFuture(new ChunkEntities<Entity>(pos, Collections.EMPTY_LIST));
            }
            
            @Override
            public void storeEntities(ChunkEntities<Entity> chunk) {}
            
            @Override
            public void flush(boolean p_182503_) {}
        });
    }
    
    @Override
    public MinecraftServer getServer() {
        return server;
    }
    
    @Override
    public List<? extends Player> players() {
        return players;
    }
    
    @Override
    public Entity getEntity(int id) {
        return this.getEntities().get(id);
    }
    
    @Override
    public LevelEntityGetter<Entity> getEntities() {
        return this.entityManager.getEntityGetter();
    }
    
    @Override
    public String gatherChunkSourceStats() {
        return "Chunks[C] W: " + this.getChunkSource().gatherStats() + " E: " + this.entityManager.gatherStats();
    }
    
    public boolean areEntitiesLoaded(long p_143320_) {
        return this.entityManager.areEntitiesLoaded(p_143320_);
    }
    
    public boolean isPositionEntityTicking(BlockPos p_143341_) {
        return this.entityManager.canPositionTick(p_143341_);
    }
    
    public boolean isPositionEntityTicking(ChunkPos p_143276_) {
        return this.entityManager.canPositionTick(p_143276_);
    }
    
    public void addLegacyChunkEntities(Stream<Entity> stream) {
        this.entityManager.addLegacyChunkEntities(stream);
    }
    
    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, Context p_220406_) {}
    
    @Override
    public LevelTicks<Block> getBlockTicks() {
        return this.blockTicks;
    }
    
    @Override
    public LevelTicks<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }
    
    private void tickFluid(BlockPos pos, Fluid fluid) {
        FluidState fluidstate = this.getFluidState(pos);
        if (fluidstate.is(fluid))
            fluidstate.tick(this, pos);
    }
    
    private void tickBlock(BlockPos pos, Block block) {
        BlockState blockstate = this.getBlockState(pos);
        if (blockstate.is(block))
            blockstate.tick((ServerLevel) (Object) this, pos, this.random);
    }
    
    public void tick(BooleanSupplier p_8794_) {
        ProfilerFiller profilerfiller = this.getProfiler();
        this.handlingTick = true;
        profilerfiller.popPush("tickPending");
        if (!this.isDebug()) {
            long k = this.getGameTime();
            profilerfiller.push("blockTicks");
            this.blockTicks.tick(k, 65536, this::tickBlock);
            profilerfiller.popPush("fluidTicks");
            this.fluidTicks.tick(k, 65536, this::tickFluid);
            profilerfiller.pop();
        }
        profilerfiller.popPush("chunkSource");
        this.getChunkSource().tick(p_8794_, true);
        profilerfiller.popPush("blockEvents");
        this.runBlockEvents();
        this.handlingTick = false;
        
        profilerfiller.pop();
        boolean flag = !this.players.isEmpty() || net.minecraftforge.common.world.ForgeChunkManager.hasForcedChunks(this); //Forge: Replace vanilla's has forced chunk check with forge's that checks both the vanilla and forge added ones
        if (flag) {
            this.resetEmptyTime();
        }
        
        if (flag || this.emptyTime++ < 300) {
            profilerfiller.push("entities");
            if (this.dragonFight != null) {
                profilerfiller.push("dragonFight");
                this.dragonFight.tick();
                profilerfiller.pop();
            }
            
            this.entityTickList.forEach((p_184065_) -> {
                if (!p_184065_.isRemoved()) {
                    if (this.shouldDiscardEntity(p_184065_)) {
                        p_184065_.discard();
                    } else {
                        profilerfiller.push("checkDespawn");
                        p_184065_.checkDespawn();
                        profilerfiller.pop();
                        if (this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(p_184065_.chunkPosition().toLong())) {
                            Entity entity = p_184065_.getVehicle();
                            if (entity != null) {
                                if (!entity.isRemoved() && entity.hasPassenger(p_184065_)) {
                                    return;
                                }
                                
                                p_184065_.stopRiding();
                            }
                            
                            profilerfiller.push("tick");
                            if (!p_184065_.isRemoved() && !(p_184065_ instanceof net.minecraftforge.entity.PartEntity)) {
                                this.guardEntityTick(this::tickNonPassenger, p_184065_);
                            }
                            profilerfiller.pop();
                        }
                    }
                }
            });
            profilerfiller.pop();
            this.tickBlockEntities();
        }
        
        profilerfiller.push("entityManagement");
        this.entityManager.tick();
        profilerfiller.popPush("gameEvents");
        this.sendGameEvents();
        profilerfiller.pop();
    }
    
    final class EntityCallbacks implements LevelCallback<Entity> {
        
        @Override
        public void onCreated(Entity entity) {}
        
        @Override
        public void onDestroyed(Entity entity) {
            LittleServerLevel.this.getScoreboard().entityRemoved(entity);
        }
        
        @Override
        public void onTickingStart(Entity entity) {
            LittleServerLevel.this.entityTickList.add(entity);
        }
        
        @Override
        public void onTickingEnd(Entity entity) {
            LittleServerLevel.this.entityTickList.remove(entity);
        }
        
        @Override
        public void onTrackingStart(Entity entity) {
            LittleServerLevel.this.getChunkSource().addEntity(entity);
            
        }
        
        @Override
        public void onTrackingEnd(Entity entity) {
            LittleServerLevel.this.getChunkSource().removeEntity(entity);
            entity.updateDynamicGameEventListener(DynamicGameEventListener::move);
        }
        
        @Override
        public void onSectionChange(Entity entity) {}
    }
    
}
