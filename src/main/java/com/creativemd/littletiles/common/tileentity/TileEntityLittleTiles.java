package com.creativemd.littletiles.common.tileentity;

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

import com.creativemd.creativecore.common.tileentity.TileEntityCreative;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.client.render.world.TileEntityRenderManager;
import com.creativemd.littletiles.common.api.te.ILittleTileTE;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.tile.combine.BasicCombiner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxReturnedVolume;
import com.creativemd.littletiles.common.tile.math.box.face.LittleBoxFace;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.parent.ParentTileList;
import com.creativemd.littletiles.common.tile.parent.StructureTileList;
import com.creativemd.littletiles.common.tile.parent.TileList;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.outdated.identifier.LittleIdentifierRelative;
import com.creativemd.littletiles.common.util.vec.LittleBlockTransformer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityLittleTiles extends TileEntityCreative implements ILittleTileTE, IGridBased {
    
    protected final TileEntityInteractor interactor = new TileEntityInteractor();
    protected TileList tiles;
    private boolean unloaded = false;
    private boolean preventUnload = true;
    protected LittleGridContext context = LittleGridContext.getMin();
    
    private boolean hasLoaded = false;
    
    public final SideSolidCache sideCache = new SideSolidCache();
    
    @SideOnly(Side.CLIENT)
    public TileEntityRenderManager render;
    
    protected void assign(TileEntityLittleTiles te) {
        try {
            for (Field field : TileEntityLittleTiles.class.getDeclaredFields())
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()))
                    field.set(this, field.get(te));
            setWorld(te.getWorld());
            tiles.te = this;
            if (isClientSide())
                render.setTe(this);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    private void init() {
        tiles = new TileList(this, isClientSide());
        if (isClientSide())
            initClient();
    }
    
    @SideOnly(Side.CLIENT)
    private void initClient() {
        this.render = new TileEntityRenderManager(this);
    }
    
    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        if (tiles == null)
            init();
    }
    
    @Override
    protected void setWorldCreate(World worldIn) {
        super.setWorldCreate(worldIn);
        if (tiles == null)
            init();
    }
    
    public Iterable<LittleStructure> ticking() {
        return tiles.loadedStructures(LittleStructureAttribute.TICKING);
    }
    
    @SideOnly(Side.CLIENT)
    public Iterable<LittleStructure> rendering() {
        return tiles.loadedStructures(LittleStructureAttribute.TICK_RENDERING);
    }
    
    @Override
    public LittleGridContext getContext() {
        return context;
    }
    
    @Override
    public void convertToSmallest() {
        int size = LittleGridContext.minSize;
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles())
            size = Math.max(size, pair.value.getSmallestContext(context));
        
        if (size < context.size)
            convertTo(LittleGridContext.get(size));
    }
    
    @Override
    public void convertTo(LittleGridContext newContext) {
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles())
            pair.value.convertTo(context, newContext);
        
        this.context = newContext;
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
    
    @SideOnly(Side.CLIENT)
    public void updateQuadCache(Object chunk) {
        if (tiles == null)
            return;
        render.chunkUpdate(chunk);
    }
    
    public void updateLighting() {
        world.checkLight(getPos());
    }
    
    public TileEntityLittleTiles forceSupportAttribute(int attribute) {
        boolean rendered = tiles.hasRendered() | LittleStructureAttribute.tickRendering(attribute);
        boolean ticking = tiles.hasTicking() | LittleStructureAttribute.ticking(attribute);
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
            world.setBlockState(pos, BlockTile.getState(ticking, rendered), 20);
            world.setTileEntity(pos, newTe);
            invalidate();
            preventUnload = true;
            return newTe;
        }
        return this;
    }
    
    protected void customTilesUpdate() {
        if (world.isRemote)
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
            invalidate();
            preventUnload = true;
        }
    }
    
    public void onNeighbourChanged() {
        if (isClientSide())
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
        
        if (world != null) {
            updateBlock();
            if (updateNeighbour)
                updateNeighbour();
            updateLighting();
        }
        
        if (isClientSide())
            render.tilesChanged();
        
        if (!world.isRemote && tiles.isCompletelyEmpty())
            world.setBlockToAir(getPos());
        
        if (world instanceof CreativeWorld)
            ((CreativeWorld) world).hasChanged = true;
        
        customTilesUpdate();
    }
    
    public void updateTiles(Consumer<TileEntityInteractor> action) {
        action.accept(interactor);
        updateTiles();
    }
    
    /** Block will not update */
    public void updateTilesSecretly(Consumer<TileEntityInteractor> action) {
        action.accept(interactor);
    }
    
    /** Tries to convert the TileEntity to a vanilla block
     * 
     * @return whether it could convert it or not */
    public boolean convertBlockToVanilla() {
        LittleTile firstTile = null;
        if (tiles.isCompletelyEmpty()) {
            world.setBlockToAir(pos);
            return true;
        }
        
        if (world instanceof IOrientatedWorld || tiles.countStructures() > 0)
            return false;
        
        if (tiles.size() == 1) {
            if (!tiles.first().canBeConvertedToVanilla() || !tiles.first().doesFillEntireBlock(context))
                return false;
            firstTile = tiles.first();
        } else {
            boolean[][][] filled = new boolean[context.size][context.size][context.size];
            for (LittleTile tile : tiles) {
                if (firstTile == null) {
                    if (!tile.canBeConvertedToVanilla())
                        return false;
                    
                    firstTile = tile;
                } else if (!firstTile.canBeCombined(tile) || !tile.canBeCombined(firstTile))
                    return false;
                
                tile.fillInSpace(filled);
            }
            
            for (int x = 0; x < filled.length; x++)
                for (int y = 0; y < filled[x].length; y++)
                    for (int z = 0; z < filled[x][y].length; z++)
                        if (!filled[x][y][z])
                            return false;
        }
        
        world.setBlockState(pos, firstTile.getBlockState());
        
        return true;
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
        world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
    }
    
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 && tiles != null && tiles.hasRendered();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return render.getMaxRenderDistanceSquared();
    }
    
    @Override
    public boolean hasFastRenderer() {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (!hasLoaded())
            return super.getRenderBoundingBox();
        return render.getRenderBoundingBox();
    }
    
    public AxisAlignedBB getSelectionBox() {
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
    @SideOnly(Side.CLIENT)
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
            HashMap<LittleIdentifierRelative, StructureTileList> structures = new HashMap<>();
            for (int i = 0; i < count; i++) {
                NBTTagCompound tileNBT = new NBTTagCompound();
                tileNBT = nbt.getCompoundTag("t" + i);
                sortOldTiles(tileNBT, structures);
            }
            for (StructureTileList child : structures.values())
                tiles.addStructure(child.getIndex(), child);
        } else if (nbt.hasKey("tiles")) {
            NBTTagList list = nbt.getTagList("tiles", 10);
            HashMap<LittleIdentifierRelative, StructureTileList> structures = new HashMap<>();
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
            for (StructureTileList child : structures.values()) {
                StructureTileList.updateStatus(child);
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
    
    protected void sortOldTiles(NBTTagCompound nbt, HashMap<LittleIdentifierRelative, StructureTileList> structures) {
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
            StructureTileList structureList = structures.get(identifier);
            if (structureList == null) {
                structures.put(identifier, structureList = new StructureTileList(tiles, identifier.generateIndex(pos), attribute));
                StructureTileList.setRelativePos(structureList, identifier.coord);
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
    public void handleUpdate(NBTTagCompound nbt, boolean chunkUpdate) {
        if (isClientSide())
            render.beforeClientReceivesUpdate();
        
        readFromNBT(nbt);
        if (!chunkUpdate)
            updateTiles(false);
        
        if (isClientSide())
            render.afterClientReceivesUpdate();
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
    
    public Pair<IParentTileList, LittleTile> getFocusedTile(EntityPlayer player, float partialTickTime) {
        if (!isClientSide())
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
    
    public Pair<IParentTileList, LittleTile> getFocusedTile(Vec3d pos, Vec3d look) {
        IParentTileList parent = null;
        LittleTile tileFocus = null;
        RayTraceResult hit = null;
        double distance = 0;
        for (Pair<IParentTileList, LittleTile> pair : tiles.allTiles()) {
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
        boolean changed = BasicCombiner.combine((StructureTileList) getStructure(structureIndex));
        convertToSmallest();
        if (changed)
            updateTiles();
        return changed;
    }
    
    public boolean combineTilesSecretly(int structureIndex) {
        if (getStructure(structureIndex) == null)
            return false;
        boolean changed = BasicCombiner.combine((StructureTileList) getStructure(structureIndex));
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
    
    @SideOnly(Side.CLIENT)
    public boolean isRenderingEmpty() {
        return tiles.isCompletelyEmpty() && !render.hasAdditional();
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
    
    public boolean unloaded() {
        return unloaded;
    }
    
    @Override
    public void onChunkUnload() {
        unloaded = true;
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
    
    public Iterable<IParentTileList> groups() {
        return tiles.groups();
    }
    
    public IParentTileList noneStructureTiles() {
        return tiles;
    }
    
    public Iterable<Pair<IParentTileList, LittleTile>> allTiles() {
        return tiles.allTiles();
    }
    
    public IStructureTileList getStructure(int index) {
        return tiles.getStructure(index);
    }
    
    public Iterable<LittleStructure> loadedStructures() {
        return tiles.loadedStructures();
    }
    
    public Iterable<LittleStructure> loadedStructures(int attribute) {
        return tiles.loadedStructures(attribute);
    }
    
    public Iterable<IStructureTileList> structures() {
        return tiles.structures();
    }
    
    public void fillUsedIds(BitSet usedIds) {
        tiles.fillUsedIds(usedIds);
    }
    
    public class TileEntityInteractor {
        
        public Iterable<ParentTileList> groups() {
            return new Iterable<ParentTileList>() {
                
                @Override
                public Iterator<ParentTileList> iterator() {
                    return new Iterator<ParentTileList>() {
                        
                        ParentTileList current = tiles;
                        Iterator<StructureTileList> children = structures().iterator();
                        
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
                        public ParentTileList next() {
                            ParentTileList result = current;
                            current = null;
                            return result;
                        }
                    };
                }
            };
        }
        
        public ParentTileList get(IParentTileList list) {
            return (ParentTileList) list;
        }
        
        public StructureTileList get(IStructureTileList list) {
            return (StructureTileList) list;
        }
        
        public ParentTileList noneStructureTiles() {
            return tiles;
        }
        
        public Iterable<StructureTileList> structures() {
            return tiles.structuresReal();
        }
        
        public StructureTileList getStructure(int index) {
            return tiles.getStructure(index);
        }
        
        public boolean removeStructure(int index) {
            return tiles.removeStructure(index);
        }
        
        public StructureTileList addStructure(int index, int attribute) {
            return tiles.addStructure(index, attribute);
        }
        
        public void clearEverything() {
            tiles.clearEverything();
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
        
        protected SideState calculate(EnumFacing facing) {
            LittleBox box;
            switch (facing) {
            case EAST:
                box = new LittleBox(context.size - 1, 0, 0, context.size, context.size, context.size);
                break;
            case WEST:
                box = new LittleBox(0, 0, 0, 1, context.size, context.size);
                break;
            case UP:
                box = new LittleBox(0, context.size - 1, 0, context.size, context.size, context.size);
                break;
            case DOWN:
                box = new LittleBox(0, 0, 0, context.size, 1, context.size);
                break;
            case SOUTH:
                box = new LittleBox(0, 0, context.size - 1, context.size, context.size, context.size);
                break;
            case NORTH:
                box = new LittleBox(0, 0, 0, context.size, context.size, 1);
                break;
            default:
                box = null;
                break;
            }
            return calculateState(facing, box);
        }
        
        protected SideState calculateState(EnumFacing facing, LittleBox box) {
            LittleVec size = box.getSize();
            boolean[][][] filled = new boolean[size.x][size.y][size.z];
            
            boolean translucent = false;
            boolean noclip = false;
            
            for (Pair<IParentTileList, LittleTile> pair : TileEntityLittleTiles.this.tiles.allTiles())
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
        
        public SideState get(EnumFacing facing) {
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
        
        public void set(EnumFacing facing, SideState value) {
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
        
    }
    
}
