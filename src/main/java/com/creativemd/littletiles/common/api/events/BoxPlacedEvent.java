package com.creativemd.littletiles.common.api.events;

import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Objects;

public class BoxPlacedEvent extends PlayerEvent {
    private final LittleTile littleTile;
    private final LittleActionPlaceStack.LittlePlaceResult placed;

    public BoxPlacedEvent(EntityPlayer player, LittleTile littleTile, LittleActionPlaceStack.LittlePlaceResult placed) {
        super(player);
        this.littleTile = littleTile;
        this.placed = placed;
    }

    public LittleActionPlaceStack.LittlePlaceResult getPlaced() {
        return placed;
    }

    public LittleTile getLittleTile() {
        return littleTile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxPlacedEvent that = (BoxPlacedEvent) o;
        return Objects.equals(littleTile, that.littleTile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(littleTile);
    }

    @Override
    public String toString() {
        return "BoxPlacedEvent{" +
                "littleTile=" + littleTile +
                '}';
    }
}
