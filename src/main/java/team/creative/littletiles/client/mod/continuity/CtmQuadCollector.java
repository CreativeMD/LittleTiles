package team.creative.littletiles.client.mod.continuity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Minimal FRAPI render context used to ask Continuity for its transformed quads
 * before CreativeCore clips those quads to a LittleTiles box.
 */
public final class CtmQuadCollector {
    private static final AtomicBoolean FIRST_BRIDGE_ATTEMPT = new AtomicBoolean();
    private static final AtomicBoolean FIRST_BRIDGE_RESULT = new AtomicBoolean();
    private static final AtomicBoolean FIRST_EXPANDED_RESULT = new AtomicBoolean();

    private CtmQuadCollector() {}

    public static List<BakedQuad> collect(BakedModel model, BlockAndTintGetter level, BlockPos pos, BlockState state, Direction requestedFace,
            RandomSource random, ModelData modelData, RenderType renderType) {
        if (!(model instanceof FabricBakedModel fabricModel) || fabricModel.isVanillaAdapter())
            return null;

        // Do not change the rendering semantics of unrelated FRAPI models.
        String modelClass = model.getClass().getName();
        if (!modelClass.startsWith("me.pepperbell.continuity.") && !modelClass.toLowerCase().contains("continuity"))
            return null;

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer == null)
            return null;

        if (FIRST_BRIDGE_ATTEMPT.compareAndSet(false, true))
            CtmLog.LOGGER.info("LittleTiles requested a Continuity model; entering the experimental FRAPI CTM bridge");

        MeshBuilder builder = renderer.meshBuilder();
        RenderMaterial defaultMaterial = renderer.materialFinder().clear().find();
        CollectingContext context = new CollectingContext(builder, state, requestedFace, random, modelData, renderType, defaultMaterial);
        Supplier<RandomSource> randomSupplier = () -> {
            random.setSeed(state.getSeed(pos));
            return random;
        };

        fabricModel.emitBlockQuads(level, state, pos, randomSupplier, context);

        Mesh mesh = builder.build();
        SpriteFinder spriteFinder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
        List<BakedQuad> result = new ArrayList<>();
        mesh.forEach(quad -> result.add(quad.toBakedQuad(spriteFinder.find(quad))));
        if (!result.isEmpty() && FIRST_BRIDGE_RESULT.compareAndSet(false, true))
            CtmLog.LOGGER.info("Experimental CTM bridge collected {} quads; this confirms collection, not visual CTM correctness", result.size());
        if (context.inputQuads > 0 && result.size() > context.inputQuads && FIRST_EXPANDED_RESULT.compareAndSet(false, true))
            CtmLog.LOGGER.info("Experimental CTM bridge collected expanded output: {} source quads -> {} output quads, state={}, face={}",
                    context.inputQuads, result.size(), state, requestedFace);
        return result;
    }

    /**
     * Handles the final vanilla-model emission while one of our collection contexts is active.
     * Sodium replaces this default method with an implementation that requires its own private
     * render-context subtype, so the compatibility mixin calls this before Sodium performs that cast.
     */
    public static boolean emitVanillaModel(BakedModel model, BlockState state, RenderContext context) {
        if (!(context instanceof CollectingContext collectingContext))
            return false;
        collectingContext.emitVanilla(model, state);
        return true;
    }

    /** Sodium Mesh.outputTo casts its argument to an internal emitter and bypasses our transforms. */
    public static void outputMesh(Mesh mesh, QuadEmitter emitter) {
        if (emitter instanceof TransformingEmitter)
            mesh.forEach(quad -> emitter.copyFrom(quad).emit());
        else
            mesh.outputTo(emitter);
    }

    private static final class CollectingContext implements RenderContext {
        private final QuadEmitter emitter;
        private final Deque<QuadTransform> transforms = new ArrayDeque<>();
        private final BlockState state;
        private final Direction requestedFace;
        private final RandomSource random;
        private final ModelData modelData;
        private final RenderType renderType;
        private final RenderMaterial defaultMaterial;
        private int inputQuads;

        private CollectingContext(MeshBuilder builder, BlockState state, Direction requestedFace, RandomSource random, ModelData modelData,
                RenderType renderType, RenderMaterial defaultMaterial) {
            this.state = state;
            this.requestedFace = requestedFace;
            this.random = random;
            this.modelData = modelData;
            this.renderType = renderType;
            this.defaultMaterial = defaultMaterial;
            this.emitter = new TransformingEmitter(builder, transforms);
        }

        @Override
        public QuadEmitter getEmitter() {
            return emitter;
        }

        @Override
        public boolean hasTransform() {
            return !transforms.isEmpty();
        }

        @Override
        public void pushTransform(QuadTransform transform) {
            transforms.addLast(transform);
        }

        @Override
        public void popTransform() {
            transforms.removeLast();
        }

        @Override
        public boolean isFaceCulled(Direction face) {
            return requestedFace != null && face != requestedFace;
        }

        @Override
        public ItemDisplayContext itemTransformationMode() {
            return ItemDisplayContext.NONE;
        }

        @Override
        public Consumer<Mesh> meshConsumer() {
            return mesh -> outputMesh(mesh, emitter);
        }

        @Override
        public BakedModelConsumer bakedModelConsumer() {
            return new BakedModelConsumer() {
                @Override
                public void accept(BakedModel bakedModel) {
                    accept(bakedModel, state);
                }

                @Override
                public void accept(BakedModel bakedModel, BlockState emittedState) {
                    emitVanilla(bakedModel, emittedState);
                }
            };
        }

        private void emitVanilla(BakedModel bakedModel, BlockState emittedState) {
            for (BakedQuad quad : bakedModel.getQuads(emittedState, requestedFace, random, modelData, renderType)) {
                inputQuads++;
                emitter.fromVanilla(quad, defaultMaterial, requestedFace).emit();
            }
        }

        @Override
        public ModelData getModelData() {
            return modelData;
        }

        @Override
        public RenderType getRenderType() {
            return renderType;
        }
    }

    /** Delegates storage to the active FRAPI renderer and applies pushed transforms on emit. */
    static final class TransformingEmitter implements QuadEmitter {
        private final MeshBuilder builder;
        private QuadEmitter delegate;
        private final Deque<RenderContext.QuadTransform> transforms;

        TransformingEmitter(MeshBuilder builder, Deque<RenderContext.QuadTransform> transforms) {
            this.builder = builder;
            this.delegate = builder.getEmitter();
            this.transforms = transforms;
        }

        @Override
        public QuadEmitter emit() {
            boolean emitted = false;
            try {
                Iterator<RenderContext.QuadTransform> iterator = transforms.descendingIterator();
                while (iterator.hasNext())
                    // CTM splitting copies this quad into a renderer-owned emitter. Supply the
                    // native object: Sodium's copyFrom cannot accept a forwarding wrapper.
                    if (!iterator.next().transform(delegate))
                        return this;
                delegate.emit();
                emitted = true;
                return this;
            } finally {
                // emit() normally resets all quad fields. A rejected/split original never
                // reaches emit(), so obtain a cleared emitter without adding that original.
                if (!emitted)
                    delegate = builder.getEmitter();
            }
        }

        @Override public QuadEmitter pos(int vertexIndex, float x, float y, float z) { delegate.pos(vertexIndex, x, y, z); return this; }
        @Override public QuadEmitter color(int vertexIndex, int color) { delegate.color(vertexIndex, color); return this; }
        @Override public QuadEmitter uv(int vertexIndex, float u, float v) { delegate.uv(vertexIndex, u, v); return this; }
        @Override public QuadEmitter spriteBake(TextureAtlasSprite sprite, int bakeFlags) { delegate.spriteBake(sprite, bakeFlags); return this; }
        @Override public QuadEmitter lightmap(int vertexIndex, int lightmap) { delegate.lightmap(vertexIndex, lightmap); return this; }
        @Override public QuadEmitter normal(int vertexIndex, float x, float y, float z) { delegate.normal(vertexIndex, x, y, z); return this; }
        @Override public QuadEmitter cullFace(Direction face) { delegate.cullFace(face); return this; }
        @Override public QuadEmitter nominalFace(Direction face) { delegate.nominalFace(face); return this; }
        @Override public QuadEmitter material(RenderMaterial material) { delegate.material(material); return this; }
        @Override public QuadEmitter colorIndex(int colorIndex) { delegate.colorIndex(colorIndex); return this; }
        @Override public QuadEmitter tag(int tag) { delegate.tag(tag); return this; }
        @Override public QuadEmitter copyFrom(QuadView quad) { delegate.copyFrom(quad instanceof TransformingEmitter wrapped ? wrapped.delegate : quad); return this; }
        @Override public QuadEmitter fromVanilla(int[] quadData, int startIndex) { delegate.fromVanilla(quadData, startIndex); return this; }
        @Override public QuadEmitter fromVanilla(BakedQuad quad, RenderMaterial material, Direction cullFace) { delegate.fromVanilla(quad, material, cullFace); return this; }

        @Override public float x(int vertexIndex) { return delegate.x(vertexIndex); }
        @Override public float y(int vertexIndex) { return delegate.y(vertexIndex); }
        @Override public float z(int vertexIndex) { return delegate.z(vertexIndex); }
        @Override public float posByIndex(int vertexIndex, int coordinateIndex) { return delegate.posByIndex(vertexIndex, coordinateIndex); }
        @Override public org.joml.Vector3f copyPos(int vertexIndex, org.joml.Vector3f target) { return delegate.copyPos(vertexIndex, target); }
        @Override public int color(int vertexIndex) { return delegate.color(vertexIndex); }
        @Override public float u(int vertexIndex) { return delegate.u(vertexIndex); }
        @Override public float v(int vertexIndex) { return delegate.v(vertexIndex); }
        @Override public org.joml.Vector2f copyUv(int vertexIndex, org.joml.Vector2f target) { return delegate.copyUv(vertexIndex, target); }
        @Override public int lightmap(int vertexIndex) { return delegate.lightmap(vertexIndex); }
        @Override public boolean hasNormal(int vertexIndex) { return delegate.hasNormal(vertexIndex); }
        @Override public float normalX(int vertexIndex) { return delegate.normalX(vertexIndex); }
        @Override public float normalY(int vertexIndex) { return delegate.normalY(vertexIndex); }
        @Override public float normalZ(int vertexIndex) { return delegate.normalZ(vertexIndex); }
        @Override public org.joml.Vector3f copyNormal(int vertexIndex, org.joml.Vector3f target) { return delegate.copyNormal(vertexIndex, target); }
        @Override public Direction cullFace() { return delegate.cullFace(); }
        @Override public Direction lightFace() { return delegate.lightFace(); }
        @Override public Direction nominalFace() { return delegate.nominalFace(); }
        @Override public org.joml.Vector3f faceNormal() { return delegate.faceNormal(); }
        @Override public RenderMaterial material() { return delegate.material(); }
        @Override public int colorIndex() { return delegate.colorIndex(); }
        @Override public int tag() { return delegate.tag(); }
        @Override public void toVanilla(int[] target, int startIndex) { delegate.toVanilla(target, startIndex); }
    }
}
