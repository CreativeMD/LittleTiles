package team.creative.littletiles.common.structure.type;

import java.util.List;

import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.client.render.tile.LittleRenderBoxItem;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittleItemHolder extends LittleStructure {
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public Facing facing;
    
    @StructureDirectional
    public Vector3f topRight;
    
    public ItemStack stack;
    
    public LittleItemHolder(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        stack = ItemStack.of(nbt.getCompound("stack"));
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.put("stack", stack.save(new CompoundTag()));
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        ItemStack mainStack = player.getMainHandItem();
        if (mainStack.isEmpty() && !stack.isEmpty()) {
            player.replaceItemInInventory(player.getInventory().selected, stack.copy());
            stack = ItemStack.EMPTY;
            updateInput();
            updateStructure();
        } else if (stack.isEmpty()) {
            player.replaceItemInInventory(player.getInventory().selected, ItemStack.EMPTY);
            stack = mainStack.copy();
            updateInput();
            updateStructure();
        }
        return InteractionResult.SUCCESS;
    }
    
    public void updateInput() {
        getInput(0).updateState(BooleanUtils.asArray(!stack.isEmpty()));
    }
    
    @Override
    public void getRenderingCubes(BlockPos pos, RenderType layer, List<LittleRenderBox> cubes) {
        if (layer == RenderType.cutout()) {
            AlignedBox box = frame.getBox().getBox(frame.getGrid());
            if (!stack.isEmpty())
                cubes.add(new LittleRenderBoxItem(this, box, frame.getBox()));
        }
    }
    
}
