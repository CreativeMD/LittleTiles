package com.creativemd.littletiles.common.tileentity;

import com.creativemd.littletiles.common.tiles.LittleTile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.List;

public interface MutableTiles extends Tiles {
    @SideOnly(Side.CLIENT)
    void removeLittleTileClient(LittleTile tile);

    void removeTiles(Collection<LittleTile> tiles);

    void removeTile(LittleTile tile);

    @SideOnly(Side.CLIENT)
    void addLittleTileClient(LittleTile tile);

    boolean addLittleTile(LittleTile tile);


    boolean addTile(LittleTile tile);

    void clear();

    @Override
    List<LittleTile> getRenderTiles();

    List<LittleTile> getTiles();

    List<LittleTile> getUpdateTiles();

    void removeLittleTile(LittleTile littleTile);

    void dispose();

    void addAll(List<LittleTile> remains);
}
