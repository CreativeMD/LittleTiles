package team.creative.littletiles.common.block.little.tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3d;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.CopyArrayCollection;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.api.common.block.LittleBlock;
import team.creative.littletiles.client.render.block.LittleBlockClientRegistry;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.collection.LittleCollection;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.IngredientUtils;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxCombiner;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.face.ILittleFace;
import team.creative.littletiles.common.math.vec.LittleVec;

public final class LittleTile extends LittleElement implements Iterable<LittleBox> {
    
    private CopyArrayCollection<LittleBox> boxes;
    
    public LittleTile(LittleElement element, Iterable<LittleBox> boxes) {
        super(element);
        this.boxes = new CopyArrayCollection<>();
        for (LittleBox box : boxes)
            this.boxes.add(box);
    }
    
    public LittleTile(LittleElement element, LittleBox box) {
        super(element);
        this.boxes = new CopyArrayCollection<>(box);
    }
    
    public LittleTile(BlockState state, int color, LittleBox box) {
        super(state, color);
        this.boxes = new CopyArrayCollection<>(box);
    }
    
    @Deprecated
    public LittleTile(BlockState state, LittleBlock block, int color, List<LittleBox> boxes) {
        super(state, block, color);
        this.boxes = new CopyArrayCollection<LittleBox>(boxes);
    }
    
    public LittleTile(BlockState state, int color, Iterable<LittleBox> boxes) {
        super(state, color);
        this.boxes = new CopyArrayCollection<>(boxes);
    }
    
    public LittleTile(BlockState state, int color, List<LittleBox> boxes) {
        super(state, color);
        this.boxes = new CopyArrayCollection<>(boxes);
        
    }
    
    public LittleTile(String name, int color, List<LittleBox> boxes) {
        super(name, color);
        this.boxes = new CopyArrayCollection<>(boxes);
    }
    
    // ================Basics================
    
    public void add(LittleBox box) {
        boxes.add(box);
    }
    
    public void add(Iterable<LittleBox> boxes) {
        for (LittleBox box : boxes)
            this.boxes.add(box);
    }
    
    public boolean remove(LittleCollection parent, Iterable<LittleBox> boxes) {
        boolean result = false;
        for (LittleBox box : boxes)
            result |= this.boxes.remove(box);
        if (this.boxes.isEmpty())
            parent.removeElement(this);
        return result;
    }
    
    public boolean remove(LittleCollection parent, LittleBox box) {
        boolean result = boxes.remove(box);
        if (boxes.isEmpty())
            parent.removeElement(this);
        return result;
    }
    
    public void move(LittleVec vec) {
        for (LittleBox box : boxes)
            box.add(vec);
    }
    
    @Override
    public Iterator<LittleBox> iterator() {
        return boxes.iterator();
    }
    
    public boolean isEmpty() {
        return boxes.isEmpty();
    }
    
    public int size() {
        return boxes.size();
    }
    
    public boolean combine() {
        List<LittleBox> tempBoxes = new ArrayList<>(boxes);
        if (LittleBoxCombiner.combine(tempBoxes)) {
            boxes.clear();
            boxes.addAll(tempBoxes);
            return true;
        }
        return false;
    }
    
    public void combineBlockwise(LittleGrid grid) {
        HashMapList<BlockPos, LittleBox> chunked = new HashMapList<>();
        for (LittleBox box : boxes)
            chunked.add(box.getMinVec().getBlockPos(grid), box);
        
        boxes.clear();
        for (ArrayList<LittleBox> list : chunked.values()) {
            LittleBoxCombiner.combine(list);
            boxes.addAll(list);
        }
    }
    
    public void split(HashMapList<BlockPos, LittleBox> boxes, BlockPos pos, LittleGrid grid, LittleVec offset, LittleBoxReturnedVolume volume) {
        for (LittleBox box : this.boxes)
            box.split(grid, pos, offset, boxes, volume);
    }
    
    @Override
    @Deprecated
    public CompoundTag save(CompoundTag nbt) {
        return super.save(nbt);
    }
    
    public LittleTile copy() {
        Collection<LittleBox> boxes = new CopyArrayCollection<>();
        for (LittleBox box : this.boxes)
            boxes.add(box.copy());
        return new LittleTile(this, boxes);
    }
    
    public LittleTile copy(List<LittleBox> boxes) {
        return new LittleTile(this, boxes);
    }
    
    public LittleTile copyEmpty() {
        return new LittleTile(this, new CopyArrayCollection<>());
    }
    
    @Override
    public int hashCode() {
        return block.hashCode() + color;
    }
    
    @Override
    /** note this does not check for boxes, just for the type */
    public boolean equals(Object obj) {
        if (obj instanceof LittleTile)
            return ((LittleTile) obj).block == block && ((LittleTile) obj).color == color;
        return false;
    }
    
    // ================Properties================
    
    public boolean isTranslucent() {
        return block.isTranslucent() || ColorUtils.isTransparent(color);
    }
    
    public boolean cullOverEdge() {
        return block.cullOverEdge();
    }
    
    // ================Grid================
    
    public int getSmallest(LittleGrid grid) {
        int smallest = 0;
        for (LittleBox box : boxes)
            smallest = Math.max(smallest, box.getSmallest(grid));
        return smallest;
    }
    
    public void convertTo(LittleGrid from, LittleGrid to) {
        for (LittleBox box : boxes)
            box.convertTo(from, to);
    }
    
    // ================Math================
    
    public int getVolume() {
        int volume = 0;
        for (LittleBox box : boxes)
            volume += box.getVolume();
        return volume;
    }
    
    public double getPercentVolume(LittleGrid grid) {
        double volume = 0;
        for (LittleBox box : boxes)
            volume += box.getPercentVolume(grid);
        return volume;
    }
    
    public boolean doesFillEntireBlock(LittleGrid grid) {
        if (boxes.size() == 1)
            return boxes.first().doesFillEntireBlock(grid);
        
        boolean[][][] filled = new boolean[grid.count][grid.count][grid.count];
        for (LittleBox box : boxes)
            box.fillInSpace(filled);
        
        for (int x = 0; x < filled.length; x++)
            for (int y = 0; y < filled[x].length; y++)
                for (int z = 0; z < filled[x][y].length; z++)
                    if (!filled[x][y][z])
                        return false;
        return true;
    }
    
    public void fillFace(IParentCollection parent, ILittleFace face, LittleGrid grid) {
        for (LittleBox box : boxes) {
            if (box == face.box())
                continue;
            if (face.getGrid() != parent.getGrid()) {
                box = box.copy();
                box.convertTo(parent.getGrid(), face.getGrid());
            }
            box.fill(face);
        }
    }
    
    public boolean fillInSpace(boolean[][][] filled) {
        boolean changed = false;
        for (LittleBox box : boxes) {
            for (int x = box.minX; x < box.maxX; x++) {
                for (int y = box.minY; y < box.maxY; y++) {
                    for (int z = box.minZ; z < box.maxZ; z++) {
                        filled[x][y][z] = true;
                        changed = true;
                    }
                }
            }
        }
        
        return changed;
    }
    
    public boolean fillInSpaceInaccurate(LittleBox otherBox, Axis one, Axis two, Axis axis, boolean[][] filled) {
        boolean changed = false;
        for (LittleBox box : boxes)
            changed |= box.fillInSpaceInaccurate(otherBox, one, two, axis, filled);
        return changed;
    }
    
    public boolean fillInSpaceInaccurate(LittleBox otherBox, boolean[][][] filled) {
        boolean changed = false;
        for (LittleBox box : boxes)
            changed |= box.fillInSpaceInaccurate(otherBox, filled);
        return changed;
    }
    
    public boolean fillInSpace(LittleBox otherBox, boolean[][][] filled) {
        boolean changed = false;
        for (LittleBox box : boxes)
            changed |= box.fillInSpace(otherBox, filled);
        return changed;
    }
    
    public boolean intersectsWith(LittleBox intersect) {
        for (LittleBox box : boxes)
            if (LittleBox.intersectsWith(box, intersect))
                return true;
        return false;
    }
    
    public boolean intersectsWith(AABB bb, IParentCollection parent) {
        for (LittleBox box : boxes)
            if (box.intersectsWith(bb, parent.getGrid()))
                return true;
        return false;
    }
    
    public void cutOut(LittleGrid grid, LittleBox cutter, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> result = null;
        for (LittleBox box : boxes) {
            List<LittleBox> temp = box.cutOut(grid, cutter, volume);
            if (result == null)
                result = temp;
            else if (temp != null)
                result.addAll(temp);
        }
        boxes.clear();
        boxes.addAll(result);
    }
    
    public void cutOut(LittleGrid grid, List<LittleBox> cutter, List<LittleBox> cutout, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> result = null;
        for (LittleBox box : boxes) {
            List<LittleBox> temp = box.cutOut(grid, cutter, cutout, volume);
            if (result == null)
                result = temp;
            else if (temp != null)
                result.addAll(temp);
        }
        boxes.clear();
        boxes.addAll(result);
    }
    
    public void getIntersectingBoxes(LittleBox intersect, List<LittleBox> boxes) {
        for (LittleBox box : this.boxes)
            if (LittleBox.intersectsWith(box, intersect))
                boxes.add(box);
    }
    
    public BlockHitResult rayTrace(LittleGrid grid, BlockPos blockPos, Vec3 pos, Vec3 look) {
        double distance = Double.POSITIVE_INFINITY;
        BlockHitResult result = null;
        for (LittleBox box : boxes) {
            BlockHitResult temp = box.rayTrace(grid, blockPos, pos, look);
            double tempDistance = 0;
            if (temp == null)
                continue;
            if (result == null || distance > (tempDistance = temp.getLocation().distanceTo(pos))) {
                result = temp;
                distance = tempDistance;
            }
        }
        return result;
    }
    
    public boolean doesProvideSolidFace() {
        return !isTranslucent();
    }
    
    public boolean contains(LittleBox other) {
        for (LittleBox box : boxes)
            if (box.equals(other))
                return true;
        return false;
    }
    
    public boolean noCollision() {
        return block.noCollision();
    }
    
    // ================Rotating/Mirror================
    
    public void mirror(Axis axis, LittleVec doubledCenter) {
        for (LittleBox box : boxes)
            box.mirror(axis, doubledCenter);
        setState(block.mirror(getState(), axis, doubledCenter));
    }
    
    public void rotate(Rotation rotation, LittleVec doubledCenter) {
        for (LittleBox box : boxes)
            box.rotate(rotation, doubledCenter);
        setState(block.rotate(getState(), rotation, doubledCenter));
    }
    
    // ================Rendering================
    
    public boolean canBeRenderCombined(LittleTile tile) {
        return block.canBeRenderCombined(this, tile);
    }
    
    public void addPlaceBoxes(LittleGrid grid, List<RenderBox> boxes, LittleVec offset) {
        for (LittleBox box : this.boxes)
            boxes.add(box.getRenderingBox(grid, offset));
    }
    
    public void addRenderingBoxes(LittleGrid grid, List<RenderBox> boxes) {
        for (LittleBox box : this.boxes)
            boxes.add(box.getRenderingBox(grid, this));
    }
    
    // ================Sound================
    
    public SoundType getSound() {
        return block.getSoundType();
    }
    
    // ================Block Event================
    
    public float getExplosionResistance() {
        return block.getExplosionResistance(this);
    }
    
    public void onTileExplodes(IParentCollection parent, Explosion explosion) {
        block.exploded(parent, this, explosion);
    }
    
    public void randomDisplayTick(IParentCollection parent, RandomSource rand) {
        block.randomDisplayTick(parent, this, rand);
    }
    
    public boolean canInteract() {
        return block.canInteract();
    }
    
    public InteractionResult use(IParentCollection parent, LittleBox box, BlockPos pos, Player player, BlockHitResult result, InteractionHand hand) {
        return block.use(parent, this, box, player, result, hand);
    }
    
    public int getLightValue() {
        return block.getLightValue();
    }
    
    public float getEnchantPowerBonus(IParentCollection parent) {
        return block.getEnchantPowerBonus(parent, this);
    }
    
    public float getFriction(IParentCollection parent, @Nullable Entity entity) {
        return block.getFriction(parent, this, entity);
    }
    
    public boolean isFluid(TagKey<Fluid> fluid) {
        return block.isFluid(fluid);
    }
    
    public Vector3d getFogColor(IParentCollection parent, Entity entity, Vector3d originalColor, float partialTicks) {
        return block.getFogColor(parent, this, entity, originalColor, partialTicks);
    }
    
    public boolean canBeConvertedToVanilla() {
        for (LittleBox box : boxes)
            if (!box.isSolid())
                return false;
        return block.canBeConvertedToVanilla() && ColorUtils.isDefault(color);
    }
    
    // ================Collision================
    
    public void entityCollided(IParentCollection parent, Entity entity) {
        block.entityCollided(parent, this, entity);
    }
    
    public void collectBoxes(IParentCollection parent, List<ABB> boxes) {
        for (LittleBox box : this.boxes)
            boxes.add(box.getABB(parent.getGrid()));
    }
    
    // ================Ingredient================
    
    @Nullable
    public BlockIngredientEntry getBlockIngredient(LittleGrid context) {
        return IngredientUtils.getBlockIngredient(block, getPercentVolume(context));
    }
    
    @Override
    public String toString() {
        return "[" + getBlockName() + "|" + color + "|" + boxes + "]";
    }
    
    // ================Ingredient================
    
    @OnlyIn(Dist.CLIENT)
    public boolean canRenderInLayer(RenderType layer) {
        if (ColorUtils.isInvisible(color))
            return false;
        if (ColorUtils.isTransparent(color))
            return layer == RenderType.translucent();
        return LittleBlockClientRegistry.canRenderInLayer(getBlock(), layer);
    }
    
}
