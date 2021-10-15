package com.creativemd.littletiles.common.action.block;

import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public class LittleActionActivated extends LittleActionInteract {
    
    public LittleActionActivated(World world, BlockPos blockPos, EntityPlayer player) {
        super(world, blockPos, player);
    }
    
    public LittleActionActivated(World world, BlockPos blockPos, Vec3d pos, Vec3d look, boolean secondMode) {
        super(world, blockPos, pos, look, secondMode);
    }
    
    public LittleActionActivated() {
        
    }
    
    @Override
    public boolean sendToServer() {
        return !preventInteraction;
    }
    
    public boolean preventInteraction = false;
    
    @Override
    protected boolean action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
        if (parent.isStructure())
            return parent.getStructure().onBlockActivated(world, tile, pos, player, EnumHand.MAIN_HAND, player
                .getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float) moving.hitVec.x, (float) moving.hitVec.y, (float) moving.hitVec.z, this);
        
        if (tile.onBlockActivated(parent, player, EnumHand.MAIN_HAND, player
            .getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float) moving.hitVec.x, (float) moving.hitVec.y, (float) moving.hitVec.z, this))
            return true;
        return false;
    }
    
    @Override
    protected boolean action(EntityPlayer player) throws LittleActionException {
        boolean result;
        try {
            result = super.action(player);
        } catch (LittleActionException e) {
            if (!player.world.isRemote)
                LittleEventHandler.addBlockTilePrevent(player);
            throw e;
        }
        if (!player.world.isRemote)
            LittleEventHandler.addBlockTilePrevent(player);
        return result;
    }
    
    @Override
    public boolean canBeReverted() {
        return false;
    }
    
    @Override
    public LittleAction revert(EntityPlayer player) {
        return null;
    }
    
    @Override
    protected boolean isRightClick() {
        return true;
    }
    
    @Override
    public LittleAction flip(Axis axis, LittleBoxAbsolute box) {
        return null;
    }
    
    @Override
    protected boolean requiresBreakEvent() {
        return false;
    }
    
}
