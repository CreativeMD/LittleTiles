package com.creativemd.littletiles.common.tileentity;

import com.creativemd.littletiles.common.tiles.LittleTile;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

public class TilesImpl implements MutableTiles {
    private final CopyOnWriteArrayList<LittleTile> tiles = TileEntityLittleTiles.createTileList();
    private final CopyOnWriteArrayList<LittleTile> updateTiles = TileEntityLittleTiles.createTileList();

    @SideOnly(Side.CLIENT)
    protected CopyOnWriteArrayList<LittleTile> renderTiles = new CopyOnWriteArrayList<>();

    private final BooleanSupplier clientSide;


    public TilesImpl(BooleanSupplier clientSide) {
        this.clientSide = clientSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public CopyOnWriteArrayList<LittleTile> getRenderTiles() {
        return renderTiles;
    }

    @Override
    public List<LittleTile> getTiles() {
        return tiles;
    }

    @Override
    public boolean contains(LittleTile tile) {
        return tiles.contains(tile);
    }

    @Override
    public CopyOnWriteArrayList<LittleTile> getUpdateTiles() {
        return updateTiles;
    }

    @Override
    public int size() {
        return tiles.size();
    }

    @Override
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    @Override
    public LittleTile first() {
        return tiles.get(0);
    }


    public void removeLittleTile(LittleTile tile) {
        tiles.remove(tile);
        getUpdateTiles().remove(tile);
        if (isClientSide())
            removeLittleTileClient(tile);
    }

    @Override
    public void dispose() {
        this.tiles.clear();
        this.renderTiles.clear();
        this.updateTiles.clear();
    }

    @Override
    public void addAll(List<LittleTile> remains) {
        this.tiles.addAll(remains);
    }

    private boolean isClientSide() {
        return clientSide.getAsBoolean();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void removeLittleTileClient(LittleTile tile) {
        synchronized (getRenderTiles()) {
            getRenderTiles().remove(tile);
        }
    }

    @Override
    public void removeTiles(Collection<LittleTile> tiles) {
        for (LittleTile tile : tiles) {
            removeLittleTile(tile);
        }
    }

    @Override
    public void removeTile(LittleTile tile) {
         removeLittleTile(tile);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addLittleTileClient(LittleTile tile) {
        if (tile.needCustomRendering()) {
            getRenderTiles().add(tile);
        }
    }

    @Override
    public boolean addLittleTile(LittleTile tile) {
        if (isClientSide())
            addLittleTileClient(tile);
        if (tile.shouldTick())
            getUpdateTiles().add(tile);
        return tiles.add(tile);
    }

    public void addTiles(Collection<LittleTile> tiles) {
        for (LittleTile tile : tiles) {
            addLittleTile(tile);
        }
    }

    @Override
    public boolean addTile(LittleTile tile) {
        return addLittleTile(tile);
    }

    @Override
    public void clear() {
        tiles.clear();
    }

}