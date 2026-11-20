package team.creative.littletiles.client.render.cache.pipeline;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.render.tile.LittleRenderBox;

/** Experimental: keep planar lighting independent of same-material box splits. */
final class LittleQuadLighting {
    final float[] brightness = new float[4];
    final int[] lightmap = new int[4];
    private final int[] samples = new int[32];
    // Each extent stores minimum XYZ followed by maximum XYZ.
    private final Map<BlockState, float[]> bounds = new IdentityHashMap<>();

    void prepare(List<LittleRenderBox> boxes) {
        bounds.clear();
        for (LittleRenderBox box : boxes) {
            float[] extent = bounds.computeIfAbsent(box.state, state -> new float[]{box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ});
            for (Facing side : Facing.VALUES) {
                int index = side.axis.ordinal() + (side.positive ? 3 : 0);
                extent[index] = side.positive ? Math.max(extent[index], box.get(side)) : Math.min(extent[index], box.get(side));
            }
        }
    }

    boolean calculate(QuadLighter lighter, BakedQuad quad, LittleRenderBox box) {
        float[] extent = bounds.get(box.state);
        if (extent == null)
            return false;
        int axis = quad.getDirection().getAxis().ordinal();
        int u = (axis + 1) % 3;
        int v = (axis + 2) % 3;
        if (extent[u + 3] <= extent[u] || extent[v + 3] <= extent[v])
            return false;
        int[] vertices = quad.getVertices();
        if (vertices.length != 32)
            return false;
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
            lightmap[i] = Math.max(Math.round(block), bakedLight & 0xFFFF)
                    | Math.max(Math.round(sky), bakedLight >>> 16) << 16;
        }
        return true;
    }
}
