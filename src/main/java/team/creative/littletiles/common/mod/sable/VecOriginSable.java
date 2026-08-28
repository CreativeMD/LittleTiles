package team.creative.littletiles.common.mod.sable;

import org.joml.Vector3d;

import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import team.creative.creativecore.common.util.math.origin.IOriginPose;
import team.creative.creativecore.common.util.math.origin.IVecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class VecOriginSable implements IVecOrigin {
    
    public final IVecOrigin child;
    public final SableContext context;
    
    private IOriginPose pose;
    private IOriginPose renderPose;
    private float renderPoseTick = -1;
    
    private Vector3d lastPos;
    private boolean hasChanged;
    
    public VecOriginSable(IVecOrigin child, SableContext context) {
        this.child = child;
        this.context = context;
        updatePose();
    }
    
    @Override
    public IOriginPose pose() {
        return pose;
    }
    
    @Override
    public IOriginPose pose(float partialTick) {
        if (partialTick == renderPoseTick)
            return renderPose;
        if (partialTick == 1)
            return pose;
        
        renderPose = new SableOriginPose(((ClientSubLevelAccess) context.level).renderPose(partialTick), child.pose(partialTick));
        renderPoseTick = partialTick;
        
        return renderPose;
    }
    
    @Override
    public boolean hasChanged() {
        return hasChanged || child.hasChanged();
    }
    
    protected void updatePose() {
        pose = new SableOriginPose(context.level.logicalPose(), child.pose());
        renderPose = null;
        renderPoseTick = -1;
        hasChanged = true;
    }
    
    @Override
    public void tick() {
        child.tick();
        Vector3d pos = context.level.logicalPose().position();
        if (lastPos == null || !lastPos.equals(pos) || child.hasChanged()) {
            updatePose();
            lastPos = new Vector3d(pos);
        } else
            hasChanged = false;
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
    public void setLast(double offX, double offY, double offZ, double rotX, double rotY, double rotZ) {
        child.setLast(offX, offY, offZ, rotX, rotY, rotZ);
        renderPose = null;
        renderPoseTick = -1;
    }
    
    @Override
    public void set(double offX, double offY, double offZ, double rotX, double rotY, double rotZ) {
        child.set(offX, offY, offZ, rotX, rotY, rotZ);
        updatePose();
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
    
}
