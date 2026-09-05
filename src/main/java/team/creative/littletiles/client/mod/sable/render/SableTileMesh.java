package team.creative.littletiles.client.mod.sable.render;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.mod.sable.SableManager;
import team.creative.littletiles.mixin.client.render.BufferBuilderFormatAccessor;

/**
 * Builds LittleTiles geometry in dedicated Sable section VBOs. Iris extends terrain vertices, so
 * LittleTiles' cached vertices cannot safely share Sable's vanilla mesh even for the same render
 * layer.
 */
public final class SableTileMesh {
    
    private static final ThreadLocal<CompileContext> COMPILING = new ThreadLocal<>();
    private static final Map<Long, StagedMesh> STAGED = new java.util.HashMap<>();
    private static final Map<SectionRenderDispatcher.RenderSection, Long> GENERATIONS = new IdentityHashMap<>();
    
    private SableTileMesh() {}
    
    public static void begin(SectionPos section, VertexSorting sorting) {
        var level = Minecraft.getInstance().level;
        if (!SableManager.INSTALLED || level == null || !SableManager.isSubLevel(level, section.origin())) {
            COMPILING.remove();
            return;
        }
        COMPILING.set(new CompileContext(section, sorting));
    }
    
    public static void enterTileUpload() {
        CompileContext context = COMPILING.get();
        if (context != null)
            context.uploadingTiles = true;
    }
    
    public static void leaveTileUpload() {
        CompileContext context = COMPILING.get();
        if (context != null)
            context.uploadingTiles = false;
    }
    
    public static BufferBuilder builder(RenderType layer) {
        CompileContext context = COMPILING.get();
        return context != null && context.uploadingTiles ? context.layer(layer).builder : null;
    }
    
    public static BufferCollection collection(RenderType layer) {
        CompileContext context = COMPILING.get();
        return context != null && context.uploadingTiles ? context.layer(layer).collection : null;
    }
    
    /**
     * Cached vertices may have been built with a different terrain stride before a shader reload.
     * Rejecting that cache makes LittleTiles rebuild it instead of corrupting the dedicated VBO.
     */
    public static boolean rejectsCachedVertices(ChunkBufferUploader uploader, int bytes, int vertices) {
        CompileContext context = COMPILING.get();
        if (context == null || !context.uploadingTiles || !(uploader instanceof BufferBuilder builder) || !context.owns(builder) || vertices <= 0)
            return false;
        
        int stride = ((BufferBuilderFormatAccessor) builder).getFormat().getVertexSize();
        return stride <= 0 || bytes != vertices * stride;
    }
    
    /**
     * Iris recognizes Sodium's bulk writer and preserves its extended terrain attributes there.
     */
    public static boolean uploadCachedVertices(ChunkBufferUploader uploader, ByteBuffer source, int bytes, int vertices) {
        CompileContext context = COMPILING.get();
        if (context == null || !context.uploadingTiles || !(uploader instanceof BufferBuilder builder) || !context.owns(builder)
                || !(builder instanceof VertexBufferWriter writer))
            return false;
        
        VertexFormat format = ((BufferBuilderFormatAccessor) builder).getFormat();
        if (vertices <= 0 || bytes != vertices * format.getVertexSize())
            return false;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            writer.push(stack, MemoryUtil.memAddress(source), vertices, format);
        }
        return true;
    }
    
    public static MeshPayload finish() {
        CompileContext context = COMPILING.get();
        COMPILING.remove();
        return context == null ? null : context.finish();
    }
    
    public static synchronized void stage(SectionPos section, MeshPayload payload, Set<RenderType> vanillaLayers) {
        StagedMesh old = STAGED.put(section.asLong(), new StagedMesh(payload, Set.copyOf(vanillaLayers)));
        if (old != null)
            old.payload.close();
    }
    
    public static synchronized void discard(SectionRenderDispatcher.RenderSection section) {
        StagedMesh old = STAGED.remove(SectionPos.asLong(section.getOrigin()));
        if (old != null)
            old.payload.close();
        GENERATIONS.merge(section, 1L, Long::sum);
    }
    
    public static void publish(SectionRenderDispatcher.RenderSection section) {
        StagedMesh staged;
        long generation;
        synchronized (SableTileMesh.class) {
            staged = STAGED.remove(SectionPos.asLong(section.getOrigin()));
            generation = GENERATIONS.merge(section, 1L, Long::sum);
        }
        
        if (staged == null) {
            Minecraft.getInstance().submit(() -> clear(section, generation));
            return;
        }

        for (RenderType layer : staged.payload.layers())
            ((RenderChunkExtender) section).setHasBlock(layer);
        
        Minecraft.getInstance().submit(() -> upload(section, staged, generation));
    }
    
    private static void upload(SectionRenderDispatcher.RenderSection section, StagedMesh staged, long generation) {
        if (!isCurrent(section, generation)) {
            staged.payload.close();
            return;
        }
        
        UploadedMesh uploaded = null;
        try {
            uploaded = staged.payload.upload().withVanillaLayers(staged.vanillaLayers);
            if (!isCurrent(section, generation)) {
                uploaded.close();
                return;
            }
            ((SableTileSection) section).publishTiles(uploaded);
        } catch (Throwable throwable) {
            if (uploaded != null)
                uploaded.close();
            else
                staged.payload.close();
            clear(section, generation);
        }
    }
    
    private static void clear(SectionRenderDispatcher.RenderSection section, long generation) {
        if (isCurrent(section, generation))
            ((SableTileSection) section).publishTiles(null);
    }
    
    private static synchronized boolean isCurrent(SectionRenderDispatcher.RenderSection section, long generation) {
        return GENERATIONS.getOrDefault(section, 0L) == generation;
    }
    
    public static void closeOnRenderThread(UploadedMesh mesh) {
        if (mesh == null)
            return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread())
            mesh.close();
        else
            minecraft.submit(mesh::close);
    }
    
    private static final class CompileContext {
        
        private final SectionPos section;
        private final VertexSorting sorting;
        private final Map<RenderType, LayerBuilder> layers = new IdentityHashMap<>();
        private boolean uploadingTiles;
        
        private CompileContext(SectionPos section, VertexSorting sorting) {
            this.section = section;
            this.sorting = sorting;
        }
        
        private LayerBuilder layer(RenderType layer) {
            return layers.computeIfAbsent(layer, LayerBuilder::new);
        }
        
        private boolean owns(BufferBuilder builder) {
            for (LayerBuilder layer : layers.values())
                if (layer.builder == builder)
                    return true;
            return false;
        }
        
        private MeshPayload finish() {
            Map<RenderType, LayerPayload> meshes = new IdentityHashMap<>();
            try {
                for (var entry : layers.entrySet()) {
                    LayerBuilder layer = entry.getValue();
                    MeshData mesh = layer.builder.build();
                    layer.built = true;
                    if (mesh == null) {
                        layer.backing.close();
                        continue;
                    }
                    if (entry.getKey() == RenderType.translucent())
                        mesh.sortQuads(layer.backing, sorting);
                    meshes.put(entry.getKey(), new LayerPayload(layer.backing, mesh));
                }
                return new MeshPayload(meshes);
            } catch (Throwable throwable) {
                for (LayerPayload mesh : meshes.values())
                    mesh.close();
                for (LayerBuilder layer : layers.values())
                    layer.closeIfUnbuilt();
                throw throwable;
            }
        }
    }
    
    private static final class LayerBuilder {
        
        private final ByteBufferBuilder backing;
        private final BufferBuilder builder;
        private final BufferCollection collection = new BufferCollection();
        private boolean built;
        
        private LayerBuilder(RenderType layer) {
            backing = new ByteBufferBuilder(layer.bufferSize());
            builder = new BufferBuilder(backing, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }
        
        private void closeIfUnbuilt() {
            if (!built)
                backing.close();
        }
    }
    
    private record StagedMesh(MeshPayload payload, Set<RenderType> vanillaLayers) {}
    
    public static final class MeshPayload implements AutoCloseable {
        
        private Map<RenderType, LayerPayload> layers;
        
        private MeshPayload(Map<RenderType, LayerPayload> layers) {
            this.layers = layers;
        }
        
        private UploadedMesh upload() {
            Map<RenderType, VertexBuffer> uploaded = new IdentityHashMap<>();
            Map<RenderType, LayerPayload> current = layers;
            layers = null;
            try {
                for (var entry : current.entrySet()) {
                    VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    try {
                        buffer.bind();
                        entry.getValue().upload(buffer);
                        uploaded.put(entry.getKey(), buffer);
                    } catch (Throwable throwable) {
                        buffer.close();
                        throw throwable;
                    } finally {
                        VertexBuffer.unbind();
                    }
                }
                return new UploadedMesh(uploaded, Set.of());
            } catch (Throwable throwable) {
                for (VertexBuffer buffer : uploaded.values())
                    buffer.close();
                for (LayerPayload payload : current.values())
                    payload.close();
                throw throwable;
            }
        }

        private Set<RenderType> layers() {
            return layers == null ? Set.of() : Set.copyOf(layers.keySet());
        }
        
        @Override
        public void close() {
            Map<RenderType, LayerPayload> current = layers;
            layers = null;
            if (current != null)
                for (LayerPayload payload : current.values())
                    payload.close();
        }
    }
    
    private static final class LayerPayload implements AutoCloseable {
        
        private ByteBufferBuilder backing;
        private MeshData mesh;
        
        private LayerPayload(ByteBufferBuilder backing, MeshData mesh) {
            this.backing = backing;
            this.mesh = mesh;
        }
        
        private void upload(VertexBuffer target) {
            MeshData current = mesh;
            mesh = null;
            try {
                target.upload(current);
            } finally {
                close();
            }
        }
        
        @Override
        public void close() {
            if (mesh != null) {
                mesh.close();
                mesh = null;
            }
            if (backing != null) {
                backing.close();
                backing = null;
            }
        }
    }
    
    public static final class UploadedMesh implements AutoCloseable {
        
        private final Map<RenderType, VertexBuffer> layers;
        private Set<RenderType> vanillaLayers;
        
        private UploadedMesh(Map<RenderType, VertexBuffer> layers, Set<RenderType> vanillaLayers) {
            this.layers = layers;
            this.vanillaLayers = vanillaLayers;
        }
        
        private UploadedMesh withVanillaLayers(Set<RenderType> layers) {
            vanillaLayers = Set.copyOf(layers);
            return this;
        }
        
        public VertexBuffer get(RenderType layer) {
            return layers.get(layer);
        }
        
        public boolean hasVanillaLayer(RenderType layer) {
            return vanillaLayers.contains(layer);
        }
        
        @Override
        public void close() {
            for (VertexBuffer buffer : layers.values())
                buffer.close();
            layers.clear();
        }
    }
}
