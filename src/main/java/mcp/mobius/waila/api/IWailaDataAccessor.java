package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getWailaStack).<br>
 * An instance of this interface is passed to most of Waila Block/TileEntity callbacks.
 *
 * @author ProfMobius */

public interface IWailaDataAccessor {
    
    @Nonnull
    World getWorld();
    
    @Nonnull
    EntityPlayer getPlayer();
    
    @Nonnull
    Block getBlock();
    
    //int          		 getBlockID();
    int getMetadata();
    
    @Nonnull
    IBlockState getBlockState();
    
    @Nullable
    TileEntity getTileEntity();
    
    @Nonnull
    RayTraceResult getMOP();
    
    @Nonnull
    BlockPos getPosition();
    
    @Nullable
    Vec3d getRenderingPosition();
    
    @Nonnull
    NBTTagCompound getNBTData();
    
    int getNBTInteger(NBTTagCompound tag, String keyname);
    
    double getPartialFrame();
    
    @Nonnull
    EnumFacing getSide();
    
    @Nonnull
    ItemStack getStack();
}
