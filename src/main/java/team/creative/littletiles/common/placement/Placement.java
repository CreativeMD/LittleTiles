package team.creative.littletiles.common.placement;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.BlockEvent.EntityMultiPlaceEvent;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.collection.LittleBlockCollection;
import team.creative.littletiles.common.block.little.tile.collection.LittleCollection;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupHolder;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.config.LittleTilesConfig.NotAllowedToPlaceException;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.level.LittleUpdateCollector;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.box.volume.LittleVolumes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.box.LittlePlaceBox;
import team.creative.littletiles.common.structure.LittleStructure;

public class Placement {
    
    public final Player player;
    public final Level level;
    public final PlacementPreview preview;
    public final LinkedHashMap<BlockPos, PlacementBlock> blocks = new LinkedHashMap<>();
    public final PlacementStructurePreview origin;
    public final List<PlacementStructurePreview> structures = new ArrayList<>();
    
    public final BitSet availableIds = new BitSet();
    
    public final LittleIngredients removedIngredients;
    public final LittleGroupAbsolute removedTiles;
    public final LittleGroup unplaceableTiles;
    public final List<SoundType> soundsToBePlayed = new ArrayList<>();
    
    protected MutableInt affectedBlocks = new MutableInt();
    protected ItemStack stack;
    protected boolean ignoreWorldBoundaries = true;
    protected BiPredicate<IParentCollection, LittleTile> predicate;
    protected boolean playSounds = true;
    
    public Placement(@Nullable Player player, Level level, PlacementPreview preview) throws LittleActionException {
        this.player = player;
        this.level = level;
        this.preview = preview;
        this.origin = createStructureTree(null, preview.previews, null);
        
        this.removedIngredients = new LittleIngredients();
        this.removedTiles = new LittleGroupAbsolute(preview.position.getPos());
        this.unplaceableTiles = new LittleGroup();
        
        createPreviews(origin, preview.position.getVec());
        
        for (PlacementBlock block : blocks.values())
            block.convertToSmallest();
    }
    
    public Placement(Player player, PlacementPreview preview) throws LittleActionException {
        this(player, preview.getLevel(player), preview);
    }
    
    public Placement setPlaySounds(boolean sounds) {
        this.playSounds = sounds;
        return this;
    }
    
    public Placement setIgnoreWorldBoundaries(boolean value) {
        this.ignoreWorldBoundaries = value;
        return this;
    }
    
    public Placement setPredicate(BiPredicate<IParentCollection, LittleTile> predicate) {
        this.predicate = predicate;
        return this;
    }
    
    public Placement setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }
    
    public void addRemovedIngredient(PlacementBlock block, LittleElement element, LittleBoxReturnedVolume volume) {
        removedIngredients.add(LittleIngredient.extract(element, volume.getPercentVolume(block.getGrid())));
    }
    
    public void addRemovedIngredient(LittleVolumes volumes) {
        if (!volumes.isEmpty())
            for (Entry<LittleElement, Integer> entry : volumes.entrySet())
                removedIngredients.add(LittleIngredient.extract(entry.getKey(), entry.getValue() * volumes.getGrid().pixelVolume));
    }
    
    public LittleIngredients overflow() {
        LittleIngredients ingredients = LittleAction.getIngredients(removedTiles);
        ingredients.add(this.removedIngredients);
        return ingredients;
    }
    
    public boolean canPlace() throws LittleActionException {
        affectedBlocks.setValue(0);
        
        for (BlockPos pos : blocks.keySet()) {
            if (!LittleAction.isAllowedToInteract(level, player, pos, true, Facing.EAST)) {
                LittleAction.sendBlockResetToClient(level, player, pos);
                return false;
            }
        }
        
        List<BlockPos> coordsToCheck = preview.mode.getCoordsToCheck(blocks.keySet(), preview.position.getPos());
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
        
        if (player != null && !level.isClientSide) {
            if (player != null) {
                if (LittleTiles.CONFIG.isPlaceLimited(player) && preview.previews.getVolumeIncludingChildren() > LittleTiles.CONFIG.build.get(player).maxPlaceBlocks) {
                    for (BlockPos pos : blocks.keySet())
                        LittleAction.sendBlockResetToClient(level, player, pos);
                    throw new NotAllowedToPlaceException(player);
                }
                
                if (LittleTiles.CONFIG.isTransparencyRestricted(player))
                    for (LittleTile tile : preview.previews.allTiles()) {
                        try {
                            LittleAction.isAllowedToPlacePreview(player, tile);
                        } catch (LittleActionException e) {
                            for (BlockPos pos : blocks.keySet())
                                LittleAction.sendBlockResetToClient(level, player, pos);
                            throw e;
                        }
                    }
            }
            
            affectedBlocks.setValue(0);
            
            List<BlockSnapshot> snaps = new ArrayList<>();
            for (BlockPos snapPos : blocks.keySet())
                snaps.add(BlockSnapshot.create(level.dimension(), level, snapPos));
            
            EntityMultiPlaceEvent event = new BlockEvent.EntityMultiPlaceEvent(snaps, level
                    .getBlockState(preview.position.facing == null ? preview.position.getPos() : preview.position.getPos().relative(preview.position.facing.toVanilla())), player);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                for (BlockPos snapPos : blocks.keySet())
                    LittleAction.sendBlockResetToClient(level, player, snapPos);
                return null;
            }
        }
        try {
            if (canPlace())
                return placeTiles();
        } catch (LittleActionException e) {
            for (BlockPos snapPos : blocks.keySet())
                LittleAction.sendBlockResetToClient(level, player, snapPos);
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
        PlacementResult result = new PlacementResult(preview.position.getPos());
        
        for (PlacementBlock block : blocks.values())
            block.place(result);
        
        result.parentStructure = origin.isStructure() ? origin.getStructure() : null;
        
        LittleUpdateCollector neighbor = new LittleUpdateCollector(level, blocks.keySet());
        
        for (Iterator iterator = blocks.values().iterator(); iterator.hasNext();) {
            PlacementBlock block = (PlacementBlock) iterator.next();
            if (block.combineTilesSecretly()) {
                result.blocks.remove(block.cached);
                iterator.remove();
            }
        }
        
        origin.place();
        
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
                level.playSound(null, preview.position.getPos(), soundsToBePlayed.get(i)
                        .getPlaceSound(), SoundSource.BLOCKS, (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
            
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
            if (preview.isStructure() && child.isStructure()) {
                preview.getStructure().children.connectToChild(i, child.getStructure());
                child.getStructure().children.connectToParentAsChild(i, preview.getStructure());
            }
            
            updateRelations(child);
        }
        
        for (Entry<String, PlacementStructurePreview> pair : preview.extensions.entrySet()) {
            if (preview.isStructure() && pair.getValue().isStructure()) {
                preview.getStructure().children.connectToExtension(pair.getValue().extension, pair.getValue().getStructure());
                pair.getValue().getStructure().children.connectToParentAsExtension(preview.getStructure());
            }
            
            updateRelations(pair.getValue());
        }
    }
    
    public PlacementBlock getOrCreateBlock(BlockPos pos) {
        PlacementBlock block = blocks.get(pos);
        if (block == null) {
            block = new PlacementBlock(pos, preview.previews.getGrid());
            blocks.put(pos, block);
        }
        return block;
    }
    
    private PlacementStructurePreview createStructureTree(PlacementStructurePreview parent, LittleGroup previews, String extension) {
        PlacementStructurePreview structure = new PlacementStructurePreview(parent, previews, extension);
        
        for (LittleGroup child : previews.children.children())
            structure.addChild(createStructureTree(structure, child, null));
        
        for (Entry<String, LittleGroup> pair : previews.children.extensionEntries())
            structure.addExtension(pair.getKey(), createStructureTree(structure, pair.getValue(), pair.getKey()));
        
        return structure;
    }
    
    private void createPreviews(PlacementStructurePreview current, LittleVec inBlockOffset) {
        if (current.previews != null) {
            LittleBlockCollection collection = new LittleBlockCollection(preview.position.getPos(), preview.previews.getGrid());
            
            LittleVolumes volumes = new LittleVolumes();
            collection.add(current.previews, inBlockOffset, volumes);
            
            addRemovedIngredient(volumes);
            
            for (Entry<BlockPos, LittleCollection> entry : collection.entrySet())
                getOrCreateBlock(entry.getKey()).addPlacePreviews(current, current.index, entry.getValue());
        }
        
        for (PlacementStructurePreview child : current.children)
            createPreviews(child, inBlockOffset);
    }
    
    public class PlacementBlock implements IGridBased {
        
        public final BlockPos pos;
        private BETiles cached;
        private LittleGrid grid;
        private final LittleCollection[] tiles;
        private int attribute = 0;
        
        public PlacementBlock(BlockPos pos, LittleGrid grid) {
            this.pos = pos;
            this.grid = grid;
            tiles = new LittleCollection[structures.size()];
            
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BETiles) {
                cached = (BETiles) blockEntity;
                cached.fillUsedIds(availableIds);
            }
        }
        
        @Override
        public LittleGrid getGrid() {
            return grid;
        }
        
        public void addPlacePreviews(PlacementStructurePreview structure, int index, LittleCollection previews) {
            LittleCollection list = this.tiles[index];
            if (list == null)
                this.tiles[index] = previews;
            else
                list.addAll(previews);
            if (structure.isStructure())
                attribute |= structure.getAttribute();
        }
        
        @Override
        public void convertTo(LittleGrid to) {
            for (int i = 0; i < tiles.length; i++)
                if (tiles[i] != null)
                    for (LittleTile preview : tiles[i])
                        preview.convertTo(this.grid, to);
                    
            this.grid = to;
        }
        
        @Override
        public int getSmallest() {
            int size = LittleGrid.min().count;
            
            for (int i = 0; i < tiles.length; i++)
                if (tiles[i] != null)
                    for (LittleTile preview : tiles[i])
                        size = Math.max(size, preview.getSmallest(grid));
                    
            return size;
        }
        
        private boolean needsCollisionTest() {
            for (int i = 0; i < tiles.length; i++)
                if (tiles[i] != null && !tiles[i].isEmpty())
                    return true;
            return false;
        }
        
        public boolean canPlace() throws LittleActionException {
            if (!needsCollisionTest())
                return true;
            
            if (!ignoreWorldBoundaries && (pos.getY() < 0 || pos.getY() >= 256))
                return false;
            
            BETiles te = LittleAction.loadBE(player, level, pos, null, false, attribute);
            if (te != null) {
                
                int size = te.tilesCount();
                for (int i = 0; i < tiles.length; i++)
                    if (tiles[i] != null)
                        size += tiles[i].size();
                    
                if (size > LittleTiles.CONFIG.general.maxAllowedDensity)
                    throw new LittleTilesConfig.TooDenseException();
                
                if (!te.sameGridReturn(te, () -> {
                    for (int i = 0; i < tiles.length; i++)
                        if (tiles[i] != null)
                            for (LittleTile tile : tiles[i])
                                for (LittleBox box : tile) {
                                    if (preview.mode.checkAll()) {
                                        if (!te.isSpaceFor(box, predicate))
                                            return false;
                                    } else if (!te.isSpaceFor(box, (x, y) -> x.isStructure() && (predicate == null || predicate.test(x, y))))
                                        return false;
                                    
                                }
                    return true;
                }))
                    return false;
                
                cached = te;
                return true;
            }
            
            int size = 0;
            for (int i = 0; i < tiles.length; i++)
                if (tiles[i] != null)
                    size += tiles[i].size();
                
            if (size > LittleTiles.CONFIG.general.maxAllowedDensity)
                throw new LittleTilesConfig.TooDenseException();
            
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.REPLACEABLE))
                return true;
            else if (preview.mode.checkAll() || !(LittleAction.isBlockValid(state) && LittleAction.canConvertBlock(player, level, pos, state, affectedBlocks.incrementAndGet())))
                return false;
            return true;
        }
        
        public boolean combineTilesSecretly() {
            if (cached == null)
                return false;
            if (hasStructure()) {
                for (int i = 0; i < tiles.length; i++)
                    if (tiles[i] != null && structures.get(i).isStructure())
                        cached.combineTilesSecretly(structures.get(i).getIndex());
                return false;
            }
            
            cached.combineTilesSecretly();
            if (cached.tilesCount() == 1 && cached.convertBlockToVanilla())
                return true;
            return false;
        }
        
        public boolean hasStructure() {
            for (int i = 0; i < tiles.length; i++)
                if (tiles[i] != null && structures.get(i).isStructure())
                    return true;
            return false;
        }
        
        public void place(PlacementResult result) throws LittleActionException {
            boolean hascollideBlock = false;
            for (int i = 0; i < tiles.length; i++)
                if (tiles[i] != null) {
                    hascollideBlock = true;
                    break;
                }
            
            if (hascollideBlock) {
                boolean requiresCollisionTest = true;
                if (cached == null) {
                    if (!(level.getBlockState(pos).getBlock() instanceof BlockTile) && level.getBlockState(pos).is(BlockTags.REPLACEABLE)) {
                        requiresCollisionTest = false;
                        level.setBlock(pos, BlockTile.getStateByAttribute(attribute), 0);
                    }
                    
                    cached = LittleAction.loadBE(player, level, pos, affectedBlocks, Placement.this.preview.mode.shouldConvertBlock(), attribute);
                } else
                    cached = cached.forceSupportAttribute(attribute);
                
                if (cached != null) {
                    int size = cached.tilesCount();
                    for (int i = 0; i < tiles.length; i++)
                        if (tiles[i] != null)
                            size += tiles[i].size();
                        
                    if (size > LittleTiles.CONFIG.general.maxAllowedDensity)
                        throw new LittleTilesConfig.TooDenseException();
                    
                    if (cached.isEmpty())
                        requiresCollisionTest = false;
                    
                    PlacementContext context = new PlacementContext(Placement.this, this, result, requiresCollisionTest);
                    
                    try {
                        cached.sameGrid(this, () -> {
                            cached.updateTilesSecretly((x) -> {
                                
                                for (int i = 0; i < tiles.length; i++) {
                                    if (tiles[i] == null || tiles[i].isEmpty())
                                        continue;
                                    ParentCollection parent = x.noneStructureTiles();
                                    PlacementStructurePreview structure = structures.get(i);
                                    if (structure.isStructure()) {
                                        StructureParentCollection list = x.addStructure(structure.getIndex(), structure.getAttribute());
                                        structure.place(list);
                                        parent = list;
                                    }
                                    context.setParent(parent);
                                    
                                    preview.mode.prepareBlock(context);
                                    
                                    for (LittleTile tile : tiles[i]) {
                                        try {
                                            if (preview.mode.placeTile(context, structure.getStructure(), tile) && playSounds && !soundsToBePlayed.contains(tile.getSound()))
                                                soundsToBePlayed.add(tile.getSound());
                                        } catch (LittleActionException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            });
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
        
        public BETiles getBE() {
            return cached;
        }
    }
    
    public class PlacementStructurePreview {
        
        private LittleStructure cachedStructure;
        public final LittleGroup previews;
        public final PlacementStructurePreview parent;
        public final int index;
        public final String extension;
        private int structureIndex = -1;
        
        List<PlacementStructurePreview> children = new ArrayList<>();
        HashMap<String, PlacementStructurePreview> extensions = new HashMap<>();
        
        public PlacementStructurePreview(PlacementStructurePreview parent, LittleGroup previews, String extension) {
            this.index = structures.size();
            structures.add(this);
            
            this.extension = extension;
            this.parent = parent;
            this.previews = previews;
            if (previews instanceof LittleGroupHolder)
                cachedStructure = ((LittleGroupHolder) previews).structure;
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
        
        public void addChild(PlacementStructurePreview child) {
            children.add(child);
        }
        
        public void addExtension(String key, PlacementStructurePreview child) {
            extensions.put(key, child);
        }
        
        public void place(StructureParentCollection parent) {
            if (cachedStructure == null) {
                cachedStructure = parent.setStructureNBT(previews.getStructureTag());
                cachedStructure.children.initAfterPlacing(children.size());
            } else {
                StructureParentCollection.setRelativePos(parent, cachedStructure.mainBlock.getPos().subtract(parent.getPos()));
                cachedStructure.addBlock(parent);
            }
        }
        
        public void place() throws LittleActionException {
            if (isStructure())
                for (LittlePlaceBox box : previews.getSpecialBoxes()) {
                    box.add(preview.position.getVec());
                    box.place(Placement.this, previews.getGrid(), preview.position.getPos(), getStructure());
                }
            
            for (PlacementStructurePreview preview : children)
                preview.place();
        }
        
        public boolean isPlaced() {
            return isStructure() && cachedStructure != null;
        }
        
        public LittleStructure getStructure() {
            return cachedStructure;
        }
        
    }
    
}