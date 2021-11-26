package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.mutable.MutableInt;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.LittleTilesConfig;
import com.creativemd.littletiles.LittleTilesConfig.NotAllowedToPlaceException;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxReturnedVolume;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.parent.ParentTileList;
import com.creativemd.littletiles.common.tile.parent.StructureTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviewsStructureHolder;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.world.LittleNeighborUpdateCollector;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;

public class Placement {
    
    public final EntityPlayer player;
    public final World world;
    public final PlacementMode mode;
    public final BlockPos pos;
    public final EnumFacing facing;
    public final LittlePreviews previews;
    public final LinkedHashMap<BlockPos, PlacementBlock> blocks = new LinkedHashMap<>();
    public final PlacementStructurePreview origin;
    public final List<PlacementStructurePreview> structures = new ArrayList<>();
    
    public final BitSet availableIds = new BitSet();
    
    public final LittleIngredients removedIngredients;
    public final LittleAbsolutePreviews removedTiles;
    public final LittleAbsolutePreviews unplaceableTiles;
    public final List<SoundType> soundsToBePlayed = new ArrayList<>();
    
    protected MutableInt affectedBlocks = new MutableInt();
    protected ItemStack stack;
    protected boolean ignoreWorldBoundaries = true;
    protected BiPredicate<IParentTileList, LittleTile> predicate;
    protected boolean playSounds = true;
    
    public Placement(EntityPlayer player, PlacementPreview preview) {
        this.player = player;
        this.world = preview.world;
        this.mode = preview.mode;
        this.pos = preview.pos;
        this.facing = preview.facing;
        this.previews = preview.previews;
        this.origin = createStructureTree(null, preview.previews);
        
        this.removedIngredients = new LittleIngredients();
        this.removedTiles = new LittleAbsolutePreviews(pos, LittleGridContext.getMin());
        this.unplaceableTiles = new LittleAbsolutePreviews(pos, LittleGridContext.getMin());
        
        createPreviews(origin, preview.inBlockOffset, preview.pos);
        
        for (PlacementBlock block : blocks.values())
            block.convertToSmallest();
    }
    
    public Placement setPlaySounds(boolean sounds) {
        this.playSounds = sounds;
        return this;
    }
    
    public Placement setIgnoreWorldBoundaries(boolean value) {
        this.ignoreWorldBoundaries = value;
        return this;
    }
    
    public Placement setPredicate(BiPredicate<IParentTileList, LittleTile> predicate) {
        this.predicate = predicate;
        return this;
    }
    
    public Placement setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }
    
    public void addRemovedIngredient(PlacementBlock block, LittleTile tile, LittleBoxReturnedVolume volume) {
        removedIngredients.add(LittleIngredient.extract(tile.getPreviewTile(), volume.getPercentVolume(block.getContext())));
    }
    
    public LittleIngredients overflow() {
        LittleIngredients ingredients = LittleAction.getIngredients(removedTiles);
        ingredients.add(this.removedIngredients);
        return ingredients;
    }
    
    public boolean canPlace() throws LittleActionException {
        affectedBlocks.setValue(0);
        
        for (BlockPos pos : blocks.keySet()) {
            if (!LittleAction.isAllowedToInteract(world, player, pos, true, EnumFacing.EAST)) {
                LittleAction.sendBlockResetToClient(world, player, pos);
                return false;
            }
        }
        
        List<BlockPos> coordsToCheck = mode.getCoordsToCheck(blocks.keySet(), pos);
        if (coordsToCheck != null) {
            for (BlockPos pos : coordsToCheck) {
                PlacementBlock block = blocks.get(pos);
                
                if (block == null)
                    continue;
                
                if (!block.canPlace())
                    return false;
            }
        }
        return true;
    }
    
    public PlacementResult place() throws LittleActionException {
        if (blocks.isEmpty())
            return null;
        
        if (player != null && !world.isRemote) {
            if (player != null) {
                if (LittleTiles.CONFIG.isPlaceLimited(player) && previews.getVolumeIncludingChildren() > LittleTiles.CONFIG.build.get(player).maxPlaceBlocks) {
                    for (BlockPos pos : blocks.keySet())
                        LittleAction.sendBlockResetToClient(world, player, pos);
                    throw new NotAllowedToPlaceException(player);
                }
                
                if (LittleTiles.CONFIG.isTransparencyRestricted(player))
                    for (LittlePreview preview : previews) {
                        try {
                            LittleAction.isAllowedToPlacePreview(player, preview);
                        } catch (LittleActionException e) {
                            for (BlockPos pos : blocks.keySet())
                                LittleAction.sendBlockResetToClient(world, player, pos);
                            throw e;
                        }
                    }
            }
            
            affectedBlocks.setValue(0);
            
            List<BlockSnapshot> snaps = new ArrayList<>();
            for (BlockPos snapPos : blocks.keySet())
                snaps.add(new BlockSnapshot(world, snapPos, BlockTile.getState(false, false)));
            
            MultiPlaceEvent event = new MultiPlaceEvent(snaps, world.getBlockState(facing == null ? pos : pos.offset(facing)), player, EnumHand.MAIN_HAND);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                for (BlockPos snapPos : blocks.keySet())
                    LittleAction.sendBlockResetToClient(world, player, pos);
                return null;
            }
        }
        try {
            if (canPlace())
                return placeTiles();
        } catch (LittleActionException e) {
            for (BlockPos snapPos : blocks.keySet())
                LittleAction.sendBlockResetToClient(world, player, pos);
            throw e;
        }
        return null;
    }
    
    public PlacementResult tryPlace() {
        try {
            return place();
        } catch (LittleActionException e) {
            return null;
        }
    }
    
    protected PlacementResult placeTiles() throws LittleActionException {
        PlacementResult result = new PlacementResult(pos);
        
        for (PlacementBlock block : blocks.values())
            block.place(result);
        
        result.parentStructure = origin.isStructure() ? origin.getStructure() : null;
        
        LittleNeighborUpdateCollector neighbor = new LittleNeighborUpdateCollector(world, blocks.keySet());
        
        for (Iterator iterator = blocks.values().iterator(); iterator.hasNext();) {
            PlacementBlock block = (PlacementBlock) iterator.next();
            if (block.combineTilesSecretly()) {
                result.tileEntities.remove(block.cached);
                iterator.remove();
            }
        }
        
        for (PlacementBlock block : blocks.values())
            block.placeLate();
        
        if (origin.isStructure()) {
            if (origin.getStructure() == null)
                throw new LittleActionException("Missing missing mainblock of structure. Placed " + result.placedPreviews.size() + " tile(s).");
            notifyStructurePlaced();
        }
        
        constructStructureRelations();
        
        if (origin.isStructure())
            origin.getStructure().notifyAfterPlaced();
        
        neighbor.process();
        
        if (playSounds)
            for (int i = 0; i < soundsToBePlayed.size(); i++)
                world.playSound((EntityPlayer) null, pos, soundsToBePlayed.get(i)
                    .getPlaceSound(), SoundCategory.BLOCKS, (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
            
        removedTiles.convertToSmallest();
        unplaceableTiles.convertToSmallest();
        return result;
    }
    
    public void notifyStructurePlaced() {
        origin.getStructure().placedStructure(stack);
    }
    
    public void constructStructureRelations() {
        updateRelations(origin);
    }
    
    private void updateRelations(PlacementStructurePreview preview) {
        for (int i = 0; i < preview.children.size(); i++) {
            PlacementStructurePreview child = preview.children.get(i);
            if (preview.getStructure() != null && child.getStructure() != null) {
                preview.getStructure().updateChildConnection(i, child.getStructure(), child.dynamic);
                child.getStructure().updateParentConnection(i, preview.getStructure(), child.dynamic);
            }
            
            updateRelations(child);
        }
    }
    
    public PlacementBlock getOrCreateBlock(BlockPos pos) {
        PlacementBlock block = blocks.get(pos);
        if (block == null) {
            block = new PlacementBlock(pos, previews.getContext());
            blocks.put(pos, block);
        }
        return block;
    }
    
    private PlacementStructurePreview createStructureTree(PlacementStructurePreview parent, LittlePreviews previews) {
        PlacementStructurePreview structure = new PlacementStructurePreview(parent, previews);
        
        for (LittlePreviews child : previews.getChildren())
            structure.addChild(createStructureTree(structure, child));
        
        return structure;
    }
    
    private void createPreviews(PlacementStructurePreview current, LittleVec inBlockOffset, BlockPos pos) {
        if (current.previews != null) {
            HashMapList<BlockPos, PlacePreview> splitted = new HashMapList<BlockPos, PlacePreview>();
            for (PlacePreview pp : current.previews.getPlacePreviews(inBlockOffset)) {
                LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
                pp.split(current.previews.getContext(), splitted, pos, volume);
                if (volume.has())
                    unplaceableTiles.addPreview(pos, volume.createFakePreview(pp.preview), current.previews.getContext());
            }
            
            for (Entry<BlockPos, ArrayList<PlacePreview>> entry : splitted.entrySet())
                getOrCreateBlock(entry.getKey()).addPlacePreviews(current, current.index, entry.getValue());
        }
        
        for (PlacementStructurePreview child : current.children)
            createPreviews(child, inBlockOffset, pos);
    }
    
    public class PlacementBlock implements IGridBased {
        
        public final BlockPos pos;
        private TileEntityLittleTiles cached;
        private LittleGridContext context;
        private final List<PlacePreview>[] previews;
        private final List<PlacePreview>[] latePreviews;
        private int attribute = 0;
        
        public PlacementBlock(BlockPos pos, LittleGridContext context) {
            this.pos = pos;
            this.context = context;
            previews = new List[structures.size()];
            latePreviews = new List[structures.size()];
            
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityLittleTiles) {
                cached = (TileEntityLittleTiles) tileEntity;
                cached.fillUsedIds(availableIds);
            }
        }
        
        @Override
        public LittleGridContext getContext() {
            return context;
        }
        
        public void addPlacePreviews(PlacementStructurePreview structure, int index, List<PlacePreview> previews) {
            List<PlacePreview> list = this.previews[index];
            if (list == null)
                this.previews[index] = previews;
            else
                list.addAll(previews);
            if (structure.isStructure())
                attribute |= structure.getAttribute();
        }
        
        @Override
        public void convertTo(LittleGridContext to) {
            for (int i = 0; i < previews.length; i++)
                if (previews[i] != null)
                    for (PlacePreview preview : previews[i])
                        preview.convertTo(this.context, to);
                    
            this.context = to;
        }
        
        @Override
        public void convertToSmallest() {
            int size = LittleGridContext.minSize;
            for (int i = 0; i < previews.length; i++)
                if (previews[i] != null)
                    for (PlacePreview preview : previews[i])
                        size = Math.max(size, preview.getSmallestContext(context));
                    
            if (size < context.size)
                convertTo(LittleGridContext.get(size));
        }
        
        private boolean needsCollisionTest() {
            for (int i = 0; i < previews.length; i++)
                if (previews[i] != null)
                    for (PlacePreview preview : previews[i])
                        if (preview.needsCollisionTest())
                            return true;
            return false;
        }
        
        public boolean canPlace() throws LittleActionException {
            if (!needsCollisionTest())
                return true;
            
            if (!ignoreWorldBoundaries && (pos.getY() < 0 || pos.getY() >= 256))
                return false;
            
            TileEntityLittleTiles te = LittleAction.loadTe(player, world, pos, null, false, attribute);
            if (te != null) {
                
                int size = te.tilesCount();
                for (int i = 0; i < previews.length; i++)
                    if (previews[i] != null)
                        size += previews[i].size();
                    
                if (size > LittleTiles.CONFIG.general.maxAllowedDensity)
                    throw new LittleTilesConfig.TooDenseException();
                
                LittleGridContext contextBefore = te.getContext();
                te.forceContext(this);
                
                for (int i = 0; i < previews.length; i++)
                    if (previews[i] != null)
                        for (PlacePreview preview : previews[i])
                            if (preview.needsCollisionTest())
                                if (mode.checkAll()) {
                                    if (!te.isSpaceForLittleTile(preview.box, predicate)) {
                                        if (te.getContext() != contextBefore)
                                            te.convertTo(contextBefore);
                                        return false;
                                    }
                                } else if (!te.isSpaceForLittleTile(preview.box, (x, y) -> x.isStructure() && (predicate == null || predicate.test(x, y)))) {
                                    if (te.getContext() != contextBefore)
                                        te.convertTo(contextBefore);
                                    return false;
                                }
                            
                cached = te;
                return true;
            }
            
            int size = 0;
            for (int i = 0; i < previews.length; i++)
                if (previews[i] != null)
                    size += previews[i].size();
                
            if (size > LittleTiles.CONFIG.general.maxAllowedDensity)
                throw new LittleTilesConfig.TooDenseException();
            
            IBlockState state = world.getBlockState(pos);
            if (state.getMaterial().isReplaceable())
                return true;
            else if (mode.checkAll() || !(LittleAction.isBlockValid(state) && LittleAction.canConvertBlock(player, world, pos, state, affectedBlocks.incrementAndGet())))
                return false;
            
            return true;
        }
        
        public boolean combineTilesSecretly() {
            if (cached == null)
                return false;
            if (hasStructure()) {
                for (int i = 0; i < previews.length; i++)
                    if (previews[i] != null && structures.get(i).isStructure())
                        cached.combineTilesSecretly(structures.get(i).getIndex());
                return false;
            }
            
            cached.combineTilesSecretly();
            if (cached.tilesCount() == 1 && cached.convertBlockToVanilla())
                return true;
            return false;
        }
        
        public boolean hasStructure() {
            for (int i = 0; i < previews.length; i++)
                if (previews[i] != null && structures.get(i).isStructure())
                    return true;
            return false;
        }
        
        public void place(PlacementResult result) throws LittleActionException {
            boolean hascollideBlock = false;
            for (int i = 0; i < previews.length; i++)
                if (previews[i] != null)
                    for (Iterator<PlacePreview> iterator = previews[i].iterator(); iterator.hasNext();) {
                        PlacePreview preview = iterator.next();
                        if (preview.needsCollisionTest())
                            hascollideBlock = true;
                        else {
                            if (latePreviews[i] == null)
                                latePreviews[i] = new ArrayList<>();
                            latePreviews[i].add(preview);
                            iterator.remove();
                        }
                    }
                
            if (hascollideBlock) {
                boolean requiresCollisionTest = true;
                if (cached == null) {
                    if (!(world.getBlockState(pos).getBlock() instanceof BlockTile) && world.getBlockState(pos).getMaterial().isReplaceable()) {
                        requiresCollisionTest = false;
                        world.setBlockState(pos, BlockTile.getStateByAttribute(attribute));
                    }
                    
                    cached = LittleAction.loadTe(player, world, pos, affectedBlocks, mode.shouldConvertBlock(), attribute);
                } else
                    cached = cached.forceSupportAttribute(attribute);
                
                if (cached != null) {
                    
                    int size = cached.tilesCount();
                    for (int i = 0; i < previews.length; i++)
                        if (previews[i] != null)
                            size += previews[i].size();
                        
                    if (size > LittleTiles.CONFIG.general.maxAllowedDensity)
                        throw new LittleTilesConfig.TooDenseException();
                    
                    if (cached.isEmpty())
                        requiresCollisionTest = false;
                    
                    final boolean collsionTest = requiresCollisionTest;
                    
                    cached.forceContext(this);
                    
                    try {
                        cached.updateTilesSecretly((x) -> {
                            
                            for (int i = 0; i < previews.length; i++) {
                                if (previews[i] == null || previews[i].isEmpty())
                                    continue;
                                ParentTileList parent = x.noneStructureTiles();
                                PlacementStructurePreview structure = structures.get(i);
                                if (structure.isStructure()) {
                                    StructureTileList list = x.addStructure(structure.getIndex(), structure.getAttribute());
                                    structure.place(list);
                                    parent = list;
                                }
                                
                                mode.prepareBlock(Placement.this, this, collsionTest);
                                
                                for (PlacePreview preview : previews[i]) {
                                    try {
                                        for (LittleTile LT : preview.placeTile(Placement.this, this, parent, structure.getStructure(), collsionTest)) {
                                            if (playSounds) {
                                                if (!soundsToBePlayed.contains(LT.getSound()))
                                                    soundsToBePlayed.add(LT.getSound());
                                            }
                                            parent.add(LT);
                                            result.addPlacedTile(parent, LT);
                                        }
                                    } catch (LittleActionException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });
                    } catch (RuntimeException e) {
                        if (e.getCause() instanceof LittleActionException)
                            throw (LittleActionException) e.getCause();
                        else
                            throw e;
                    }
                }
            }
        }
        
        public void placeLate() throws LittleActionException {
            for (int i = 0; i < latePreviews.length; i++) {
                if (latePreviews[i] == null)
                    continue;
                
                PlacementStructurePreview structure = structures.get(i);
                for (PlacePreview preview : latePreviews[i])
                    preview.placeTile(Placement.this, this, null, structure.getStructure(), false);
            }
        }
        
        public TileEntityLittleTiles getTe() {
            return cached;
        }
    }
    
    public class PlacementStructurePreview {
        
        private LittleStructure cachedStructure;
        public final LittlePreviews previews;
        public final PlacementStructurePreview parent;
        public final int index;
        public final boolean dynamic;
        private int structureIndex = -1;
        
        public PlacementStructurePreview(PlacementStructurePreview parent, LittlePreviews previews) {
            this.index = structures.size();
            structures.add(this);
            
            this.dynamic = previews.isDynamic();
            this.parent = parent;
            this.previews = previews;
            if (previews instanceof LittlePreviewsStructureHolder)
                cachedStructure = ((LittlePreviewsStructureHolder) previews).structure;
        }
        
        public int getAttribute() {
            return previews.getStructureType().attribute;
        }
        
        public int getIndex() {
            if (structureIndex == -1) {
                structureIndex = availableIds.nextClearBit(0);
                availableIds.set(structureIndex);
            }
            return structureIndex;
        }
        
        public boolean isStructure() {
            return previews.hasStructure();
        }
        
        List<PlacementStructurePreview> children = new ArrayList<>();
        
        public void addChild(PlacementStructurePreview child) {
            children.add(child);
        }
        
        public void place(StructureTileList parent) {
            if (cachedStructure == null)
                cachedStructure = parent.setStructureNBT(previews.structureNBT);
            else {
                StructureTileList.setRelativePos(parent, cachedStructure.mainBlock.getPos().subtract(parent.getPos()));
                cachedStructure.addBlock(parent);
            }
        }
        
        public boolean isPlaced() {
            return isStructure() && cachedStructure != null;
        }
        
        public LittleStructure getStructure() {
            return cachedStructure;
        }
        
    }
    
}