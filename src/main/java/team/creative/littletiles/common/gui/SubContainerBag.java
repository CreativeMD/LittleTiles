package team.creative.littletiles.common.gui;

import java.util.List;

import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.common.gui.premade.SubContainerHeldItem;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.gui.controls.SlotControlBlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredient;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemLittleBag;

public class SubContainerBag extends SubContainerHeldItem {
    
    public LittleIngredients bag;
    public InventoryBasic input = new InventoryBasic("input", false, 1);
    
    public SubContainerBag(EntityPlayer player, ItemStack stack, int index) {
        super(player, stack, index);
        
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound nbt = stack.getTagCompound().copy();
        nbt.setBoolean("reload", true);
        sendNBTToGui(nbt);
    }
    
    @CustomEventSubscribe
    public void onSlotChange(SlotChangeEvent event) {
        if (event.source instanceof SlotControl) {
            if (event.source instanceof SlotControlBlockIngredient) {
                SlotControlBlockIngredient slot = (SlotControlBlockIngredient) event.source;
                
                BlockIngredient blocks = new BlockIngredient().setLimits(ItemLittleBag.inventorySize, ItemLittleBag.maxStackSize);
                for (int y = 0; y < ItemLittleBag.inventoryHeight; y++) {
                    for (int x = 0; x < ItemLittleBag.inventoryWidth; x++) {
                        int index = x + y * ItemLittleBag.inventoryWidth;
                        BlockIngredientEntry ingredient = ((SlotControlBlockIngredient) get("item" + index)).getIngredient();
                        if (ingredient != null)
                            blocks.add(ingredient);
                    }
                }
                
                bag.set(blocks.getClass(), blocks);
                ((ItemLittleBag) stack.getItem()).setInventory(stack, bag, null);
                if (player instanceof EntityPlayerMP)
                    ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                reloadControls();
            } else if (event.source.name.startsWith("input")) {
                
                ItemStack input = ((SlotControl) event.source).slot.getStack();
                
                if (input.getItem() instanceof ILittleIngredientInventory) {
                    
                    LittleIngredients ingredients = ((ILittleIngredientInventory) input.getItem()).getInventory(input);
                    
                    boolean containsBlocks = ingredients.contains(BlockIngredient.class);
                    boolean containsColor = ingredients.contains(ColorIngredient.class);
                    
                    LittleIngredients remaining = bag.add(ingredients);
                    ((ItemLittleBag) stack.getItem()).setInventory(stack, bag, null);
                    
                    if (remaining == null)
                        remaining = new LittleIngredients();
                    
                    if (remaining.copy().sub(ingredients.copy()) != null) {
                        if (containsBlocks) {
                            updateSlots();
                            player.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
                        }
                        
                        if (containsColor) {
                            reloadControls();
                            player.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
                        }
                    }
                    
                    ((ILittleIngredientInventory) input.getItem()).setInventory(input, remaining, null);
                } else {
                    LittleIngredients ingredients = LittleIngredient.extractWithoutCount(input, true);
                    if (ingredients != null) {
                        ingredients.scale(input.getCount());
                        
                        boolean containsBlocks = ingredients.contains(BlockIngredient.class);
                        boolean containsColor = ingredients.contains(ColorIngredient.class);
                        
                        if (bag.add(ingredients) == null) {
                            
                            input.setCount(0);
                            ((ItemLittleBag) stack.getItem()).setInventory(stack, bag, null);
                            
                            if (containsBlocks) {
                                updateSlots();
                                player.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
                            }
                            
                            if (containsColor) {
                                reloadControls();
                                player.playSound(SoundEvents.BLOCK_BREWING_STAND_BREW, 1.0F, 1.0F);
                            }
                            
                        } else
                            bag = ((ItemLittleBag) stack.getItem()).getInventory(stack);
                        
                    }
                }
                if (player instanceof EntityPlayerMP)
                    ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
            
        }
    }
    
    public void reloadControls() {
        controls.clear();
        createControls();
        refreshControls();
        NBTTagCompound nbt = stack.getTagCompound().copy();
        nbt.setBoolean("reload", true);
        sendNBTToGui(nbt);
    }
    
    public InventoryBasic bagInventory;
    
    public void updateSlots() {
        List<BlockIngredientEntry> inventory = bag.get(BlockIngredient.class).getContent();
        for (int y = 0; y < ItemLittleBag.inventoryHeight; y++) {
            for (int x = 0; x < ItemLittleBag.inventoryWidth; x++) {
                int index = x + y * ItemLittleBag.inventoryWidth;
                
                ItemStack stack;
                if (index < inventory.size()) {
                    stack = new ItemStack(LittleTiles.blockIngredient);
                    stack.setTagCompound(new NBTTagCompound());
                    ItemBlockIngredient.saveIngredient(stack, inventory.get(index));
                } else
                    stack = ItemStack.EMPTY;
                
                bagInventory.setInventorySlotContents(index, stack);
            }
        }
        if (player instanceof EntityPlayerMP)
            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
    }
    
    @Override
    public void createControls() {
        bag = ((ItemLittleBag) stack.getItem()).getInventory(stack);
        List<BlockIngredientEntry> inventory = bag.get(BlockIngredient.class).getContent();
        
        bagInventory = new InventoryBasic("item", false, ItemLittleBag.inventorySize) {
            @Override
            public int getInventoryStackLimit() {
                return ItemLittleBag.maxStackSizeOfTiles;
            }
        };
        for (int y = 0; y < ItemLittleBag.inventoryHeight; y++) {
            for (int x = 0; x < ItemLittleBag.inventoryWidth; x++) {
                int index = x + y * ItemLittleBag.inventoryWidth;
                
                ItemStack stack;
                if (index < inventory.size()) {
                    stack = new ItemStack(LittleTiles.blockIngredient);
                    stack.setTagCompound(new NBTTagCompound());
                    ItemBlockIngredient.saveIngredient(stack, inventory.get(index));
                } else
                    stack = ItemStack.EMPTY;
                
                bagInventory.setInventorySlotContents(index, stack);
                controls.add(new SlotControlBlockIngredient(new Slot(bagInventory, index, 5 + x * 18, 5 + y * 18) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return false;
                    }
                }));
            }
        }
        addSlotToContainer(new Slot(input, 0, 120, 5));
        
        addPlayerSlotsToContainer(player);
        
    }
    
    @Override
    public void onClosed() {
        ItemStack stack = ((SlotControl) get("input0")).slot.getStack();
        if (!stack.isEmpty())
            LevelUtils.dropItem(player, stack);
        if (player instanceof EntityPlayerMP)
            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
    }
    
}
