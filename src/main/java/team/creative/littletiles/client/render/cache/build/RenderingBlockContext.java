package team.creative.littletiles.client.render.cache.build;

import java.util.HashMap;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.cache.build.RenderingThread.RemovedBlockEntityException;
import team.creative.littletiles.client.render.cache.build.RenderingThread.RenderingException;
import team.creative.littletiles.common.block.entity.BETiles;

public class RenderingBlockContext {
    
    public final BETiles be;
    public final BlockState state;
    public final Object chunk;
    public final boolean subWorld;
    public int index;
    
    public HashMap<Facing, BETiles> neighboursBEs;
    
    public RenderingBlockContext(BETiles be, Object chunk) {
        this.be = be;
        this.state = be.getBlockTileState();
        this.chunk = chunk;
        this.subWorld = !(chunk instanceof RenderChunk);
    }
    
    public void checkRemoved() throws RemovedBlockEntityException {
        if (be.isRemoved())
            throw new RemovedBlockEntityException(be.getBlockPos() + "");
    }
    
    public void checkLoaded() throws RenderingException {
        if (be.getLevel() == null || !be.hasLoaded())
            throw new RenderingException("Tileentity is not loaded yet");
    }
    
    public void beforeBuilding() {
        be.render.beforeBuilding(this);
    }
    
    public void clearQuadBuilding() {
        neighboursBEs = null;
    }
    
    public BETiles getNeighbour(Facing facing) {
        if (neighboursBEs.containsKey(facing))
            return neighboursBEs.get(facing);
        BlockEntity be = this.be.getLevel().getBlockEntity(this.be.getBlockPos().relative(facing.toVanilla()));
        BETiles result = be instanceof BETiles ? (BETiles) be : null;
        neighboursBEs.put(facing, result);
        return result;
    }
}
