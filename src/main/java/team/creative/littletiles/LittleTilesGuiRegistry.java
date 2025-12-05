package team.creative.littletiles;

import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator.GuiCreatorBasic;
import team.creative.creativecore.common.gui.sync.GuiSyncGlobalLayer;
import team.creative.creativecore.common.gui.sync.GuiSyncHolder;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiCreator;
import team.creative.littletiles.common.gui.premade.GuiExport;
import team.creative.littletiles.common.gui.premade.GuiImport;
import team.creative.littletiles.common.gui.signal.GuiSignalStructurePlaced;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignal;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalInput;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalMode;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalVirtualInput;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalVirtualNumberInput;
import team.creative.littletiles.common.gui.structure.GuiBlankOMatic;
import team.creative.littletiles.common.gui.structure.GuiBuilder;
import team.creative.littletiles.common.gui.structure.GuiItemHolder;
import team.creative.littletiles.common.gui.structure.GuiParticle;
import team.creative.littletiles.common.gui.structure.GuiStorage;
import team.creative.littletiles.common.gui.structure.GuiWorkbench;
import team.creative.littletiles.common.structure.type.LittleItemHolder;
import team.creative.littletiles.common.structure.type.LittleStorage;
import team.creative.littletiles.common.structure.type.premade.LittleBlankOMatic;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;

public class LittleTilesGuiRegistry {
    
    public static final GuiSyncGlobalLayer<GuiDialogSignalEvents> SIGNAL_EVENTS_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_events_dialog", (x, y) -> new GuiDialogSignalEvents());
    public static final GuiSyncGlobalLayer<GuiDialogSignal> SIGNAL_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_dialog", (x, y) -> new GuiDialogSignal());
    public static final GuiSyncGlobalLayer<GuiDialogSignalInput> INPUT_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_input_dialog", (x, y) -> new GuiDialogSignalInput());
    public static final GuiSyncGlobalLayer<GuiDialogSignalMode> MODE_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_mode_dialog", (x, y) -> new GuiDialogSignalMode());
    public static final GuiSyncGlobalLayer<GuiDialogSignalVirtualInput> VIRTUAL_INPUT_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_virtual_input_dialog", (x,
            y) -> new GuiDialogSignalVirtualInput());
    public static final GuiSyncGlobalLayer<GuiDialogSignalVirtualNumberInput> VIRTUAL_NUMBER_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_virtual_number_dialog", (x,
            y) -> new GuiDialogSignalVirtualNumberInput());
    
    public static final LittleStructureGuiCreator STRUCTURE_SIGNAL = GuiCreator.register("structure_signal", new LittleStructureGuiCreator((nbt, player, structure) -> {
        if (player.level().isClientSide) {
            var events = new GuiDialogSignalEvents();
            events.init(new GuiSignalStructurePlaced(structure, true, true));
            events.clear();
            return events;
        }
        return new GuiDialogSignalEvents();
    }, x -> x.wrenchEditSignal));
    
    public static final GuiCreatorBasic OPEN_CONFIG = GuiCreator.register("configure", new GuiCreatorBasic((nbt, player) -> {
        if (player.getMainHandItem().getItem() instanceof ILittleTool tool)
            return tool.getConfigure(player, ContainerSlotView.mainHand(player), nbt.getBoolean("second"));
        return null;
    }));
    
    public static final LittleStructureGuiCreator STORAGE = GuiCreator.register("storage", new LittleStructureGuiCreator((nbt, player,
            structure) -> new GuiStorage((LittleStorage) structure, player), x -> x.storageGui));
    public static final LittleStructureGuiCreator BLANKOMATIC = GuiCreator.register("blankomatic", new LittleStructureGuiCreator((nbt, player,
            structure) -> new GuiBlankOMatic((LittleBlankOMatic) structure), x -> x.blankomaticGui));
    
    public static final GuiCreatorBasic EXPORTER = GuiCreator.register("exporter", new GuiCreatorBasic((nbt, player) -> new GuiExport()));
    public static final GuiCreatorBasic IMPORTER = GuiCreator.register("importer", new GuiCreatorBasic((nbt, player) -> new GuiImport()));
    
    public static final LittleStructureGuiCreator PARTICLE = GuiCreator.register("particle", new LittleStructureGuiCreator((nbt, player,
            structure) -> new GuiParticle((LittleParticleEmitter) structure), x -> x.particleGui));
    public static final LittleStructureGuiCreator ITEM_HOLDER = GuiCreator.register("item_holder", new LittleStructureGuiCreator((nbt, player,
            structure) -> new GuiItemHolder((LittleItemHolder) structure), x -> x.itemHolderGui));
    
    public static final LittleStructureGuiCreator STRUCTURE_BUILDER = GuiCreator.register("structure_builder", new LittleStructureGuiCreator((nbt, player,
            structure) -> new GuiBuilder((LittleStructureBuilder) structure), x -> x.structureBuilderGui));
    public static final LittleStructureGuiCreator WORKBENCH = GuiCreator.register("workbench", new LittleStructureGuiCreator((nbt, player,
            structure) -> new GuiWorkbench(), x -> x.workbenchGui));
    
    public static void init() {}
    
}
