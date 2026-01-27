package team.creative.littletiles.common.structure.type.premade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import team.creative.creativecore.common.util.inventory.InventoryUtils;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.action.ActionEvent;
import team.creative.littletiles.client.action.ActionEvent.ActionType;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.action.exception.LittleActionException.StructureNotFoundException;
import team.creative.littletiles.common.action.source.LittleActionSource;
import team.creative.littletiles.common.action.source.LittleActionSourceStructure;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PlacementPreviewAdd;
import team.creative.littletiles.common.placement.PlacementResult;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class Little3dPrinter extends LittleStructurePremade {
    
    public static final int INVENTORY_SIZE = 20;
    
    @StructureDirectional(color = ColorUtils.YELLOW)
    private LittleGroup blueprint;
    @StructureDirectional
    private Facing facing = Facing.EAST;
    
    private List<List<Component>> errors;
    
    private int ticks;
    /** if zero, means it is at the start. It basically represents the next index to be done. If it is -1 it means it is done */
    private int index;
    private PlacementTracker tracker;
    
    public int amountPerTick = 1;
    public int ticksToSkip = 10;
    
    public boolean rememberStructures = true;
    public boolean continueOnError = false;
    
    public SimpleContainer inventory;
    
    public Little3dPrinter(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    public boolean hasNoErrors() {
        return errors == null || errors.isEmpty();
    }
    
    public boolean shouldContinueWorking() {
        return !mainBlock.isRemoved() && getOutput(0).getState().any() && blueprint != null && index != -1 && (continueOnError || hasNoErrors());
    }
    
    public Component blueprintInfo() {
        if (blueprint == null || blueprint.isEmptyIncludeChildren())
            return Component.literal("");
        return Component.translatable("gui.3dprinter.blueprint_info", blueprint.totalTiles(), blueprint.totalBoxes());
    }
    
    public void clearErrors() {
        errors = null;
        if (shouldContinueWorking())
            queueForNextTick();
    }
    
    public List<List<Component>> errors() {
        if (errors == null || errors.isEmpty())
            return Collections.EMPTY_LIST;
        return errors;
    }
    
    public void resetKeepErrors() {
        ticks = 0;
        index = 0;
        tracker = null;
        if (shouldContinueWorking())
            queueForNextTick();
    }
    
    public void reset() {
        errors = null;
        ticks = 0;
        index = 0;
        tracker = null;
        if (shouldContinueWorking())
            queueForNextTick();
    }
    
    @Override
    public void postLoad() {
        super.postLoad();
        if (shouldContinueWorking())
            queueForNextTick();
    }
    
    @Override
    public void loadExtra(CompoundTag nbt, Provider provider) {
        if (nbt.contains("errors")) {
            var list = nbt.getList("errors", Tag.TAG_LIST);
            errors = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                var errorList = list.getList(i);
                List<Component> error = new ArrayList<>();
                for (int j = 0; j < errorList.size(); j++)
                    error.add(ComponentSerialization.CODEC.decode(NbtOps.INSTANCE, errorList.get(j)).getOrThrow().getFirst());
                errors.add(error);
            }
        } else
            errors = null;
        if (nbt.contains("tracker"))
            tracker = new PlacementTracker(nbt.getCompound("location"));
        else
            tracker = null;
        
        ticks = nbt.getInt("ticks");
        index = nbt.getInt("index");
        amountPerTick = nbt.contains("amount") ? nbt.getInt("amount") : 1;
        ticksToSkip = nbt.contains("skip") ? nbt.getInt("skip") : 10;
        rememberStructures = nbt.getBoolean("structures");
        continueOnError = nbt.getBoolean("continue");
        
        if (nbt.contains("inventory")) {
            var invNBT = nbt.getCompound("inventory");
            inventory = new SimpleContainer(INVENTORY_SIZE);
            InventoryUtils.load(inventory, provider, invNBT);
        } else
            inventory = new SimpleContainer(INVENTORY_SIZE);
        
        if (inventory != null)
            inventory.addListener(x -> onInventoryChanged());
    }
    
    @Override
    public void saveExtra(CompoundTag nbt, Provider provider) {
        ListTag list;
        if (errors != null) {
            list = new ListTag();
            for (int i = 0; i < errors.size(); i++) {
                ListTag errorList = new ListTag();
                var error = errors.get(i);
                for (int j = 0; j < error.size(); j++) {
                    var tag = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, error.get(j)).getOrThrow();
                    if (tag instanceof StringTag s) {
                        CompoundTag wrapper = new CompoundTag();
                        wrapper.putString("text", s.getAsString());
                        tag = wrapper;
                    }
                    errorList.add(tag);
                }
                
                list.add(errorList);
            }
            nbt.put("errors", list);
        } else
            nbt.remove("errors");
        if (tracker != null)
            nbt.put("tracker", tracker.save(new CompoundTag()));
        else
            nbt.remove("tracker");
        
        nbt.putInt("ticks", ticks);
        nbt.putInt("index", index);
        nbt.putInt("amount", amountPerTick);
        nbt.putInt("skip", ticksToSkip);
        nbt.putBoolean("structures", rememberStructures);
        nbt.putBoolean("continue", continueOnError);
        
        if (inventory != null)
            nbt.put("inventory", InventoryUtils.save(provider, inventory));
        else
            nbt.remove("inventory");
    }
    
    public void onInventoryChanged() {
        if (isClient())
            return;
        markDirty();
    }
    
    public void setBlueprint(ItemStack stack) {
        if (stack.getItem() instanceof ILittlePlacer p && p.hasTiles(stack)) {
            LittleGroup group = p.getTiles(stack);
            
            try {
                var structureBox = getSurroundingBox().getAbsoluteBox();
                structureBox.sameGrid(group, () -> {
                    LittleBox box = group.getSurroundingBox();
                    
                    LittleVec moved = new LittleVec(0, 0, 0);
                    moved.set(facing.axis, structureBox.box.get(facing) - box.get(facing.opposite()));
                    
                    Axis one = facing.one();
                    moved.set(one, structureBox.box.getMin(one) - box.getMin(one));
                    Axis two = facing.two();
                    moved.set(two, structureBox.box.getMin(two) - box.getMin(two));
                    
                    group.move(new LittleVecGrid(moved, group.getGrid()));
                });
                
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            
            reset();
            blueprint = group;
            updateStructure();
            
            playSound(SoundEvents.ITEM_FRAME_PLACE);
        }
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        super.performInternalOutputChange(output);
        if (shouldContinueWorking())
            queueForNextTick();
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide)
            LittleTilesGuiRegistry.PRINTER3D.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
    public int printIndex() {
        return index;
    }
    
    protected void collectError(LittleActionException e) {
        if (errors == null)
            errors = new ArrayList<>();
        var action = e.getActionMessage();
        if (action == null)
            action = Arrays.asList(e.getTranslatable());
        errors.add(action);
    }
    
    @Override
    public boolean queuedTick() {
        if (!shouldContinueWorking() || isClient())
            return false;
        
        if (ticks >= ticksToSkip) {
            if (tracker == null)
                tracker = new PlacementTracker();
            
            try {
                LittleActionSource source = new LittleActionSource3dPrinter(this);
                int amount = 0;
                while (amount < amountPerTick && shouldContinueWorking()) {
                    
                    ExtractedTile tile = tracker.extract(getStructureLevel(), blueprint, new AtomicInteger(), index, rememberStructures);
                    if (tile == null) { // Reached end of structure
                        tracker.finishStructure(getStructureLevel());
                        tracker = null;
                        index = -1;
                        return false;
                    }
                    
                    try {
                        var action = tile.generateAction(this);
                        var result = action.action(source);
                        if (action.wasSuccessful(result)) {
                            NeoForge.EVENT_BUS.post(new ActionEvent(action, ActionType.NORMAL, null));
                            if (tile.first)
                                tile.tracker.firstPlaced(action.result);
                        } else
                            collectError(new LittleActionException("action.failed"));
                        markDirty();
                    } catch (LittleActionException e) {
                        collectError(e);
                    }
                    amount++;
                    if (continueOnError || hasNoErrors()) {
                        index++;
                        tile.tracker.increment();
                    }
                }
            } catch (LittleActionException e) {
                collectError(e);
            }
            
            ticks = 0;
            
        } else
            ticks++;
        
        return shouldContinueWorking();
    }
    
    public static class LittleActionSource3dPrinter extends LittleActionSourceStructure<Little3dPrinter> {
        
        public LittleActionSource3dPrinter(Little3dPrinter structure) {
            super(structure);
        }
        
        @Override
        public LittleInventory createInventory() {
            return new LittleInventory(new InvWrapper(structure.inventory));
        }
        
        @Override
        public boolean needsIngredients() {
            return true;
        }
        
        @Override
        public boolean addStack(ItemStack stack) {
            var result = structure.inventory.addItem(stack);
            stack.setCount(result.getCount());
            return stack.isEmpty();
        }
        
    }
    
    public static record ExtractedTile(LittleGrid grid, LittleTile tile, boolean first, PlacementTracker tracker) {
        
        public LittleActionPlace generateAction(LittleStructure structure) throws LittleActionException {
            CompoundTag structureNBT;
            if (first && tracker.isStructure()) {
                structureNBT = new CompoundTag();// Basically a placeholder, until the structure has finished its placement
                structureNBT.putString("id", "fixed");
            } else
                structureNBT = null;
            
            PlacementPosition pos = new PlacementPosition(structure.getSurroundingBox().getAbsoluteBox().getMin(), null);
            LittleGroup group = new LittleGroup(structureNBT, Collections.EMPTY_LIST);
            group.addTile(grid, tile);
            if (first || !tracker.isStructure())
                return new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.create(structure.getStructureLevel(), group, PlacementMode.FILL, pos));
            if (tracker.location == null)
                throw new StructureNotFoundException();
            return new LittleActionPlace(PlaceAction.ABSOLUTE, new PlacementPreviewAdd(tracker.location, structure.getStructureLevel(), group, PlacementMode.FILL, pos));
        }
        
    }
    
    public static class PlacementTracker {
        
        private StructureLocation location;
        private CompoundTag structureNBT;
        private List<PlacementTracker> children = new ArrayList<>();
        private Map<String, PlacementTracker> extensions = new Object2ObjectArrayMap<>();
        
        private int doneSelf = -1;
        private int doneAll = -1;
        
        /** does not need to be saved as it will be processed at least once when being generated */
        private boolean first = true;
        private LittleStructure cached;
        
        public PlacementTracker() {}
        
        public PlacementTracker(CompoundTag nbt) {
            if (nbt.contains("location")) {
                location = new StructureLocation(nbt.getCompound("location"));
                structureNBT = nbt.getCompound("structure");
            }
            doneSelf = nbt.getInt("self");
            doneAll = nbt.getInt("all");
            
            if (nbt.contains("children")) {
                ListTag list = nbt.getList("children", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++)
                    children.add(new PlacementTracker(list.getCompound(i)));
            }
            if (nbt.contains("extensions")) {
                CompoundTag ex = nbt.getCompound("ex");
                for (String key : ex.getAllKeys())
                    extensions.put(key, new PlacementTracker(ex.getCompound(key)));
            }
        }
        
        public CompoundTag save(CompoundTag nbt) {
            if (structureNBT != null && location != null) {
                nbt.put("location", location.write(new CompoundTag()));
                nbt.put("structure", structureNBT);
            }
            nbt.putInt("self", doneSelf);
            nbt.putInt("all", doneAll);
            
            if (!children.isEmpty()) {
                ListTag list = new ListTag();
                for (int i = 0; i < children.size(); i++)
                    list.add(children.get(i).save(new CompoundTag()));
                nbt.put("children", list);
            }
            if (!extensions.isEmpty()) {
                CompoundTag ex = new CompoundTag();
                for (Entry<String, PlacementTracker> entry : extensions.entrySet())
                    ex.put(entry.getKey(), entry.getValue().save(new CompoundTag()));
                nbt.put("extensions", ex);
            }
            return nbt;
        }
        
        public ExtractedTile extract(Level level, LittleGroup group, AtomicInteger current, int index, boolean rememberStructures) throws LittleActionException {
            if (doneSelf == -1) {
                boolean firstCached = first;
                if (firstCached && rememberStructures)
                    structureNBT = group.getStructureTag();
                
                for (LittleTile tile : group) {
                    if (tile.size() + current.get() < index)
                        current.addAndGet(tile.size());
                    else
                        for (LittleBox box : tile) {
                            if (current.get() == index) // Found
                                return new ExtractedTile(group.getGrid(), tile.copy(new SingletonList<>(box)), firstCached, this);
                            current.incrementAndGet();
                        }
                }
                
                doneSelf = current.get();
            } else
                current.addAndGet(doneSelf);
            
            int childIndex = 0;
            for (LittleGroup child : group.children.children()) {
                if (childIndex >= children.size())
                    children.add(new PlacementTracker());
                PlacementTracker tracker = children.get(childIndex);
                if (tracker.doneAll != -1)
                    current.addAndGet(tracker.doneAll);
                else {
                    var result = tracker.extract(level, child, current, index, rememberStructures);
                    if (result != null)
                        return result;
                    if (structureNBT == null)
                        tracker.finishStructure(level);
                }
                childIndex++;
            }
            
            for (Entry<String, LittleGroup> entry : group.children.extensionEntries()) {
                var tracker = extensions.get(entry.getKey());
                if (tracker == null)
                    extensions.put(entry.getKey(), tracker = new PlacementTracker());
                if (tracker.doneAll != -1)
                    current.addAndGet(tracker.doneAll);
                else {
                    var result = tracker.extract(level, entry.getValue(), current, index, rememberStructures);
                    if (result != null)
                        return result;
                    if (structureNBT == null)
                        tracker.finishStructure(level);
                }
            }
            
            doneAll = current.get();
            return null;
        }
        
        public void increment() {
            first = false;
        }
        
        public void firstPlaced(PlacementResult result) {
            if (structureNBT != null)
                location = result.parentStructure.getStructureLocation();
        }
        
        protected void setStructure(Level level) throws LittleActionException {
            if (location == null)
                throw new StructureNotFoundException();
            var be = location.findBE(level);
            be.updateTiles(x -> {
                try {
                    // Turns the structure from a fixed to the wanted one
                    var parent = x.getStructure(location.index);
                    var previous = parent.getStructure();
                    cached = parent.setStructureNBT(structureNBT, be.getLevel().registryAccess(), true);
                    cached.takeOverBlocks(previous);
                    cached.children.initAfterPlacing(children.size());
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            });
            
            for (PlacementTracker tracker : children)
                tracker.setStructure(level);
            
            for (PlacementTracker tracker : extensions.values())
                tracker.setStructure(level);
        }
        
        public boolean isStructure() {
            return structureNBT != null;
        }
        
        protected void updateRelations() {
            for (int i = 0; i < children.size(); i++) {
                PlacementTracker child = children.get(i);
                if (child.isStructure()) {
                    cached.children.connectToChild(i, child.cached);
                    child.cached.children.connectToParentAsChild(i, cached);
                }
                
                child.updateRelations();
            }
            
            for (Entry<String, PlacementTracker> pair : extensions.entrySet()) {
                if (pair.getValue().isStructure()) {
                    cached.children.connectToExtension(pair.getKey(), pair.getValue().cached);
                    pair.getValue().cached.children.connectToParentAsExtension(cached);
                }
                
                pair.getValue().updateRelations();
            }
        }
        
        protected void afterPlaced() {
            cached.afterPlaced();
            
            for (int i = 0; i < children.size(); i++) {
                PlacementTracker child = children.get(i);
                if (child.isStructure())
                    child.afterPlaced();
            }
            
            for (PlacementTracker extension : extensions.values())
                if (extension.isStructure())
                    extension.afterPlaced();
        }
        
        public void finishStructure(Level level) throws LittleActionException {
            if (structureNBT == null)
                return;
            
            setStructure(level);
            updateRelations();
            afterPlaced();
            
            structureNBT = null;
            location = null;
        }
        
    }
    
}
