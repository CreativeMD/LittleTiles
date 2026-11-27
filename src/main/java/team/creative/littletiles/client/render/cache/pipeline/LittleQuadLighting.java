package team.creative.littletiles.client.render.cache.pipeline;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import team.creative.littletiles.client.render.tile.LittleRenderBox;

/** Experimental: keep planar lighting independent of same-material box splits. */
final class LittleQuadLighting {
    
    final float[] brightness = new float[4];
    final int[] lightmap = new int[4];
    private final int[] samples = new int[32];
    
    boolean calculate(QuadLighter lighter, BakedQuad quad, LittleRenderBox box) {
        float[] extent = new float[] { 0, 0, 0, 1, 1, 1 };
        int axis = quad.getDirection().getAxis().ordinal();
        int u = (axis + 1) % 3;
        int v = (axis + 2) % 3;
        int[] vertices = quad.getVertices();
        float depth = Float.intBitsToFloat(vertices[axis]);
        int normal = ((quad.getDirection().getAxisDirection().getStep() * 127) & 255) << (axis * 8);
        for (int i = 0; i < 4; i++) {
            if (Float.intBitsToFloat(vertices[i * 8 + axis]) != depth)
                return false; // Slopes keep NeoForge's original vertex lighting.
            int packedNormal = vertices[i * 8 + 7] & 0xFFFFFF;
            if (packedNormal != 0 && packedNormal != normal)
                return false;
            for (int a = 0; a < 3; a++) {
                float coordinate = Float.intBitsToFloat(vertices[i * 8 + a]);
                if (!Float.isFinite(coordinate) || coordinate < extent[a] || coordinate > extent[a + 3] || extent[a] < 0 || extent[a + 3] > 1)
                    return false;
            }
            samples[i * 8 + axis] = Float.floatToRawIntBits(depth);
            samples[i * 8 + u] = Float.floatToRawIntBits((i & 1) == 0 ? extent[u] : extent[u + 3]);
            samples[i * 8 + v] = Float.floatToRawIntBits((i & 2) == 0 ? extent[v] : extent[v + 3]);
            samples[i * 8 + 7] = normal;
        }
        
        // Keep NeoForge's own AO, depth interpolation, lightmap and directional shade.
        lighter.computeLightingForQuad(samples, quad.isShade());
        float[] shade = lighter.getComputedBrightness();
        int[] light = lighter.getComputedLightmap();
        for (int i = 0; i < 4; i++) {
            float x = (Float.intBitsToFloat(vertices[i * 8 + u]) - extent[u]) / (extent[u + 3] - extent[u]);
            float y = (Float.intBitsToFloat(vertices[i * 8 + v]) - extent[v]) / (extent[v + 3] - extent[v]);
            float ao = 0, block = 0, sky = 0;
            for (int corner = 0; corner < 4; corner++) {
                float weight = ((corner & 1) == 0 ? 1 - x : x) * ((corner & 2) == 0 ? 1 - y : y);
                ao += shade[corner] * weight;
                block += (light[corner] & 0xFFFF) * weight;
                sky += (light[corner] >>> 16) * weight;
            }
            brightness[i] = ao;
            int bakedLight = vertices[i * 8 + 6];
            lightmap[i] = Math.max(Math.round(block), bakedLight & 0xFFFF) | Math.max(Math.round(sky), bakedLight >>> 16) << 16;
        }
        return true;
    }
    
}
