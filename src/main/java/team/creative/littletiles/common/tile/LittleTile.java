package team.creative.littletiles.common.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.item.ItemBlockTiles;
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
import com.creativemd.littletiles.common.tile.math.box.face.LittleBoxFace;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.registry.LittleTileType;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleTile {
    
    public final LittleBlock block;
    public final int color;
    public final List<LittleBox> boxes = new ArrayList<>(1);
    
    public LittleTile(LittleBlock block, int color, LittleBox box) {
        this.block = block;
        this.color = color;
        this.boxes.add(box);
    }
    
    public boolean isTranslucent() {
        return block.isTranslucent() || ColorUtils.isTransparent(color);
    }
    
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
    
    public void combine() {
        BasicCombiner.combineBoxes(boxes);
    }
    
    public boolean canBeConvertedToVanilla() {
        if (!box.isSolid())
            return false;
        if (hasSpecialBlockHandler())
            return handler.canBeConvertedToVanilla(this);
        return true;
    }
    
    public LittleVec getMinVec() {
        return box.getMinVec();
    }
    
    public int getMaxY() {
        return box.maxY;
    }
    
    public AxisAlignedBB getSelectedBox(BlockPos pos, LittleGridContext context) {
        return box.getSelectionBox(context, pos);
    }
    
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
    
    public LittleVec getSize() {
        return box.getSize();
    }
    
    public boolean doesFillEntireBlock(LittleGridContext context) {
        return box.doesFillEntireBlock(context);
    }
    
    public void fillFace(LittleBoxFace face, LittleGridContext context) {
        LittleBox box = this.box;
        if (face.context != context) {
            box = box.copy();
            box.convertTo(context, face.context);
        }
        box.fill(face);
    }
    
    public boolean fillInSpace(boolean[][][] filled) {
        if (!box.isSolid())
            return false;
        
        boolean changed = false;
        for (int x = box.minX; x < box.maxX; x++) {
            for (int y = box.minY; y < box.maxY; y++) {
                for (int z = box.minZ; z < box.maxZ; z++) {
                    filled[x][y][z] = true;
                    changed = true;
                }
            }
        }
        return changed;
    }
    
    public boolean fillInSpaceInaccurate(LittleBox otherBox, boolean[][][] filled) {
        return box.fillInSpaceInaccurate(otherBox, filled);
    }
    
    @Override
    public boolean fillInSpace(LittleBox otherBox, boolean[][][] filled) {
        return box.fillInSpace(otherBox, filled);
    }
    
    public boolean intersectsWith(LittleBox box) {
        return LittleBox.intersectsWith(this.box, box);
    }
    
    public List<LittleBox> cutOut(LittleBox box, @Nullable LittleBoxReturnedVolume volume) {
        return this.box.cutOut(box, volume);
    }
    
    public List<LittleBox> cutOut(List<LittleBox> boxes, List<LittleBox> cutout, @Nullable LittleBoxReturnedVolume volume) {
        return this.box.cutOut(boxes, cutout, volume);
    }
    
    public void getIntersectingBox(LittleBox box, List<LittleBox> boxes) {
        if (LittleBox.intersectsWith(box, this.box))
            boxes.add(this.box);
    }
    
    public LittleBox getCompleteBox() {
        return box;
    }
    
    public LittleVec getCenter() {
        return box.getCenter();
    }
    
    public RayTraceResult rayTrace(LittleGridContext context, BlockPos blockPos, Vec3d pos, Vec3d look) {
        return box.calculateIntercept(context, blockPos, pos, look);
    }
    
    public boolean equalsBox(LittleBox box) {
        return this.box.equals(box);
    }
    
    public boolean canBeCombined(LittleTile tile) {
        if (invisible != tile.invisible)
            return false;
        
        if (glowing != tile.glowing)
            return false;
        
        return block == tile.block && meta == tile.meta;
    }
    
    @Override
    public boolean canCombine(ICombinable combinable) {
        return this.canBeCombined((LittleTile) combinable) && ((LittleTile) combinable).canBeCombined(this);
    }
    
    public boolean doesProvideSolidFace(EnumFacing facing) {
        return !invisible && box.isFaceSolid(facing) && !isTranslucent() && block != Blocks.BARRIER;
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
    
    public boolean doesTouch(LittleTile tile) {
        return box.doesTouch(tile.box);
    }
    
    public void saveTile(NBTTagCompound nbt) {
        saveTileCore(nbt);
        saveTileExtra(nbt);
    }
    
    /** Used to save extra data like block-name, meta, color etc. everything
     * necessary for a preview **/
    public void saveTileExtra(NBTTagCompound nbt) {
        if (invisible)
            nbt.setBoolean("invisible", invisible);
        if (glowing)
            nbt.setBoolean("glowing", glowing);
        nbt.setString("block", handler instanceof MissingBlockHandler ? ((MissingBlockHandler) handler).blockname : Block.REGISTRY.getNameForObject(block)
                .toString() + (meta != 0 ? ":" + meta : ""));
    }
    
    public void saveTileCore(NBTTagCompound nbt) {
        LittleTileType type = getType();
        if (type.saveId)
            nbt.setString("tID", type.id);
        nbt.setIntArray("box", box.getArray());
    }
    
    public void loadTile(NBTTagCompound nbt) {
        loadTileCore(nbt);
        loadTileExtra(nbt);
    }
    
    public void loadTileExtra(NBTTagCompound nbt) {
        invisible = nbt.getBoolean("invisible");
        glowing = nbt.getBoolean("glowing");
        if (nbt.hasKey("meta"))
            setBlock(nbt.getString("block"), Block.getBlockFromName(nbt.getString("block")), nbt.getInteger("meta"));
        else {
            String[] parts = nbt.getString("block").split(":");
            if (parts.length == 3)
                setBlock(nbt.getString("block"), Block.getBlockFromName(parts[0] + ":" + parts[1]), Integer.parseInt(parts[2]));
            else
                setBlock(nbt.getString("block"), Block.getBlockFromName(parts[0] + ":" + parts[1]), 0);
        }
    }
    
    public void loadTileCore(NBTTagCompound nbt) {
        if (nbt.hasKey("bSize")) { // Old (used till 1.4)
            int count = nbt.getInteger("bSize");
            box = LittleBox.loadBox("bBox" + 0, nbt);
        } else if (nbt.hasKey("boxes")) { // Out of date (used in pre-releases of 1.5)
            NBTTagList list = nbt.getTagList("boxes", 11);
            box = LittleBox.createBox(list.getIntArrayAt(0));
        } else if (nbt.hasKey("box")) { // Active one
            box = LittleBox.createBox(nbt.getIntArray("box"));
        }
        
    }
    
    // ================Copy================
    
    @Override
    public LittleTile copy() {
        LittleTile tile = null;
        try {
            tile = this.getClass().getConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("Invalid LittleTile class=" + this.getClass().getName());
            tile = null;
        }
        if (tile != null) {
            copyCore(tile);
            copyExtra(tile);
        }
        return tile;
    }
    
    public void assignTo(LittleTile target) {
        copyCore(target);
        copyExtra(target);
    }
    
    public void copyExtra(LittleTile tile) {
        tile.invisible = this.invisible;
        tile.glowing = this.glowing;
        tile.handler = this.handler;
        tile.block = this.block;
        tile.meta = this.meta;
    }
    
    public void copyCore(LittleTile tile) {
        tile.box = box != null ? box.copy() : null;
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
    
    // ================Sound================
    
    public SoundType getSound() {
        return block.getSoundType();
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
    
    public void onTileExplodes(IParentTileList parent, Explosion explosion) {
        if (hasSpecialBlockHandler())
            handler.onTileExplodes(parent, this, explosion);
    }
    
    public void randomDisplayTick(IParentTileList parent, Random rand) {
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
    
    public boolean onBlockActivated(IParentTileList parent, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (hasSpecialBlockHandler())
            return handler.onBlockActivated(parent, this, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
        return block.onBlockActivated(parent.getWorld(), parent.getPos(), getBlockState(), playerIn, hand, side, hitX, hitY, hitZ);
    }
    
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        if (glowing)
            return glowing ? 14 : 0;
        return block.getLightValue(getBlockState());
    }
    
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return block.getEnchantPowerBonus(world, pos);
    }
    
    public float getSlipperiness(IBlockAccess world, BlockPos pos, Entity entity) {
        return block.getSlipperiness(getBlockState(), world, pos, entity);
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
    
    public Vec3d getFogColor(IParentTileList parent, Entity entity, Vec3d originalColor, float partialTicks) {
        if (hasSpecialBlockHandler())
            return handler.getFogColor(parent, this, entity, originalColor, partialTicks);
        return originalColor;
    }
    
    public Vec3d modifyAcceleration(IParentTileList parent, Entity entityIn, Vec3d motion) {
        if (hasSpecialBlockHandler())
            return handler.modifyAcceleration(parent, this, entityIn, motion);
        return null;
    }
    
    // ================Collision================
    
    public boolean shouldCheckForCollision() {
        return true;
    }
    
    public void onEntityCollidedWithBlock(IParentTileList parent, Entity entityIn) {
        if (hasSpecialBlockHandler())
            handler.onEntityCollidedWithBlock(parent, this, entityIn);
    }
    
    public boolean hasNoCollision() {
        if (hasSpecialBlockHandler())
            return handler.canWalkThrough(this);
        return false;
    }
    
    public LittleBox getCollisionBox() {
        if (hasSpecialBlockHandler())
            return handler.getCollisionBox(this, box);
        
        return box;
    }
}
