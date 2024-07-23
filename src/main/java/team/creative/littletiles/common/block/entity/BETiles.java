package team.creative.littletiles.common.block.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.be.BlockEntityCreative;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.itr.IterableIterator;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.block.ILittleBlockEntity;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.level.RenderUploader;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.BlockParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.face.LittleServerFace;
import team.creative.littletiles.common.math.transformation.LittleBlockTransformer;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;

public class BETiles extends BlockEntityCreative implements IGridBased, ILittleBlockEntity {
    
    private boolean hasLoaded = false;
    private boolean preventUnload = false;
    protected final BlockEntityInteractor interactor = new BlockEntityInteractor();
    private LittleGrid grid = LittleGrid.MIN;
    private BlockParentCollection tiles;
    private boolean unloaded = false;
    public final SideSolidCache sideCache = new SideSolidCache();
    
    @OnlyIn(Dist.CLIENT)
    public BERenderManager render;
    
    public BETiles(BlockPos pos, BlockState state) {
        this(LittleTilesRegistry.BE_TILES_TYPE.get(), pos, state);
    }
    
    public BETiles(BlockEntityType<? extends BETiles> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    protected void assign(BETiles te) {
        try {
            for (Field field : BETiles.class.getDeclaredFields())
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))
                    field.set(this, field.get(te));
            setLevel(getLevel());
            tiles.be = this;
            if (isClient())
                render.setBe(this);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (tiles == null)
            init();
    }
    
    private void init() {
        tiles = new BlockParentCollection(this, isClient());
        if (isClient())
            initClient();
    }
    
    @OnlyIn(Dist.CLIENT)
    private void initClient() {
        this.render = new BERenderManager(this);
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        boolean rendering = false;
        if (level != null && level.isClientSide)
            rendering = render.getAndSetBlocked();
        
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            pair.value.convertTo(grid, to);
        
        this.grid = to;
        
        if (level != null && level.isClientSide) {
            render.unsetBlocked();
            if (rendering) // Fixes incorrect rendering when receiving an update while render cache is building
                render.queue(true, SectionPos.asLong(getBlockPos()));
        }
    }
    
    @Override
    public int getSmallest() {
        int size = LittleGrid.MIN.count;
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            size = Math.max(size, pair.value.getSmallest(grid));
        return size;
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof BETiles be) {
            if (!be.tiles.hasTicking() && !be.level.isClientSide) {
                be.customTilesUpdate();
                System.out.println("Ticking tileentity which shouldn't " + be.getBlockPos());
                return;
            }
            
            be.tick();
        }
    }
    
    public Iterable<LittleStructure> ticking() {
        return tiles.loadedStructures(LittleStructureAttribute.TICKING);
    }
    
    @OnlyIn(Dist.CLIENT)
    public Iterable<LittleStructure> rendering() {
        return tiles.loadedStructures(LittleStructureAttribute.TICK_RENDERING);
    }
    
    public int tilesCount() {
        return tiles.size();
    }
    
    public boolean hasLoaded() {
        return hasLoaded && level != null && tiles != null;
    }
    
    public void setLoaded() {
        hasLoaded = true;
    }
    
    public boolean shouldCheckForCollision() {
        return tiles.hasCollisionListener();
    }
    
    @OnlyIn(Dist.CLIENT)
    public void updateQuadCache(long pos) {
        if (tiles == null)
            return;
        render.sectionUpdate(pos);
    }
    
    public void updateLighting() {
        level.getLightEngine().checkBlock(getBlockPos());
    }
    
    public BETiles forceSupportAttribute(int attribute) {
        return changeState(tiles.hasTicking() || LittleStructureAttribute.ticking(attribute), tiles.hasRendered() || LittleStructureAttribute.tickRendering(attribute));
    }
    
    protected BETiles changeState(boolean ticking, boolean rendered) {
        if (ticking == isTicking() && rendered == isRendered())
            return this;
        BlockState state = BlockTile.getState(getBlockState(), ticking, rendered);
        preventUnload = true;
        level.setBlock(worldPosition, state, 20);
        BETiles newBE = (BETiles) level.getBlockEntity(worldPosition);
        newBE.assign(this);
        newBE.tiles.be = newBE;
        setRemoved();
        preventUnload = false;
        return newBE;
    }
    
    protected void customTilesUpdate() {
        if (level.isClientSide)
            return;
        
        if (tiles.isCompletelyEmpty()) {
            BlockState state = getBlockState();
            if (state.getValue(BlockTile.WATERLOGGED))
                level.setBlockAndUpdate(getBlockPos(), level.getFluidState(worldPosition).createLegacyBlock());
            else
                level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
            return;
        }
        
        changeState(tiles.hasTicking(), tiles.hasRendered());
    }
    
    private void updateNeighbour(Facing facing) {
        LittleServerFace face = new LittleServerFace(this);
        for (Pair<IParentCollection, LittleTile> pair : allTiles())
            for (LittleBox box : pair.value) {
                if (box.hasOrCreateFaceState(pair.key, pair.value, face) && box.getFaceState(facing).outside())
                    box.setFaceState(facing, face.set(pair.key, pair.value, box, facing).calculate());
            }
    }
    
    public void onNeighbourChanged(@Nullable Facing facing) {
        if (facing == null)
            for (int i = 0; i < Facing.VALUES.length; i++)
                updateNeighbour(Facing.VALUES[i]);
        else
            updateNeighbour(facing);
        
        if (isClient())
            render.onNeighbourChanged();
        
        notifyStructure();
    }
    
    public void notifyStructure() {
        for (LittleStructure structure : tiles.loadedStructures(LittleStructureAttribute.NEIGHBOR_LISTENER))
            structure.neighbourChanged();
    }
    
    public void updateTiles() {
        updateTiles(true);
    }
    
    public void updateTiles(boolean updateNeighbour) {
        tiles.removeEmptyLists();
        notifyStructure();
        
        sideCache.reset();
        rebuildFaces();
        
        if (level != null) {
            markDirty();
            if (updateNeighbour)
                updateNeighbour();
            updateLighting();
        }
        
        if (isClient())
            render.tilesChanged();
        
        customTilesUpdate();
    }
    
    public void updateTiles(Consumer<BlockEntityInteractor> action) {
        action.accept(interactor);
        updateTiles();
    }
    
    /** Block will not update */
    public void updateTilesSecretly(Consumer<BlockEntityInteractor> action) {
        action.accept(interactor);
    }
    
    /** Tries to convert the TileEntity to a vanilla block
     * 
     * @return whether it could convert it or not */
    public boolean convertBlockToVanilla() {
        LittleTile firstTile = null;
        if (tiles.isCompletelyEmpty()) {
            level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 35);
            return true;
        }
        
        if (level instanceof IOrientatedLevel || tiles.countStructures() > 0)
            return false;
        
        if (tiles.size() == 1) {
            LittleTile first = tiles.first();
            if (!first.canBeConvertedToVanilla() || !first.doesFillEntireBlock(grid))
                return false;
            firstTile = tiles.first();
            level.setBlockAndUpdate(getBlockPos(), firstTile.getBlock().getState());
            return true;
        }
        
        return false;
    }
    
    public boolean isBoxFilled(LittleBox box) {
        LittleVec size = box.getSize();
        boolean[][][] filled = new boolean[size.x][size.y][size.z];
        
        for (LittleTile tile : tiles)
            tile.fillInSpace(box, filled);
        
        for (int x = 0; x < filled.length; x++)
            for (int y = 0; y < filled[x].length; y++)
                for (int z = 0; z < filled[x][y].length; z++)
                    if (!filled[x][y][z])
                        return false;
                    
        return true;
    }
    
    public void updateNeighbour() {
        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
    }
    
    private LittleBox getLittleBlockBox() {
        int minX = grid.count;
        int minY = grid.count;
        int minZ = grid.count;
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles()) {
            for (LittleBox box : pair.value) {
                minX = Math.min(box.minX, minX);
                minY = Math.min(box.minY, minY);
                minZ = Math.min(box.minZ, minZ);
                maxX = Math.max(box.maxX, maxX);
                maxY = Math.max(box.maxY, maxY);
                maxZ = Math.max(box.maxZ, maxZ);
            }
        }
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public AABB getBlockBB() {
        return getLittleBlockBox().getBB(grid);
    }
    
    public AABB getBlockBBWithOffset() {
        return getLittleBlockBox().getBB(grid, worldPosition);
    }
    
    public VoxelShape getBlockShape() {
        return getLittleBlockBox().getShape(grid);
    }
    
    public void rebuildFaces() {
        LittleServerFace face = new LittleServerFace(this);
        for (Pair<IParentCollection, LittleTile> entry : allTiles())
            for (LittleBox box : entry.value)
                for (int i = 0; i < Facing.VALUES.length; i++) {
                    Facing facing = Facing.VALUES[i];
                    box.setFaceState(facing, face.set(entry.getKey(), entry.getValue(), box, facing).calculate());
                }
    }
    
    public boolean shouldFaceBeRendered(LittleFace face, LittleTile rendered) {
        return unsafeSameGridRestore(face, () -> {
            for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles()) {
                if (pair.key.isStructure() && LittleStructureAttribute.noCollision(pair.key.getAttribute()))
                    continue;
                if (pair.value.doesProvideSolidFace() || pair.value.canBeRenderCombined(rendered))
                    pair.value.fillFace(pair.key, face, grid);
            }
            
            return !face.isFilled(rendered.isTranslucent());
        });
    }
    
    /** @param box
     * @param cutout
     *            filled with all boxes which are cutout by tiles
     * @return all boxes which are not cutout by other tiles */
    public List<LittleBox> cutOut(LittleGrid grid, LittleBox box, List<LittleBox> cutout, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> cutting = new ArrayList<>();
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            pair.value.getIntersectingBoxes(box, cutting);
        return box.cutOut(grid, cutting, cutout, volume);
    }
    
    public Pair<IParentCollection, LittleTile> intersectingTile(LittleBox box) {
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            if (pair.value.intersectsWith(box))
                return pair;
        return null;
    }
    
    public boolean isSpaceFor(LittleBox box, BiPredicate<IParentCollection, LittleTile> predicate) {
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles()) {
            if (predicate != null && !predicate.test(pair.key, pair.value))
                continue;
            if (pair.value.intersectsWith(box))
                return false;
            
        }
        return true;
    }
    
    public boolean isSpaceFor(LittleBox box, Predicate<LittleTile> predicate) {
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles()) {
            if (predicate != null && !predicate.test(pair.value))
                continue;
            if (pair.value.intersectsWith(box))
                return false;
            
        }
        return true;
    }
    
    public boolean isSpaceFor(LittleBox box) {
        return isSpaceFor(box, (Predicate<LittleTile>) null);
    }
    
    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        
        if (tiles == null)
            init();
        
        boolean rendering = false;
        if (level != null && level.isClientSide)
            rendering = render.getAndSetBlocked();
        
        grid = LittleGrid.getOrThrow(nbt);
        
        tiles.load(nbt.getCompound("content"));
        sideCache.load(nbt);
        
        if (level != null && !level.isClientSide) {
            level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
            customTilesUpdate();
        }
        
        if (level != null && level.isClientSide) {
            render.unsetBlocked();
            if (rendering) // Fixes incorrect rendering when receiving an update while render cache is building
                render.queue(true, SectionPos.asLong(getBlockPos()));
        }
    }
    
    protected int[] getIdentifier(LittleBox box) {
        return new int[] { box.minX, box.minY, box.minZ };
    }
    
    @Override
    protected void saveAdditional(CompoundTag nbt, Provider provider) {
        super.saveAdditional(nbt, provider);
        grid.set(nbt);
        nbt.put("content", tiles.save(new LittleServerFace(this)));
        sideCache.write(nbt);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleUpdate(CompoundTag nbt, boolean chunkUpdate) {
        RenderUploader.notifyReceiveClientUpdate(this);
        loadAdditional(nbt, level.registryAccess());
        if (!chunkUpdate)
            updateTiles(false);
    }
    
    public BlockHitResult rayTrace(Player player) {
        Vec3 pos = player.getPosition(TickUtils.getFrameTime(level));
        double distance = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(TickUtils.getFrameTime(level));
        Vec3 look = pos.add(view.x * distance, view.y * distance, view.z * distance);
        
        if (level != player.level() && level instanceof IOrientatedLevel or) {
            pos = or.getOrigin().transformPointToFakeWorld(pos);
            look = or.getOrigin().transformPointToFakeWorld(look);
        }
        
        return rayTrace(pos, look);
    }
    
    public BlockHitResult rayTrace(Vec3 pos, Vec3 look) {
        BlockHitResult result = null;
        double distance = 0;
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            for (LittleBox box : pair.value) {
                BlockHitResult temphit = box.rayTrace(grid, getBlockPos(), pos, look);
                if (temphit != null) {
                    double tempDistance = temphit.getLocation().distanceToSqr(pos);
                    if (result == null || distance > tempDistance) {
                        distance = tempDistance;
                        result = temphit;
                    }
                }
            }
        
        return result;
    }
    
    public LittleTileContext getFocusedTile(Player player, float partialTickTime) {
        if (!isClient())
            return null;
        Vec3 pos = player.getEyePosition(partialTickTime);
        double distance = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        Vec3 look = pos.add(view.x * distance, view.y * distance, view.z * distance);
        
        if (level != player.level() && level instanceof IOrientatedLevel or) {
            pos = or.getOrigin().transformPointToFakeWorld(pos);
            look = or.getOrigin().transformPointToFakeWorld(look);
        }
        
        return getFocusedTile(pos, look);
    }
    
    public LittleTileContext getFocusedTile(Vec3 pos, Vec3 look) {
        IParentCollection parent = null;
        LittleTile tileFocus = null;
        LittleBox boxFocus = null;
        double distance = 0;
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            for (LittleBox box : pair.value) {
                BlockHitResult Temphit = box.rayTrace(grid, getBlockPos(), pos, look);
                if (Temphit != null) {
                    double tempDistance = Temphit.getLocation().distanceToSqr(pos);
                    if (tileFocus == null || distance > tempDistance) {
                        distance = tempDistance;
                        parent = pair.key;
                        tileFocus = pair.value;
                        boxFocus = box;
                    }
                }
            }
        
        if (tileFocus == null)
            return LittleTileContext.FAILED;
        return new LittleTileContext(parent, tileFocus, boxFocus);
    }
    
    @Override
    public void onLoad() {
        setLoaded();
    }
    
    public boolean isTicking() {
        return BlockTile.isTicking(getBlockState());
    }
    
    public boolean isRendered() {
        return false;
    }
    
    public boolean combineTiles(int structureIndex) {
        if (getStructure(structureIndex) == null)
            return false;
        boolean changed = ((StructureParentCollection) getStructure(structureIndex)).combine();
        convertToSmallest();
        if (changed)
            updateTiles();
        return changed;
    }
    
    public boolean combineTilesSecretly(int structureIndex) {
        if (getStructure(structureIndex) == null)
            return false;
        boolean changed = ((StructureParentCollection) getStructure(structureIndex)).combine();
        convertToSmallest();
        return changed;
    }
    
    public boolean combineTiles() {
        boolean changed = tiles.combine();
        
        convertToSmallest();
        if (changed)
            updateTiles();
        return changed;
    }
    
    public boolean combineTilesSecretly() {
        boolean changed = tiles.combine();
        convertToSmallest();
        return changed;
    }
    
    @Override
    @Nullable
    public BlockState getState(AABB box, boolean realistic) {
        if (tiles == null)
            return null;
        
        if (realistic) {
            box = box.expandTowards(0, -grid.pixelLength, 0);
            for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles()) {
                if (pair.value.noCollision())
                    continue;
                for (LittleBox tileBox : pair.value)
                    if (tileBox.getBB(grid, getBlockPos()).intersects(box))
                        return pair.value.getBlock().getState();
            }
            return null;
        }
        box = box.expandTowards(0, -1, 0);
        LittleTile highest = null;
        LittleBox highestBox = null;
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles()) {
            if (pair.value.noCollision())
                continue;
            for (LittleBox tileBox : highest)
                if ((highest == null || (tileBox.maxY > highestBox.maxY) && tileBox.getBB(grid, getBlockPos()).intersects(box))) {
                    highest = pair.value;
                    highestBox = tileBox;
                }
            
        }
        return highest != null ? highest.getBlock().getState() : null;
    }
    
    public boolean isEmpty() {
        return tiles.isCompletelyEmpty();
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean isRenderingEmpty() {
        return tiles.isCompletelyEmpty() && !render.getBufferCache().hasAdditional();
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!preventUnload && tiles != null)
            tiles.unload();
    }
    
    public boolean unloaded() {
        return unloaded;
    }
    
    @Override
    public void onChunkUnloaded() {
        unloaded = true;
        super.onChunkUnloaded();
        tiles.unload();
        if (level.isClientSide) {
            tiles = null;
            render.chunkUnload();
        }
    }
    
    public void rotate(net.minecraft.world.level.block.Rotation rotation) {
        LittleBlockTransformer.rotate(this, Rotation.getRotation(rotation), Rotation.getRotationCount(rotation));
        updateTiles();
    }
    
    public void mirror(Mirror mirror) {
        LittleBlockTransformer.mirror(this, Axis.getMirrorAxis(mirror));
        updateTiles();
    }
    
    @Override
    public String toString() {
        return getBlockPos().toString();
    }
    
    public void tick() {
        for (LittleStructure structure : ticking())
            structure.tick();
    }
    
    public Iterable<IParentCollection> groups() {
        return tiles.groups();
    }
    
    public IParentCollection noneStructureTiles() {
        return tiles;
    }
    
    public Iterable<Pair<IParentCollection, LittleTile>> allTiles() {
        return tiles.allTiles();
    }
    
    public Iterable<Pair<IParentCollection, LittleTile>> allBoxes() {
        return tiles.allTiles();
    }
    
    public IStructureParentCollection getStructure(int index) {
        return tiles.getStructure(index);
    }
    
    public Iterable<LittleStructure> loadedStructures() {
        if (tiles == null)
            return Collections.EMPTY_LIST;
        return tiles.loadedStructures();
    }
    
    public Iterable<LittleStructure> loadedStructures(int attribute) {
        if (tiles == null)
            return Collections.EMPTY_LIST;
        return tiles.loadedStructures(attribute);
    }
    
    public Iterable<IStructureParentCollection> structures() {
        return tiles.structures();
    }
    
    public void fillUsedIds(BitSet usedIds) {
        tiles.fillUsedIds(usedIds);
    }
    
    public class BlockEntityInteractor {
        
        public Iterable<ParentCollection> groups() {
            return new IterableIterator<ParentCollection>() {
                
                ParentCollection current = tiles;
                Iterator<StructureParentCollection> children = structures().iterator();
                
                @Override
                public boolean hasNext() {
                    if (current != null)
                        return true;
                    if (!children.hasNext())
                        return false;
                    current = children.next();
                    return true;
                }
                
                @Override
                public ParentCollection next() {
                    ParentCollection result = current;
                    current = null;
                    return result;
                }
            };
        }
        
        public ParentCollection get(IParentCollection list) {
            return (ParentCollection) list;
        }
        
        public StructureParentCollection get(IStructureParentCollection list) {
            return (StructureParentCollection) list;
        }
        
        public ParentCollection noneStructureTiles() {
            return tiles;
        }
        
        public Iterable<StructureParentCollection> structures() {
            return tiles.structuresReal();
        }
        
        public StructureParentCollection getStructure(int index) {
            return tiles.getStructure(index);
        }
        
        public boolean removeStructure(int index) {
            return tiles.removeStructure(index);
        }
        
        public StructureParentCollection addStructure(int index, int attribute) {
            return tiles.addStructure(index, attribute);
        }
        
    }
    
    public static enum SideState {
        EMPTY {
            @Override
            public boolean doesBlockCollision() {
                return false;
            }
            
            @Override
            public boolean doesBlockLight() {
                return false;
            }
            
            @Override
            public boolean isFilled() {
                return false;
            }
        },
        SEETHROUGH {
            @Override
            public boolean doesBlockCollision() {
                return true;
            }
            
            @Override
            public boolean doesBlockLight() {
                return false;
            }
            
            @Override
            public boolean isFilled() {
                return true;
            }
        },
        NOCLIP {
            @Override
            public boolean doesBlockCollision() {
                return false;
            }
            
            @Override
            public boolean doesBlockLight() {
                return true;
            }
            
            @Override
            public boolean isFilled() {
                return true;
            }
        },
        SEETHROUGH_NOCLIP {
            @Override
            public boolean doesBlockCollision() {
                return false;
            }
            
            @Override
            public boolean doesBlockLight() {
                return false;
            }
            
            @Override
            public boolean isFilled() {
                return true;
            }
        },
        SOLID {
            @Override
            public boolean doesBlockCollision() {
                return true;
            }
            
            @Override
            public boolean doesBlockLight() {
                return true;
            }
            
            @Override
            public boolean isFilled() {
                return true;
            }
        };
        
        public abstract boolean isFilled();
        
        public abstract boolean doesBlockCollision();
        
        public abstract boolean doesBlockLight();
        
        public static SideState getState(boolean empty, boolean noclip, boolean translucent) {
            if (empty)
                return EMPTY;
            if (noclip && translucent)
                return SEETHROUGH_NOCLIP;
            if (noclip)
                return NOCLIP;
            if (translucent)
                return SideState.SEETHROUGH;
            return SOLID;
        }
    }
    
    public class SideSolidCache {
        
        SideState EAST;
        SideState WEST;
        SideState UP;
        SideState DOWN;
        SideState SOUTH;
        SideState NORTH;
        
        SideState YAXIS;
        
        public SideSolidCache() {}
        
        public void load(CompoundTag nbt) {
            EAST = nbt.contains("east") ? SideState.values()[nbt.getInt("east")] : null;
            WEST = nbt.contains("west") ? SideState.values()[nbt.getInt("west")] : null;
            UP = nbt.contains("up") ? SideState.values()[nbt.getInt("up")] : null;
            DOWN = nbt.contains("down") ? SideState.values()[nbt.getInt("down")] : null;
            SOUTH = nbt.contains("south") ? SideState.values()[nbt.getInt("south")] : null;
            NORTH = nbt.contains("north") ? SideState.values()[nbt.getInt("north")] : null;
            YAXIS = nbt.contains("y_axis") ? SideState.values()[nbt.getInt("y_axis")] : null;
        }
        
        public void write(CompoundTag nbt) {
            if (EAST != null)
                nbt.putInt("east", EAST.ordinal());
            if (WEST != null)
                nbt.putInt("west", WEST.ordinal());
            if (UP != null)
                nbt.putInt("up", UP.ordinal());
            if (DOWN != null)
                nbt.putInt("down", DOWN.ordinal());
            if (SOUTH != null)
                nbt.putInt("south", SOUTH.ordinal());
            if (NORTH != null)
                nbt.putInt("north", NORTH.ordinal());
            if (YAXIS != null)
                nbt.putInt("y_axis", YAXIS.ordinal());
        }
        
        public void reset() {
            DOWN = null;
            UP = null;
            NORTH = null;
            SOUTH = null;
            WEST = null;
            EAST = null;
            YAXIS = null;
        }
        
        public SideState getYAxis() {
            if (YAXIS != null)
                return YAXIS;
            
            LittleBox box = new LittleBox(0, 0, 0, grid.count, grid.count, grid.count);
            boolean[][] filled = new boolean[grid.count][grid.count];
            
            boolean translucent = false;
            boolean noclip = false;
            
            for (Pair<IParentCollection, LittleTile> pair : BETiles.this.tiles.allTiles())
                if (pair.value.fillInSpaceInaccurate(box, Axis.X, Axis.Z, Axis.Y, filled)) {
                    if (!pair.value.doesProvideSolidFace())
                        translucent = true;
                    if (LittleStructureAttribute.noCollision(pair.key.getAttribute()) || pair.value.getBlock().noCollision())
                        noclip = true;
                }
            
            for (int one = 0; one < filled.length; one++)
                for (int two = 0; two < filled[one].length; two++)
                    if (!filled[one][two])
                        return SideState.EMPTY;
                    
            YAXIS = SideState.getState(false, noclip, translucent);
            return YAXIS;
        }
        
        protected SideState calculate(Facing facing) {
            LittleBox box = switch (facing) {
                case EAST -> new LittleBox(grid.count - 1, 0, 0, grid.count, grid.count, grid.count);
                case WEST -> new LittleBox(0, 0, 0, 1, grid.count, grid.count);
                case UP -> new LittleBox(0, grid.count - 1, 0, grid.count, grid.count, grid.count);
                case DOWN -> new LittleBox(0, 0, 0, grid.count, 1, grid.count);
                case SOUTH -> new LittleBox(0, 0, grid.count - 1, grid.count, grid.count, grid.count);
                case NORTH -> new LittleBox(0, 0, 0, grid.count, grid.count, 1);
                default -> null;
            };
            return calculateState(facing, box);
        }
        
        protected SideState calculateState(Facing facing, LittleBox box) {
            LittleVec size = box.getSize();
            boolean[][][] filled = new boolean[size.x][size.y][size.z];
            
            boolean translucent = false;
            boolean noclip = false;
            
            for (Pair<IParentCollection, LittleTile> pair : BETiles.this.tiles.allTiles())
                if (pair.value.fillInSpaceInaccurate(box, filled)) {
                    if (!pair.value.doesProvideSolidFace())
                        translucent = true;
                    if (LittleStructureAttribute.noCollision(pair.key.getAttribute()) || pair.value.getBlock().noCollision())
                        noclip = true;
                }
            
            for (int x = 0; x < filled.length; x++)
                for (int y = 0; y < filled[x].length; y++)
                    for (int z = 0; z < filled[x][y].length; z++)
                        if (!filled[x][y][z])
                            return SideState.EMPTY;
                        
            return SideState.getState(false, noclip, translucent);
        }
        
        public SideState get(Facing facing) {
            SideState result = switch (facing) {
                case DOWN -> DOWN;
                case UP -> UP;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
                default -> SideState.EMPTY;
            };
            
            if (result == null)
                set(facing, result = calculate(facing));
            
            return result;
        }
        
        public void set(Facing facing, SideState value) {
            switch (facing) {
                case DOWN:
                    DOWN = value;
                    break;
                case UP:
                    UP = value;
                    break;
                case NORTH:
                    NORTH = value;
                    break;
                case SOUTH:
                    SOUTH = value;
                    break;
                case WEST:
                    WEST = value;
                    break;
                case EAST:
                    EAST = value;
                    break;
            }
        }
        
        public boolean isCollisionFullBlock() {
            for (int i = 0; i < Facing.VALUES.length; i++)
                if (!get(Facing.VALUES[i]).isFilled())
                    return false;
            return true;
        }
        
    }
}
