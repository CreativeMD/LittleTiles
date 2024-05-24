package team.creative.littletiles.client.mod.rubidium.pipeline;

import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public class LittleRenderPipelineTypeRubidium extends LittleRenderPipelineType<LittleRenderPipelineRubidium> {
    
    public LittleRenderPipelineTypeRubidium() {
        super(LittleRenderPipelineRubidium::new);
    }
    
}
