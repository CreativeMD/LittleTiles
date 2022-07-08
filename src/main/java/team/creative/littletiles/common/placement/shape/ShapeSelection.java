package team.creative.littletiles.common.placement.shape;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.GuiMarkShapeSelection;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.mark.IMarkMode;
import team.creative.littletiles.common.placement.shape.ShapeSelection.ShapeSelectPos;

public class ShapeSelection implements Iterable<ShapeSelectPos>, IGridBased, IMarkMode {
    
    public ItemStack stack;
    public ILittleTool tool;
    private final List<ShapeSelectPos> positions = new ArrayList<>();
    
    public final boolean inside;
    
    protected LittleShape shape;
    protected String shapeKey;
    
    protected BlockPos pos;
    private ShapeSelectPos last;
    protected LittleGrid grid = LittleGrid.min();
    
    protected LittleBox overallBox;
    protected ShapeSelectCache cache;
    
    private boolean marked;
    private int markedPosition;
    public boolean allowLowResolution = true;
    
    public ShapeSelection(ItemStack stack, boolean inside) {
        this.inside = inside;
        this.tool = (ILittleTool) stack.getItem();
        this.stack = stack;
        
        shapeKey = getNBT().getString("shape");
        shape = ShapeRegistry.REGISTRY.get(shapeKey);
    }
    
    public CompoundTag getNBT() {
        return stack.getOrCreateTag();
    }
    
    protected boolean requiresCacheUpdate() {
        if (cache == null)
            return true;
        
        if (cache.grid != grid)
            return true;
        
        CompoundTag nbt = getNBT();
        if (!shapeKey.equals(nbt.getString("shape"))) {
            shapeKey = nbt.getString("shape");
            shape = ShapeRegistry.REGISTRY.get(shapeKey);
            if (!cache.shapeKey.equals(shapeKey))
                return true;
        }
        
        if (countPositions() != cache.positions.size())
            return true;
        
        int i = 0;
        for (ShapeSelectPos pos : this) {
            if (!pos.equals(cache.positions.get(i)))
                return true;
            i++;
        }
        
        return false;
    }
    
    public LittleBox getOverallBox() {
        return overallBox.copy();
    }
    
    protected ShapeSelectCache getCache() {
        if (requiresCacheUpdate()) {
            LittleBox[] pointBoxes = new LittleBox[countPositions()];
            List<ShapeSelectPos> positions = new ArrayList<>(pointBoxes.length);
            int i = 0;
            for (ShapeSelectPos pos : ShapeSelection.this) {
                pointBoxes[i] = new LittleBox(pos.pos.getRelative(ShapeSelection.this.pos));
                positions.add(pos.copy());
                i++;
            }
            
            overallBox = new LittleBox(pointBoxes);
            cache = new ShapeSelectCache(grid, positions);
        }
        return cache;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    public LittleBoxes getBoxes(boolean allowLowResolution) {
        if (this.allowLowResolution && allowLowResolution)
            return getCache().get(true);
        return getCache().get(false);
    }
    
    public void deleteCache() {
        cache = null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean addAndCheckIfPlace(Player player, PlacementPosition position, BlockHitResult result) {
        if (marked)
            return true;
        ShapeSelectPos pos = new ShapeSelectPos(player, position, result);
        if ((shape.pointsBeforePlacing > positions.size() + 1 || Screen.hasControlDown()) && (shape.maxAllowed() == -1 || shape.maxAllowed() > positions.size() + 1)) {
            positions.add(pos);
            ensureSameContext(pos);
            return false;
        }
        last = pos;
        ensureSameContext(last);
        return true;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void setLast(Player player, ItemStack stack, PlacementPosition position, BlockHitResult result) {
        this.stack = stack;
        if (result == null)
            return;
        if (positions.isEmpty())
            pos = position.getPos();
        last = new ShapeSelectPos(player, position, result);
        ensureSameContext(last);
    }
    
    private void ensureSameContext(ShapeSelectPos pos) {
        if (grid.count > pos.getGrid().count)
            pos.convertTo(grid);
        else if (grid.count < pos.getGrid().count)
            convertTo(pos.getGrid());
    }
    
    public void toggleMark() {
        if (marked) {
            while (shape.maxAllowed() != -1 && positions.size() >= shape.maxAllowed())
                positions.remove(positions.size() - 1);
            markedPosition = positions.size() - 1;
            marked = false;
        } else {
            markedPosition = positions.size();
            positions.add(last);
            marked = true;
        }
    }
    
    @Override
    public boolean allowLowResolution() {
        return allowLowResolution;
    }
    
    @Override
    public PlacementPosition getPosition() {
        return positions.get(markedPosition).pos.copy();
    }
    
    @Override
    public GuiConfigure getConfigurationGui() {
        return new GuiMarkShapeSelection(this);
    }
    
    @Override
    public void move(LittleGrid grid, Facing facing) {
        positions.get(markedPosition).move(grid, facing);
        deleteCache();
    }
    
    @Override
    public void done() {
        toggleMark();
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(LittleGrid positionGrid, PoseStack pose) {
        if (marked)
            for (int i = 0; i < positions.size(); i++)
                positions.get(i).render(positionGrid, pose, markedPosition == i);
    }
    
    public void rotate(Player player, ItemStack stack, Rotation rotation) {
        shape.rotate(getNBT(), rotation);
        deleteCache();
    }
    
    public void mirror(Player player, ItemStack stack, Axis axis) {
        shape.mirror(getNBT(), axis);
        deleteCache();
    }
    
    @OnlyIn(Dist.CLIENT)
    public void click(Player player) {
        if (!marked)
            return;
        int index = -1;
        double distance = Double.MAX_VALUE;
        float partialTickTime = TickUtils.getFrameTime(player.level);
        Vec3 pos = player.getPosition(partialTickTime);
        double reach = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
        for (int i = 0; i < positions.size(); i++) {
            Optional<Vec3> result = positions.get(i).getBox().clip(pos, look);
            if (result.isPresent()) {
                double tempDistance = pos.distanceToSqr(result.get());
                if (tempDistance < distance) {
                    index = i;
                    distance = tempDistance;
                }
            }
        }
        if (index != -1)
            markedPosition = index;
    }
    
    @Override
    public Iterator<ShapeSelectPos> iterator() {
        if (marked)
            return positions.iterator();
        return new Iterator<ShapeSelection.ShapeSelectPos>() {
            
            private Iterator<ShapeSelectPos> iter = positions.iterator();
            private boolean last = false;
            
            @Override
            public boolean hasNext() {
                return iter.hasNext() || !last;
            }
            
            @Override
            public ShapeSelectPos next() {
                if (iter.hasNext())
                    return iter.next();
                else if (!last) {
                    last = true;
                    return ShapeSelection.this.last;
                }
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public ShapeSelectPos getFirst() {
        if (positions.size() > 0)
            return positions.get(0);
        return last;
    }
    
    public ShapeSelectPos getLast() {
        if (marked)
            return positions.get(positions.size() - 1);
        return last;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        for (ShapeSelectPos other : positions)
            other.convertTo(to);
        grid = to;
    }
    
    @Override
    public int getSmallest() {
        int smallest = LittleGrid.min().count;
        for (int i = 0; i < positions.size(); i++)
            smallest = Math.max(smallest, positions.get(i).getSmallest());
        return smallest;
    }
    
    public int countPositions() {
        return positions.size() + (marked ? 0 : 1);
    }
    
    public class ShapeSelectCache {
        
        protected final List<ShapeSelectPos> positions;
        protected final LittleGrid grid;
        
        protected final LittleShape shape;
        protected final String shapeKey;
        protected final LittleBoxes cachedBoxesLowRes;
        protected LittleBoxes cachedBoxes;
        
        public ShapeSelectCache(LittleGrid grid, List<ShapeSelectPos> positions) {
            this.grid = grid;
            CompoundTag nbt = getNBT();
            shapeKey = nbt.getString("shape");
            shape = ShapeRegistry.REGISTRY.get(shapeKey);
            
            this.positions = positions;
            cachedBoxesLowRes = shape.getBoxes(ShapeSelection.this, true);
        }
        
        public LittleBoxes get(boolean allowLowResolution) {
            if (allowLowResolution)
                return cachedBoxesLowRes;
            if (cachedBoxes == null)
                cachedBoxes = shape.getBoxes(ShapeSelection.this, false);
            return cachedBoxes;
        }
    }
    
    public class ShapeSelectPos implements IGridBased {
        
        public final PlacementPosition pos;
        public final BlockHitResult ray;
        public final LittleTileContext result;
        
        public ShapeSelectPos(Player player, PlacementPosition position, BlockHitResult result) {
            this.pos = position;
            this.ray = result;
            this.result = LittleTileContext.selectFocused(player.level, result.getBlockPos(), player);
            if (inside && result.getDirection().getAxisDirection() == AxisDirection.POSITIVE && grid
                    .isAtEdge(VectorUtils.get(result.getDirection().getAxis(), result.getLocation())))
                pos.getVec().sub(Facing.get(result.getDirection()));
        }
        
        public ShapeSelectPos(PlacementPosition position, BlockHitResult ray, LittleTileContext result) {
            this.pos = position;
            this.ray = ray;
            this.result = result;
        }
        
        public void move(LittleGrid context, Facing facing) {
            LittleVec vec = new LittleVec(facing);
            vec.scale(Screen.hasControlDown() ? context.count : 1);
            pos.subVec(vec);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ShapeSelectPos) {
                if (!pos.equals(((ShapeSelectPos) obj).pos))
                    return false;
                if (result.parent != ((ShapeSelectPos) obj).result.parent)
                    return false;
                if (result.tile != ((ShapeSelectPos) obj).result.tile)
                    return false;
                return true;
            }
            return false;
        }
        
        @OnlyIn(Dist.CLIENT)
        public void render(LittleGrid grid, PoseStack pose, boolean selected) {
            Minecraft mc = Minecraft.getInstance();
            AABB box = this.getBox().inflate(0.002);
            VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
            RenderSystem.lineWidth(4.0F);
            LevelRenderer.renderLineBox(pose, consumer, box, 0, 0, 0, 1F);
            RenderSystem.lineWidth(1.0F);
            LevelRenderer.renderLineBox(pose, consumer, box, 1F, 0.3F, 0.0F, 1F);
        }
        
        public AABB getBox() {
            return pos.getBox(grid);
        }
        
        @Override
        public LittleGrid getGrid() {
            return pos.getGrid();
        }
        
        @Override
        public void convertTo(LittleGrid to) {
            pos.convertTo(to);
        }
        
        @Override
        public int getSmallest() {
            return pos.getSmallest();
        }
        
        public ShapeSelectPos copy() {
            return new ShapeSelectPos(pos.copy(), ray, result);
        }
    }
    
}
