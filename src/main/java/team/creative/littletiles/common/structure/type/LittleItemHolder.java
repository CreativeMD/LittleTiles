package team.creative.littletiles.common.structure.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.client.render.tile.LittleRenderBoxItem;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureRelative;
import team.creative.littletiles.common.structure.signal.SignalState;

public class LittleItemHolder extends LittleStructure {
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public Facing facing;
    
    @StructureDirectional
    public Vec3f topRight;
    
    public ItemStack stack;
    public boolean locked;
    public boolean whitelist;
    public List<CreativeIngredient> filter;
    
    public LittleItemHolder(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    public void loadSettings(CompoundTag nbt, HolderLookup.Provider provider) {
        locked = nbt.getBoolean("locked");
        whitelist = nbt.getBoolean("f_white");
        if (nbt.contains("filter")) {
            ListTag list = nbt.getList("filter", Tag.TAG_COMPOUND);
            filter = new ArrayList<>();
            for (int i = 0; i < list.size(); i++)
                filter.add(CreativeIngredient.load(provider, list.getCompound(i)));
        } else
            filter = null;
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        stack = ItemStack.parseOptional(provider, nbt.getCompound("stack"));
        loadSettings(nbt, provider);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt, HolderLookup.Provider provider) {
        nbt.put("stack", stack.saveOptional(provider));
        nbt.putBoolean("locked", locked);
        nbt.putBoolean("f_white", whitelist);
        if (filter != null) {
            ListTag list = new ListTag();
            for (int i = 0; i < filter.size(); i++)
                list.add(filter.get(i).save(provider));
            nbt.put("filter", list);
        } else
            nbt.remove("filter");
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public boolean wrenchInteract(Player player) {
        if (!player.level().isClientSide)
            LittleTilesGuiRegistry.ITEM_HOLDER.open(player, this);
        return true;
    }
    
    public boolean canBeFoundInFilterList(ItemStack stack) {
        if (filter == null)
            return false;
        for (CreativeIngredient ingredient : filter)
            if (ingredient.is(stack))
                return true;
        return false;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (level.isClientSide || (locked && !(player.getMainHandItem().getItem() instanceof ItemLittleWrench)))
            return InteractionResult.SUCCESS;
        ItemStack mainStack = player.getMainHandItem();
        if (mainStack.isEmpty() && !stack.isEmpty()) {
            if (player.getInventory().add(player.getInventory().selected, stack))
                stack = ItemStack.EMPTY;
            updateInput();
            updateStructure(true);
        } else if (stack.isEmpty()) {
            if (canBeFoundInFilterList(mainStack) == whitelist) {
                stack = mainStack.copyWithCount(1);
                if (!player.getAbilities().instabuild)
                    mainStack.shrink(1);
                updateInput();
                updateStructure(true);
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    public void updateInput() {
        getInput(0).updateState(SignalState.of(!stack.isEmpty()));
    }
    
    @Override
    public void getRenderingBoxes(BlockPos pos, ChunkLayerMapList<LittleRenderBox> cubes) {
        LittleBox littleBox = frame.getBox();
        AlignedBox box = littleBox.getBox(frame.getGrid());
        if (!stack.isEmpty())
            cubes.add(RenderType.cutout(), new LittleRenderBoxItem(this, box, littleBox));
    }
    
}
