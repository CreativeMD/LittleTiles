package team.creative.littletiles.common.mod.sable;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.common.level.context.ILittleLevelContext;

public class SableContext implements ILittleLevelContext {
    
    public final SubLevel level;
    
    public SableContext(SubLevel level) {
        this.level = level;
    }
    
    @Override
    public Matrix4f transform(double x, double y, double z, Vec3 camera, float partialTick) {
        Pose3dc pose = ((ClientSubLevel) level).renderPose(partialTick);
        Vec3 subCamera = pose.transformPositionInverse(camera);
        Matrix4f matrix = new Matrix4f();
        matrix.translate((float) (pose.position().x() - camera.x), (float) (pose.position().y() - camera.y), (float) (pose.position().z() - camera.z));
        matrix.rotate(new Quaternionf(pose.orientation()));
        matrix.translate((float) (subCamera.x - pose.rotationPoint().x()), (float) (subCamera.y - pose.rotationPoint().y()), (float) (subCamera.z - pose.rotationPoint().z()));
        matrix.scale((float) pose.scale().x(), (float) pose.scale().y(), (float) pose.scale().z());
        matrix.translate((float) (x - subCamera.x), (float) (y - subCamera.y), (float) (z - subCamera.z));
        return matrix;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    public void transformPose(PoseStack stack, double x, double y, double z, Vec3 camera, float partialTick) {
        Pose3dc pose = ((ClientSubLevel) level).renderPose(partialTick);
        Vec3 subCamera = pose.transformPositionInverse(camera);
        stack.translate((float) (pose.position().x() - camera.x), (float) (pose.position().y() - camera.y), (float) (pose.position().z() - camera.z));
        stack.mulPose(new Quaternionf(pose.orientation()));
        stack.translate((float) (subCamera.x - pose.rotationPoint().x()), (float) (subCamera.y - pose.rotationPoint().y()), (float) (subCamera.z - pose.rotationPoint().z()));
        stack.scale((float) pose.scale().x(), (float) pose.scale().y(), (float) pose.scale().z());
        stack.translate((float) (x - subCamera.x), (float) (y - subCamera.y), (float) (z - subCamera.z));
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    @OnlyIn(Dist.CLIENT)
    public void transformMatrix(Matrix4fStack matrix, double x, double y, double z, Vec3 camera, float partialTick) {
        Pose3dc pose = ((ClientSubLevel) level).renderPose(partialTick);
        Vec3 subCamera = pose.transformPositionInverse(camera);
        matrix.translate((float) (pose.position().x() - camera.x), (float) (pose.position().y() - camera.y), (float) (pose.position().z() - camera.z));
        matrix.rotate(new Quaternionf(pose.orientation()));
        matrix.translate((float) (subCamera.x - pose.rotationPoint().x()), (float) (subCamera.y - pose.rotationPoint().y()), (float) (subCamera.z - pose.rotationPoint().z()));
        matrix.scale((float) pose.scale().x(), (float) pose.scale().y(), (float) pose.scale().z());
        matrix.translate((float) (x - subCamera.x), (float) (y - subCamera.y), (float) (z - subCamera.z));
    }
    
    @Override
    public Matrix4f transformInverse(double x, double y, double z, Vec3 camera, float partialTick) {
        return transform(x, y, z, camera, partialTick).invert();
    }
    
    @Override
    public Vec3 toFakeWorld(Vec3 vec) {
        return level.logicalPose().transformPositionInverse(vec);
    }
    
    @Override
    public Vec3 toRealWorld(Vec3 vec) {
        return level.logicalPose().transformPosition(vec);
    }
    
    @Override
    public int hashCode() {
        return level.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SableContext c)
            return c.level == level;
        return false;
    }
}
