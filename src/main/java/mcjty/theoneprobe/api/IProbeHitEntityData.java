package mcjty.theoneprobe.api;

import net.minecraft.util.math.Vec3d;

/**
 * Access information about where the probe hit the entity
 */
public interface IProbeHitEntityData {

    Vec3d getHitVec();
}
