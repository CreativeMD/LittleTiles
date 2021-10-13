package team.creative.littletiles.common.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.tile.place.PlacePreview;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.creativecore.common.util.type.SingletonList;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.little.LittleBlockRegistry;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.IngredientUtils;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxCombiner;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.parent.IParentCollection;
import team.creative.littletiles.common.tile.parent.ParentCollection;

public final class LittleTile implements Iterable<LittleBox> {
    
    private LittleBlock block;
    public final int color;
    private List<LittleBox> boxes;
    
    public LittleTile(LittleBlock block, int color, LittleBox box) {
        this.block = block;
        this.color = color;
        this.boxes = new SingletonList<>(box);
    }
    
    public LittleTile(LittleBlock block, int color, Iterable<LittleBox> boxes) {
        this.block = block;
        this.color = color;
        if (boxes instanceof SingletonList)
            this.boxes = new SingletonList<LittleBox>(((SingletonList<LittleBox>) boxes).get(0));
        else {
            this.boxes = new ArrayList<>();
            for (LittleBox box : boxes)
                this.boxes.add(box);
        }
    }
    
    public LittleTile(LittleBlock block, int color, List<LittleBox> boxes) {
        this.block = block;
        this.color = color;
        this.boxes = new ArrayList<>();
        for (LittleBox box : boxes)
            this.boxes.add(box);
    }
    
    public LittleTile(CompoundTag nbt) {
        this.block = LittleBlockRegistry.get(nbt.getString("b"));
        this.color = nbt.contains("c") ? nbt.getInt("c") : ColorUtils.WHITE;
        ListTag list = nbt.getList("s", Tag.TAG_INT_ARRAY);
        this.boxes = list.size() == 1 ? new SingletonList(LittleBox.create(list.getIntArray(0))) : new ArrayList<>();
        if (list.size() > 1)
            for (Tag tag : list)
                this.boxes.add(LittleBox.create(((IntArrayTag) tag).getAsIntArray()));
    }
    
    // ================Basics================
    
    public LittleBlock getBlock() {
        return block;
    }
    
    private void prepareExpand() {
        if (boxes instanceof SingletonList) {
            LittleBox box = boxes.get(0);
            boxes = new ArrayList<>();
            boxes.add(box);
        }
    }
    
    public void add(LittleBox box) {
        prepareExpand();
        boxes.add(box);
    }
    
    public void add(Iterable<LittleBox> boxes) {
        prepareExpand();
        for (LittleBox box : boxes)
            this.boxes.add(box);
    }
    
    public void remove(ParentCollection parent, LittleBox box) {
        boxes.remove(box);
        if (boxes.isEmpty())
            parent.remove(this);
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
        return LittleBoxCombiner.combine(boxes);
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
    
    public CompoundTag write(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (LittleBox box : boxes)
            list.add(box.getArrayTag());
        nbt.put("s", list);
        if (ColorUtils.isDefault(color))
            nbt.remove("c");
        else
            nbt.putInt("c", color);
        
        nbt.putString("b", block.blockName());
        return nbt;
    }
    
    public LittleTile copy() {
        List<LittleBox> boxes = new ArrayList<>();
        for (LittleBox box : this.boxes)
            boxes.add(box.copy());
        return new LittleTile(block, color, boxes);
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
    
    public boolean hasColor() {
        return ColorUtils.isDefault(color);
    }
    
    public boolean isTranslucent() {
        return block.isTranslucent() || ColorUtils.isTransparent(color);
    }
    
    // ================Grid================
    
    public int getSmallest(LittleGrid grid) {
        int smallest = 0;
        for (int i = 0; i < boxes.size(); i++)
            smallest = Math.max(smallest, boxes.get(i).getSmallest(grid));
        return smallest;
    }
    
    public void convertTo(LittleGrid from, LittleGrid to) {
        for (int i = 0; i < boxes.size(); i++)
            boxes.get(i).convertTo(from, to);
    }
    
    // ================Math================
    
    public int getVolume() {
        int volume = 0;
        for (int i = 0; i < boxes.size(); i++)
            volume += boxes.get(i).getVolume();
        return volume;
    }
    
    public double getPercentVolume(LittleGrid grid) {
        double volume = 0;
        for (int i = 0; i < boxes.size(); i++)
            volume += boxes.get(i).getPercentVolume(grid);
        return volume;
    }
    
    public boolean doesFillEntireBlock(LittleGrid grid) {
        if (boxes.size() == 1)
            return boxes.get(0).doesFillEntireBlock(grid);
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
    
    public void fillFace(IParentCollection parent, LittleFace face, LittleGrid grid) {
        for (LittleBox box : boxes) {
            if (face.grid != parent.getGrid()) {
                box = box.copy();
                box.convertTo(parent.getGrid(), face.grid);
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
    
    public List<LittleBox> cutOut(LittleBox cutter, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> result = null;
        for (LittleBox box : boxes) {
            List<LittleBox> temp = box.cutOut(cutter, volume);
            if (result == null)
                result = temp;
            else if (temp != null)
                result.addAll(temp);
        }
        return result;
    }
    
    public List<LittleBox> cutOut(List<LittleBox> cutter, List<LittleBox> cutout, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> result = null;
        for (LittleBox box : boxes) {
            List<LittleBox> temp = box.cutOut(cutter, cutout, volume);
            if (result == null)
                result = temp;
            else if (temp != null)
                result.addAll(temp);
        }
        return result;
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
        block = block.mirror(axis, doubledCenter);
    }
    
    public void rotate(Rotation rotation, LittleVec doubledCenter) {
        for (LittleBox box : boxes)
            box.rotate(rotation, doubledCenter);
        block = block.rotate(rotation, doubledCenter);
    }
    
    // ================Rendering================
    
    @OnlyIn(Dist.CLIENT)
    public boolean canBeRenderCombined(LittleTile tile) {
        return block.canBeRenderCombined(this, tile);
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
    
    public void randomDisplayTick(IParentCollection parent, Random rand) {
        block.randomDisplayTick(parent, this, rand);
    }
    
    public boolean canInteract() {
        return block.canInteract();
    }
    
    public InteractionResult use(IParentCollection parent, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        return block.use(parent, this, box, player, hand, result);
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
    
    public boolean isMaterial(Material material) {
        return block.isMaterial(material);
    }
    
    public boolean isLiquid() {
        return block.isLiquid();
    }
    
    public Vec3d getFogColor(IParentCollection parent, Entity entity, Vec3d originalColor, float partialTicks) {
        return block.getFogColor(parent, this, entity, originalColor, partialTicks);
    }
    
    public Vec3d modifyAcceleration(IParentCollection parent, Entity entity, Vec3d motion) {
        return block.modifyAcceleration(parent, this, entity, motion);
    }
    
    public boolean canBeConvertedToVanilla() {
        for (LittleBox box : boxes)
            if (!box.isSolid())
                return false;
        return block.canBeConvertedToVanilla();
    }
    
    // ================Collision================
    
    public boolean checkEntityCollision() {
        return block.checkEntityCollision();
    }
    
    public void entityCollided(IParentCollection parent, Entity entity) {
        block.entityCollided(parent, this, entity);
    }
    
    public VoxelShape getShapes(IParentCollection parent) {
        VoxelShape shape = Shapes.empty();
        for (LittleBox box : boxes)
            shape = Shapes.or(shape, box.getShape(parent.getGrid()));
        return shape;
    }
    
    // ================Ingredient================
    
    @Nullable
    public BlockIngredientEntry getBlockIngredient(LittleGrid context) {
        return IngredientUtils.getBlockIngredient(block, getPercentVolume(context));
    }
    
    public PlacePreview getPlaceableTile(LittleVec offset) {
        LittleBox newBox = this.box.copy();
        if (offset != null)
            newBox.add(offset);
        return new PlacePreview(newBox, this);
    }
}
