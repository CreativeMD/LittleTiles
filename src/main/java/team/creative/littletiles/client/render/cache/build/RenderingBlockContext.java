package team.creative.littletiles.client.render.cache.build;

import java.util.HashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.cache.build.RenderingThread.RemovedBlockEntityException;
import team.creative.littletiles.client.render.cache.build.RenderingThread.RenderingBlockedException;
import team.creative.littletiles.client.render.cache.build.RenderingThread.RenderingException;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

public class RenderingBlockContext {
    
    public final BETiles be;
    public final BlockState state;
    public final long pos;
    private final RenderingLevelHandler handler;
    public int index;
    
    public HashMap<Facing, BETiles> neighboursBEs;
    
    public RenderingBlockContext(BETiles be, long pos, RenderingLevelHandler handler) {
        this.be = be;
        this.state = be.getBlockState();
        this.pos = pos;
        this.handler = handler;
    }
    
    public void checkRemoved() throws RemovedBlockEntityException {
        if (be.isRemoved())
            throw new RemovedBlockEntityException(be.getBlockPos() + "");
    }
    
    public void checkLoaded() throws RenderingException {
        if (be.getLevel() == null || !be.hasLoaded())
            throw new RenderingException("BlockEntity is not loaded yet");
        if (be.render.isBlocked())
            throw new RenderingBlockedException();
    }
    
    public void beforeBuilding() {
        be.render.beforeBuilding(this);
    }
    
    public void clearQuadBuilding() {
        neighboursBEs = null;
    }
    
    public BETiles getNeighbour(Facing facing) {
        if (neighboursBEs == null)
            neighboursBEs = new HashMap<>();
        else if (neighboursBEs.containsKey(facing))
            return neighboursBEs.get(facing);
        BlockEntity be = this.be.getLevel().getBlockEntity(this.be.getBlockPos().relative(facing.toVanilla()));
        BETiles result = be instanceof BETiles ? (BETiles) be : null;
        neighboursBEs.put(facing, result);
        return result;
    }
    
    public void unsetBlocked() {
        be.render.unsetBlocked();
    }
    
    public Level getLevel() {
        return be.getLevel();
    }
    
    public RenderChunkExtender getRenderChunk() {
        return handler.getRenderChunk(getLevel(), pos);
    }
    
    public void prepareModelOffset(MutableBlockPos modelOffset, BlockPos pos) {
        handler.prepareModelOffset(getLevel(), modelOffset, pos);
    }
    
    public LittleRenderPipelineType getPipeline() {
        return handler.getPipeline();
    }
    
    public int sectionIndex() {
        return handler.sectionIndex(getLevel(), pos);
    }
}
