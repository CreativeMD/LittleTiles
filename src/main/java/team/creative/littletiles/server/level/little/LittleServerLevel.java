package team.creative.littletiles.server.level.little;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.level.little.LevelBlockChangeListener;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.mixin.server.level.MinecraftServerAccessor;
import team.creative.littletiles.server.player.LittleServerPlayerConnection;

public abstract class LittleServerLevel extends ServerLevel implements LittleLevel {
    
    private static LevelStem overworldStem(MinecraftServer server) {
        Registry<LevelStem> registry = server.registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
        return registry.get(LevelStem.OVERWORLD);
    }
    
    public Entity holder;
    public IVecOrigin origin;
    
    private final List<LevelBlockChangeListener> blockChangeListeners = new ArrayList<>();
    public boolean hasChanged = false;
    public boolean preventNeighborUpdate = false;
    
    protected LittleServerLevel(MinecraftServer server, ServerLevelData worldInfo, ResourceKey<Level> dimension, boolean debug, long seed, RegistryAccess access) {
        super(server, Util.backgroundExecutor(), ((MinecraftServerAccessor) server).getStorageSource(), worldInfo, dimension, overworldStem(
            server), LittleChunkProgressListener.INSTANCE, debug, seed, Collections.EMPTY_LIST, false, null);
    }
    
    @Override
    public void removeEntityById(int id, RemovalReason reason) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleEntityRenderManager getRenderManager() {
        return null;
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
    public void registerBlockChangeListener(LevelBlockChangeListener listener) {
        blockChangeListeners.add(listener);
    }
    
    @Override
    public void neighborChanged(BlockPos pos, Block block, BlockPos fromPos) {
        if (preventNeighborUpdate)
            return;
        if (this.isClientSide) {
            BlockState blockstate = this.getBlockState(pos);
            
            try {
                blockstate.handleNeighborChanged(this, pos, block, fromPos, false);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
                crashreportcategory.setDetail("Source block type", () -> {
                    try {
                        return String.format("ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
                    } catch (Throwable throwable1) {
                        return "ID #" + BuiltInRegistries.BLOCK.getKey(block);
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
    public void setBlocksDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        blockChangeListeners.forEach(x -> x.blockChanged(pos, setState));
    }
    
    @Override
    public LittleServerChunkCache getChunkSource() {
        return (LittleServerChunkCache) super.getChunkSource();
    }
    
    @Override
    public void unload(LevelChunk chunk) {
        super.unload(chunk);
        this.getChunkSource().getLightEngine().setLightEnabled(chunk.getPos(), false);
    }
    
    @Override
    public void unload() {
        try {
            close();
        } catch (IOException e) {}
    }
    
    @Override
    public MapId getFreeMapId() {
        return new MapId(0);
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
    
    public void load(ChunkPos pos, CompoundTag nbt) {
        getChunkSource().loadLevelChunk(pos, nbt);
    }
    
    @Override
    public Iterable<? extends ChunkAccess> chunks() {
        return getChunkSource().all();
    }
    
    @Override
    public void destroyBlockProgress(int id, BlockPos pos, int progress) {
        Level toCompare = this;
        if (this instanceof LittleSubLevel sub)
            toCompare = sub.getRealLevel();
        for (ServerPlayer serverplayer : this.getServer().getPlayerList().getPlayers()) {
            if (serverplayer != null && serverplayer.level() == toCompare && serverplayer.getId() != id) {
                double d0 = pos.getX() - serverplayer.getX();
                double d1 = pos.getY() - serverplayer.getY();
                double d2 = pos.getZ() - serverplayer.getZ();
                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D)
                    LittleServerPlayerConnection.send(this, serverplayer, new ClientboundBlockDestructionPacket(id, pos, progress));
            }
        }
        
    }
}
