package team.creative.littletiles.common.mod.sable;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.origin.IOriginPose;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class SableOriginPose implements IOriginPose {
    
    private final Pose3dc pose;
    private final IOriginPose childPose;
    
    public SableOriginPose(Pose3dc pose, IOriginPose childPose) {
        this.pose = pose;
        this.childPose = childPose;
    }
    
    @Override
    public double translation(Axis axis) {
        return childPose.translation(axis) + pose.position().get(axis.ordinal());
    }
    
    @Override
    public void rotateWithoutCenter(Vec3d vec) {
        childPose.rotateWithoutCenter(vec);
        Vector3d temp = new Vector3d(vec.x, vec.y, vec.z);
        pose.orientation().transform(temp);
        vec.x = temp.x;
        vec.y = temp.y;
        vec.z = temp.z;
    }
    
    @Override
    public void transform(Vec3d vec) {
        childPose.transform(vec);
        Vector3d temp = new Vector3d(vec.x, vec.y, vec.z);
        pose.transformPosition(temp);
        vec.x = temp.x;
        vec.y = temp.y;
        vec.z = temp.z;
    }
    
    @Override
    public void transformInverse(Vec3d vec) {
        Vector3d temp = new Vector3d(vec.x, vec.y, vec.z);
        pose.transformPositionInverse(temp);
        vec.x = temp.x;
        vec.y = temp.y;
        vec.z = temp.z;
        childPose.transformInverse(vec);
    }
    
    @Override
    public Vec3 setup(Matrix4fStack matrixStack, Vec3 cam) {
        matrixStack.rotate(new Quaternionf(pose.orientation()));
        var fakeCam = pose.transformPositionInverse(cam);
        fakeCam = childPose.setup(matrixStack, fakeCam);
        return fakeCam;
    }
    
    @Override
    public Vec3 setup(PoseStack matrixStack, Vec3 cam) {
        matrixStack.mulPose(new Quaternionf(pose.orientation()));
        var fakeCam = pose.transformPositionInverse(cam);
        fakeCam = childPose.setup(matrixStack, fakeCam);
        return fakeCam;
    }
    
    @Override
    public Matrix4f transform(double camX, double camY, double camZ) {
        var result = SableContext.transform(pose, 0, 0, 0, new Vec3(camX, camY, camZ));
        result.mul(childPose.transform(camX, camY, camZ));
        return result;
    }
    
    @Override
    public Matrix4f transformInverse(double camX, double camY, double camZ) {
        var result = SableContext.transform(pose, 0, 0, 0, new Vec3(camX, camY, camZ)).invert();
        result.mul(childPose.transformInverse(camX, camY, camZ));
        return result;
    }
    
}
