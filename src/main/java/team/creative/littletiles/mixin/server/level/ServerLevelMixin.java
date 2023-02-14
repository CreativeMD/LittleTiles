package team.creative.littletiles.mixin.server.level;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.DataFixer;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.box.AABBVoxelShape;
import team.creative.littletiles.server.level.little.LittleServerChunkCache;
import team.creative.littletiles.server.level.little.LittleServerLevel;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
    
    protected ServerLevelMixin(WritableLevelData data, ResourceKey<Level> key, Holder<DimensionType> dim, Supplier<ProfilerFiller> prof, boolean p_220356_, boolean p_220357_, long p_220358_, int p_220359_) {
        super(data, key, dim, prof, p_220356_, p_220357_, p_220358_, p_220359_);
    }
    
    @Redirect(at = @At(value = "NEW", target = "net/minecraft/server/level/ServerChunkCache"), method = "<init>", require = 1)
    public ServerChunkCache newServerChunkCache(ServerLevel level, LevelStorageAccess storageAccess, DataFixer dataFixer, StructureTemplateManager structureTemplate, Executor exe, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progress, ChunkStatusUpdateListener status, Supplier<DimensionDataStorage> supplier) {
        if (level instanceof LittleServerLevel)
            return new LittleServerChunkCache((LittleServerLevel) level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, progress, status, supplier);
        return new ServerChunkCache(level, storageAccess, dataFixer, structureTemplate, exe, generator, viewDistance, simulationDistance, sync, progress, status, supplier);
    }
    
    @Unique
    private ServerLevel as() {
        return (ServerLevel) (Object) this;
    }
    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;ensureStructuresGenerated()V"), method = "<init>", require = 1)
    public void callEnsureStructuresGenerated(ChunkGeneratorStructureState state) {
        if (as() instanceof LittleServerLevel)
            return;
        state.ensureStructuresGenerated();
    }
    
    @Override
    public boolean noCollision(@Nullable Entity entity, AABB bb) {
        for (VoxelShape voxelshape : this.getBlockCollisions(entity, bb))
            if (!(voxelshape instanceof AABBVoxelShape) && !voxelshape.isEmpty())
                return false;
            
        if (!this.getEntityCollisions(entity, bb).isEmpty())
            return false;
        else if (entity == null)
            return true;
        VoxelShape voxelshape1 = this.borderCollision(entity, bb);
        return voxelshape1 == null || !Shapes.joinIsNotEmpty(voxelshape1, Shapes.create(bb), BooleanOp.AND);
    }
    
    public VoxelShape borderCollision(Entity entity, AABB bb) {
        WorldBorder worldborder = getWorldBorder();
        return worldborder.isInsideCloseToBorder(entity, bb) ? worldborder.getCollisionShape() : null;
    }
}
