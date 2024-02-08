package team.creative.littletiles.client.mod.rubidium;

import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.rubidium.entity.LittleAnimationRenderManagerRubidium;
import team.creative.littletiles.client.mod.rubidium.pipeline.LittleRenderPipelineTypeRubidium;
import team.creative.littletiles.client.render.entity.LittleEntityRenderManager;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

public class RubidiumInteractor {
    
    public static final LittleRenderPipelineTypeRubidium PIPELINE = new LittleRenderPipelineTypeRubidium();
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Rubidium extension");
    }
    
    public static LittleEntityRenderManager createRenderManager(LittleAnimationEntity entity) {
        return new LittleAnimationRenderManagerRubidium(entity);
    }
    
}
