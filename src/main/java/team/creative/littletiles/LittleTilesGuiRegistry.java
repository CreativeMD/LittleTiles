package team.creative.littletiles;

import team.creative.creativecore.common.gui.sync.GuiSyncGlobalLayer;
import team.creative.creativecore.common.gui.sync.GuiSyncHolder;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignal;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalInput;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalMode;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalVirtualInput;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalVirtualNumberInput;

public class LittleTilesGuiRegistry {
    
    public static final GuiSyncGlobalLayer<GuiDialogSignalEvents> SIGNAL_EVENTS_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_events_dialog", x -> new GuiDialogSignalEvents());
    public static final GuiSyncGlobalLayer<GuiDialogSignal> SIGNAL_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_dialog", x -> new GuiDialogSignal());
    public static final GuiSyncGlobalLayer<GuiDialogSignalInput> INPUT_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_input_dialog", x -> new GuiDialogSignalInput());
    public static final GuiSyncGlobalLayer<GuiDialogSignalMode> MODE_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_mode_dialog", x -> new GuiDialogSignalMode());
    public static final GuiSyncGlobalLayer<GuiDialogSignalVirtualInput> VIRTUAL_INPUT_DIALOG = GuiSyncHolder.GLOBAL
            .layer("signal_virtual_input_dialog", x -> new GuiDialogSignalVirtualInput());
    public static final GuiSyncGlobalLayer<GuiDialogSignalVirtualNumberInput> VIRTUAL_NUMBER_DIALOG = GuiSyncHolder.GLOBAL
            .layer("signal_virtual_number_dialog", x -> new GuiDialogSignalVirtualNumberInput());
    
    public static void init() {}
    
}
