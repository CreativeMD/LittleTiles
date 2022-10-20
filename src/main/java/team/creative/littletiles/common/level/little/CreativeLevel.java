package team.creative.littletiles.common.level.little;

import java.util.function.Supplier;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;

public abstract class CreativeLevel extends Level implements IOrientatedLevel {
    
    public Entity holder;
    public IVecOrigin origin;
    
    private final FakeChunkCache chunkSource;
    
    public final BlockUpdateLevelSystem blockUpdate = new BlockUpdateLevelSystem(this);
    
    public boolean hasChanged = false;
    public boolean preventNeighborUpdate = false;
    
    protected CreativeLevel(WritableLevelData worldInfo, int radius, Supplier<ProfilerFiller> supplier, boolean client, boolean debug, long seed) {
        super(worldInfo, CreativeCore.FAKE_DIMENSION_NAME, CreativeCore.FAKE_DIMENSION, supplier, client, debug, seed, 1000000);
        this.chunkSource = new FakeChunkCache(this, radius);
    }
    
    @Override
    public Entity getHolder() {
        return holder;
    }
    
    @Override
    public void setHolder(Entity entity) {
        this.holder = entity;
    }
    
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
    
    public BlockPos transformToRealWorld(BlockPos pos) {
        return getOrigin().transformPointToWorld(pos);
    }
    
    @Override
    public FakeChunkCache getChunkSource() {
        return chunkSource;
    }
    
    public void unload(LevelChunk chunk) {
        chunk.clearAllBlockEntities();
        this.chunkSource.getLightEngine().enableLightSources(chunk.getPos(), false);
    }
    
    public void onChunkLoaded(ChunkPos pos) {}
    
    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState state, BlockState p_184138_3_, int p_184138_4_) {
        this.hasChanged = true;
    }
    
    public Iterable<Entity> loadedEntities() {
        return getEntities().getAll();
    }
    
    @Override
    public int getFreeMapId() {
        return 0;
    }
    
    @Override
    public void destroyBlockProgress(int p_175715_1_, BlockPos p_175715_2_, int p_175715_3_) {}
    
    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }
    
    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }
    
    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, Context p_220406_) {}
    
    public Iterable<Entity> entities() {
        return getEntities().getAll();
    }
    
    @Override
    public void tickBlockEntities() {
        super.tickBlockEntities();
    }
    
}
