package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.common.gui.client.style.ColoredDisplayStyle;
import com.creativemd.creativecore.common.gui.client.style.DisplayStyle;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.container.SlotControlNoSync;
import com.creativemd.creativecore.common.gui.controls.container.client.GuiSlotControl;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiProgressBar;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.common.structure.type.premade.LittleBlankOMatic;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry;
import team.creative.littletiles.common.recipe.BlankOMaticRecipeRegistry.BleachRecipe;

public class SubGuiBlankOMatic extends SubGui {
    
    private static final DisplayStyle SELECTED_DISPLAY = new ColoredDisplayStyle(ColorUtils.YELLOW);
    public LittleBlankOMatic whitener;
    
    public SubGuiBlankOMatic(LittleBlankOMatic whitener) {
        this.whitener = whitener;
    }
    
    @Override
    public void createControls() {
        controls.add(new GuiVariantSelector("variant", 30, 8, 100, 100));
        controls.add(new GuiLabel("cost", 132, 30));
        GuiAvatarLabel label = new GuiAvatarLabel("", 148, 40, 0, null) {
            @Override
            public boolean mousePressed(int x, int y, int button) {
                return false;
            }
            
        };
        label.name = "avatar";
        label.height = 60;
        label.avatarSize = 32;
        controls.add(label);
        
        controls.add(new GuiButton("craft", 135, 10) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setBoolean("craft", true);
                nbt.setInteger("amount", GuiScreen.isShiftKeyDown() ? 2 : GuiScreen.isCtrlKeyDown() ? 1 : 0);
                GuiVariantSelector selector = (GuiVariantSelector) get("variant");
                nbt.setInteger("variant", selector.selected);
                sendPacketToServer(nbt);
            }
        });
        controls.add(new GuiProgressBar("volume", 8, 60, 80, 6, BlankOMaticRecipeRegistry.bleachTotalVolume, whitener.whiteColor));
        updateVariants();
    }
    
    @Override
    public void receiveContainerPacket(NBTTagCompound nbt) {
        GuiProgressBar volume = (GuiProgressBar) get("volume");
        volume.pos = nbt.getInteger("volume");
    }
    
    @CustomEventSubscribe
    public void changed(GuiControlChangedEvent event) {
        if (event.source.is("variant"))
            updateLabel();
    }
    
    public void updateLabel() {
        GuiVariantSelector selector = (GuiVariantSelector) get("variant");
        IBlockState state = selector.getSeleted();
        ItemStack stack;
        if (state != null)
            stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        else
            stack = ItemStack.EMPTY;
        GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
        label.avatar = new AvatarItemStack(stack);
        GuiLabel cost = (GuiLabel) get("cost");
        BleachRecipe recipe = selector.getSelectedRecipe();
        cost.setCaption(recipe != null ? "cost: " + recipe.needed : "");
    }
    
    @CustomEventSubscribe
    public void slotChanged(SlotChangeEvent event) {
        updateVariants();
    }
    
    public void updateVariants() {
        GuiVariantSelector selector = (GuiVariantSelector) get("variant");
        selector.setRecipes(BlankOMaticRecipeRegistry.getRecipe(whitener.inventory.getStackInSlot(0)));
        updateLabel();
    }
    
    public static class GuiVariantSelector extends GuiParent {
        
        public List<Pair<BleachRecipe, IBlockState>> states;
        private int selected = 0;
        
        public GuiVariantSelector(String name, int x, int y, int width, int height) {
            super(name, x, y, width, height);
        }
        
        @Override
        public boolean hasBackground() {
            return false;
        }
        
        @Override
        public boolean hasBorder() {
            return false;
        }
        
        public BleachRecipe getSelectedRecipe() {
            if (selected >= 0 && selected < states.size())
                return states.get(selected).key;
            return null;
        }
        
        public IBlockState getSeleted() {
            if (selected >= 0 && selected < states.size())
                return states.get(selected).value;
            return null;
        }
        
        public void select(int index) {
            this.selected = index;
            for (int i = 0; i < controls.size(); i++) {
                GuiSlotControlSelect slot = (GuiSlotControlSelect) controls.get(i);
                slot.selected = index == i;
            }
            raiseEvent(new GuiControlChangedEvent(this));
        }
        
        public void setRecipes(List<BleachRecipe> recipes) {
            List<Pair<BleachRecipe, IBlockState>> states = new ArrayList<>();
            for (int i = 0; i < recipes.size(); i++) {
                BleachRecipe recipe = recipes.get(i);
                for (int j = 0; j < recipe.results.length; j++)
                    states.add(new Pair<>(recipe, recipe.results[j]));
            }
            controls.clear();
            this.states = states;
            InventoryBasic basic = new InventoryBasic("", false, states.size());
            for (int i = 0; i < states.size(); i++) {
                IBlockState state = states.get(i).value;
                final int index = i;
                int col = i % 4;
                int row = i / 4;
                basic.setInventorySlotContents(i, new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
                addControl(new GuiSlotControlSelect(this, col, row, index, basic).setCustomTooltip("cost: " + states.get(i).key.needed));
            }
            if (selected >= states.size())
                select(0);
            else
                select(selected);
        }
        
    }
    
    public static class GuiSlotControlSelect extends GuiSlotControl {
        
        public final GuiVariantSelector selector;
        public final int index;
        public boolean selected = false;
        
        public GuiSlotControlSelect(GuiVariantSelector selector, int col, int row, int index, InventoryBasic basic) {
            super(col * 18, row * 18, new SlotControlNoSync(new Slot(basic, index, col * 18, row * 18)));
            this.selector = selector;
            this.index = index;
        }
        
        @Override
        public DisplayStyle getBorderDisplay(DisplayStyle display) {
            if (selected)
                return SELECTED_DISPLAY;
            return super.getBorderDisplay(display);
        }
        
        @Override
        public boolean mousePressed(int posX, int posY, int button) {
            selector.select(index);
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return super.mousePressed(posX, posY, button);
        }
        
    }
    
}
