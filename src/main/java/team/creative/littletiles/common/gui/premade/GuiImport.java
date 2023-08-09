package team.creative.littletiles.common.gui.premade;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
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
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser;
import team.creative.littletiles.common.convertion.OldLittleTilesDataParser.LittleConvertException;
import team.creative.littletiles.common.grid.LittleGridException;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.structure.LittleStructureType;

public class GuiImport extends GuiLayer {
    
    public static boolean checkImport(List<Component> errors, Player player, LittleGroup group) {
        try {
            LittleStructureType type = group.getStructureType();
            if (type != null)
                type.checkImport(errors, group, player);
            for (LittleGroup child : group.children.all())
                checkImport(errors, player, child);
        } catch (RuntimeException e) {
            errors.add(Component.translatable("gui.error.unknown", e.getMessage()));
        }
        return errors.isEmpty();
    }
    
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
                List<Component> errors = new ArrayList<>();
                if (checkImport(errors, getPlayer(), LittleGroup.load(nbt))) {
                    CompoundTag stackTag = stack.getOrCreateTag();
                    stackTag.put(ItemLittleBlueprint.CONTENT_KEY, nbt);
                    get("import", GuiInventoryGrid.class).setChanged();
                } else {
                    LittleTiles.LOGGER.error("Failed to import structure ...");
                    for (Component component : errors)
                        LittleTiles.LOGGER.error(component.getString());
                }
            } catch (LittleGridException | LittleConvertException e) {
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
                
                CompoundTag tempNbt = nbt.copy();
                if (OldLittleTilesDataParser.isOld(tempNbt))
                    tempNbt = OldLittleTilesDataParser.convert(tempNbt);
                List<Component> errors = new ArrayList<>();
                if (checkImport(errors, getPlayer(), LittleGroup.load(tempNbt)))
                    IMPORT_DATA.send(nbt);
                else {
                    MutableComponent component = null;
                    for (Component c : errors) {
                        if (component == null)
                            component = Component.empty();
                        else
                            component.append("\\n");
                        component.append(c);
                    }
                    GuiDialogHandler.openDialog(getIntegratedParent(), "import_fail", component, (y, z) -> {}, DialogButton.OK);
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            } catch (LittleConvertException e) {
                GuiDialogHandler.openDialog(getIntegratedParent(), "could_not_convert", e.translatable(), (y, z) -> {}, DialogButton.OK);
            } catch (LittleGridException e) {
                GuiDialogHandler.openDialog(getIntegratedParent(), "invalid_grid", e.translatable(), (y, z) -> {}, DialogButton.OK);
            }
        }).setTranslate("gui.import"));
        
        add(new GuiInventoryGrid("import", importSlot));
        
        add(new GuiPlayerInventoryGrid(getPlayer()).setUnexpandableX());
    }
}
