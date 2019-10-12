package com.creativemd.littletiles.common.api.events;

import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.tiles.LittleTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Objects;

public class LittleTileUpdateEvent extends WorldEvent {
    private final BlockPos blockPos;

    public LittleTileUpdateEvent(World world, BlockPos blockPos) {
        super(world);
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LittleTileUpdateEvent that = (LittleTileUpdateEvent) o;
        return Objects.equals(blockPos, that.blockPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos);
    }

    @Override
    public String toString() {
        return "LittleTileUpdateEvent{" +
                "blockPos=" + blockPos +
                '}';
    }
}
