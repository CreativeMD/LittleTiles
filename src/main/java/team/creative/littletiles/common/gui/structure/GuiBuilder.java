package team.creative.littletiles.common.gui.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.inventory.GuiInventoryGrid;
import team.creative.creativecore.common.gui.controls.inventory.GuiPlayerInventoryGrid;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCounter;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;

public class GuiBuilder extends GuiLayer {
    
    public LittleStructureBuilder builder;
    public final GuiSyncLocal<CompoundTag> CRAFT = getSyncHolder().register("craft", nbt -> {
        if ((getPlayer().isCreative() && builder.inventory.getItem(0).isEmpty()) || builder.inventory.getItem(0).getItem() instanceof ItemLittleBlueprint) {
            int width = nbt.getInt("width");
            int height = nbt.getInt("height");
            int thickness = nbt.getInt("thickness");
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("block")));
            LittleGrid grid = LittleGrid.get(nbt.getInt("grid"));
            
            builder.lastBlockState = block.defaultBlockState();
            builder.lastSizeX = width;
            builder.lastSizeY = height;
            builder.lastThickness = thickness;
            builder.lastGrid = grid.count;
            builder.lastStructureType = nbt.getString("type");
            builder.updateStructure();
            
            LittleStructureBuilderType type = LittleStructureBuilder.REGISTRY.get(builder.lastStructureType);
            if (type != null) {
                ItemStack stack = builder.inventory.getItem(0);
                if (!LittleAction.needIngredients(getPlayer()) && stack.isEmpty()) {
                    stack = new ItemStack(LittleTilesRegistry.BLUEPRINT.get());
                    builder.inventory.setItem(0, stack);
                }
                if (stack.getItem() instanceof ItemLittleBlueprint blue)
                    blue.saveTiles(stack, type.construct(grid, width, height, thickness, block.defaultBlockState()));
            }
        }
    });
    
    public GuiBuilder(LittleStructureBuilder builder) {
        super("structure_builder", 200, 200);
        this.builder = builder;
    }
    
    @Override
    public void create() {
        flow = GuiFlow.STACK_Y;
        GuiTable table = new GuiTable();
        add(table.setExpandableX());
        GuiRow row = new GuiRow();
        table.addRow(row);
        GuiColumn col = new GuiColumn();
        row.addColumn(col);
        col.add(new GuiLabeledControl("gui.structure_builder.width", new GuiCounter("width", builder.lastSizeX, 1, Integer.MAX_VALUE)));
        
        col = new GuiColumn();
        row.addColumn(col);
        col.add(new GuiLabeledControl("gui.structure_builder.height", new GuiCounter("height", builder.lastSizeX, 1, Integer.MAX_VALUE)));
        
        row = new GuiRow();
        table.addRow(row);
        col = new GuiColumn();
        row.addColumn(col);
        col.add(new GuiLabeledControl("gui.structure_builder.thickness", new GuiCounter("thickness", builder.lastThickness, 1, Integer.MAX_VALUE)));
        
        var gridSelect = new GuiStateButtonMapped<LittleGrid>("grid", LittleGrid.mapBuilder());
        gridSelect.select(LittleTiles.CONFIG.build.get(getPlayer()).defaultGrid());
        col = new GuiColumn();
        row.addColumn(col);
        col.add(new GuiLabeledControl("gui.grid", gridSelect));
        
        GuiComboBoxMapped<LittleStructureBuilderType> box = new GuiComboBoxMapped<>("type", new TextMapBuilder<LittleStructureBuilderType>().addEntrySet(
            LittleStructureBuilder.REGISTRY.entrySet(), x -> Component.translatable("structure." + x.getKey())));
        box.select(LittleStructureBuilder.REGISTRY.get(builder.lastStructureType));
        add(box.setExpandableX());
        
        GuiStackSelector selector = new GuiStackSelector("preview", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(new ItemStack(builder.lastBlockState.getBlock()));
        add(selector.setExpandableX());
        
        add(new GuiLeftRightBox().addLeft(new GuiLabel("failed").setTranslate("gui.structure_builder.failed").setVisible(false)).addRight(new GuiButton("craft", x -> {
            if ((getPlayer().isCreative() && builder.inventory.getItem(0).isEmpty()) || builder.inventory.getItem(0).getItem() instanceof ItemLittleBlueprint) {
                get("failed").visible = false;
                CompoundTag nbt = new CompoundTag();
                GuiCounter width = (GuiCounter) get("width");
                nbt.putInt("width", width.getValue());
                GuiCounter height = (GuiCounter) get("height");
                nbt.putInt("height", height.getValue());
                GuiCounter thickness = (GuiCounter) get("thickness");
                nbt.putInt("thickness", thickness.getValue());
                GuiComboBoxMapped<LittleStructureBuilderType> type = (GuiComboBoxMapped<LittleStructureBuilderType>) get("type");
                nbt.putString("type", type.getSelected().type.id);
                GuiStateButtonMapped<LittleGrid> gridButton = (GuiStateButtonMapped<LittleGrid>) get("grid");
                LittleGrid grid = gridButton.getSelected();
                nbt.putInt("grid", grid.count);
                ItemStack stack = selector.getSelected();
                Block block = Block.byItem(stack.getItem());
                nbt.putString("block", block.builtInRegistryHolder().key().location().toString());
                CRAFT.send(nbt);
            } else
                get("failed").visible = true;
            
        }).setTranslate("gui.structure_builder.craft")));
        
        add(new GuiInventoryGrid("builder", builder.inventory));
        add(new GuiPlayerInventoryGrid(getPlayer()));
    }
    
}
