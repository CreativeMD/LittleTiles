import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.block.BERenderManager;

/** Native build leases and extracted cleanup/empty-queue paths, without a game launch. */
public final class RenderBuildOwnershipRegression implements Opcodes {
    static final int BUSY = Integer.MIN_VALUE;
    static int checks;
    public static void main(String[] args) throws Exception {
        List<String> failures = new ArrayList<>();
        try { exclusiveBuilders(); } catch (AssertionError error) { failures.add(error.getMessage()); }
        try { queuedEditBurst(); } catch (AssertionError error) { failures.add(error.getMessage()); }
        try { cleanupOwnership(Path.of(args[0])); } catch (AssertionError error) { failures.add(error.getMessage()); }
        try { emptyQueue(Path.of(args[0])); } catch (AssertionError error) { failures.add(error.getMessage()); }
        if (!failures.isEmpty()) {
            failures.forEach(message -> System.err.println("FAIL build ownership: " + message));
            throw new AssertionError(failures.size() + " build ownership groups failed");
        }
        System.out.println("PASS render build ownership: " + checks + " assertions; concurrent builders, rejected-task cleanup and empty-queue leases");
    }

    static void queuedEditBurst() throws Exception {
        BERenderManager manager = new BERenderManager(null);
        var queued = BERenderManager.class.getDeclaredField("queued");
        queued.setAccessible(true);
        queued.setBoolean(manager, true);
        manager.queue(false, false, 0);
        int building = manager.startBuildingCache();
        for (int i = 0; i < 256; i++) manager.onNeighbourChanged();
        check(!manager.finishBuildingCache(building, new ChunkLayerMap<>(), 0, false),
            "256 updates during one build must still schedule the follow-up");
        manager.unsetBlocked();
    }

    static void exclusiveBuilders() throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            for (int attempt = 0; attempt < 100; attempt++) {
                BERenderManager manager = new BERenderManager(null);
                CyclicBarrier ready = new CyclicBarrier(2);
                Callable<Integer> claim = () -> { ready.await(); return manager.startBuildingCache(); };
                Future<Integer> first = workers.submit(claim), second = workers.submit(claim);
                int a = first.get(5, TimeUnit.SECONDS), b = second.get(5, TimeUnit.SECONDS);
                check((a == BUSY) != (b == BUSY), "two workers must not acquire the same render boxes concurrently");
                check(manager.isBlocked(), "successful builder retains its lease");
                manager.unsetBlocked();
                check(!manager.isBlocked(), "one matching release fully restores the counter");
            }
            BERenderManager converting = new BERenderManager(null);
            check(!converting.getAndSetBlocked(), "grid conversion starts without a builder");
            check(converting.startBuildingCache() == BUSY, "a grid conversion prevents a new build lease");
            converting.unsetBlocked();
            check(!converting.isBlocked(), "a rejected builder never increments the conversion counter");
        } finally { workers.shutdownNow(); }
    }

    static void cleanupOwnership(Path path) throws Exception {
        String renderer = "team/creative/littletiles/client/render/cache/build/RenderingThread";
        String context = "team/creative/littletiles/client/render/cache/build/RenderingBlockContext";
        ClassNode worker = read(path, renderer);
        MethodNode cleanup = worker.methods.stream().filter(m -> m.name.equals("releaseCache")).findFirst().orElse(null);
        boolean old = cleanup == null;
        if (old) cleanup = read(path, context).methods.stream().filter(m -> m.name.equals("unsetBlocked")).findFirst().orElseThrow();
        Map<String, String> names = new HashMap<>();
        names.put(context, Type.getInternalName(Context.class));
        names.put("team/creative/littletiles/common/block/entity/BETiles", Type.getInternalName(Entity.class));
        ClassWriter out = writer("NativeBuildCleanupProbe");
        cleanup.accept(new MethodRemapper(out.visitMethod(ACC_PUBLIC | ACC_STATIC, "release", "(" + Type.getDescriptor(Context.class) + "Z)V", null, null), new SimpleRemapper(names)));
        out.visitEnd(); Class<?> cls = define("NativeBuildCleanupProbe", out.toByteArray());
        var release = cls.getMethod("release", Context.class, boolean.class);
        BERenderManager manager = new BERenderManager(null);
        manager.startBuildingCache(); Context rejected = new Context(manager);
        release.invoke(null, rejected, false);
        check(manager.isBlocked(), "cleanup of a rejected task must not release the active builder");
        release.invoke(null, rejected, true);
        check(!manager.isBlocked(), "cleanup releases a lease only when that iteration acquired it");
    }

    static void emptyQueue(Path path) throws Exception {
        String owner = "team/creative/littletiles/client/render/cache/build/RenderingThread";
        ClassNode source = read(path, owner);
        MethodNode method = source.methods.stream().filter(m -> m.name.equals("queue")).findFirst().orElseThrow();
        String name = "NativeEmptyQueueProbe";
        Map<String, String> names = new HashMap<>();
        names.put(owner, name);
        names.put("team/creative/littletiles/common/block/entity/BETiles", Type.getInternalName(Entity.class));
        names.put("team/creative/littletiles/client/render/cache/build/RenderingBlockQueue", Type.getInternalName(Queue.class));
        Remapper remapper = new SimpleRemapper(names);
        ClassWriter out = writer(name);
        out.visitField(ACC_PUBLIC | ACC_STATIC, "THREADS", "Ljava/util/List;", null, null).visitEnd();
        out.visitField(ACC_PUBLIC | ACC_STATIC, "QUEUE", Type.getDescriptor(Queue.class), null, null).visitEnd();
        out.visitField(ACC_PUBLIC | ACC_STATIC, "EMPTY_HOLDERS", Type.getDescriptor(ChunkLayerMap.class), null, null).visitEnd();
        out.visitField(ACC_PUBLIC | ACC_STATIC, "CURRENT_RENDERING_INDEX", "I", null, null).visitEnd();
        method.accept(new MethodRemapper(out.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_SYNCHRONIZED, method.name, remapper.mapMethodDesc(method.desc), null, null), remapper));
        out.visitEnd(); Class<?> cls = define(name, out.toByteArray());
        cls.getField("THREADS").set(null, List.of());
        Queue queued = new Queue(); cls.getField("QUEUE").set(null, queued);
        cls.getField("EMPTY_HOLDERS").set(null, new ChunkLayerMap<>());
        cls.getField("CURRENT_RENDERING_INDEX").setInt(null, 3);
        var queue = cls.getMethod("queue", Entity.class, boolean.class, long.class);
        BERenderManager manager = new BERenderManager(null);
        check(!(boolean) queue.invoke(null, new Entity(manager), false, 0L), "empty entity completes without a queued task");
        check(!manager.isBlocked(), "empty-entity fast path must release its build lease");
        manager.startBuildingCache();
        check((boolean) queue.invoke(null, new Entity(manager), false, 0L), "empty-entity update defers while another builder owns the cache");
        check(queued.requests == 1, "busy empty update remains queued");
        manager.unsetBlocked();
        check(!manager.isBlocked(), "deferred empty update must not leak or release another owner's lease");
    }

    static ClassWriter writer(String name) {
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        out.visit(V21, ACC_PUBLIC, name, null, "java/lang/Object", null); return out;
    }
    static Class<?> define(String name, byte[] bytes) { return new ClassLoader(RenderBuildOwnershipRegression.class.getClassLoader()) {
        Class<?> define() { return defineClass(name, bytes, 0, bytes.length); }
    }.define(); }
    static ClassNode read(Path path, String owner) throws Exception { try (JarFile jar = new JarFile(path.toFile())) {
        ClassNode node = new ClassNode(); new ClassReader(jar.getInputStream(jar.getJarEntry(owner + ".class"))).accept(node, 0); return node;
    } }
    static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
    public static final class Entity {
        public BERenderManager render;
        Entity(BERenderManager render) { this.render = render; }
        public boolean isRenderingEmpty() { return true; }
    }
    public static final class Context {
        public Entity be;
        Context(BERenderManager manager) { be = new Entity(manager); }
        public void unsetBlocked() { be.render.unsetBlocked(); }
    }
    public static final class Queue { int requests; public void queue(Entity entity, boolean hasPos, long pos) { requests++; } }
}
