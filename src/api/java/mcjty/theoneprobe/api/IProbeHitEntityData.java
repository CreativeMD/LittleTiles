package mcjty.theoneprobe.api;

import net.minecraft.world.phys.Vec3;

/**
 * Access information about where the probe hit the entity
 */
public interface IProbeHitEntityData {

    Vec3 getHitVec();
}
