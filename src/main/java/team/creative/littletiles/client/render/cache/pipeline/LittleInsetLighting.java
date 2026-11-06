package team.creative.littletiles.client.render.cache.pipeline;

/** Scope and interpolation for LT's inset faces in the vanilla AO pipeline. */
public final class LittleInsetLighting {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);

    private LittleInsetLighting() {}

    public static boolean begin() {
        boolean previous = ACTIVE.get();
        ACTIVE.set(true);
        return previous;
    }

    public static void end(boolean previous) {
        if (previous)
            ACTIVE.set(true);
        else
            ACTIVE.remove();
    }

    public static boolean isActive() { return ACTIVE.get(); }

    public static void blend(float[] brightness, int[] lightmap, float[] inside, int[] insideLight, float outsideWeight) {
        float insideWeight = 1.0F - outsideWeight;
        for (int i = 0; i < 4; i++) {
            brightness[i] = inside[i] * insideWeight + brightness[i] * outsideWeight;
            // Packed block light and sky light must be interpolated independently.
            int block = Math.round((insideLight[i] & 0xFFFF) * insideWeight + (lightmap[i] & 0xFFFF) * outsideWeight);
            int sky = Math.round((insideLight[i] >>> 16 & 0xFFFF) * insideWeight + (lightmap[i] >>> 16 & 0xFFFF) * outsideWeight);
            lightmap[i] = block | sky << 16;
        }
    }
}
