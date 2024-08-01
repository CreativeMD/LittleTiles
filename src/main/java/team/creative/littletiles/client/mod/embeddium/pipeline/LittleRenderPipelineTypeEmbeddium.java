package team.creative.littletiles.client.mod.embeddium.pipeline;

import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public class LittleRenderPipelineTypeEmbeddium extends LittleRenderPipelineType<LittleRenderPipelineEmbeddium> {
    
    public LittleRenderPipelineTypeEmbeddium() {
        super(LittleRenderPipelineEmbeddium::new);
    }
    
}
