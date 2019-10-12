package com.creativemd.littletiles.common.tileentity;

import com.creativemd.littletiles.common.tiles.LittleTile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public interface Tiles extends Iterable<LittleTile> {

    @SideOnly(Side.CLIENT)
    Iterable<LittleTile> getRenderTiles();

    Iterable<LittleTile> getTiles();

    Iterable<LittleTile> getUpdateTiles();

    boolean contains(LittleTile tile);

    int size();

    boolean isEmpty();

    LittleTile first();

    @Override
    default Iterator<LittleTile> iterator() {
        return getTiles().iterator();
    }
}
