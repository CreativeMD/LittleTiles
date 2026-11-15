package team.creative.littletiles.client.render.cache.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.render.dynamic_shade.SableDynamicDirectionalShading;
import dev.ryanhcode.sable.render.dynamic_shade.SubLevelVertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.mod.sable.SableManager;

/** Loaded only when the optional Sable integration is installed. */
public final class LittleSableLighting {
    static boolean isDynamic(Level level, BlockPos pos) {
        return SableDynamicDirectionalShading.isEnabled() && SableManager.isSubLevel(level, pos);
    }

    public static VertexConsumer wrap(VertexConsumer consumer) {
        // Match Sable's treatment of explicitly unshaded quads as well as normals.
        return new SubLevelVertexConsumer(consumer);
    }
}
