package team.creative.littletiles.common.entity;

import team.creative.creativecore.common.util.math.collision.CollisionCoordinator;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;

public interface OrientationAwareEntity {
    
    public IVecOrigin getOrigin();
    
    public void parentVecOriginChange(IVecOrigin origin);
    
    public void markOriginChange();
    
    public void performTick();
    
    public void transform(CollisionCoordinator coordinator);
    
}
