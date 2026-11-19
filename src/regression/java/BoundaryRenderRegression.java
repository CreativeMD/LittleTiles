import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.face.RenderBoxFace;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.face.LittleFaceState;

/** Real cache entry point plus extracted geometry-selection method; not an in-game test. */
public final class BoundaryRenderRegression implements Opcodes {
    private static int checks;
    public static void main(String[] args) throws Exception {
        List<AssertionError> failures = new ArrayList<>();
        try { notificationDuringInvalidation(); } catch (AssertionError failure) { failures.add(failure); }
        try { freshBoundaryFaces(Path.of(args[0])); } catch (AssertionError failure) { failures.add(failure); }
        try { genericDeferredRefresh(Path.of(args[0])); } catch (AssertionError failure) { failures.add(failure); }
        if (!failures.isEmpty()) {
            for (AssertionError failure : failures) System.err.println("FAIL boundary regression: " + failure.getMessage());
            throw new AssertionError(failures.size() + " boundary regression groups failed");
        }
        System.out.println("PASS boundary render regression: " + checks + " assertions; update interleaving and stale boundary-face fixtures");
    }

    static void notificationDuringInvalidation() throws Exception {
        class Manager extends BERenderManager {
            int requests;
            boolean notificationHeldLock;
            Manager() { super(null); }
            @Override public void queue(boolean erase, boolean hasPos, long pos) {
                requests++;
                notificationHeldLock = Thread.holdsLock(this);
            }
        }
        class Box extends LittleRenderBox {
            Runnable duringInvalidation;
            Box() { super(new RenderBox(0, 0, 0, 1, 1, 1, (BlockState) null)); }
            @Override public void deleteQuadCache() {
                super.deleteQuadCache();
                Runnable update = duringInvalidation;
                duringInvalidation = null;
                if (update != null) update.run();
            }
        }
        Manager manager = new Manager();
        Box box = new Box();
        var layers = new ChunkLayerMapList<LittleRenderBox>();
        layers.add(RenderType.solid(), box);
        var boxes = new Int2ObjectArrayMap<ChunkLayerMapList<LittleRenderBox>>();
        boxes.put(-1, layers);
        var cache = BERenderManager.class.getDeclaredField("boxCache"); cache.setAccessible(true); cache.set(manager, boxes);
        var dirty = BERenderManager.class.getDeclaredField("neighbourChanged"); dirty.setAccessible(true);
        for (Facing side : Facing.VALUES) box.setQuad(side, List.of(new BakedQuad(new int[32], -1, side.toVanilla(), null, true)));
        box.doesNeedQuadUpdate = false;
        manager.onNeighbourChanged();
        box.duringInvalidation = manager::onNeighbourChanged;
        // Deterministically insert an edit between dirty consumption and invalidation completion.
        manager.beforeBuilding(null);
        check(dirty.getBoolean(manager), "a neighbor update during invalidation must survive for the next build");
        check(manager.requests == 2, "both neighbor edits request a rebuild");
        check(manager.notificationHeldLock, "dirty publication and queue request share the render-manager monitor");
        check(box.doesNeedQuadUpdate, "current build invalidates the old quads");
        manager.beforeBuilding(null);
        check(!dirty.getBoolean(manager), "the subsequent build consumes the retained update");
    }

    static void freshBoundaryFaces(Path jar) throws Exception {
        Method calculate = probe(jar);
        Object manager = calculate.getDeclaringClass().getConstructor().newInstance();
        calculate.getDeclaringClass().getField("be").set(manager, new Entity());
        for (Facing side : Facing.VALUES) {
            for (boolean exposed : List.of(false, true)) {
                Tile tile = new Tile();
                Box box = new Box(exposed);
                Cube cube = new Cube(box);
                LittleFaceState stale = exposed ? LittleFaceState.OUTISDE_COVERED : LittleFaceState.OUTSIDE_UNCOVERED;
                calculate.invoke(manager, side, stale, new Context(), tile, box, cube, false);
                check(cube.selected == (exposed ? RenderBoxFace.RENDER : RenderBoxFace.NOT_RENDER), "fresh cube queries actual neighbor rather than stale face flags: " + side + "/" + exposed);
                check(box.queries == 1, "external boundary is recalculated once");
                check(cube.customData == tile, "fresh cube retains material for later neighbor rechecks");
            }
            Box inside = new Box(true); Cube cube = new Cube(inside);
            calculate.invoke(manager, side, LittleFaceState.INSIDE_COVERED, new Context(), new Tile(), inside, cube, false);
            check(cube.selected == RenderBoxFace.NOT_RENDER && inside.queries == 0, "internal face cache remains valid: " + side);
        }
    }

    static Method probe(Path path) throws Exception {
        String owner = "team/creative/littletiles/client/render/block/BERenderManager";
        String name = "NativeBoundaryFaceProbe";
        ClassNode source = new ClassNode();
        try (JarFile jar = new JarFile(path.toFile())) { new ClassReader(jar.getInputStream(jar.getJarEntry(owner + ".class"))).accept(source, 0); }
        MethodNode method = source.methods.stream().filter(m -> m.name.equals("calculateFaces")).findFirst().orElseThrow();
        Map<String, String> mapping = new HashMap<>();
        mapping.put(owner, name);
        mapping.put("team/creative/littletiles/common/block/entity/BETiles", Type.getInternalName(Entity.class));
        mapping.put("team/creative/littletiles/common/block/little/tile/LittleTile", Type.getInternalName(Tile.class));
        mapping.put("team/creative/littletiles/common/math/box/LittleBox", Type.getInternalName(Box.class));
        mapping.put("team/creative/littletiles/common/math/face/LittleFace", Type.getInternalName(Face.class));
        mapping.put("team/creative/littletiles/client/render/tile/LittleRenderBox", Type.getInternalName(Cube.class));
        mapping.put("team/creative/littletiles/client/render/cache/build/RenderingBlockContext", Type.getInternalName(Context.class));
        Remapper remapper = new SimpleRemapper(mapping);
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        out.visit(V21, ACC_PUBLIC, name, null, "java/lang/Object", null);
        out.visitField(ACC_PUBLIC, "be", Type.getDescriptor(Entity.class), null, null).visitEnd();
        MethodVisitor ctor = out.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode(); ctor.visitVarInsn(ALOAD, 0); ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(RETURN); ctor.visitMaxs(1, 1); ctor.visitEnd();
        method.accept(new MethodRemapper(out.visitMethod(ACC_PUBLIC, method.name, remapper.mapMethodDesc(method.desc), null, null), remapper));
        out.visitEnd(); byte[] bytes = out.toByteArray();
        Class<?> cls = new ClassLoader(BoundaryRenderRegression.class.getClassLoader()) {
            Class<?> define() { return defineClass(name, bytes, 0, bytes.length); }
        }.define();
        return cls.getMethod("calculateFaces", Facing.class, LittleFaceState.class, Context.class, Tile.class, Box.class, Cube.class, boolean.class);
    }

    static void check(boolean condition, String description) { checks++; if (!condition) throw new AssertionError(description); }

    static void genericDeferredRefresh(Path path) throws Exception {
        var entities = refresh(path, "team/creative/littletiles/client/render/material/BoundaryRefresh", false);
        for (WorldEntity entity : entities) {
            check(entity.manager.requests == 1, "ordinary-material LT on both sides of an edit receives one refresh");
            check(entity.manager.fullRequests == 1, "deferred geometry refresh discards old render boxes, as the working patch does");
            var dirty = BERenderManager.class.getDeclaredField("neighbourChanged"); dirty.setAccessible(true);
            check(dirty.getBoolean(entity.render), "deferred refresh records changed boundary geometry");
        }
        check(Client.INSTANCE.levelRenderer.redraws == 1, "all seven interior positions share a section redraw");
    }

    static List<WorldEntity> refresh(Path path, String owner, boolean supported) throws Exception {
        String name = "NativeBoundaryRefreshProbe";
        ClassNode source = new ClassNode();
        try (JarFile jar = new JarFile(path.toFile())) { new ClassReader(jar.getInputStream(jar.getJarEntry(owner + ".class"))).accept(source, 0); }
        MethodNode method = source.methods.stream().filter(m -> m.name.equals("tick")).findFirst().orElseThrow();
        Map<String, String> mapping = new HashMap<>();
        mapping.put(owner, name);
        mapping.put("net/minecraft/client/Minecraft", Type.getInternalName(Client.class));
        mapping.put("net/minecraft/client/renderer/LevelRenderer", Type.getInternalName(Renderer.class));
        mapping.put("net/minecraft/client/multiplayer/ClientLevel", Type.getInternalName(World.class));
        mapping.put("net/minecraft/world/level/Level", Type.getInternalName(World.class));
        mapping.put("net/minecraft/core/BlockPos", Type.getInternalName(Pos.class));
        mapping.put("net/minecraft/world/level/block/entity/BlockEntity", Type.getInternalName(BaseEntity.class));
        mapping.put("net/minecraft/world/level/block/state/BlockState", Type.getInternalName(State.class));
        mapping.put("team/creative/littletiles/common/block/entity/BETiles", Type.getInternalName(WorldEntity.class));
        mapping.put("team/creative/littletiles/client/render/material/ConnectedMaterialState", Type.getInternalName(UnsupportedMaterial.class));
        mapping.put("team/creative/littletiles/client/render/material/MaterialNeighbors", Type.getInternalName(MaterialLookup.class));
        mapping.put("team/creative/littletiles/client/render/material/NeighborMaterials", Type.getInternalName(Materials.class));
        mapping.put("com/yuushya/compat/connected/ConnectedMaterialState", Type.getInternalName(UnsupportedMaterial.class));
        mapping.put("com/yuushya/compat/connected/MaterialNeighbors", Type.getInternalName(MaterialLookup.class));
        mapping.put("com/yuushya/compat/connected/NeighborMaterials", Type.getInternalName(Materials.class));
        mapping.put("net/neoforged/neoforge/client/event/ClientTickEvent$Post", "java/lang/Object");
        Remapper remapper = new SimpleRemapper(mapping);
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        out.visit(V21, ACC_PUBLIC, name, null, "java/lang/Object", null);
        out.visitField(ACC_PUBLIC | ACC_STATIC, "PENDING", "Ljava/util/Map;", null, null).visitEnd();
        for (MethodNode helper : source.methods)
            if (helper.name.equals("needsRedraw") || helper.name.equals("interior"))
                helper.accept(new MethodRemapper(out.visitMethod(helper.access, helper.name, helper.desc, null, null), remapper));
        method.accept(new MethodRemapper(out.visitMethod(ACC_PUBLIC | ACC_STATIC, method.name, remapper.mapMethodDesc(method.desc), null, null), remapper));
        out.visitEnd(); byte[] bytes = out.toByteArray();
        Class<?> cls = new ClassLoader(BoundaryRenderRegression.class.getClassLoader()) {
            Class<?> define() { return defineClass(name, bytes, 0, bytes.length); }
        }.define();
        MaterialLookup.supported = supported;
        World level = new World(); Client.INSTANCE = new Client(level);
        Pos origin = new Pos(3, 10, 8);
        List<WorldEntity> entities = new ArrayList<>();
        for (Pos pos : List.of(origin, origin.relative(Direction.WEST), origin.relative(Direction.EAST))) {
            WorldEntity entity = new WorldEntity(); entities.add(entity); level.entities.put(pos, entity);
        }
        cls.getField("PENDING").set(null, new HashMap<>(Map.of(level, Set.of(origin))));
        cls.getMethod("tick", Object.class).invoke(null, new Object());
        return entities;
    }

    public record Pos(int x, int y, int z) {
        public Pos relative(Direction direction) { return new Pos(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ()); }
        public int getX() { return x; } public int getY() { return y; } public int getZ() { return z; }
    }
    public static class BaseEntity {}
    public static final class WorldEntity extends BaseEntity {
        final RefreshManager manager = new RefreshManager();
        public BERenderManager render = manager;
    }
    public static final class RefreshManager extends BERenderManager {
        int requests, fullRequests;
        RefreshManager() { super(null); }
        @Override public void queue(boolean erase, boolean hasPos, long pos) { requests++; if (erase) fullRequests++; }
    }
    public static final class World {
        final Map<Pos, BaseEntity> entities = new HashMap<>();
        public boolean hasChunkAt(Pos pos) { return true; }
        public BaseEntity getBlockEntity(Pos pos) { return entities.get(pos); }
        public State getBlockState(Pos pos) { return new State(); }
    }
    public static final class State {}
    public static final class UnsupportedMaterial { public static boolean supports(State state) { return false; } }
    public static final class Materials { public Set<Object> materials() { return MaterialLookup.supported ? Set.of("supported") : Set.of(); } }
    public static final class MaterialLookup { static boolean supported; public static Materials tiles(WorldEntity entity) { return new Materials(); } }
    public static final class Renderer { int redraws; public void setBlocksDirty(int a, int b, int c, int d, int e, int f) { redraws++; } }
    public static final class Client {
        static Client INSTANCE;
        public World level; public Renderer levelRenderer = new Renderer();
        Client(World level) { this.level = level; }
        public static Client getInstance() { return INSTANCE; }
    }
    public static final class Entity {
        public LittleGrid getGrid() { return null; }
        public boolean shouldFaceBeRendered(Face face, Tile tile) { return true; }
    }
    public static final class Tile { public boolean isTranslucent() { return false; } }
    public static final class Context { public Entity getNeighbour(Facing side) { return null; } }
    public static final class Box {
        final boolean exposed; int queries;
        Box(boolean exposed) { this.exposed = exposed; }
        public Face generateFace(LittleGrid grid, Facing side) { queries++; return new Face(exposed); }
    }
    public static final class Face {
        public LittleGrid grid;
        final boolean exposed; Face(boolean exposed) { this.exposed = exposed; }
        public LittleFaceState calculateOutsideClient(Tile tile, Context context) { return exposed ? LittleFaceState.OUTSIDE_UNCOVERED : LittleFaceState.OUTISDE_COVERED; }
        public void move(Facing side) {}
        public List<?> generateFans() { return List.of(); }
    }
    public static final class Cube {
        public Box box; public Object customData; RenderBoxFace selected;
        Cube(Box box) { this.box = box; }
        public void setFace(Facing side, RenderBoxFace face) { selected = face; }
    }
}
