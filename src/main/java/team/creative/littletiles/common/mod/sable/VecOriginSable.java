package team.creative.littletiles.common.mod.sable;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.matrix.Matrix3;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class VecOriginSable implements IVecOrigin {
    
    public final IVecOrigin child;
    public final SableContext context;
    
    public VecOriginSable(IVecOrigin child, SableContext context) {
        this.child = child;
        this.context = context;
    }
    
    @Override
    public void onlyRotateWithoutCenter(Vec3d vec) {
        child.onlyRotateWithoutCenter(vec);
        Vector3d temp = new Vector3d(vec.x, vec.y, vec.z);
        context.level.logicalPose().orientation().transform(temp);
        vec.x = temp.x;
        vec.y = temp.y;
        vec.z = temp.z;
    }
    
    @Override
    public void transformPointToWorld(Vec3d vec) {
        child.transformPointToWorld(vec);
        Vector3d temp = new Vector3d(vec.x, vec.y, vec.z);
        context.level.logicalPose().transformPosition(temp);
        vec.x = temp.x;
        vec.y = temp.y;
        vec.z = temp.z;
    }
    
    @Override
    public void transformPointToFakeWorld(Vec3d vec) {
        Vector3d temp = new Vector3d(vec.x, vec.y, vec.z);
        context.level.logicalPose().transformPositionInverse(temp);
        vec.x = temp.x;
        vec.y = temp.y;
        vec.z = temp.z;
        child.transformPointToFakeWorld(vec);
    }
    
    @Override
    public Vec3 setupRenderingInternal(Matrix4fStack matrixStack, Vec3 cam, float partialTicks) {
        var pose = context.level.logicalPose();
        matrixStack.rotate(new Quaternionf(pose.orientation()));
        var fakeCam = context.toFakeWorld(cam);
        fakeCam = child.setupRenderingInternal(matrixStack, fakeCam, partialTicks);
        return fakeCam;
    }
    
    @Override
    public Vec3 setupRenderingInternal(PoseStack matrixStack, Vec3 cam, float partialTicks) {
        var pose = context.level.logicalPose();
        matrixStack.mulPose(new Quaternionf(pose.orientation()));
        var fakeCam = context.toFakeWorld(cam);
        fakeCam = child.setupRenderingInternal(matrixStack, fakeCam, partialTicks);
        return fakeCam;
    }
    
    @Override
    public double translationCombined(Axis axis) {
        return child.translationCombined(axis) + context.level.logicalPose().position().get(axis.ordinal());
    }
    
    @Override
    public boolean hasChanged() {
        return child.hasChanged();
    }
    
    @Override
    public IVecOrigin getParent() {
        return null;
    }
    
    @Override
    public IVecOrigin copy() {
        return new VecOriginSable(child.copy(), context);
    }
    
    @Override
    public Matrix4f transform(double camX, double camY, double camZ, float partialTicks) {
        var result = context.transform(0, 0, 0, new Vec3(camX, camY, camZ), partialTicks);
        result.mul(child.transform(camX, camY, camZ, partialTicks));
        return result;
    }
    
    @Override
    public Matrix4f transformInverse(double camX, double camY, double camZ, float partialTicks) {
        var result = context.transformInverse(0, 0, 0, new Vec3(camX, camY, camZ), partialTicks);
        result.mul(child.transformInverse(camX, camY, camZ, partialTicks));
        return result;
    }
    
    @Override
    public double offX() {
        return child.offX();
    }
    
    @Override
    public double offY() {
        return child.offY();
    }
    
    @Override
    public double offZ() {
        return child.offZ();
    }
    
    @Override
    public double rotX() {
        return child.rotX();
    }
    
    @Override
    public double rotY() {
        return child.rotY();
    }
    
    @Override
    public double rotZ() {
        return child.rotZ();
    }
    
    @Override
    public double offXLast() {
        return child.offXLast();
    }
    
    @Override
    public double offYLast() {
        return child.offYLast();
    }
    
    @Override
    public double offZLast() {
        return child.offZLast();
    }
    
    @Override
    public double rotXLast() {
        return child.rotXLast();
    }
    
    @Override
    public double rotYLast() {
        return child.rotYLast();
    }
    
    @Override
    public double rotZLast() {
        return child.rotZLast();
    }
    
    @Override
    public boolean isRotated() {
        return child.isRotated();
    }
    
    @Override
    public void offX(double value) {
        child.offX(value);
    }
    
    @Override
    public void offY(double value) {
        child.offY(value);
    }
    
    @Override
    public void offZ(double value) {
        child.offZ(value);
    }
    
    @Override
    public void off(double x, double y, double z) {
        child.off(x, y, z);
    }
    
    @Override
    public void rotX(double value) {
        child.rotX(value);
    }
    
    @Override
    public void rotY(double value) {
        child.rotY(value);
    }
    
    @Override
    public void rotZ(double value) {
        child.rotZ(value);
    }
    
    @Override
    public void rot(double x, double y, double z) {
        child.rot(x, y, z);
    }
    
    @Override
    public Vec3d deltaMovement() {
        return child.deltaMovement();
    }
    
    @Override
    public void deltaMovement(Vec3d value) {
        child.deltaMovement(value);
    }
    
    @Override
    public Vec3d center() {
        return child.center();
    }
    
    @Override
    public void setCenter(Vec3d vec) {
        child.setCenter(vec);
    }
    
    @Override
    public Matrix3 rotation() {
        return child.rotation();
    }
    
    @Override
    public Matrix3 rotationInv() {
        return child.rotationInv();
    }
    
    @Override
    public Vec3d translation() {
        return child.translation();
    }
    
    @Override
    public void tick() {
        child.tick();
    }
}
