package team.creative.littletiles.common.structure;

import team.creative.littletiles.common.entity.level.LittleEntity;

public interface IAnimatedStructure {
    
    public void setAnimation(LittleEntity entity);
    
    public LittleEntity getAnimation();
    
    public boolean isInMotion();
    
    public boolean isAnimated();
    
    public void destroyAnimation();
    
}
