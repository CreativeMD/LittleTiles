package team.creative.littletiles.common.structure;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviewsStructureHolder;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.SubLevel;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.WorldUtils;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.EntityAnimation;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.packet.LittleUpdateStructurePacket;
import team.creative.littletiles.common.structure.connection.ChildrenList;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.connection.LevelChildrenList;
import team.creative.littletiles.common.structure.connection.StructureChildConnection;
import team.creative.littletiles.common.structure.connection.StructureChildFromSubLevelConnection;
import team.creative.littletiles.common.structure.connection.StructureChildToSubLevelConnection;
import team.creative.littletiles.common.structure.connection.StructureChildToSubWorldConnection;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.MissingBlockException;
import team.creative.littletiles.common.structure.exception.MissingChildException;
import team.creative.littletiles.common.structure.exception.MissingParentException;
import team.creative.littletiles.common.structure.exception.MissingStructureException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.InternalSignalInput;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.ISignalSchedulable;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.group.LittleGroup;
import team.creative.littletiles.common.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.tile.parent.StructureParentCollection;

public abstract class LittleStructure implements ISignalSchedulable, ILevelPositionProvider {
    
    private static final Iterator<LittleTile> EMPTY_ITERATOR = new Iterator<LittleTile>() {
        
        @Override
        public boolean hasNext() {
            return false;
        }
        
        @Override
        public LittleTile next() {
            return null;
        }
        
    };
    
    private static final HashMapList<BlockPos, LittleTile> EMPTY_HASHMAPLIST = new HashMapList<>();
    
    public final LittleStructureType type;
    public final IStructureParentCollection mainBlock;
    private final List<StructureBlockConnector> blocks = new ArrayList<>();
    
    public String name;
    
    private StructureChildConnection parent;
    protected LevelChildrenList children;
    
    private HashMap<Integer, SignalExternalOutputHandler> externalHandler;
    private final InternalSignalInput[] inputs;
    private final InternalSignalOutput[] outputs;
    
    private boolean signalChanged = false;
    
    public LittleStructure(LittleStructureType type, IStructureParentCollection mainBlock) {
        this.type = type;
        this.mainBlock = mainBlock;
        this.inputs = type.createInputs(this);
        this.outputs = type.createOutputs(this);
    }
    
    // ================Basics================
    
    @Override
    public Level getLevel() {
        if (mainBlock.isRemoved())
            return null;
        return mainBlock.getLevel();
    }
    
    @Override
    public Level getComponentLevel() {
        return getLevel();
    }
    
    public boolean hasLevel() {
        return mainBlock != null && !mainBlock.isRemoved() && mainBlock.getLevel() != null;
    }
    
    public boolean isClient() {
        return getLevel().isClientSide;
    }
    
    @Override
    public BlockPos getPos() {
        return mainBlock.getPos();
    }
    
    public int getIndex() {
        return mainBlock.getIndex();
    }
    
    public int getAttribute() {
        return type.attribute;
    }
    
    public StructureLocation getStructureLocation() {
        return new StructureLocation(this);
    }
    
    // ================Connections================
    
    public void load() throws CorruptedConnectionException, NotYetConnectedException {
        for (StructureBlockConnector block : blocks)
            block.connect();
        
        try {
            if (parent != null)
                parent.checkConnection();
        } catch (CorruptedConnectionException e) {
            throw new MissingParentException(parent, e);
        }
        
        for (StructureChildConnection child : children)
            try {
                child.getStructure().load();
            } catch (CorruptedConnectionException e) {
                throw new MissingChildException(child, e);
            }
    }
    
    /** use it at your own risk getAttribute() must return the new attribute */
    public void tryAttributeChangeForBlocks() throws CorruptedConnectionException, NotYetConnectedException {
        int attribute = getAttribute();
        mainBlock.setAttribute(attribute);
        for (StructureBlockConnector block : blocks)
            try {
                block.getList().setAttribute(attribute);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    public int count() throws CorruptedConnectionException, NotYetConnectedException {
        int count = mainBlock.size();
        for (StructureBlockConnector block : blocks)
            count += block.count();
        return count;
    }
    
    public boolean isChildOf(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException {
        if (structure == this)
            return true;
        if (parent != null)
            return parent.getStructure().isChildOf(structure);
        return false;
    }
    
    public void removeParent() {
        if (!parent.dynamic)
            throw new RuntimeException("Cannot remove non dynamic child");
        parent = null;
    }
    
    public void updateChildConnection(int i, LittleStructure child, boolean dynamic) {
        Level level = getLevel();
        Level childLevel = child.getLevel();
        
        StructureChildConnection connector;
        if (childLevel == level)
            connector = new StructureChildConnection(this, false, dynamic, i, child.getPos().subtract(getPos()), child.getIndex(), child.getAttribute());
        else if (childLevel instanceof SubLevel && ((SubLevel) childLevel).parent != null)
            connector = new StructureChildToSubLevelConnection(this, dynamic, i, child.getPos().subtract(getPos()), child.getIndex(), child
                    .getAttribute(), ((SubLevel) childLevel).parent.getUUID());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        
        children.set(connector);
    }
    
    public void updateParentConnection(int i, LittleStructure parent, boolean dynamic) {
        Level level = getLevel();
        Level parentLevel = parent.getLevel();
        
        StructureChildConnection connector;
        if (parentLevel == level)
            connector = new StructureChildConnection(this, true, dynamic, i, parent.getPos().subtract(getPos()), parent.getIndex(), parent.getAttribute());
        else if (level instanceof SubLevel && ((SubLevel) level).parent != null)
            connector = new StructureChildFromSubLevelConnection(this, dynamic, i, parent.getPos().subtract(getPos()), parent.getIndex(), parent.getAttribute());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        
        this.parent = connector;
    }
    
    public StructureChildConnection generateConnection(ILevelPositionProvider parent) {
        Level level = getLevel();
        Level parentLevel = parent.getLevel();
        
        StructureChildConnection connector;
        if (parentLevel == level)
            connector = new StructureChildConnection(parent, true, false, 0, this.getPos().subtract(parent.getPos()), this.getIndex(), this.getAttribute());
        else if (level instanceof SubLevel && ((SubLevel) level).parent != null)
            connector = new StructureChildToSubLevelConnection(parent, false, 0, this.getPos().subtract(parent.getPos()), this.getIndex(), this
                    .getAttribute(), ((SubLevel) level).parent.getUUID());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        return connector;
    }
    
    public void removeDynamicChild(int i) throws CorruptedConnectionException, NotYetConnectedException {
        children.remove(i);
    }
    
    public StructureChildConnection getParent() {
        return parent;
    }
    
    public LittleStructure findTopStructure() throws CorruptedConnectionException, NotYetConnectedException {
        if (parent != null)
            return parent.getStructure().findTopStructure();
        return this;
    }
    
    public int countChildren() {
        return children.size();
    }
    
    public Iterable<StructureChildConnection> getChildren() {
        return children;
    }
    
    public StructureChildConnection getChild(int index) throws CorruptedConnectionException, NotYetConnectedException {
        return children.get(index);
    }
    
    // ================Tiles================
    
    public void addBlock(StructureParentCollection block) {
        blocks.add(new StructureBlockConnector(block.getPos().subtract(getPos())));
    }
    
    public Iterable<BETiles> blocks() throws CorruptedConnectionException, NotYetConnectedException {
        load();
        return new Iterable<BETiles>() {
            
            @Override
            public Iterator<BETiles> iterator() {
                
                return new Iterator<BETiles>() {
                    
                    boolean first = true;
                    Iterator<StructureBlockConnector> iterator = blocks.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return first || iterator.hasNext();
                    }
                    
                    @Override
                    public BETiles next() {
                        if (first) {
                            first = false;
                            return mainBlock.getBE();
                        }
                        try {
                            return iterator.next().getBlockEntity();
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }
    
    public Iterable<IStructureParentCollection> blocksList() throws CorruptedConnectionException, NotYetConnectedException {
        load();
        return new Iterable<IStructureParentCollection>() {
            
            @Override
            public Iterator<IStructureParentCollection> iterator() {
                
                return new Iterator<IStructureParentCollection>() {
                    
                    boolean first = true;
                    Iterator<StructureBlockConnector> iterator = blocks.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return first || iterator.hasNext();
                    }
                    
                    @Override
                    public IStructureParentCollection next() {
                        if (first) {
                            first = false;
                            return mainBlock;
                        }
                        try {
                            return iterator.next().getList();
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }
    
    public Iterable<Pair<IStructureParentCollection, LittleTile>> tiles() throws CorruptedConnectionException, NotYetConnectedException {
        Iterator<IStructureParentCollection> iterator = blocksList().iterator();
        return new Iterable<Pair<IStructureParentCollection, LittleTile>>() {
            
            @Override
            public Iterator<Pair<IStructureParentCollection, LittleTile>> iterator() {
                return new Iterator<Pair<IStructureParentCollection, LittleTile>>() {
                    
                    Iterator<LittleTile> inBlock = null;
                    Pair<IStructureParentCollection, LittleTile> pair = null;
                    
                    @Override
                    public boolean hasNext() {
                        while (inBlock == null || !inBlock.hasNext()) {
                            if (!iterator.hasNext())
                                return false;
                            IStructureParentCollection list = iterator.next();
                            pair = new Pair<>(list, null);
                            inBlock = list.iterator();
                        }
                        return true;
                    }
                    
                    @Override
                    public Pair<IStructureParentCollection, LittleTile> next() {
                        pair.setValue(inBlock.next());
                        return pair;
                    }
                };
            }
        };
    }
    
    public HashMapList<BlockPos, IStructureParentCollection> collectAllBlocksListSameWorld() throws CorruptedConnectionException, NotYetConnectedException {
        return collectAllBlocksListSameWorld(new HashMapList<>());
    }
    
    protected HashMapList<BlockPos, IStructureParentCollection> collectAllBlocksListSameWorld(HashMapList<BlockPos, IStructureParentCollection> map) throws CorruptedConnectionException, NotYetConnectedException {
        for (IStructureParentCollection list : blocksList())
            map.add(list.getPos(), list);
        for (StructureChildConnection child : children)
            if (!child.isLinkToAnotherWorld())
                child.getStructure().collectAllBlocksListSameWorld(map);
        return map;
    }
    
    // ================Placing================
    
    /** takes name of stack and connects the structure to its children (does so recursively)
     * 
     * @param stack
     */
    public void placedStructure(@Nullable ItemStack stack) {
        CompoundTag nbt;
        if (name == null && stack != null && (nbt = stack.getTagElement("display")) != null && nbt.contains("Name", 8))
            name = nbt.getString("Name");
        if (!isClient())
            schedule();
    }
    
    public void notifyAfterPlaced() {
        afterPlaced();
        for (StructureChildConnection child : children)
            try {
                child.getStructure().notifyAfterPlaced();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    protected void afterPlaced() {}
    
    // ================Save and loading================
    
    public void loadFromNBT(CompoundTag nbt) {
        blocks.clear();
        
        // LoadTiles
        if (nbt.contains("blocks")) {
            blocks.clear();
            int[] array = nbt.getIntArray("blocks");
            for (int i = 0; i + 2 < array.length; i += 3)
                blocks.add(new StructureBlockConnector(new BlockPos(array[i], array[i + 1], array[i + 2])));
        }
        
        if (nbt.contains("name"))
            name = nbt.getString("name");
        else
            name = null;
        
        // Load family (parent and children)        
        if (nbt.contains("parent"))
            parent = StructureChildConnection.loadFromNBT(this, nbt.getCompound("parent"), true);
        else
            parent = null;
        
        if (nbt.contains("children")) {
            
            ListTag list = nbt.getList("children", 10);
            children = new ChildrenList();
            for (int i = 0; i < list.size(); i++)
                children.set(StructureChildConnection.loadFromNBT(this, list.getCompound(i), false));
            
            if (this instanceof IAnimatedStructure && ((IAnimatedStructure) this).isAnimated())
                for (StructureChildConnection child : children)
                    if (child instanceof StructureChildToSubWorldConnection && ((StructureChildToSubWorldConnection) child).entityUUID
                            .equals(((IAnimatedStructure) this).getAnimation().getUUID()))
                        throw new RuntimeException("Something went wrong during loading!");
                    
        } else
            children = new ChildrenList();
        
        for (StructureDirectionalField field : type.directional) {
            if (nbt.contains(field.saveKey))
                field.createAndSet(this, nbt);
            else
                field.set(this, failedLoadingRelative(nbt, field));
        }
        
        if (nbt.contains("signal")) {
            ListTag list = nbt.getList("signal", 10);
            externalHandler = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                try {
                    SignalExternalOutputHandler handler = new SignalExternalOutputHandler(this, list.getCompound(i));
                    externalHandler.put(handler.index, handler);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        loadFromNBTExtra(nbt);
        if (inputs != null)
            for (int i = 0; i < inputs.length; i++)
                inputs[i].load(nbt);
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                outputs[i].load(nbt.getCompound(outputs[i].component.identifier));
    }
    
    protected Object failedLoadingRelative(CompoundTag nbt, StructureDirectionalField field) {
        return field.getDefault();
    }
    
    protected abstract void loadFromNBTExtra(CompoundTag nbt);
    
    public CompoundTag writeToNBTPreview(CompoundTag nbt, BlockPos newCenter) {
        LittleVecGrid vec = new LittleVecGrid(new LittleVec(mainBlock.getGrid(), getPos().subtract(newCenter)), mainBlock.getGrid());
        
        LittleVecGrid inverted = vec.copy();
        inverted.invert();
        
        for (StructureDirectionalField field : type.directional) {
            Object value = field.get(this);
            field.move(value, vec);
            field.save(nbt, value);
            field.move(value, inverted);
        }
        
        writeToNBTExtraInternal(nbt, true);
        return nbt;
    }
    
    public void writeToNBT(CompoundTag nbt) {
        
        // Save family (parent and children)
        if (parent != null)
            nbt.put("parent", parent.writeToNBT(new CompoundTag()));
        
        if (children != null && !children.isEmpty()) {
            ListTag list = new ListTag();
            for (StructureChildConnection child : children)
                list.add(child.writeToNBT(new CompoundTag()));
            
            nbt.put("children", list);
        }
        
        // Save blocks
        int[] array = new int[blocks.size() * 3];
        for (int i = 0; i < blocks.size(); i++) {
            StructureBlockConnector block = blocks.get(i);
            array[i * 3] = block.pos.getX();
            array[i * 3 + 1] = block.pos.getY();
            array[i * 3 + 2] = block.pos.getZ();
        }
        nbt.putIntArray("blocks", array);
        
        for (StructureDirectionalField field : type.directional) {
            Object value = field.get(this);
            field.save(nbt, value);
        }
        
        writeToNBTExtraInternal(nbt, false);
    }
    
    protected void writeToNBTExtraInternal(CompoundTag nbt, boolean preview) {
        nbt.putString("id", type.id);
        if (name != null)
            nbt.putString("name", name);
        else
            nbt.remove("name");
        
        if (externalHandler != null && !externalHandler.isEmpty()) {
            ListTag list = new ListTag();
            for (SignalExternalOutputHandler handler : externalHandler.values())
                list.add(handler.write(preview));
            nbt.put("signal", list);
        }
        if (inputs != null)
            for (int i = 0; i < inputs.length; i++)
                inputs[i].write(preview, nbt);
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                nbt.put(outputs[i].component.identifier, outputs[i].write(preview, new CompoundTag()));
            
        writeToNBTExtra(nbt);
    }
    
    protected abstract void writeToNBTExtra(CompoundTag nbt);
    
    public void unload() {}
    
    // ====================Destroy====================
    
    public void onLittleTileDestroy() throws CorruptedConnectionException, NotYetConnectedException {
        if (parent != null) {
            parent.getStructure().onLittleTileDestroy();
            return;
        }
        
        load();
        removeStructure();
    }
    
    public void removeStructure() throws CorruptedConnectionException, NotYetConnectedException {
        load();
        onStructureDestroyed();
        
        for (StructureChildConnection child : children)
            child.destroyStructure();
        
        if (this instanceof IAnimatedStructure && ((IAnimatedStructure) this).isAnimated())
            ((IAnimatedStructure) this).destroyAnimation();
        else {
            mainBlock.getBE().updateTiles((x) -> x.removeStructure(getIndex()));
            for (StructureBlockConnector block : blocks)
                block.remove();
        }
        
    }
    
    public void callStructureDestroyedToSameWorld() {
        for (StructureChildConnection child : children)
            if (!child.isLinkToAnotherWorld())
                try {
                    child.getStructure().callStructureDestroyedToSameWorld();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        onStructureDestroyed();
    }
    
    /** Is called before the structure is removed */
    @Override
    public void onStructureDestroyed() {
        unload();
    }
    
    // ================Signal================
    
    public Iterable<ISignalStructureComponent> inputs() {
        return new Iterable<ISignalStructureComponent>() {
            
            @Override
            public Iterator<ISignalStructureComponent> iterator() {
                return new Iterator<ISignalStructureComponent>() {
                    
                    Iterator<StructureChildConnection> iterator = children.iterator();
                    ISignalStructureComponent next;
                    
                    @Override
                    public boolean hasNext() {
                        if (next == null) {
                            while (iterator.hasNext()) {
                                StructureChildConnection connection = iterator.next();
                                try {
                                    if (connection.getStructure() instanceof ISignalStructureComponent && ((ISignalStructureComponent) connection.getStructure())
                                            .getComponentType() == SignalComponentType.INPUT) {
                                        next = (ISignalStructureComponent) connection.getStructure();
                                        break;
                                    }
                                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                            }
                        }
                        return next != null;
                    }
                    
                    @Override
                    public ISignalStructureComponent next() {
                        ISignalStructureComponent result = next;
                        next = null;
                        return result;
                    }
                };
            }
        };
    }
    
    public Iterable<ISignalStructureComponent> outputs() {
        return new Iterable<ISignalStructureComponent>() {
            
            @Override
            public Iterator<ISignalStructureComponent> iterator() {
                return new Iterator<ISignalStructureComponent>() {
                    
                    Iterator<StructureChildConnection> iterator = children.iterator();
                    ISignalStructureComponent next;
                    
                    @Override
                    public boolean hasNext() {
                        if (next == null) {
                            while (iterator.hasNext()) {
                                StructureChildConnection connection = iterator.next();
                                try {
                                    if (connection.getStructure() instanceof ISignalStructureComponent && ((ISignalStructureComponent) connection.getStructure())
                                            .getComponentType() == SignalComponentType.OUTPUT) {
                                        next = (ISignalStructureComponent) connection.getStructure();
                                        break;
                                    }
                                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                            }
                        }
                        return next != null;
                    }
                    
                    @Override
                    public ISignalStructureComponent next() {
                        ISignalStructureComponent result = next;
                        next = null;
                        return result;
                    }
                };
            }
        };
    }
    
    @Override
    public void notifyChange() {
        if (parent != null)
            try {
                parent.getStructure().processSignalChanges();
                return;
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        processSignalChanges();
    }
    
    protected void processSignalChanges() {
        if (externalHandler != null && !externalHandler.isEmpty())
            for (SignalExternalOutputHandler handler : externalHandler.values())
                handler.update();
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                outputs[i].update();
        for (StructureChildConnection child : children)
            try {
                child.getStructure().processSignalChanges();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    @Override
    public boolean isStillAvailable() {
        return !mainBlock.isRemoved();
    }
    
    @Override
    public boolean hasChanged() {
        return signalChanged;
    }
    
    @Override
    public void markChanged() {
        signalChanged = true;
    }
    
    @Override
    public void markUnchanged() {
        signalChanged = false;
    }
    
    public void changed(ISignalComponent changed) {
        schedule();
    }
    
    public InternalSignalInput getInput(int id) {
        if (inputs != null && id < inputs.length)
            return inputs[id];
        return null;
    }
    
    public InternalSignalOutput getOutput(int id) {
        if (outputs != null && id < outputs.length)
            return outputs[id];
        return null;
    }
    
    public void performInternalOutputChange(InternalSignalOutput output) {
        
    }
    
    public void receiveInternalOutputChange(InternalSignalOutput output) {
        
    }
    
    public void setExternalHandler(HashMap<Integer, SignalExternalOutputHandler> handlers) {
        this.externalHandler = handlers;
    }
    
    // ====================Previews====================
    
    public LittleGroupAbsolute getAbsolutePreviews(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        return new LittleGroupAbsolute(pos, getPreviews(pos));
    }
    
    public LittleGroup getPreviews(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        NBTTagCompound structureNBT = new NBTTagCompound();
        this.writeToNBTPreview(structureNBT, pos);
        LittlePreviews previews = new LittlePreviews(structureNBT, LittleGridContext.getMin());
        
        for (Pair<IStructureTileList, LittleTile> pair : tiles()) {
            LittlePreview preview = previews.addTile(pair.key, pair.value);
            preview.box.add(new LittleVec(previews.getContext(), pair.key.getPos().subtract(pos)));
        }
        
        for (StructureChildConnection child : children)
            previews.addChild(child.getStructure().getPreviews(pos), child.dynamic);
        
        previews.convertToSmallest();
        return previews;
    }
    
    public LittleGroupAbsolute getAbsolutePreviewsSameWorldOnly(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        return new LittleGroupAbsolute(pos, getPreviewsSameWorldOnly(pos));
    }
    
    public LittleGroup getPreviewsSameWorldOnly(BlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        CompoundTag structureNBT = new CompoundTag();
        this.writeToNBTPreview(structureNBT, pos);
        LittleGroup previews = new LittleGroup(structureNBT, LittleGrid.min());
        
        for (Pair<IStructureParentCollection, LittleTile> pair : tiles()) {
            LittlePreview preview = previews.addTile(pair.key, pair.value);
            preview.box.add(new LittleVec(previews.getContext(), pair.key.getPos().subtract(pos)));
        }
        
        for (StructureChildConnection child : children)
            if (!child.isLinkToAnotherWorld())
                previews.addChild(child.getStructure().getPreviews(pos), child.dynamic);
            else
                previews.addChild(new LittlePreviewsStructureHolder(child.getStructure()), child.dynamic);
            
        previews.convertToSmallest();
        return previews;
    }
    
    public MutableBlockPos getMinPos(MutableBlockPos pos) throws CorruptedConnectionException, NotYetConnectedException {
        for (StructureBlockConnector block : blocks) {
            BlockPos tePos = block.getAbsolutePos();
            pos.set(Math.min(pos.getX(), tePos.getX()), Math.min(pos.getY(), tePos.getY()), Math.min(pos.getZ(), tePos.getZ()));
        }
        
        for (StructureChildConnection child : children)
            child.getStructure().getMinPos(pos);
        
        return pos;
    }
    
    // ====================Transform & Transfer====================
    
    public void transferChildrenToAnimation(EntityAnimation animation) throws CorruptedConnectionException, NotYetConnectedException {
        for (StructureChildConnection child : children) {
            LittleStructure childStructure = child.getStructure();
            if (child.isLinkToAnotherWorld()) {
                EntityAnimation subAnimation = ((IAnimatedStructure) childStructure).getAnimation();
                int l1 = subAnimation.chunkCoordX;
                int i2 = subAnimation.chunkCoordZ;
                World world = getWorld();
                if (subAnimation.addedToChunk) {
                    Chunk chunk = world.getChunkFromChunkCoords(l1, i2);
                    if (chunk != null)
                        chunk.removeEntity(subAnimation);
                    subAnimation.addedToChunk = false;
                }
                world.loadedEntityList.remove(subAnimation);
                subAnimation.setParentWorld(animation.fakeWorld);
                animation.fakeWorld.spawnEntity(subAnimation);
                subAnimation.updateTickState();
                
                childStructure.updateParentConnection(child.childId, this, child.dynamic);
                this.updateChildConnection(child.childId, childStructure, child.dynamic);
                
            } else
                childStructure.transferChildrenToAnimation(animation);
        }
    }
    
    public void transferChildrenFromAnimation(EntityAnimation animation) throws CorruptedConnectionException, NotYetConnectedException {
        World parentWorld = animation.fakeWorld.getParent();
        for (StructureChildConnection child : children) {
            LittleStructure childStructure = child.getStructure();
            if (child.isLinkToAnotherWorld()) {
                EntityAnimation subAnimation = ((IAnimatedStructure) childStructure).getAnimation();
                int l1 = subAnimation.chunkCoordX;
                int i2 = subAnimation.chunkCoordZ;
                if (subAnimation.addedToChunk) {
                    Chunk chunk = animation.fakeWorld.getChunkFromChunkCoords(l1, i2);
                    if (chunk != null)
                        chunk.removeEntity(subAnimation);
                    subAnimation.addedToChunk = false;
                }
                animation.fakeWorld.loadedEntityList.remove(subAnimation);
                subAnimation.setParentWorld(parentWorld);
                parentWorld.spawnEntity(subAnimation);
                subAnimation.updateTickState();
            } else
                childStructure.transferChildrenFromAnimation(animation);
        }
    }
    
    // ====================Helpers====================
    
    public SurroundingBox getSurroundingBox() throws CorruptedConnectionException, NotYetConnectedException {
        SurroundingBox box = new SurroundingBox(true, getLevel());
        box.add(mainBlock);
        for (StructureBlockConnector block : blocks)
            box.add(block.getList());
        return box;
    }
    
    public double getPercentVolume() throws CorruptedConnectionException, NotYetConnectedException {
        return getSurroundingBox().getPercentVolume();
    }
    
    public Vec3d getHighestCenterVec() throws CorruptedConnectionException, NotYetConnectedException {
        return getSurroundingBox().getHighestCenterVec();
    }
    
    public LittleVecAbsolute getHighestCenterPoint() throws CorruptedConnectionException, NotYetConnectedException {
        return getSurroundingBox().getHighestCenterPoint();
    }
    
    // ====================Packets====================
    
    public void updateStructure() {
        if (getLevel() == null || getLevel().isClientSide)
            return;
        LittleEventHandler.queueStructureForUpdatePacket(this);
    }
    
    public void sendUpdatePacket() {
        if (mainBlock.isRemoved())
            return;
        CompoundTag nbt = new CompoundTag();
        writeToNBT(nbt);
        PacketHandler.sendPacketToTrackingPlayers(new LittleUpdateStructurePacket(getStructureLocation(), nbt), getWorld(), getPos(), null);
    }
    
    // ====================Extra====================
    
    public ItemStack getStructureDrop() throws CorruptedConnectionException, NotYetConnectedException {
        if (parent != null)
            return parent.getStructure().getStructureDrop();
        
        load();
        BlockPos pos = getMinPos(getPos().mutable());
        
        ItemStack stack = new ItemStack(LittleTiles.multiTiles);
        LittlePreviews previews = getPreviews(pos);
        
        LittlePreview.savePreview(previews, stack);
        
        if (name != null) {
            CompoundTag display = new CompoundTag();
            display.putString("Name", name);
            stack.getTag().put("display", display);
        }
        return stack;
    }
    
    public boolean canInteract() {
        return false;
    }
    
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        return InteractionResult.PASS;
    }
    
    public boolean isBed(LivingEntity player) {
        return false;
    }
    
    public void onEntityCollidedWithBlock(Level level, IStructureParentCollection parent, BlockPos pos, Entity entityIn) {}
    
    public void onUpdatePacketReceived() {}
    
    public int getLightValue(BlockPos pos) {
        return 0;
    }
    
    public float getExplosionResistance() {
        return 0;
    }
    
    // ====================Active====================
    
    public boolean hasStructureColor() {
        return false;
    }
    
    public int getStructureColor() {
        return -1;
    }
    
    public int getDefaultColor() {
        return -1;
    }
    
    public void paint(int color) {
        
    }
    
    public void tick() {}
    
    /** only server side **/
    public void queueForNextTick() {
        LittleEventHandler.queueStructureForNextTick(this);
    }
    
    /** only server side **/
    public boolean queueTick() {
        return false;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderTick(PoseStack pose, BlockPos pos, double x, double y, double z, float partialTickTime) {}
    
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 0;
    }
    
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void getRenderingCubes(BlockPos pos, RenderType layer, List<LittleRenderBox> cubes) {}
    
    public VoxelShape getExtraShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    
    public void neighbourChanged() {}
    
    // ====================Mods====================
    
    @Deprecated
    public void flipForWarpDrive(LittleGrid context, Axis axis) {
        List<StructureBlockConnector> newBlocks = new ArrayList<>(blocks.size());
        for (StructureBlockConnector block : blocks)
            newBlocks.add(new StructureBlockConnector(axis.mirror(block.pos)));
        
        blocks.clear();
        blocks.addAll(newBlocks);
        
        for (StructureDirectionalField relative : type.directional)
            relative.flip(relative.get(this), context, axis, context.rotationCenter);
    }
    
    @Deprecated
    public void rotateForWarpDrive(LittleGrid context, Rotation rotation, int steps) {
        List<StructureBlockConnector> newBlocks = new ArrayList<>(blocks.size());
        for (StructureBlockConnector block : blocks) {
            BlockPos pos = block.pos;
            for (int rotationStep = 0; rotationStep < steps; rotationStep++)
                pos = rotation.transform(pos);
            newBlocks.add(new StructureBlockConnector(pos));
        }
        
        blocks.clear();
        blocks.addAll(newBlocks);
        
        for (StructureDirectionalField relative : type.directional)
            relative.rotate(relative.get(this), context, rotation, context.rotationCenter);
    }
    
    public class StructureBlockConnector {
        
        public final BlockPos pos;
        private BETiles cachedBE;
        
        public StructureBlockConnector(BlockPos pos) {
            this.pos = pos;
        }
        
        public BlockPos getAbsolutePos() {
            return getPos().offset(pos);
        }
        
        public BETiles getBlockEntity() throws CorruptedConnectionException, NotYetConnectedException {
            if (cachedBE != null && !cachedBE.isRemoved())
                return cachedBE;
            
            Level level = getLevel();
            
            BlockPos absoluteCoord = getAbsolutePos();
            LevelChunk chunk = level.getChunkAt(absoluteCoord);
            if (WorldUtils.checkIfChunkExists(chunk)) {
                BlockEntity be = level.getBlockEntity(absoluteCoord);
                if (be instanceof BETiles)
                    return cachedBE = (BETiles) be;
                else
                    throw new MissingBlockException(absoluteCoord);
            } else
                throw new NotYetConnectedException();
        }
        
        public void connect() throws CorruptedConnectionException, NotYetConnectedException {
            BETiles be = getBlockEntity();
            if (!be.hasLoaded())
                throw new NotYetConnectedException();
            IStructureParentCollection structure = be.getStructure(getIndex());
            if (structure == null)
                throw new MissingStructureException(be.getBlockPos());
        }
        
        public IStructureParentCollection getList() throws CorruptedConnectionException, NotYetConnectedException {
            BETiles be = getBlockEntity();
            if (!be.hasLoaded())
                throw new NotYetConnectedException();
            IStructureParentCollection structure = be.getStructure(getIndex());
            if (structure != null)
                return structure;
            throw new MissingStructureException(be.getBlockPos());
        }
        
        public int count() throws CorruptedConnectionException, NotYetConnectedException {
            return getList().size();
        }
        
        public void remove() throws CorruptedConnectionException, NotYetConnectedException {
            getBlockEntity().updateTiles((x) -> x.removeStructure(getIndex()));
        }
        
    }
    
    public String info() {
        List<String> infos = new ArrayList<>();
        if (inputs != null)
            for (int i = 0; i < inputs.length; i++)
                infos.add("a" + i + ":" + BooleanUtils.print(inputs[i].getState()));
        for (ISignalStructureComponent component : inputs())
            try {
                infos.add("i" + component.getId() + ":" + BooleanUtils.print(component.getState()));
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                infos.add("i" + component.getId() + ":broken");
            }
        if (outputs != null)
            for (int i = 0; i < outputs.length; i++)
                infos.add("b" + i + ":" + BooleanUtils.print(outputs[i].getState()));
        for (ISignalStructureComponent component : outputs())
            try {
                infos.add("o" + component.getId() + ":" + BooleanUtils.print(component.getState()));
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                infos.add("o" + component.getId() + ":broken");
            }
        return String.join(",", infos);
    }
    
}
