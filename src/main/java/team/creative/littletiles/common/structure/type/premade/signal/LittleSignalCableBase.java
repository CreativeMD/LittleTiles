package team.creative.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.SurroundingBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeType;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureBase;
import team.creative.littletiles.common.structure.signal.component.InvalidSignalComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.network.SignalNetwork;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public abstract class LittleSignalCableBase extends LittleStructurePremade implements ISignalStructureBase {
    
    public static final int DEFAULT_CABLE_COLOR = -13619152;
    private SignalNetwork network;
    
    public int color;
    protected LittleConnectionFace[] faces;
    
    public LittleSignalCableBase(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
        this.faces = new LittleConnectionFace[getNumberOfConnections()];
    }
    
    @Override
    public boolean compatible(ISignalStructureBase other) {
        if (ISignalStructureBase.super.compatible(other)) {
            if (other.getComponentType() == SignalComponentType.TRANSMITTER && this.getComponentType() == SignalComponentType.TRANSMITTER)
                return other.getColor() == DEFAULT_CABLE_COLOR || this.getColor() == DEFAULT_CABLE_COLOR || getColor() == other.getColor();
            return true;
        }
        return false;
        
    }
    
    @Override
    public boolean hasStructureColor() {
        return true;
    }
    
    @Override
    public int getStructureColor() {
        return color;
    }
    
    @Override
    public int getDefaultColor() {
        return DEFAULT_CABLE_COLOR;
    }
    
    @Override
    public void paint(int color) {
        this.color = color;
    }
    
    @Override
    public int getColor() {
        return color;
    }
    
    @Override
    public SignalNetwork getNetwork() {
        return network;
    }
    
    @Override
    public void setNetwork(SignalNetwork network) {
        this.network = network;
    }
    
    @Override
    public Level getStructureLevel() {
        return getLevel();
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        int[] result = nbt.getIntArray("faces");
        if (result != null && result.length == getNumberOfConnections() * 3) {
            for (int i = 0; i < faces.length; i++) {
                int distance = result[i * 3];
                if (distance < 0)
                    faces[i] = null;
                else {
                    faces[i] = new LittleConnectionFace();
                    faces[i].distance = distance;
                    faces[i].grid = LittleGrid.get(result[i * 3 + 1]);
                    faces[i].oneSidedRenderer = result[i * 3 + 2] == 1;
                }
            }
        } else if (result != null && result.length == getNumberOfConnections() * 2)
            for (int i = 0; i < faces.length; i++) {
                int distance = result[i * 2];
                if (distance < 0)
                    faces[i] = null;
                else {
                    faces[i] = new LittleConnectionFace();
                    faces[i].distance = distance;
                    faces[i].grid = LittleGrid.get(result[i * 2 + 1]);
                }
            }
        if (nbt.contains("color"))
            color = nbt.getInt("color");
        else
            color = DEFAULT_CABLE_COLOR;
    }
    
    @Override
    protected void saveInternalExtra(CompoundTag nbt, boolean preview) {
        super.saveInternalExtra(nbt, preview);
        if (!preview && faces != null) {
            int[] result = new int[getNumberOfConnections() * 3];
            for (int i = 0; i < faces.length; i++) {
                if (faces[i] != null && !faces[i].invalid) {
                    result[i * 3] = faces[i].distance;
                    result[i * 3 + 1] = faces[i].grid.count;
                    result[i * 3 + 2] = faces[i].oneSidedRenderer ? 1 : 0;
                } else {
                    result[i * 3] = -1;
                    result[i * 3 + 1] = 0;
                    result[i * 3 + 2] = 0;
                }
            }
            nbt.putIntArray("faces", result);
        }
        if (color != -1)
            nbt.putInt("color", color);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
    public abstract Facing getFacing(int index);
    
    public abstract int getIndex(Facing facing);
    
    @Override
    public int getBandwidth() {
        return ((LittleStructureTypeNetwork) type).bandwidth;
    }
    
    public int getNumberOfConnections() {
        return ((LittleStructureTypeNetwork) type).numberOfConnections;
    }
    
    @Override
    public boolean connect(Facing facing, ISignalStructureBase base, LittleGrid grid, int distance, boolean oneSidedRenderer) {
        int index = getIndex(facing);
        if (faces[index] != null) {
            if (faces[index].getConnection() == base)
                return false;
            faces[index].disconnect(facing);
        } else
            faces[index] = new LittleConnectionFace();
        faces[index].connect(base, grid, distance, oneSidedRenderer);
        return true;
    }
    
    @Override
    public void disconnect(Facing facing, ISignalStructureBase base) {
        int index = getIndex(facing);
        if (faces[index] != null) {
            faces[index] = null;
            updateStructure();
        }
    }
    
    @Override
    public void neighbourChanged() {
        try {
            checkConnections();
            
            if (getLevel().isClientSide)
                return;
            
            LittleBoxAbsolute box = getSurroundingBox().getAbsoluteBox();
            boolean changed = false;
            for (int i = 0; i < faces.length; i++) {
                Facing facing = getFacing(i);
                
                LittleConnectResult result = checkConnection(facing, box);
                if (result != null) {
                    changed |= this.connect(facing, result.base, result.grid, result.distance, result.oneSidedRenderer);
                    changed |= result.base.connect(facing.opposite(), this, result.grid, result.distance, result.oneSidedRenderer);
                } else {
                    if (faces[i] != null) {
                        faces[i].disconnect(facing);
                        changed = true;
                    }
                    faces[i] = null;
                }
            }
            
            if (changed)
                findNetwork();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
    }
    
    @Override
    public Iterator<ISignalStructureBase> connections() {
        try {
            checkConnections();
            return new Iterator<ISignalStructureBase>() {
                
                LittleBoxAbsolute box = getSurroundingBox().getAbsoluteBox();
                int index = searchForNextIndex(0);
                
                int searchForNextIndex(int index) {
                    while (index < faces.length && (faces[index] == null || !faces[index].verifyConnect(getFacing(index), box))) {
                        faces[index] = null;
                        index++;
                    }
                    return index;
                }
                
                @Override
                public boolean hasNext() {
                    return index < faces.length && faces[index] != null;
                }
                
                @Override
                public ISignalStructureBase next() {
                    ISignalStructureBase next = faces[index].getConnection();
                    this.index = searchForNextIndex(index + 1);
                    return next;
                }
            };
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        return new Iterator<ISignalStructureBase>() {
            
            @Override
            public boolean hasNext() {
                return false;
            }
            
            @Override
            public ISignalStructureBase next() {
                return null;
            }
            
        };
    }
    
    protected LittleConnectResult checkConnection(Level level, LittleBoxAbsolute box, Facing facing, BlockPos pos) throws ConnectionException, NotYetConnectedException {
        try {
            LevelChunk chunk = level.getChunkAt(pos); // TODO Check if this can even happen, not sure if chunk can be null
            if (chunk == null)
                throw new NotYetConnectedException();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BETiles) {
                BETiles be = (BETiles) blockEntity;
                
                LittleTile closest = null;
                IParentCollection parent = null;
                int minDistance = 0;
                
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
                    LittleTile tile = pair.value;
                    if (!pair.key.isStructureChild(this)) {
                        for (LittleBox tileBox : tile) {
                            int distance = box.getDistanceIfEqualFromOneSide(facing, tileBox, pair.key.getPos(), pair.key.getGrid());
                            if (distance < 0)
                                continue;
                            
                            if (closest == null || minDistance > distance) {
                                closest = tile;
                                parent = pair.key;
                                minDistance = distance;
                            }
                        }
                        
                    }
                }
                
                if (closest != null && parent.isStructure()) {
                    LittleStructure structure = parent.getStructure();
                    
                    if (structure instanceof ISignalStructureBase && ((ISignalStructureBase) structure).compatible(this)) {
                        box = box.createBoxFromFace(facing, minDistance);
                        
                        HashMapList<BlockPos, LittleBox> boxes = box.splitted();
                        for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.entrySet()) {
                            BlockEntity toSearchIn = level.getBlockEntity(entry.getKey());
                            if (toSearchIn instanceof BETiles) {
                                BETiles parsedSearch = (BETiles) toSearchIn;
                                LittleBox toCheck = entry.getValue().get(0);
                                try {
                                    parsedSearch.minGrid(box.getGrid());
                                    if (parsedSearch.getGrid().count > box.getGrid().count)
                                        toCheck.convertTo(box.getGrid(), parsedSearch.getGrid());
                                    
                                    if (!parsedSearch.isSpaceFor(toCheck))
                                        throw new ConnectionException("No space");
                                } finally {
                                    parsedSearch.convertToSmallest();
                                }
                            } else if (!level.getBlockState(entry.getKey()).getMaterial().isReplaceable())
                                throw new ConnectionException("Block in the way");
                        }
                        
                        ISignalStructureBase base = (ISignalStructureBase) structure;
                        if (base.canConnect(facing.opposite()))
                            return new LittleConnectResult(base, box.getGrid(), minDistance, false);
                        throw new ConnectionException("Side is invalid");
                    } else if (closest != null)
                        throw new ConnectionException("Tile in the way");
                }
            } else if (blockEntity instanceof ISignalStructureBase && ((ISignalStructureBase) blockEntity).compatible(this)) {
                LittleGrid grid = box.grid;
                int minDistance = facing.positive ? 0 - grid.toGrid(VectorUtils.get(facing.axis, box.pos) - VectorUtils.get(facing.axis, pos)) - box.box
                        .getMax(facing.axis) : box.box.getMin(facing.axis) - (grid.count - grid.toGrid(VectorUtils.get(facing.axis, box.pos) - VectorUtils.get(facing.axis, pos)));
                
                box = box.createBoxFromFace(facing, minDistance);
                
                HashMapList<BlockPos, LittleBox> boxes = box.splitted();
                for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.entrySet()) {
                    BlockEntity toSearchIn = level.getBlockEntity(entry.getKey());
                    if (toSearchIn instanceof BETiles) {
                        BETiles parsedSearch = (BETiles) toSearchIn;
                        LittleBox toCheck = entry.getValue().get(0);
                        try {
                            parsedSearch.minGrid(box.getGrid());
                            if (parsedSearch.getGrid().count > box.getGrid().count)
                                toCheck.convertTo(box.getGrid(), parsedSearch.getGrid());
                            
                            if (!parsedSearch.isSpaceFor(toCheck))
                                throw new ConnectionException("No space");
                        } finally {
                            parsedSearch.convertToSmallest();
                        }
                    } else if (!level.getBlockState(entry.getKey()).getMaterial().isReplaceable())
                        throw new ConnectionException("Block in the way");
                }
                
                ISignalStructureBase base = (ISignalStructureBase) blockEntity;
                if (base.canConnect(facing.opposite()))
                    return new LittleConnectResult(base, box.getGrid(), minDistance, true);
                throw new ConnectionException("Side is invalid");
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        return null;
    }
    
    public LittleConnectResult checkConnection(Facing facing, LittleBoxAbsolute box) throws NotYetConnectedException {
        if (!canConnect(facing))
            return null;
        
        BlockPos pos = box.getMinPos();
        if (facing.positive)
            pos = VectorUtils.set(pos, box.getMaxPos(facing.axis), facing.axis);
        
        Level level = getLevel();
        
        try {
            if (facing.positive ? box.getMaxGridFrom(facing.axis, pos) < box.getGrid().count : box.getMinGridFrom(facing.axis, pos) > 0) {
                LittleConnectResult result = checkConnection(level, box, facing, pos);
                if (result != null)
                    return result;
            }
            
            return checkConnection(level, box, facing, pos.relative(facing.toVanilla()));
        } catch (ConnectionException e) {
            return null;
        }
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void getRenderingBoxes(BlockPos pos, RenderType layer, List<LittleRenderBox> cubes) {
        if (ColorUtils.isInvisible(color))
            return;
        
        if (layer != (ColorUtils.isTransparent(color) ? RenderType.translucent() : RenderType.solid()))
            return;
        
        try {
            SurroundingBox box = getSurroundingBox();
            LittleVec min = box.getMinPosOffset();
            LittleVec max = box.getSize();
            max.add(min);
            LittleBox overallBox = new LittleBox(min, max);
            BlockPos difference = pos.subtract(box.getMinPos());
            overallBox.sub(box.getGrid().toGrid(difference.getX()), box.getGrid().toGrid(difference.getY()), box.getGrid().toGrid(difference.getZ()));
            
            render(box, overallBox, cubes);
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderFace(Facing facing, LittleGrid grid, LittleBox renderBox, int distance, Axis axis, Axis one, Axis two, boolean positive, boolean oneSidedRenderer, List<LittleRenderBox> cubes) {
        if (positive) {
            renderBox.setMin(axis, renderBox.getMax(axis));
            renderBox.setMax(axis, renderBox.getMax(axis) + distance);
        } else {
            renderBox.setMax(axis, renderBox.getMin(axis));
            renderBox.setMin(axis, renderBox.getMin(axis) - distance);
        }
        
        LittleRenderBox cube = renderBox.getRenderingBox(grid, LittleTilesRegistry.SINGLE_CABLE.get().defaultBlockState().setValue(BlockStateProperties.AXIS, axis.toVanilla()));
        if (!oneSidedRenderer) {
            if (positive)
                cube.setMax(axis, cube.getMin(axis) + cube.getSize(axis) / 2);
            else
                cube.setMin(axis, cube.getMax(axis) - cube.getSize(axis) / 2);
        }
        
        cube.color = color;
        cube.keepVU = true;
        cube.allowOverlap = true;
        float shrink = 0.18F;
        float shrinkOne = cube.getSize(one) * shrink;
        float shrinkTwo = cube.getSize(two) * shrink;
        cube.setMin(one, cube.getMin(one) + shrinkOne);
        cube.setMax(one, cube.getMax(one) - shrinkOne);
        cube.setMin(two, cube.getMin(two) + shrinkTwo);
        cube.setMax(two, cube.getMax(two) - shrinkTwo);
        cubes.add(cube);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void render(SurroundingBox box, LittleBox overallBox, List<LittleRenderBox> cubes) {
        
        for (int i = 0; i < faces.length; i++) {
            if (faces[i] == null)
                continue;
            
            int distance = faces[i].distance;
            Facing facing = getFacing(i);
            
            Axis one = facing.axis.one();
            Axis two = facing.axis.two();
            LittleGrid context = faces[i].grid;
            
            LittleBox renderBox = overallBox.copy();
            
            if (box.getGrid().count > context.count) {
                distance *= box.getGrid().count / context.count;
                context = box.getGrid();
            } else if (context.count > box.getGrid().count)
                renderBox.convertTo(box.getGrid(), context);
            
            renderFace(facing, context, renderBox, distance, facing.axis, one, two, facing.positive, faces[i].oneSidedRenderer, cubes);
        }
    }
    
    @Override
    public void onStructureDestroyed() {
        if (network != null)
            if (network.remove(this)) {
                for (int i = 0; i < faces.length; i++) {
                    if (faces[i] != null) {
                        ISignalStructureBase connection = faces[i].connection;
                        faces[i].disconnect(getFacing(i));
                        connection.findNetwork();
                    }
                }
            }
    }
    
    @Override
    public void unload() {
        super.unload();
        for (int i = 0; i < faces.length; i++)
            if (faces[i] != null)
                faces[i].unload(getFacing(i));
        if (network != null)
            network.unload(this);
    }
    
    @Override
    public void unload(Facing facing, ISignalStructureBase base) {
        int index = getIndex(facing);
        if (faces[index] != null)
            faces[index].connection.unload(facing, base);
    }
    
    public class LittleConnectionFace {
        
        public ISignalStructureBase connection;
        public int distance;
        public LittleGrid grid;
        public boolean oneSidedRenderer;
        private boolean invalid;
        
        public LittleConnectionFace() {
            
        }
        
        public void disconnect(Facing facing) {
            if (connection != null)
                connection.disconnect(facing.opposite(), LittleSignalCableBase.this);
            if (hasNetwork())
                getNetwork().remove(connection);
            connection = null;
            updateStructure();
        }
        
        public void unload(Facing facing) {
            if (connection != null)
                connection.unload(facing.opposite(), LittleSignalCableBase.this);
            connection = null;
        }
        
        public void connect(ISignalStructureBase connection, LittleGrid grid, int distance, boolean oneSidedRenderer) {
            if (this.connection != null)
                throw new RuntimeException("Cannot connect until old connection is closed");
            
            this.oneSidedRenderer = oneSidedRenderer;
            if (hasNetwork())
                getNetwork().add(connection);
            this.connection = connection;
            this.grid = grid;
            this.distance = distance;
            updateStructure();
        }
        
        public boolean verifyConnect(Facing facing, LittleBoxAbsolute box) {
            if (connection != null)
                return true;
            
            try {
                LittleConnectResult result = checkConnection(facing, box);
                invalid = false;
                if (result != null) {
                    this.connection = result.base;
                    this.grid = result.grid;
                    this.distance = result.distance;
                    return true;
                }
            } catch (NotYetConnectedException e) {
                invalid = true;
            }
            return false;
            
        }
        
        public ISignalStructureBase getConnection() {
            if (invalid)
                return InvalidSignalComponent.INSTANCE;
            return connection;
        }
    }
    
    public static class LittleConnectResult {
        
        public final ISignalStructureBase base;
        public final LittleGrid grid;
        public final int distance;
        public final boolean oneSidedRenderer;
        
        public LittleConnectResult(ISignalStructureBase base, LittleGrid grid, int distance, boolean oneSidedRenderer) {
            this.base = base;
            this.grid = grid;
            this.distance = distance;
            this.oneSidedRenderer = oneSidedRenderer;
        }
    }
    
    public static class ConnectionException extends Exception {
        
        public ConnectionException(String msg) {
            super(msg);
        }
        
    }
    
    public static abstract class LittleStructureTypeNetwork extends LittlePremadeType implements ISignalComponent {
        
        public final int bandwidth;
        public final int numberOfConnections;
        
        public <T extends LittleStructure> LittleStructureTypeNetwork(String id, Class<T> structureClass, BiFunction<LittleStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid, int bandwidth, int numberOfConnections) {
            super(id, structureClass, factory, attribute.neighborListener(), modid);
            this.bandwidth = bandwidth;
            this.numberOfConnections = numberOfConnections;
        }
        
        public int getColor(LittleGroup group) {
            if (group.hasStructure() && group.getStructureTag().contains("color"))
                return group.getStructureTag().getInt("color");
            return DEFAULT_CABLE_COLOR;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getPositingCubes(Level level, BlockPos pos, ItemStack stack) {
            
            try {
                List<RenderBox> cubes = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    Facing facing = Facing.values()[i];
                    
                    BlockEntity blockEntity = level.getBlockEntity(pos.relative(facing.toVanilla()));
                    if (blockEntity instanceof BETiles) {
                        for (LittleStructure structure : ((BETiles) blockEntity).loadedStructures()) {
                            if (structure instanceof ISignalStructureBase && ((ISignalStructureBase) structure).getBandwidth() == bandwidth && ((ISignalStructureBase) structure)
                                    .canConnect(facing.opposite())) {
                                RenderBox cube = new RenderBox(new AlignedBox(structure.getSurroundingBox().getAABB()
                                        .move(-blockEntity.getBlockPos().getX(), -blockEntity.getBlockPos().getY(), -blockEntity.getBlockPos().getZ())));
                                cube.setMin(facing.axis, 0);
                                cube.setMax(facing.axis, 1);
                                cubes.add(cube);
                            }
                        }
                        
                    }
                }
                
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof BETiles) {
                    for (LittleStructure structure : ((BETiles) blockEntity).loadedStructures()) {
                        if (structure instanceof ISignalStructureBase && ((ISignalStructureBase) structure).getBandwidth() == bandwidth) {
                            AABB box = structure.getSurroundingBox().getAABB()
                                    .move(-blockEntity.getBlockPos().getX(), -blockEntity.getBlockPos().getY(), -blockEntity.getBlockPos().getZ());
                            RenderBox cube;
                            
                            if (((ISignalStructureBase) structure).canConnect(Facing.WEST) || ((ISignalStructureBase) structure).canConnect(Facing.EAST)) {
                                cube = new RenderBox(new AlignedBox(box));
                                if (((ISignalStructureBase) structure).canConnect(Facing.WEST))
                                    cube.setMin(Axis.X, 0);
                                if (((ISignalStructureBase) structure).canConnect(Facing.EAST))
                                    cube.setMax(Axis.X, 1);
                                cubes.add(cube);
                            }
                            
                            if (((ISignalStructureBase) structure).canConnect(Facing.DOWN) || ((ISignalStructureBase) structure).canConnect(Facing.UP)) {
                                cube = new RenderBox(new AlignedBox(box));
                                if (((ISignalStructureBase) structure).canConnect(Facing.DOWN))
                                    cube.setMin(Axis.Y, 0);
                                if (((ISignalStructureBase) structure).canConnect(Facing.UP))
                                    cube.setMax(Axis.Y, 1);
                                cubes.add(cube);
                            }
                            
                            if (((ISignalStructureBase) structure).canConnect(Facing.NORTH) || ((ISignalStructureBase) structure).canConnect(Facing.SOUTH)) {
                                cube = new RenderBox(new AlignedBox(box));
                                if (((ISignalStructureBase) structure).canConnect(Facing.NORTH))
                                    cube.setMin(Axis.Z, 0);
                                if (((ISignalStructureBase) structure).canConnect(Facing.SOUTH))
                                    cube.setMax(Axis.Z, 1);
                                cubes.add(cube);
                            }
                        }
                    }
                    
                }
                if (cubes.isEmpty())
                    return null;
                for (RenderBox cube : cubes)
                    cube.color = ColorUtils.rgba(255, 255, 255, 90);
                return cubes;
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            
            return null;
        }
        
        @Override
        public Level getStructureLevel() {
            return null;
        }
        
        @Override
        public LittleStructure getStructure() {
            return null;
        }
    }
    
}
