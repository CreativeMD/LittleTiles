package team.creative.littletiles.client.render.block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.common.block.entity.BETiles;

/** Retains section meshes until LT's normal neighbour updates finish. */
@EventBusSubscriber(modid = "littletiles", value = Dist.CLIENT)
public final class NeighbourRenderTransition {
    private static Level world;
    private static final Set<BETiles> pending = new HashSet<>();
    private static final Map<Long, Set<BETiles>> sections = new HashMap<>();

    public static void changed(Level level, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (level == null || !level.isClientSide || level != mc.level)
            return;
        if (!mc.isSameThread()) {
            BlockPos copy = pos.immutable();
            mc.execute(() -> changed(level, copy));
            return;
        }
        synchronized (NeighbourRenderTransition.class) {
            if (world != level) {
                clear();
                world = level;
            }
            Set<BETiles> dependencies = new HashSet<>();
            Set<Long> affected = new HashSet<>();
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                if (level.hasChunkAt(neighbour) && level.getBlockEntity(neighbour) instanceof BETiles be && !be.isRemoved()) {
                    pending.add(be);
                    dependencies.add(be);
                    affected.add(SectionPos.asLong(neighbour));
                }
            }
            if (!dependencies.isEmpty()) {
                affected.add(SectionPos.asLong(pos));
                if (level.getBlockEntity(pos) instanceof BETiles be)
                    dependencies.add(be);
                for (long section : affected)
                    sections.computeIfAbsent(section, key -> new HashSet<>()).addAll(dependencies);
            }
        }
    }

    public static synchronized boolean defer(long section) {
        return world == Minecraft.getInstance().level && sections.containsKey(section);
    }

    public static synchronized void naturallyInvalidated(BETiles be) {
        pending.remove(be);
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Pre event) {
        Set<Long> ready;
        Level level;
        synchronized (NeighbourRenderTransition.class) {
            if (world != Minecraft.getInstance().level) {
                clear();
                return;
            }
            if (sections.isEmpty())
                return;
            ready = new HashSet<>();
            level = world;
            var iterator = sections.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                boolean complete = true;
                for (BETiles be : entry.getValue()) {
                    if (!be.isRemoved() && be.hasLoaded() && (pending.contains(be) || be.render.isInQueue())) {
                        complete = false;
                        break;
                    }
                }
                if (complete) {
                    ready.add(entry.getKey());
                    iterator.remove();
                }
            }
            if (sections.isEmpty())
                pending.clear();
        }
        for (long section : ready) {
            var chunk = RenderingLevelHandler.of(level, SectionPos.of(section).origin()).getRenderChunk(level, section);
            if (chunk != null)
                chunk.markReadyForUpdate(false);
        }
    }

    private static void clear() {
        pending.clear();
        sections.clear();
        world = null;
    }
}
