package team.creative.littletiles.common.block.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.AxisAlignedBB;
import com.creativemd.littletiles.common.tileentity.CreativeWorld;
import com.creativemd.littletiles.common.tileentity.EntityPlayer;
import com.creativemd.littletiles.common.tileentity.EnumFacing;
import com.creativemd.littletiles.common.tileentity.IBlockState;
import com.creativemd.littletiles.common.tileentity.IOrientatedWorld;
import com.creativemd.littletiles.common.tileentity.IParentTileList;
import com.creativemd.littletiles.common.tileentity.LittleGridContext;
import com.creativemd.littletiles.common.tileentity.LittleTilePosition;
import com.creativemd.littletiles.common.tileentity.Method;
import com.creativemd.littletiles.common.tileentity.NBTTagCompound;
import com.creativemd.littletiles.common.tileentity.NBTTagList;
import com.creativemd.littletiles.common.tileentity.NetworkManager;
import com.creativemd.littletiles.common.tileentity.RayTraceResult;
import com.creativemd.littletiles.common.tileentity.SPacketUpdateTileEntity;
import com.creativemd.littletiles.common.tileentity.SideOnly;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles.SideState;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles.TileEntityInteractor;
import com.creativemd.littletiles.common.tileentity.TileList;
import com.creativemd.littletiles.common.util.outdated.identifier.LittleIdentifierRelative;
import com.creativemd.littletiles.common.util.vec.LittleBlockTransformer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.block.TileEntityRenderManager;
import team.creative.littletiles.common.api.block.ILittleBlockEntity;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.BasicCombiner;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.face.LittleBoxFace;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.LittleTileContext;
import team.creative.littletiles.common.tile.parent.BlockParentCollection;
import team.creative.littletiles.common.tile.parent.IParentCollection;
import team.creative.littletiles.common.tile.parent.IStructureCollection;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.tile.parent.ParentCollection;
import team.creative.littletiles.common.tile.parent.StructureParentCollection;

public class BETiles extends BlockEntity implements IGridBased, ILittleBlockEntity {
    
    protected final BlockEntityInteractor interactor = new BlockEntityInteractor();
    private LittleGrid grid;
    private BlockParentCollection tiles;
    public final SideSolidCache sideCache = new SideSolidCache();
    
    @OnlyIn(Dist.CLIENT)
    public BERenderManager render;
    
    public BETiles(BlockEntityType<? extends BETiles> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    public BETiles(BlockPos pos, BlockState state) {
        super(LittleTiles.BE_TILES_TYPE, pos, state);
    }
    
    protected void assign(BETiles te) {
        try {
            for (Field field : BETiles.class.getDeclaredFields())
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))
                    field.set(this, field.get(te));
            setWorld(te.getWorld());
            tiles.te = this;
            if (isClient())
                render.setTe(this);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    private boolean isClient() {
        if (level != null)
            return level.isClientSide;
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
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            pair.value.convertTo(grid, to);
        
        this.grid = to;
    }
    
    @Override
    public int getSmallest() {
        int size = LittleGrid.min().count;
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles())
            size = Math.max(size, pair.value.getSmallest(grid));
        return size;
    }
    
    public static void tickUpdate() {
        if (!tiles.hasTicking() && !world.isRemote) {
            customTilesUpdate();
            System.out.println("Ticking tileentity which shouldn't " + pos);
            return;
        }
        
        tick();
    }
    
    public Iterable<LittleStructure> ticking() {
        return tiles.loadedStructures(LittleStructureAttribute.TICKING);
    }
    
    @OnlyIn(Dist.CLIENT)
    public Iterable<LittleStructure> rendering() {
        return tiles.loadedStructures(LittleStructureAttribute.TICK_RENDERING);
    }
    
    public boolean contains(LittleTile tile) {
        return tiles.contains(tile);
    }
    
    public int tilesCount() {
        return tiles.size();
    }
    
    public boolean hasLoaded() {
        return hasLoaded && world != null && tiles != null;
    }
    
    public void setLoaded() {
        hasLoaded = true;
    }
    
    public boolean shouldCheckForCollision() {
        return tiles.hasCollisionListener();
    }
    
    @OnlyIn(Dist.CLIENT)
    public void updateQuadCache(Object chunk) {
        if (tiles == null)
            return;
        render.chunkUpdate(chunk);
    }
    
    public void updateLighting() {
        level.getLightEngine().checkBlock(getBlockPos());
    }
    
    public BETiles forceSupportAttribute(int attribute) {
        boolean rendered = tiles.hasRendered() | LittleStructureAttribute.tickRendering(attribute);
        boolean ticking = tiles.hasTicking() | LittleStructureAttribute.ticking(attribute);
        if (ticking != isTicking() || rendered != isRendered()) {
            BETiles newTe;
            if (rendered)
                if (ticking)
                    newTe = new TileEntityLittleTilesTickingRendered();
                else
                    newTe = new TileEntityLittleTilesRendered();
            else if (ticking)
                newTe = new TileEntityLittleTilesTicking();
            else
                newTe = new TileEntityLittleTiles();
            
            newTe.assign(this);
            newTe.tiles.te = newTe;
            
            preventUnload = true;
            world.setBlockState(pos, BlockTile.getState(ticking, rendered), 20);
            world.setTileEntity(pos, newTe);
            preventUnload = true;
            return newTe;
        }
        return this;
    }
    
    protected void customTilesUpdate() {
        if (level.isClientSide)
            return;
        boolean rendered = tiles.hasRendered();
        boolean ticking = tiles.hasTicking();
        if (ticking != isTicking() || rendered != isRendered()) {
            TileEntityLittleTiles newTe;
            if (rendered)
                if (ticking)
                    newTe = new TileEntityLittleTilesTickingRendered();
                else
                    newTe = new TileEntityLittleTilesRendered();
            else if (ticking)
                newTe = new TileEntityLittleTilesTicking();
            else
                newTe = new TileEntityLittleTiles();
            
            newTe.assign(this);
            newTe.tiles.te = newTe;
            
            preventUnload = true;
            world.setBlockState(pos, BlockTile.getState(ticking, rendered), 2);
            world.setTileEntity(pos, newTe);
            preventUnload = true;
        }
    }
    
    public void onNeighbourChanged() {
        if (isClient())
            render.neighborChanged();
        
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
        
        if (level != null) {
            updateBlock();
            if (updateNeighbour)
                updateNeighbour();
            updateLighting();
        }
        
        if (isClient())
            render.tilesChanged();
        
        if (!level.isClientSide && tiles.isCompletelyEmpty())
            level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
        
        if (level instanceof CreativeLevel)
            ((CreativeLevel) level).hasChanged = true;
        
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
            level.setBlockAndUpdate(getBlockPos(), firstTile.block.getState());
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
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 && tiles != null && tiles.hasRendered();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return render.getMaxRenderDistanceSquared();
    }
    
    @Override
    public boolean hasFastRenderer() {
        return false;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        if (!hasLoaded())
            return super.getRenderBoundingBox();
        return render.getRenderBoundingBox();
    }
    
    public VoxelShape getBlockShape() {
        int minX = context.size;
        int minY = context.size;
        int minZ = context.size;
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
            LittleBox box = pair.value.getCompleteBox();
            minX = Math.min(box.minX, minX);
            minY = Math.min(box.minY, minY);
            minZ = Math.min(box.minZ, minZ);
            maxX = Math.max(box.maxX, maxX);
            maxY = Math.max(box.maxY, maxY);
            maxZ = Math.max(box.maxZ, maxZ);
        }
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ).getBox(context, pos);
    }
    
    /** Used for rendering */
    @OnlyIn(Dist.CLIENT)
    public boolean shouldSideBeRendered(EnumFacing facing, LittleBoxFace face, LittleTile rendered) {
        face.ensureContext(context);
        
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
            if (pair.key.isStructure() && LittleStructureAttribute.noCollision(pair.key.getAttribute()))
                continue;
            if (pair.value != rendered && (pair.value.doesProvideSolidFace(facing) || pair.value.canBeRenderCombined(rendered)))
                pair.value.fillFace(face, context);
        }
        
        return !face.isFilled(rendered.isTranslucent());
    }
    
    /** @param box
     * @param cutout
     *            filled with all boxes which are cutout by tiles
     * @return all boxes which are not cutout by other tiles */
    public List<LittleBox> cutOut(LittleBox box, List<LittleBox> cutout, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> cutting = new ArrayList<>();
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles())
            pair.value.getIntersectingBox(box, cutting);
        return box.cutOut(cutting, cutout, volume);
    }
    
    public Pair<IParentTileList, LittleTile> intersectingTile(LittleBox box) {
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles())
            if (pair.value.intersectsWith(box))
                return pair;
        return null;
    }
    
    public boolean isSpaceForLittleTile(LittleBox box, BiPredicate<IParentTileList, LittleTile> predicate) {
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
            if (predicate != null && !predicate.test(pair.key, pair.value))
                continue;
            if (pair.value.intersectsWith(box))
                return false;
            
        }
        return true;
    }
    
    public boolean isSpaceForLittleTile(LittleBox box, Predicate<LittleTile> predicate) {
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
            if (predicate != null && !predicate.test(pair.value))
                continue;
            if (pair.value.intersectsWith(box))
                return false;
            
        }
        return true;
    }
    
    public boolean isSpaceForLittleTile(LittleBox box) {
        return isSpaceForLittleTile(box, (Predicate<LittleTile>) null);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        
        if (tiles == null)
            init();
        
        if (!tiles.isCompletelyEmpty())
            tiles.clearEverything();
        context = LittleGridContext.get(nbt);
        
        if (nbt.hasKey("tilesCount")) {
            int count = nbt.getInteger("tilesCount");
            HashMap<LittleIdentifierRelative, StructureParentCollection> structures = new HashMap<>();
            for (int i = 0; i < count; i++) {
                NBTTagCompound tileNBT = new NBTTagCompound();
                tileNBT = nbt.getCompoundTag("t" + i);
                sortOldTiles(tileNBT, structures);
            }
            for (StructureParentCollection child : structures.values())
                tiles.addStructure(child.getIndex(), child);
        } else if (nbt.hasKey("tiles")) {
            NBTTagList list = nbt.getTagList("tiles", 10);
            HashMap<LittleIdentifierRelative, StructureParentCollection> structures = new HashMap<>();
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound ltNBT = list.getCompoundTagAt(i);
                if (ltNBT.hasKey("boxes")) {
                    LittleTile create = LittleTileRegistry.getTypeFromNBT(ltNBT).createTile();
                    if (create != null) {
                        List<NBTTagCompound> nbts = create.extractNBTFromGroup(ltNBT);
                        for (int j = 0; j < nbts.size(); j++)
                            sortOldTiles(nbts.get(j), structures);
                    }
                } else
                    sortOldTiles(ltNBT, structures);
            }
            for (StructureParentCollection child : structures.values()) {
                StructureParentCollection.updateStatus(child);
                tiles.addStructure(child.getIndex(), child);
            }
            
        } else
            tiles.read(nbt.getCompoundTag("content"));
        
        if (world != null && !world.isRemote) {
            updateBlock();
            customTilesUpdate();
        }
        
        deleteTempWorld();
    }
    
    protected int[] getIdentifier(LittleBox box) {
        return new int[] { box.minX, box.minY, box.minZ };
    }
    
    protected void sortOldTiles(NBTTagCompound nbt, HashMap<LittleIdentifierRelative, StructureParentCollection> structures) {
        LittleTile tile = LittleTileRegistry.loadTile(nbt);
        
        LittleIdentifierRelative identifier = null;
        NBTTagCompound structureNBT = null;
        int attribute = LittleStructureAttribute.NONE;
        
        if (nbt.hasKey("structure", 10)) {
            NBTTagCompound temp = nbt.getCompoundTag("structure");
            if (temp.getBoolean("main")) {
                structureNBT = temp;
                identifier = new LittleIdentifierRelative(0, 0, 0, context, getIdentifier(tile.getBox()));
                attribute = LittleStructureRegistry.getStructureType(temp.getString("id")).attribute;
            } else {
                identifier = new LittleIdentifierRelative(temp);
                if (temp.hasKey("attr"))
                    attribute = LittleStructureAttribute.loadOld(temp.getInteger("attr"));
                else
                    attribute = temp.getInteger("type");
            }
        } else { // Old
            if (nbt.getBoolean("isStructure")) {
                if (nbt.getBoolean("main")) {
                    structureNBT = nbt;
                    identifier = new LittleIdentifierRelative(0, 0, 0, context, getIdentifier(tile.getBox()));
                } else {
                    if (nbt.hasKey("coX")) {
                        LittleTilePosition pos = new LittleTilePosition(nbt);
                        identifier = new LittleIdentifierRelative(getPos().getX() - pos.coord.getX(), getPos().getY() - pos.coord.getY(), getPos().getZ() - pos.coord
                                .getZ(), context, new int[] { pos.position.x, pos.position.y, pos.position.z });
                        System.out.println("Converting old positioning to new relative coordinates " + pos + " to " + identifier);
                    } else
                        identifier = new LittleIdentifierRelative(nbt);
                }
            }
        }
        
        if (identifier == null)
            tiles.add(tile);
        else {
            StructureParentCollection structureList = structures.get(identifier);
            if (structureList == null) {
                structures.put(identifier, structureList = new StructureParentCollection(tiles, identifier.generateIndex(pos), attribute));
                StructureParentCollection.setRelativePos(structureList, identifier.coord);
            }
            if (structureNBT != null) {
                LittleStructure structure = structureList.setStructureNBT(structureNBT);
                LittleVec vec = tile.getMinVec();
                for (StructureDirectionalField field : structure.type.directional)
                    field.move(field.get(structure), context, vec);
            }
            structureList.add(tile);
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        context.set(nbt);
        nbt.setTag("content", tiles.write());
        return nbt;
    }
    
    @Override
    public void getDescriptionNBT(NBTTagCompound nbt) {
        writeToNBT(nbt);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdatePacket(net, pkt.getNbtCompound());
        super.onDataPacket(net, pkt);
    }
    
    public void handleUpdatePacket(NetworkManager net, NBTTagCompound nbt) {
        readFromNBT(nbt);
        updateTiles(false);
    }
    
    public RayTraceResult rayTrace(EntityPlayer player) {
        RayTraceResult hit = null;
        
        Vec3d pos = player.getPositionEyes(TickUtils.getPartialTickTime());
        double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
        Vec3d look = player.getLook(TickUtils.getPartialTickTime());
        Vec3d vec32 = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
        return rayTrace(pos, vec32);
    }
    
    public RayTraceResult rayTrace(Vec3d pos, Vec3d look) {
        RayTraceResult hit = null;
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
            RayTraceResult Temphit = pair.value.rayTrace(context, getPos(), pos, look);
            if (Temphit != null) {
                if (hit == null || hit.hitVec.distanceTo(pos) > Temphit.hitVec.distanceTo(pos)) {
                    hit = Temphit;
                }
            }
        }
        return hit;
    }
    
    public LittleTileContext getFocusedTile(Player player, float partialTickTime) {
        if (!isClient())
            return null;
        Vec3d pos = player.getPositionEyes(partialTickTime);
        double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
        Vec3d look = player.getLook(partialTickTime);
        Vec3d vec32 = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
        
        if (world != player.world && world instanceof CreativeWorld) {
            pos = ((CreativeWorld) world).getOrigin().transformPointToFakeWorld(pos);
            vec32 = ((CreativeWorld) world).getOrigin().transformPointToFakeWorld(vec32);
        }
        
        return getFocusedTile(pos, vec32);
    }
    
    public LittleTileContext getFocusedTile(Vec3d pos, Vec3d look) {
        IParentCollection parent = null;
        LittleTile tileFocus = null;
        HitResult hit = null;
        double distance = 0;
        for (Pair<IParentCollection, LittleTile> pair : tiles.allTiles()) {
            RayTraceResult Temphit = pair.value.rayTrace(context, getPos(), pos, look);
            if (Temphit != null) {
                if (hit == null || distance > Temphit.hitVec.distanceTo(pos)) {
                    distance = Temphit.hitVec.distanceTo(pos);
                    hit = Temphit;
                    parent = pair.key;
                    tileFocus = pair.value;
                }
            }
        }
        return new Pair(parent, tileFocus);
    }
    
    @Override
    public void onLoad() {
        setLoaded();
    }
    
    public boolean isTicking() {
        return false;
    }
    
    public boolean isRendered() {
        return false;
    }
    
    public IBlockState getBlockTileState() {
        return BlockTile.getState(this);
    }
    
    public boolean combineTiles(int structureIndex) {
        if (getStructure(structureIndex) == null)
            return false;
        boolean changed = BasicCombiner.combine((StructureParentCollection) getStructure(structureIndex));
        convertToSmallest();
        if (changed)
            updateTiles();
        return changed;
    }
    
    public boolean combineTilesSecretly(int structureIndex) {
        if (getStructure(structureIndex) == null)
            return false;
        boolean changed = BasicCombiner.combine((StructureParentCollection) getStructure(structureIndex));
        convertToSmallest();
        return changed;
    }
    
    public boolean combineTiles() {
        boolean changed = BasicCombiner.combine(tiles);
        
        convertToSmallest();
        if (changed)
            updateTiles();
        return changed;
    }
    
    public boolean combineTilesSecretly() {
        boolean changed = BasicCombiner.combine(tiles);
        convertToSmallest();
        return changed;
    }
    
    @Override
    @Method(modid = ChiselsAndBitsManager.chiselsandbitsID)
    public Object getVoxelBlob(boolean force) throws Exception {
        return ChiselsAndBitsManager.getVoxelBlob(this, force);
    }
    
    @Override
    @Nullable
    public IBlockState getState(AxisAlignedBB box, boolean realistic) {
        if (tiles == null)
            return null;
        
        if (realistic) {
            box = box.expand(0, -context.pixelSize, 0);
            for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
                LittleBox tileBox = pair.value.getCollisionBox();
                if (tileBox != null && tileBox.getBox(context, pos).intersects(box))
                    return pair.value.getBlockState();
            }
            return null;
        }
        box = box.expand(0, -1, 0);
        LittleTile highest = null;
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
            if (pair.value.getCollisionBox() == null)
                continue;
            if ((highest == null || (pair.value.getMaxY() > highest.getMaxY()) && pair.value.getCollisionBox().getBox(context, pos).intersects(box)))
                highest = pair.value;
            
        }
        return highest != null ? highest.getBlockState() : null;
    }
    
    public boolean isEmpty() {
        return tiles.isCompletelyEmpty();
    }
    
    @Override
    public void invalidate() {
        super.invalidate();
        if (!preventUnload)
            tiles.unload();
    }
    
    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        tiles.unload();
        if (world.isRemote) {
            tiles = null;
            render.chunkUnload();
        }
    }
    
    @Override
    public void rotate(Rotation rotationIn) {
        LittleBlockTransformer.rotateTE(this, RotationUtils.getRotation(rotationIn), RotationUtils.getRotationCount(rotationIn));
        updateTiles();
    }
    
    @Override
    public void mirror(Mirror mirrorIn) {
        LittleBlockTransformer.flipTE(this, RotationUtils.getMirrorAxis(mirrorIn));
        updateTiles();
    }
    
    @Override
    public String toString() {
        return pos.toString();
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
    
    public IStructureParentCollection getStructure(int index) {
        return tiles.getStructure(index);
    }
    
    public Iterable<LittleStructure> loadedStructures() {
        return tiles.loadedStructures();
    }
    
    public Iterable<LittleStructure> loadedStructures(int attribute) {
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
            return new Iterable<ParentCollection>() {
                
                @Override
                public Iterator<ParentCollection> iterator() {
                    return new Iterator<ParentCollection>() {
                        
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
            };
        }
        
        public ParentCollection get(IParentCollection list) {
            return (ParentCollection) list;
        }
        
        public StructureParentCollection get(IStructureCollection list) {
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
        
        public void clearEverything() {
            tiles.clearEverything();
        }
        
    }
    
    public class SideSolidCache {
        
        SideState DOWN;
        SideState UP;
        SideState NORTH;
        SideState SOUTH;
        SideState WEST;
        SideState EAST;
        
        public void reset() {
            DOWN = null;
            UP = null;
            NORTH = null;
            SOUTH = null;
            WEST = null;
            EAST = null;
        }
        
        protected SideState calculate(Facing facing) {
            LittleBox box;
            switch (facing) {
            case EAST:
                box = new LittleBox(grid.count - 1, 0, 0, grid.count, grid.count, grid.count);
                break;
            case WEST:
                box = new LittleBox(0, 0, 0, 1, grid.count, grid.count);
                break;
            case UP:
                box = new LittleBox(0, grid.count - 1, 0, grid.count, grid.count, grid.count);
                break;
            case DOWN:
                box = new LittleBox(0, 0, 0, grid.count, 1, grid.count);
                break;
            case SOUTH:
                box = new LittleBox(0, 0, grid.count - 1, grid.count, grid.count, grid.count);
                break;
            case NORTH:
                box = new LittleBox(0, 0, 0, grid.count, grid.count, 1);
                break;
            default:
                box = null;
                break;
            }
            return calculateState(facing, box);
        }
        
        protected SideState calculateState(Facing facing, LittleBox box) {
            LittleVec size = box.getSize();
            boolean[][][] filled = new boolean[size.x][size.y][size.z];
            
            boolean translucent = false;
            boolean noclip = false;
            
            for (Pair<IParentCollection, LittleTile> pair : BETiles.this.tiles.allTiles())
                if (pair.value.fillInSpaceInaccurate(box, filled)) {
                    if (!pair.value.doesProvideSolidFace(facing))
                        translucent = true;
                    if (LittleStructureAttribute.noCollision(pair.key.getAttribute()) || pair.value.hasNoCollision())
                        noclip = true;
                }
            
            for (int x = 0; x < filled.length; x++) {
                for (int y = 0; y < filled[x].length; y++) {
                    for (int z = 0; z < filled[x][y].length; z++) {
                        if (!filled[x][y][z])
                            return SideState.EMPTY;
                    }
                }
            }
            return SideState.getState(false, noclip, translucent);
        }
        
        public SideState get(Facing facing) {
            SideState result;
            
            switch (facing) {
            case DOWN:
                result = DOWN;
                break;
            case UP:
                result = UP;
                break;
            case NORTH:
                result = NORTH;
                break;
            case SOUTH:
                result = SOUTH;
                break;
            case WEST:
                result = WEST;
                break;
            case EAST:
                result = EAST;
                break;
            default:
                result = SideState.EMPTY;
            }
            
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
            adas
            // TODO Auto-generated method stub
            return false;
        }
        
    }
}
