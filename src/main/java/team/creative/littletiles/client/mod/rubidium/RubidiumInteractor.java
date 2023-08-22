package team.creative.littletiles.client.mod.rubidium;

import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.rubidium.pipeline.LittleRenderPipelineTypeRubidium;

public class RubidiumInteractor {
    
    public static final LittleRenderPipelineTypeRubidium PIPELINE = new LittleRenderPipelineTypeRubidium();
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Rubidium extension");
    }
    
}
