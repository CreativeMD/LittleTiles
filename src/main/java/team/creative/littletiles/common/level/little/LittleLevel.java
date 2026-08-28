package team.creative.littletiles.common.level.little;

import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

public interface LittleLevel extends IOrientatedLevel, ILittleLevelContext {
    
    public default Level asLevel() {
        return (Level) this;
    }
    
    public void registerBlockChangeListener(LevelBlockChangeListener listener);
    
    @Override
    public Entity getHolder();
    
    @Override
    public void setHolder(Entity entity);
    
    public UUID key();
    
    public void unload(LevelChunk chunk);
    
    @Override
    public void unload();
    
    public Iterable<Entity> entities();
    
    public Iterable<? extends ChunkAccess> chunks();
    
    public void tick();
    
    public default boolean allowPlacement() {
        return true;
    }
    
    public void removeEntityById(int id, RemovalReason reason);
    
    @OnlyIn(Dist.CLIENT)
    public LittleEntityRenderManager getRenderManager();
    
    @Override
    default Vec3 toFakeWorld(Vec3 vec) {
        return getOrigin().pose().transformInverse(vec);
    }
    
    @Override
    default Vec3 toFakeWorld(Vec3 vec, float partialTick) {
        return getOrigin().pose(partialTick).transformInverse(vec);
    }
    
    @Override
    default Vec3 toRealWorld(Vec3 vec) {
        return getOrigin().pose().transform(vec);
    }
    
    @Override
    default Vec3 toRealWorld(Vec3 vec, float partialTick) {
        return getOrigin().pose(partialTick).transform(vec);
    }
    
    @Override
    default Matrix4f transform(double x, double y, double z, Vec3 camera, float partialTick) {
        Matrix4f matrix = getOrigin().pose(partialTick).transform(x, y, z);
        matrix.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
        return matrix;
    }
    
    @Override
    default void transformPose(PoseStack pose, double x, double y, double z, Vec3 camera, float partialTick) {
        getOrigin().pose(partialTick).setup(pose, camera);
        pose.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
    }
    
    @Override
    default void transformMatrix(Matrix4fStack matrix, double x, double y, double z, Vec3 camera, float partialTick) {
        getOrigin().pose(partialTick).setup(matrix, camera);
        matrix.translate((float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z));
    }
    
    @Override
    default Matrix4f transformInverse(double x, double y, double z, Vec3 camera, float partialTick) {
        Matrix4f matrix = getOrigin().pose(partialTick).transformInverse(x, y, z);
        matrix.translate((float) -(x - camera.x), (float) -(y - camera.y), (float) -(z - camera.z));
        return matrix;
    }
}
