package team.creative.littletiles.common.gui.premade;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.dialog.DialogGuiLayer.DialogButton;
import team.creative.creativecore.common.gui.dialog.GuiDialogHandler;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser.LittleConvertException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.ItemLittleBlueprint;

public class GuiImport extends GuiLayer {
    
    public GuiTextfield textfield;
    public Container importSlot = new SimpleContainer(1);
    
    public GuiSyncLocal<CompoundTag> IMPORT_DATA = getSyncHolder().register("import_data", (nbt) -> {
        ItemStack stack = importSlot.getItem(0);
        if (stack.getItem() instanceof ItemLittleBlueprint || (getPlayer().isCreative() && stack.isEmpty())) {
            if (stack.isEmpty())
                importSlot.setItem(0, stack = new ItemStack(LittleTilesRegistry.BLUEPRINT.get()));
            
            try {
                if (OldLittleTilesDataParser.isOld(nbt))
                    nbt = OldLittleTilesDataParser.convert(nbt);
                LittleGrid.get(nbt);
                CompoundTag stackTag = stack.getOrCreateTag();
                stackTag.put(ItemLittleBlueprint.CONTENT_KEY, nbt);
                get("import", GuiInventoryGrid.class).setChanged();
            } catch (RuntimeException | LittleConvertException e) {
                e.printStackTrace();
            }
        }
    });
    
    public GuiImport() {
        super("import");
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
    }
    
    @Override
    public void create() {
        add(textfield = new GuiTextfield("import_textfield"));
        textfield.setMaxStringLength(Integer.MAX_VALUE);
        GuiParent secondRow = new GuiParent();
        add(secondRow);
        secondRow.add(new GuiButton("paste_button", x -> {
            String text = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (text == null)
                text = "";
            textfield.setText(text);
        }).setTranslate("gui.paste"));
        
        secondRow.add(new GuiButton("import_button", x -> {
            try {
                CompoundTag nbt = TagParser.parseTag(textfield.getText());
                try {
                    LittleGrid.get(nbt);
                    IMPORT_DATA.send(nbt);
                } catch (RuntimeException e) {
                    GuiDialogHandler
                            .openDialog(getIntegratedParent(), "invalid_grid", Component.translatable("invalid_grid", nbt.getString("grid")), (y, z) -> {}, DialogButton.OK);
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }).setTranslate("gui.import"));
        
        add(new GuiInventoryGrid("import", importSlot));
        
        add(new GuiPlayerInventoryGrid(getPlayer()).setUnexpandableX());
    }
}
