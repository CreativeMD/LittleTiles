package team.creative.littletiles.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.ItemGuiCreator;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.GuiBag;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.ingredient.IngredientUtils;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;

public class ItemLittleBag extends Item implements ILittleIngredientInventory, ItemGuiCreator {
    
    public ItemLittleBag() {
        super(new Item.Properties().stacksTo(1));
    }
    
    @Override
    public GuiLayer create(CompoundTag nbt, Player player) {
        return new GuiBag(ContainerSlotView.mainHand(player));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND)
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        if (!level.isClientSide)
            GuiCreator.ITEM_OPENER.open(player, hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
    
    @Override
    public LittleIngredients getInventory(ItemStack stack) {
        CompoundTag nbt = ILittleTool.getData(stack);
        
        LittleIngredients ingredients = new LittleIngredients() {
            
            @Override
            protected boolean canAddNewIngredients() {
                return false;
            }
            
        };
        BlockIngredient blocks = new BlockIngredient().setLimits(LittleTiles.CONFIG.general.bag.inventorySize, Item.DEFAULT_MAX_STACK_SIZE);
        
        ListTag list = nbt.getList("inv", Tag.TAG_COMPOUND);
        int size = Math.min(LittleTiles.CONFIG.general.bag.inventorySize, list.size());
        for (int i = 0; i < size; i++) {
            CompoundTag blockNBT = list.getCompound(i);
            BlockIngredientEntry ingredient = IngredientUtils.loadBlockIngredient(blockNBT);
            if (ingredient != null && ingredient.value >= LittleGrid.getMax().pixelVolume)
                blocks.add(ingredient);
        }
        ingredients.set(blocks.getClass(), blocks);
        
        ColorIngredient color = new ColorIngredient(nbt.getInt("black"), nbt.getInt("cyan"), nbt.getInt("magenta"), nbt.getInt("yellow"));
        color.setLimit(LittleTiles.CONFIG.general.bag.colorStorage);
        ingredients.set(color.getClass(), color);
        return ingredients;
    }
    
    @Override
    public void setInventory(ItemStack stack, LittleIngredients ingredients, LittleInventory inventory) {
        CompoundTag nbt = ILittleTool.getData(stack);
        
        ListTag list = new ListTag();
        int i = 0;
        for (BlockIngredientEntry ingredient : ingredients.get(BlockIngredient.class).getContent()) {
            if (ingredient.block instanceof AirBlock && ingredient.value < LittleGrid.getMax().pixelVolume)
                continue;
            if (i >= LittleTiles.CONFIG.general.bag.inventorySize)
                break;
            list.add(ingredient.save(new CompoundTag()));
            i++;
        }
        
        nbt.put("inv", list);
        
        ColorIngredient color = ingredients.get(ColorIngredient.class);
        nbt.putInt("black", color.black);
        nbt.putInt("cyan", color.cyan);
        nbt.putInt("magenta", color.magenta);
        nbt.putInt("yellow", color.yellow);
        ILittleTool.setData(stack, nbt);
    }
    
}
