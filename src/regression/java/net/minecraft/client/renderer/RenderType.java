package net.minecraft.client.renderer;

import java.util.List;

/** Test-only layer identities avoid game/NeoForge registry startup. Never shipped in either JAR. */
public final class RenderType {
    private static final RenderType SOLID = new RenderType();
    private static final RenderType CUTOUT_MIPPED = new RenderType();
    private static final RenderType CUTOUT = new RenderType();
    private static final RenderType TRANSLUCENT = new RenderType();
    public static final com.google.common.collect.ImmutableList<RenderType> CHUNK_BUFFER_LAYERS = com.google.common.collect.ImmutableList.of(SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);
    private RenderType() {}
    public static RenderType solid() { return SOLID; }
    public static RenderType cutoutMipped() { return CUTOUT_MIPPED; }
    public static RenderType cutout() { return CUTOUT; }
    public static RenderType translucent() { return TRANSLUCENT; }
    public static List<RenderType> chunkBufferLayers() { return List.of(SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT); }
}
