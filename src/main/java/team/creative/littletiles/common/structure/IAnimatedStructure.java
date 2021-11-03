package team.creative.littletiles.common.structure;

import team.creative.littletiles.common.animation.entity.EntityAnimation;

public interface IAnimatedStructure {
    
    public void setAnimation(EntityAnimation animation);
    
    public EntityAnimation getAnimation();
    
    public boolean isInMotion();
    
    public boolean isAnimated();
    
    public void destroyAnimation();
    
}