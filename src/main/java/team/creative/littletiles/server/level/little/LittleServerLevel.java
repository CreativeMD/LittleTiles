package team.creative.littletiles.server.level.little;

import java.util.Collections;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.common.level.little.BlockUpdateLevelSystem;
import team.creative.littletiles.common.level.little.LevelBoundsListener;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.mixin.MinecraftServerAccessor;

public abstract class LittleServerLevel extends ServerLevel implements LittleLevel {
    
    private static LevelStem overworldStem(MinecraftServer server) {
        Registry<LevelStem> registry = server.getWorldData().worldGenSettings().dimensions();
        return registry.get(LevelStem.OVERWORLD);
    }
    
    public Entity holder;
    public IVecOrigin origin;
    
    public final BlockUpdateLevelSystem blockUpdate = new BlockUpdateLevelSystem(this);
    
    public boolean hasChanged = false;
    public boolean preventNeighborUpdate = false;
    
    private RegistryAccess access;
    
    protected LittleServerLevel(MinecraftServer server, ServerLevelData worldInfo, ResourceKey<Level> dimension, boolean debug, long seed, RegistryAccess access) {
        super(server, Util.backgroundExecutor(), ((MinecraftServerAccessor) server)
                .getStorageSource(), worldInfo, dimension, overworldStem(server), LittleChunkProgressListener.INSTANCE, debug, seed, Collections.EMPTY_LIST, false);
        this.access = access;
    }
    
    @Override
    public Entity getHolder() {
        return holder;
    }
    
    @Override
    public void setHolder(Entity entity) {
        this.holder = entity;
    }
    
    @Override
    public void registerLevelBoundListener(LevelBoundsListener listener) {
        this.blockUpdate.registerLevelBoundListener(listener);
    }
    
    @Override
    public void neighborChanged(BlockPos pos, Block block, BlockPos fromPos) {
        if (preventNeighborUpdate)
            return;
        if (this.isClientSide) {
            BlockState blockstate = this.getBlockState(pos);
            
            try {
                blockstate.neighborChanged(this, pos, block, fromPos, false);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
                crashreportcategory.setDetail("Source block type", () -> {
                    try {
                        return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                    } catch (Throwable throwable1) {
                        return "ID #" + Registry.BLOCK.getKey(block);
                    }
                });
                CrashReportCategory.populateBlockDetails(crashreportcategory, this, pos, blockstate);
                throw new ReportedException(crashreport);
            }
        } else
            super.neighborChanged(pos, block, fromPos);
    }
    
    @Override
    public void updateNeighborsAtExceptFromFacing(BlockPos pos, Block block, Direction facing) {
        if (preventNeighborUpdate)
            return;
        super.updateNeighborsAtExceptFromFacing(pos, block, facing);
    }
    
    @Override
    public void updateNeighborsAt(BlockPos pos, Block block) {
        if (preventNeighborUpdate)
            return;
        super.updateNeighborsAt(pos, block);
    }
    
    @Override
    public LittleServerChunkCache getChunkSource() {
        return (LittleServerChunkCache) super.getChunkSource();
    }
    
    @Override
    public void unload(LevelChunk chunk) {
        super.unload(chunk);
        this.getChunkSource().getLightEngine().enableLightSources(chunk.getPos(), false);
    }
    
    @Override
    public int getFreeMapId() {
        return 0;
    }
    
    @Override
    public Iterable<Entity> entities() {
        return getEntities().getAll();
    }
    
    @Override
    public void tick() {
        while (getChunkSource().pollTask()) {}
        tick(((MinecraftServerAccessor) getServer())::callHaveTime);
    }
    
    @Override
    public RegistryAccess registryAccess() {
        if (access == null)
            return getServer().registryAccess();
        return access;
    }
    
    @Override
    public void load(ChunkPos pos, CompoundTag nbt) {
        getChunkSource().addLevelChunkTag(pos, nbt);
    }
    
    @Override
    public Iterable<? extends ChunkAccess> chunks() {
        return getChunkSource().all();
    }
    
}
