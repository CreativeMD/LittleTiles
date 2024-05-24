package team.creative.littletiles.client.mod.oculus;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;

public class OculusSodiumInteractor {
    
    public static Object createVertexFormatEmbeddium(Object format) {
        GlVertexFormat vertexFormat = (GlVertexFormat) format;
        return new GlVertexAttributeBinding[] { new GlVertexAttributeBinding(1, vertexFormat.getAttribute(
            ChunkMeshAttribute.POSITION_MATERIAL_MESH)), new GlVertexAttributeBinding(2, vertexFormat.getAttribute(
                ChunkMeshAttribute.COLOR_SHADE)), new GlVertexAttributeBinding(3, vertexFormat.getAttribute(
                    ChunkMeshAttribute.BLOCK_TEXTURE)), new GlVertexAttributeBinding(4, vertexFormat.getAttribute(
                        ChunkMeshAttribute.LIGHT_TEXTURE)), new GlVertexAttributeBinding(14, vertexFormat.getAttribute(
                            IrisChunkMeshAttributes.MID_BLOCK)), new GlVertexAttributeBinding(11, vertexFormat.getAttribute(
                                IrisChunkMeshAttributes.BLOCK_ID)), new GlVertexAttributeBinding(12, vertexFormat.getAttribute(
                                    IrisChunkMeshAttributes.MID_TEX_COORD)), new GlVertexAttributeBinding(13, vertexFormat.getAttribute(
                                        IrisChunkMeshAttributes.TANGENT)), new GlVertexAttributeBinding(10, vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL)) };
    }
    
}
