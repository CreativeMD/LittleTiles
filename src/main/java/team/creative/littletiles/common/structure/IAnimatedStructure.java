package team.creative.littletiles.common.structure;

import team.creative.littletiles.common.entity.LittleLevelEntity;

public interface IAnimatedStructure {
    
    public void setAnimation(LittleLevelEntity entity);
    
    public LittleLevelEntity getAnimation();
    
    public boolean isInMotion();
    
    public boolean isAnimated();
    
    public void destroyAnimation();
    
}
