package team.creative.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.mc.WorldUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.ingredient.NotEnoughIngredientsException;
import team.creative.littletiles.common.item.ItemLittleRecipe;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.placement.PlacementHelper;

public class SubContainerWorkbench extends SubContainer {
    
    public InventoryBasic basic = new InventoryBasic("default", false, 2);
    
    public SubContainerWorkbench(EntityPlayer player) {
        super(player);
    }
    
    @Override
    public void createControls() {
        addSlotToContainer(new Slot(basic, 0, 5, 5));
        addSlotToContainer(new Slot(basic, 1, 45, 5));
        
        addPlayerSlotsToContainer(player, 20, 120);
    }
    
    @Override
    public void onClosed() {
        for (int i = 0; i < basic.getSizeInventory(); i++) {
            if (basic.getStackInSlot(i) != null)
                player.dropItem(basic.getStackInSlot(i), false);
        }
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        ItemStack stack1 = basic.getStackInSlot(0);
        ItemStack stack2 = basic.getStackInSlot(1);
        if (!stack1.isEmpty()) {
            if (stack1.getItem() instanceof ItemLittleRecipe || stack1.getItem() instanceof ItemLittleBlueprint) {
                if (stack1.hasTagCompound() && !stack1.getTagCompound().hasKey("x")) {
                    
                    if (stack2.getItem() instanceof ItemLittleRecipe || stack2.getItem() instanceof ItemLittleBlueprint) {
                        if (stack2.hasTagCompound() && !stack2.getTagCompound().hasKey("x") && stack1.getTagCompound().hasKey("structure"))
                            stack2.getTagCompound().setTag("structure", stack1.getTagCompound().getTag("structure"));
                    } else {
                        LittlePreviews tiles = LittlePreview.getPreview(stack1);
                        
                        try {
                            LittleInventory inventory = new LittleInventory(player);
                            if (LittleAction.checkAndTake(player, inventory, LittleAction.getIngredients(tiles))) {
                                ItemStack stack = new ItemStack(LittleTiles.multiTiles);
                                stack.setTagCompound(stack1.getTagCompound().copy());
                                if (!player.inventory.addItemStackToInventory(stack))
                                    WorldUtils.dropItem(player, stack);
                            }
                        } catch (NotEnoughIngredientsException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (ChiselsAndBitsManager.isChiselsAndBitsStructure(stack1)) {
                LittlePreviews previews = ChiselsAndBitsManager.getPreviews(stack1);
                if (previews != null && !previews.isEmpty() && stack2.isEmpty()) {
                    stack2 = new ItemStack(LittleTiles.multiTiles);
                    LittlePreview.savePreview(previews, stack2);
                    basic.setInventorySlotContents(0, ItemStack.EMPTY);
                    basic.setInventorySlotContents(1, stack2);
                }
            } else {
                ILittlePlacer tile = PlacementHelper.getLittleInterface(stack1);
                if (tile != null && !stack2.isEmpty() && (stack2.getItem() instanceof ItemLittleRecipe || stack2.getItem() instanceof ItemLittleBlueprint))
                    stack2.setTagCompound(stack1.getTagCompound().copy());
            }
        }
    }
    
}
