package mcp.mobius.waila.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getWailaStack).<br>
 * An instance of this interface is passed to most of Waila Entity callbacks.
 *
 * @author ProfMobius */

public interface IWailaEntityAccessor {
    
    @Nonnull
    World getWorld();
    
    @Nonnull
    EntityPlayer getPlayer();
    
    @Nonnull
    Entity getEntity();
    
    @Nonnull
    RayTraceResult getMOP();
    
    @Nullable
    Vec3d getRenderingPosition();
    
    @Nonnull
    NBTTagCompound getNBTData();
    
    int getNBTInteger(NBTTagCompound tag, String keyname);
    
    double getPartialFrame();
}
