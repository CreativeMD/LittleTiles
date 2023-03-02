package team.creative.littletiles.common.entity.level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.creativecore.common.util.type.set.QuadBitSet;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.level.little.LevelBoundsListener;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleEntityPhysicPacket;

public class BlockUpdateLevelSystem {
    
    public final LittleLevel level;
    
    private final List<LevelBoundsListener> levelBoundListeners = new ArrayList<>();
    private final ConcurrentHashMap<BlockPos, BlockState> changes = new ConcurrentHashMap<>();
    
    private boolean emptyLevel = true;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int minZ = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    
    private boolean[] changed = new boolean[6];
    
    private QuadBitSet east = new QuadBitSet();
    private QuadBitSet west = new QuadBitSet();
    private QuadBitSet up = new QuadBitSet();
    private QuadBitSet down = new QuadBitSet();
    private QuadBitSet south = new QuadBitSet();
    private QuadBitSet north = new QuadBitSet();
    
    public BlockUpdateLevelSystem(LittleLevel level) {
        this.level = level;
    }
    
    public void load(CompoundTag nbt) {
        clearAllEdges();
        if (nbt.getBoolean("empty")) {
            rescanEntireLevel();
            return;
        }
        
        int[] bounds = nbt.getIntArray("bounds");
        if (bounds.length != 6) {
            rescanEntireLevel();
            return;
        }
        
        minX = bounds[0];
        minY = bounds[1];
        minZ = bounds[2];
        maxX = bounds[3];
        maxY = bounds[4];
        maxZ = bounds[5];
        
        emptyLevel = false;
        east.load(nbt.getCompound("e"));
        west.load(nbt.getCompound("w"));
        up.load(nbt.getCompound("u"));
        down.load(nbt.getCompound("d"));
        south.load(nbt.getCompound("s"));
        north.load(nbt.getCompound("n"));
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        if (emptyLevel) {
            nbt.putBoolean("empty", emptyLevel);
            return nbt;
        }
        nbt.putIntArray("bounds", new int[] { minX, minY, minZ, maxX, maxY, maxZ });
        nbt.put("e", east.save());
        nbt.put("w", west.save());
        nbt.put("u", up.save());
        nbt.put("d", down.save());
        nbt.put("s", south.save());
        nbt.put("n", north.save());
        return nbt;
    }
    
    private void clearAllEdges() {
        east.clear();
        west.clear();
        up.clear();
        down.clear();
        south.clear();
        north.clear();
    }
    
    public int get(Facing facing) {
        return switch (facing) {
            case EAST -> maxX;
            case WEST -> minX;
            case UP -> maxY;
            case DOWN -> minY;
            case SOUTH -> maxZ;
            case NORTH -> minZ;
        };
    }
    
    protected void set(Facing facing, int value) {
        switch (facing) {
            case EAST -> maxX = value;
            case WEST -> minX = value;
            case UP -> maxY = value;
            case DOWN -> minY = value;
            case SOUTH -> maxZ = value;
            case NORTH -> minZ = value;
        };
    }
    
    public QuadBitSet getEdgeSet(Facing facing) {
        return switch (facing) {
            case EAST -> east;
            case WEST -> west;
            case UP -> up;
            case DOWN -> down;
            case SOUTH -> south;
            case NORTH -> north;
        };
    }
    
    public void registerLevelBoundListener(LevelBoundsListener listener) {
        this.levelBoundListeners.add(listener);
    }
    
    public Iterable<LevelBoundsListener> levelBoundListeners() {
        return levelBoundListeners;
    }
    
    protected boolean isWithinBoundsNoEdge(BlockPos pos) {
        return minX < pos.getX() && maxX > pos.getX() && minY < pos.getY() && maxY > pos.getY() && minZ < pos.getZ() && maxZ > pos.getZ();
    }
    
    protected boolean isWithinBounds(BlockPos pos) {
        return minX <= pos.getX() && maxX >= pos.getX() && minY <= pos.getY() && maxY >= pos.getY() && minZ <= pos.getZ() && maxZ >= pos.getZ();
    }
    
    private Iterable<BlockPos> edges(Facing facing) {
        MutableBlockPos pos = new MutableBlockPos();
        facing.axis.set(pos, get(facing));
        Axis one = facing.one();
        Axis two = facing.two();
        return () -> new FunctionIterator<>(getEdgeSet(facing), x -> {
            one.set(pos, x.x);
            two.set(pos, x.y);
            return pos;
        });
    }
    
    public void tick(LittleEntity entity) {
        synchronized (changes) {
            if (!changes.isEmpty()) {
                for (Entry<BlockPos, BlockState> entry : changes.entrySet())
                    blockChangedInternal(entry.getKey(), entry.getValue());
                changes.clear();
            }
        }
        
        boolean needsUpdate = false;
        for (int i = 0; i < changed.length; i++) {
            if (!changed[i])
                continue;
            Facing facing = Facing.get(i);
            levelBoundListeners.forEach(x -> x.rescan(level, this, facing, edges(facing), facing.positive ? get(facing) + 1 : get(facing)));
            changed[i] = false;
            needsUpdate = true;
        }
        
        if (needsUpdate) {
            levelBoundListeners.forEach(x -> x.afterChangesApplied(this));
            if (!entity.level.isClientSide)
                LittleTiles.NETWORK.sendToClientTracking(new LittleEntityPhysicPacket(entity), entity);
        }
    }
    
    protected boolean isEmpty(ChunkAccess chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++)
            if (!sections[i].hasOnlyAir())
                return false;
        return true;
    }
    
    protected int getMax(ChunkAccess chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = sections.length - 1; i >= 0; i--)
            if (!sections[i].hasOnlyAir())
                return i;
        return -1;
    }
    
    protected int getMin(ChunkAccess chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++)
            if (!sections[i].hasOnlyAir())
                return i;
        return -1;
    }
    
    protected void findYEdge(Facing facing, int start) {
        QuadBitSet edge = getEdgeSet(facing);
        edge.clear();
        List<ChunkAccess> edgeChunks = new ArrayList<>();
        
        int currentSection = facing.positive ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (ChunkAccess chunk : level.chunks()) {
            int pos = facing.positive ? getMax(chunk) : getMin(chunk);
            if (pos == -1 || (facing.positive ? pos < currentSection : pos > currentSection))
                continue;
            
            if (currentSection == pos)
                edgeChunks.add(chunk);
            else {
                currentSection = pos;
                edgeChunks.clear();
                edgeChunks.add(chunk);
            }
        }
        
        if (edgeChunks.isEmpty()) {
            set(facing, facing.positive ? Integer.MIN_VALUE : Integer.MAX_VALUE);
            emptyLevel = true;
            return;
        }
        
        emptyLevel = false;
        int sectionIndex = level.getSectionYFromSectionIndex(currentSection);
        boolean ignoreStart = SectionPos.blockToSectionCoord(start) != sectionIndex;
        int blockPosOffset = SectionPos.sectionToBlockCoord(sectionIndex);
        int axisValueStart;
        if (ignoreStart)
            axisValueStart = facing.positive ? 15 : 0;
        else
            axisValueStart = facing.positive ? Math.min(start - blockPosOffset, 15) : Math.max(start - blockPosOffset, 0);
        int axisValueEnd = facing.positive ? 0 : 15;
        
        Axis one = facing.one();
        Axis two = facing.two();
        for (ChunkAccess chunk : edgeChunks) {
            int offsetOne = SectionPos.sectionToBlockCoord(one.get(chunk.getPos()));
            int offsetTwo = SectionPos.sectionToBlockCoord(two.get(chunk.getPos()));
            
            LevelChunkSection section = chunk.getSection(currentSection);
            
            for (int axisValue = axisValueStart; facing.positive ? axisValue >= axisValueEnd : axisValue <= axisValueEnd; axisValue += facing.positive ? -1 : 1) {
                boolean found = false;
                
                for (int valueOne = 0; valueOne < LevelChunkSection.SECTION_WIDTH; valueOne++) {
                    for (int valueTwo = 0; valueTwo < LevelChunkSection.SECTION_WIDTH; valueTwo++) {
                        int x = one == Axis.X ? valueOne : valueTwo;
                        int y = axisValue;
                        int z = one == Axis.Z ? valueOne : valueTwo;
                        
                        if (section.getBlockState(x, y, z).isAir())
                            continue;
                        
                        if (facing.positive ? axisValue > axisValueEnd : axisValue < axisValueEnd) {
                            edge.clear();
                            axisValueEnd = axisValue;
                        }
                        
                        edge.set(valueOne + offsetOne, valueTwo + offsetTwo);
                        found = true;
                    }
                }
                
                if (!found)
                    continue;
                
                break;
            }
        }
        
        set(facing, axisValueEnd + blockPosOffset);
    }
    
    protected void findEdge(Facing facing, int start) {
        if (facing.axis == Axis.Y) {
            findYEdge(facing, start);
            return;
        }
        
        Axis axis = facing.axis;
        QuadBitSet edge = getEdgeSet(facing);
        edge.clear();
        List<ChunkAccess> edgeChunks = new ArrayList<>();
        
        int currentChunkPos = facing.positive ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (ChunkAccess chunk : level.chunks()) {
            int pos = axis.get(chunk.getPos());
            if (facing.positive ? pos < currentChunkPos : pos > currentChunkPos)
                continue;
            
            if (isEmpty(chunk))
                continue;
            
            if (currentChunkPos == pos)
                edgeChunks.add(chunk);
            else {
                currentChunkPos = pos;
                edgeChunks.clear();
                edgeChunks.add(chunk);
            }
        }
        
        if (edgeChunks.isEmpty()) {
            set(facing, facing.positive ? Integer.MIN_VALUE : Integer.MAX_VALUE);
            emptyLevel = true;
            return;
        }
        
        emptyLevel = false;
        
        boolean ignoreStart = SectionPos.blockToSectionCoord(start) != currentChunkPos;
        int blockPosOffset = SectionPos.sectionToBlockCoord(currentChunkPos);
        int axisValueStart;
        if (ignoreStart)
            axisValueStart = facing.positive ? 15 : 0;
        else
            axisValueStart = facing.positive ? Math.min(start - blockPosOffset, 15) : Math.max(start - blockPosOffset, 0);
        int axisValueEnd = facing.positive ? 0 : 15;
        
        Axis one = facing.one();
        Axis two = facing.two();
        for (ChunkAccess chunk : edgeChunks) {
            int sectionOffsetX = SectionPos.sectionToBlockCoord(chunk.getPos().x);
            int sectionOffsetZ = SectionPos.sectionToBlockCoord(chunk.getPos().z);
            
            for (int axisValue = axisValueStart; facing.positive ? axisValue >= axisValueEnd : axisValue <= axisValueEnd; axisValue += facing.positive ? -1 : 1) {
                boolean found = false;
                LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    if (sections[i].hasOnlyAir())
                        continue;
                    LevelChunkSection section = sections[i];
                    int offsetOne = one.get(sectionOffsetX, section.bottomBlockY(), sectionOffsetZ);
                    int offsetTwo = two.get(sectionOffsetX, section.bottomBlockY(), sectionOffsetZ);
                    
                    for (int valueOne = 0; valueOne < LevelChunkSection.SECTION_WIDTH; valueOne++) {
                        for (int valueTwo = 0; valueTwo < LevelChunkSection.SECTION_WIDTH; valueTwo++) {
                            int x = one == Axis.X ? valueOne : (two == Axis.X ? valueTwo : axisValue);
                            int y = one == Axis.Y ? valueOne : (two == Axis.Y ? valueTwo : axisValue);
                            int z = one == Axis.Z ? valueOne : (two == Axis.Z ? valueTwo : axisValue);
                            
                            if (section.getBlockState(x, y, z).isAir())
                                continue;
                            
                            if (facing.positive ? axisValue > axisValueEnd : axisValue < axisValueEnd) {
                                edge.clear();
                                axisValueEnd = axisValue;
                            }
                            
                            edge.set(valueOne + offsetOne, valueTwo + offsetTwo);
                            found = true;
                        }
                    }
                }
                
                if (!found)
                    continue;
                
                break;
            }
        }
        
        set(facing, axisValueEnd + blockPosOffset);
    }
    
    @OnlyIn(Dist.CLIENT)
    private boolean isSameThreadClient() {
        return Minecraft.getInstance().isSameThread();
    }
    
    private boolean isSameThread() {
        if (level.isClientSide())
            return isSameThreadClient();
        return level.getServer().isSameThread();
    }
    
    public void blockChanged(BlockPos pos, BlockState newState) {
        synchronized (changes) {
            if (isSameThread() && changes.isEmpty())
                blockChangedInternal(pos, newState);
            else
                changes.put(pos.immutable(), newState);
        }
    }
    
    protected void blockChangedInternal(BlockPos pos, BlockState newState) {
        if (isWithinBoundsNoEdge(pos))
            return;
        
        if (newState.isAir()) { // Shrinking
            for (int i = 0; i < Facing.VALUES.length; i++) {
                Facing facing = Facing.get(i);
                if (facing.axis.get(pos) == get(facing)) {
                    QuadBitSet set = getEdgeSet(facing);
                    set.set(facing.one().get(pos), facing.two().get(pos), false);
                    if (set.isEmpty())
                        findEdge(facing, get(facing) + (facing.positive ? -1 : 1));
                    changed[facing.ordinal()] = true;
                }
            }
            return;
        }
        
        for (int i = 0; i < Facing.VALUES.length; i++) {
            Facing facing = Facing.get(i);
            int bound = get(facing);
            if (facing.axis.get(pos) == bound) {
                QuadBitSet set = getEdgeSet(facing);
                set.set(facing.one().get(pos), facing.two().get(pos), true);
                emptyLevel = false;
                changed[facing.ordinal()] = true;
            } else if (facing.positive ? facing.axis.get(pos) > bound : facing.axis.get(pos) < bound) {
                QuadBitSet set = getEdgeSet(facing);
                set.clear();
                set.set(facing.one().get(pos), facing.two().get(pos), true);
                set(facing, facing.axis.get(pos));
                emptyLevel = false;
                changed[facing.ordinal()] = true;
            }
        }
    }
    
    protected void rescanEntireLevel() {
        for (int i = 0; i < Facing.VALUES.length; i++)
            findEdge(Facing.get(i), Facing.get(i).positive ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        Arrays.fill(changed, true);
    }
    
    public boolean isEntirelyEmpty() {
        return emptyLevel;
    }
}
