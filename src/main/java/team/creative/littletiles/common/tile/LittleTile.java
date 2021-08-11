package team.creative.littletiles.common.tile;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.tile.BlockRenderLayer;
import com.creativemd.littletiles.common.tile.EntityPlayer;
import com.creativemd.littletiles.common.tile.EnumFacing;
import com.creativemd.littletiles.common.tile.EnumHand;
import com.creativemd.littletiles.common.tile.IBlockAccess;
import com.creativemd.littletiles.common.tile.LittleTile.MissingBlockHandler;
import com.creativemd.littletiles.common.tile.NBTTagCompound;
import com.creativemd.littletiles.common.tile.NBTTagList;
import com.creativemd.littletiles.common.tile.SideOnly;
import com.creativemd.littletiles.common.tile.combine.ICombinable;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxReturnedVolume;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.Axis;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittleTile;
import com.creativemd.littletiles.common.tile.preview.RenderBox;
import com.creativemd.littletiles.common.tile.preview.Rotation;
import com.creativemd.littletiles.common.tile.registry.LittleTileType;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.IngredientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemBlockTiles;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.face.LittleBoxFace;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public final class LittleTile {
    
    public final LittleBlock block;
    public final int color;
    public final List<LittleBox> boxes = null;
    
    public LittleTile(LittleBlock block, int color, LittleBox box) {
        this(block, color);
        addBox(box);
    }
    
    public LittleTile(LittleBlock block, int color, Iterable<LittleBox> boxes) {
        this(block, color);
        addBoxes(boxes);
    }
    
    private LittleTile(LittleBlock block, int color) {
        this.block = block;
        this.color = color;
    }
    
    public LittleTile(CompoundTag nbt) {
        box = LittleBox.createBox(nbt.getIntArray("box"));
        String[] parts = nbt.getString("block").split(":");
        if (parts.length == 3)
            setBlock(nbt.getString("block"), Block.getBlockFromName(parts[0] + ":" + parts[1]), Integer.parseInt(parts[2]));
        else
            setBlock(nbt.getString("block"), Block.getBlockFromName(parts[0] + ":" + parts[1]), 0);
    }
    
    // ================Basics================
    
    protected void addBox(LittleBox box) {
        adas
    }
    
    protected void addBoxes(Iterable<LittleBox> boxes) {
        adasd
    }
    
    public void combine() {
        BasicCombiner.combineBoxes(boxes);
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
        LittleTile tile = new LittleTile(block, color);
        for (LittleBox box : boxes)
            tile.addBox(box.copy());
        return tile;
    }
    
    // ================Properties================
    
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
    
    public double getVolume() {
        double volume = 0;
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
        do the calcuations
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
    
    public List<LittleBox> cutOut(LittleBox box, @Nullable LittleBoxReturnedVolume volume) {
        return this.box.cutOut(box, volume);
    }
    
    public List<LittleBox> cutOut(List<LittleBox> boxes, List<LittleBox> cutout, @Nullable LittleBoxReturnedVolume volume) {
        return this.box.cutOut(boxes, cutout, volume);
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
    
    public boolean equalsBox(LittleBox box) {
        return this.box.equals(box);
    }
    
    public boolean doesProvideSolidFace(EnumFacing facing) {
        return !invisible && box.isFaceSolid(facing) && !isTranslucent() && block != Blocks.BARRIER;
    }
    
    public boolean doesTouch(LittleTile tile) {
        return box.doesTouch(tile.box);
    }
    
    // ================Rotating/Mirror================
    
    public void mirror(Axis axis, LittleVec doubledCenter) {
        box.flipBox(axis, doubledCenter);
        getSpecialHandler().flipPreview(axis, this, doubledCenter);
    }
    
    public void rotate(Rotation rotation, LittleVec doubledCenter) {
        box.rotateBox(rotation, doubledCenter);
        getSpecialHandler().rotatePreview(rotation, this, doubledCenter);
    }
    
    // ================Drop================
    
    public ItemStack getDrop(LittleGridContext context) {
        return getDropInternal(context);
    }
    
    protected ItemStack getDropInternal(LittleGridContext context) {
        return ItemBlockTiles.getStackFromPreview(context, getPreviewTile());
    }
    
    public LittlePreview getPreviewTile() {
        if (hasSpecialBlockHandler()) {
            LittlePreview preview = handler.getPreview(this);
            if (preview != null)
                return preview;
        }
        
        NBTTagCompound nbt = new NBTTagCompound();
        saveTileExtra(nbt);
        LittleTileType type = getType();
        if (type.saveId)
            nbt.setString("tID", type.id);
        return new LittlePreview(box.copy(), nbt);
    }
    
    // ================Rendering================
    
    @SideOnly(Side.CLIENT)
    public boolean shouldBeRenderedInLayer(BlockRenderLayer layer) {
        if (FMLClientHandler.instance().hasOptifine() && block.canRenderInLayer(state, BlockRenderLayer.CUTOUT))
            return layer == BlockRenderLayer.CUTOUT_MIPPED; // Should fix an Optifine bug
            
        try {
            return block.canRenderInLayer(getBlockState(), layer);
        } catch (Exception e) {
            try {
                return block.getBlockLayer() == layer;
            } catch (Exception e2) {
                return layer == BlockRenderLayer.SOLID;
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    public final LittleRenderBox getRenderingCube(LittleGridContext context, BlockRenderLayer layer) {
        if (invisible)
            return null;
        return getInternalRenderingCube(context, layer);
    }
    
    @SideOnly(Side.CLIENT)
    protected LittleRenderBox getInternalRenderingCube(LittleGridContext context, BlockRenderLayer layer) {
        if (block != Blocks.BARRIER)
            return box.getRenderingCube(context, block, meta);
        return null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public RenderBox getPreviewBox(LittleGrid context) {
        RenderBox cube = box.getRenderingCube(context, getBlock(), getMeta());
        cube.color = getColor();
        return cube;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean canBeRenderCombined(LittleTile tile) {
        if (this.invisible != tile.invisible)
            return false;
        
        if (block == tile.block && meta == tile.meta && block != Blocks.BARRIER && tile.block != Blocks.BARRIER)
            return true;
        
        if (hasSpecialBlockHandler() && handler.canBeRenderCombined(this, tile))
            return true;
        
        return false;
    }
    
    // ================Sound================
    
    public SoundType getSound() {
        return block.getSoundType();
    }
    
    // ================Placing================
    
    public PlacePreview getPlaceableTile(LittleVec offset) {
        LittleBox newBox = this.box.copy();
        if (offset != null)
            newBox.add(offset);
        return new PlacePreview(newBox, this);
    }
    
    // ================Interaction================
    
    public boolean canSawResizeTile(EnumFacing facing, EntityPlayer player) {
        return true;
    }
    
    public boolean canBeMoved(EnumFacing facing) {
        return true;
    }
    
    // ================Block Event================
    
    public float getExplosionResistance() {
        return block.getExplosionResistance(null);
    }
    
    public void onTileExplodes(IParentCollection parent, Explosion explosion) {
        if (hasSpecialBlockHandler())
            handler.onTileExplodes(parent, this, explosion);
    }
    
    public void randomDisplayTick(IParentCollection parent, Random rand) {
        if (hasSpecialBlockHandler())
            handler.randomDisplayTick(parent, this, rand);
        else
            block.randomDisplayTick(getBlockState(), parent.getWorld(), parent.getPos(), rand);
        
        if (block == Blocks.BARRIER)
            spawnBarrierParticles(parent.getPos());
    }
    
    @SideOnly(Side.CLIENT)
    private void spawnBarrierParticles(BlockPos pos) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack itemstack = mc.player.getHeldItemMainhand();
        if (mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER))
            mc.world.spawnParticle(EnumParticleTypes.BARRIER, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D, new int[0]);
    }
    
    public boolean canInteract(BlockGetter level, BlockPos pos) {
        block.canInteract();
        return adasds;
    }
    
    public boolean onBlockActivated(IParentTileList parent, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (hasSpecialBlockHandler())
            return handler.onBlockActivated(parent, this, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
        return block.onBlockActivated(parent.getWorld(), parent.getPos(), getBlockState(), playerIn, hand, side, hitX, hitY, hitZ);
    }
    
    public int getLightValue() {
        return block.getLightValue();
    }
    
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return block.getEnchantPowerBonus(world, pos);
    }
    
    public float getFriction(LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return block.getFriction(level, pos, entity);
    }
    
    public boolean isMaterial(Material material) {
        if (hasSpecialBlockHandler())
            return handler.isMaterial(this, material);
        return material == block.getMaterial(state);
    }
    
    public boolean isLiquid() {
        if (hasSpecialBlockHandler())
            return handler.isLiquid(this);
        return getBlockState().getMaterial().isLiquid();
    }
    
    public Vec3d getFogColor(IParentCollection parent, Entity entity, Vec3d originalColor, float partialTicks) {
        if (hasSpecialBlockHandler())
            return handler.getFogColor(parent, this, entity, originalColor, partialTicks);
        return originalColor;
    }
    
    public Vec3d modifyAcceleration(IParentCollection parent, Entity entityIn, Vec3d motion) {
        if (hasSpecialBlockHandler())
            return handler.modifyAcceleration(parent, this, entityIn, motion);
        return null;
    }
    
    public boolean canBeConvertedToVanilla() {
        for (LittleBox box : boxes)
            if (!box.isSolid())
                return false;
        return block.canBeConvertedToVanilla();
    }
    
    // ================Collision================
    
    public boolean shouldCheckForCollision() {
        // TODO seems weird
        return true;
    }
    
    public void onEntityCollidedWithBlock(IParentCollection parent, Entity entityIn) {
        if (hasSpecialBlockHandler())
            handler.onEntityCollidedWithBlock(parent, this, entityIn);
    }
    
    public boolean hasNoCollision() {
        if (hasSpecialBlockHandler())
            return handler.canWalkThrough(this);
        return false;
    }
    
    public VoxelShape getSelectionShape(IParentCollection parent) {
        
    }
    
    public VoxelShape getOcclusionShape(IParentCollection parent) {
        if (hasSpecialBlockHandler())
            return handler.getOcclusionShape(this, box);
        
        return box;
    }
    
    public VoxelShape getCollisionShape(IParentCollection parent, CollisionContext context) {
        if (hasSpecialBlockHandler())
            return handler.getCollisionShape(this, box);
        
        return box;
    }
    
    // ================Ingredient================
    
    @Nullable
    public BlockIngredientEntry getBlockIngredient(LittleGridContext context) {
        return IngredientUtils.getBlockIngredient(getBlock(), getMeta(), getPercentVolume(context));
    }
    
    public ItemStack getBlockStack() {
        return new ItemStack(getBlock(), 1, getMeta());
    }
    
    public PlacePreview getPlaceableTile(LittleVec offset) {
        LittleBox newBox = this.box.copy();
        if (offset != null)
            newBox.add(offset);
        return new PlacePreview(newBox, this);
    }
}
