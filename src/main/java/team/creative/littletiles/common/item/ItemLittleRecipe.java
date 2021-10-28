package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.ItemModelCache;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.gui.SubGuiRecipe;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;

public class ItemLittleRecipe extends Item implements ICreativeRendered, GuiHandler {
    
    public ItemLittleRecipe() {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB));
    }
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {}
    
    @Override
    public Component getName(ItemStack stack) {
        if (stack.getOrCreateTag().contains("structure") && stack.getOrCreateTagElement("structure").contains("name"))
            return new TextComponent(stack.getOrCreateTag().getCompound("structure").getString("name"));
        return super.getName(stack);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand == EnumHand.OFF_HAND)
            return new ActionResult(EnumActionResult.PASS, stack);
        if (!player.isSneaking() && stack.hasTagCompound() && !stack.getTagCompound().hasKey("x")) {
            if (!world.isRemote)
                GuiHandler.openGuiItem(player, world);
            return new ActionResult(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult(EnumActionResult.PASS, stack);
    }
    
    public LittlePreviews saveBlocks(World world, ItemStack stack, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        LittlePreviews previews = new LittlePreviews(LittleGridContext.getMin());
        
        for (int posX = minX; posX <= maxX; posX++) {
            for (int posY = minY; posY <= maxY; posY++) {
                for (int posZ = minZ; posZ <= maxZ; posZ++) {
                    BlockPos newPos = new BlockPos(posX, posY, posZ);
                    TileEntity tileEntity = world.getTileEntity(newPos);
                    if (tileEntity instanceof TileEntityLittleTiles) {
                        TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
                        for (Pair<IParentTileList, LittleTile> pair : te.allTiles()) {
                            LittlePreview preview = previews.addPreview(null, pair.value.getPreviewTile(), te.getContext());
                            preview.box.add(new LittleVec((posX - minX) * previews.getContext().size, (posY - minY) * previews.getContext().size, (posZ - minZ) * previews
                                    .getContext().size));
                        }
                        continue;
                    }
                    LittlePreviews specialPreviews = ChiselsAndBitsManager.getPreviews(tileEntity);
                    if (specialPreviews != null) {
                        for (int i = 0; i < specialPreviews.size(); i++) {
                            LittlePreview preview = previews.addPreview(null, specialPreviews.get(i), LittleGridContext.get(ChiselsAndBitsManager.convertingFrom));
                            preview.box.add(new LittleVec((posX - minX) * previews.getContext().size, (posY - minY) * previews.getContext().size, (posZ - minZ) * previews
                                    .getContext().size));
                        }
                    }
                }
            }
        }
        
        previews.removeOffset();
        return previews;
        
    }
    
    public void saveRecipe(World world, EntityPlayer player, ItemStack stack, BlockPos second) {
        int firstX = stack.getTagCompound().getInteger("x");
        int firstY = stack.getTagCompound().getInteger("y");
        int firstZ = stack.getTagCompound().getInteger("z");
        
        stack.getTagCompound().removeTag("x");
        stack.getTagCompound().removeTag("y");
        stack.getTagCompound().removeTag("z");
        
        LittlePreview.savePreview(saveBlocks(world, stack, Math.min(firstX, second.getX()), Math.min(firstY, second.getY()), Math.min(firstZ, second.getZ()), Math
                .max(firstX, second.getX()), Math.max(firstY, second.getY()), Math.max(firstZ, second.getZ())), stack);
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote)
                stack.setTagCompound(null);
            
            return EnumActionResult.SUCCESS;
        }
        
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("x")) {
            if (!world.isRemote) {
                saveRecipe(world, player, stack, pos);
                player.sendMessage(new TextComponentTranslation("Second position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
            }
            return EnumActionResult.SUCCESS;
        } else if (!stack.hasTagCompound()) {
            if (!world.isRemote) {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setInteger("x", pos.getX());
                stack.getTagCompound().setInteger("y", pos.getY());
                stack.getTagCompound().setInteger("z", pos.getZ());
                player.sendMessage(new TextComponentTranslation("First position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            if (stack.getTagCompound().hasKey("x"))
                tooltip.add("First pos: x=" + stack.getTagCompound().getInteger("x") + ",y=" + stack.getTagCompound().getInteger("y") + ",z=" + stack.getTagCompound()
                        .getInteger("z"));
            else {
                String id = "none";
                if (stack.getTagCompound().hasKey("structure"))
                    id = stack.getTagCompound().getCompoundTag("structure").getString("id");
                tooltip.add("structure: " + id);
                tooltip.add("contains " + stack.getTagCompound().getInteger("count") + " tiles");
            }
        }
    }
    
    @Override
    public GuiLayer create(Player player, CompoundTag nbt) {
        return new SubGuiRecipe(player.getMainHandItem());
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<RenderBox> getRenderingBoxes(BlockState state, BlockEntity be, ItemStack stack) {
        if (stack.hasTag() && !stack.getTag().contains("x"))
            return LittlePreview.getCubesForStackRendering(stack);
        return new ArrayList<RenderBox>();
    }
    
    public ModelResourceLocation getBackgroundLocation() {
        return new ModelResourceLocation(LittleTiles.MODID + ":recipe_background", "inventory");
    }
    
    @OnlyIn(Dist.CLIENT)
    public static BakedModel model;
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        
        if (cameraTransformType == TransformType.GUI || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("tiles")) {
            if (cameraTransformType == TransformType.GUI)
                GlStateManager.disableDepth();
            if (model == null)
                model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(getBackgroundLocation());
            ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
            
            try {
                mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
            } catch (Exception e) {
                model = null;
                return;
            }
            
            if (cameraTransformType == TransformType.GUI)
                GlStateManager.enableDepth();
        }
        GlStateManager.popMatrix();
        
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCachedModel(Facing facing, RenderType layer, List<BakedQuad> cachedQuads, BlockState state, BlockEntity be, ItemStack stack, boolean threaded) {
        ItemModelCache.cacheModel(stack, facing, cachedQuads);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> getCachedModel(Facing facing, RenderType layer, BlockState state, BlockEntity be, ItemStack stack, boolean threaded) {
        return ItemModelCache.requestCache(stack, facing);
    }
}
