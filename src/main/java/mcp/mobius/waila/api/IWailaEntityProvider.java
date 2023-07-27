package mcp.mobius.waila.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/** Callback class interface used to provide Entity tooltip informations to Waila.<br>
 * All methods in this interface shouldn't to be called by the implementing mod. An instance of the class is to be
 * registered to Waila via the {@link IWailaRegistrar} instance provided in the original registration callback method
 * (cf. {@link IWailaRegistrar} documentation for more information).
 *
 * @author ProfMobius */
public interface IWailaEntityProvider {
    
    /** Callback used to override the default Waila lookup system.</br>
     * Will only be called if the implementing class is registered via {@link IWailaRegistrar#registerOverrideEntityProvider}.</br>
     *
     * @param accessor
     *            Contains most of the relevant information about the current environment.
     * @param config
     *            Current configuration of Waila.
     * @return null if override is not required, an Entity otherwise. */
    @Nullable
    default Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return null;
    }
    
    /** Callback used to add lines to one of the three sections of the tooltip (Head, Body, Tail).</br>
     * Will only be called if the implementing class is registered via {@link IWailaRegistrar#registerHeadProvider}.</br>
     * You are supposed to always return the modified input currenttip.</br>
     * <p>
     * You may return null if you have not registered this as a head provider. However, you should return the provided list
     * to be safe.
     * <p>
     * This method is only called on the client side. If you require data from the server, you should also implement
     * {@link #getNBTData(EntityPlayerMP, Entity, NBTTagCompound, World)} and add the data to the {@link NBTTagCompound}
     * there, which can then be read back using {@link IWailaDataAccessor#getNBTData()}. If you rely on the client knowing
     * the data you need, you are not guaranteed to have the proper values.
     *
     * @param entity
     *            Current Entity scanned.
     * @param currenttip
     *            Current list of tooltip lines (might have been processed by other providers and might be processed by other providers).
     * @param accessor
     *            Contains most of the relevant information about the current environment.
     * @param config
     *            Current configuration of Waila.
     * @return Modified input currenttip */
    @Nonnull
    default List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
    
    /** Callback used to add lines to one of the three sections of the tooltip (Head, Body, Tail).</br>
     * Will only be called if the implementing class is registered via {@link IWailaRegistrar#registerBodyProvider}.</br>
     * You are supposed to always return the modified input currenttip.</br>
     * <p>
     * You may return null if you have not registered this as a body provider. However, you should return the provided list
     * to be safe.
     * <p>
     * This method is only called on the client side. If you require data from the server, you should also implement
     * {@link #getNBTData(EntityPlayerMP, Entity, NBTTagCompound, World)} and add the data to the {@link NBTTagCompound}
     * there, which can then be read back using {@link IWailaDataAccessor#getNBTData()}. If you rely on the client knowing
     * the data you need, you are not guaranteed to have the proper values.
     *
     * @param entity
     *            Current Entity scanned.
     * @param currenttip
     *            Current list of tooltip lines (might have been processed by other providers and might be processed by other providers).
     * @param accessor
     *            Contains most of the relevant information about the current environment.
     * @param config
     *            Current configuration of Waila.
     * @return Modified input currenttip */
    @Nonnull
    default List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
    
    /** Callback used to add lines to one of the three sections of the tooltip (Head, Body, Tail).</br>
     * Will only be called if the implementing class is registered via {@link IWailaRegistrar#registerTailProvider}.</br>
     * You are supposed to always return the modified input currenttip.</br>
     * <p>
     * You may return null if you have not registered this as a tail provider. However, you should return the provided list
     * to be safe.
     * <p>
     * This method is only called on the client side. If you require data from the server, you should also implement
     * {@link #getNBTData(EntityPlayerMP, Entity, NBTTagCompound, World)} and add the data to the {@link NBTTagCompound}
     * there, which can then be read back using {@link IWailaDataAccessor#getNBTData()}. If you rely on the client knowing
     * the data you need, you are not guaranteed to have the proper values.
     *
     * @param entity
     *            Current Entity scanned.
     * @param currenttip
     *            Current list of tooltip lines (might have been processed by other providers and might be processed by other providers).
     * @param accessor
     *            Contains most of the relevant information about the current environment.
     * @param config
     *            Current configuration of Waila.
     * @return Modified input currenttip */
    @Nonnull
    default List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
    
    /** Callback used server side to return a custom synchronization NBTTagCompound.</br>
     * Will only be called if the implementing class is registered via {@link IWailaRegistrar#registerNBTProvider}.</br>
     * You are supposed to always return the modified input NBTTagCompound tag.</br>
     * <p>
     * You may return null if you have not registered this as an NBT provider. However, you should return the provided tag
     * to be safe.
     *
     * @param player
     *            The player requesting data synchronization (The owner of the current connection).
     * @param ent
     *            The Entity targeted for synchronization.
     * @param tag
     *            Current synchronization tag (might have been processed by other providers and might be processed by other providers).
     * @param world
     *            TileEntity's World.
     * @return Modified input NBTTagCompound tag. */
    @Nonnull
    default NBTTagCompound getNBTData(EntityPlayerMP player, Entity ent, NBTTagCompound tag, World world) {
        return tag;
    }
}
