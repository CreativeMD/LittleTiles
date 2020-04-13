package com.creativemd.littletiles.common.structure.animation.event;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.littletiles.client.gui.controls.SubGuiSoundSelector.GuiPickSoundButton;
import com.creativemd.littletiles.client.gui.dialogs.SubGuiDoorEvents;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityAnimationController;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AnimationEvent implements Comparable<AnimationEvent> {
	
	private static HashMap<String, Class<? extends AnimationEvent>> eventTypes = new HashMap<>();
	private static HashMap<String, AnimationEventGuiParser> eventTypeParsers = new HashMap<>();
	private static HashMap<Class<? extends AnimationEvent>, String> eventTypeInv = new HashMap<>();
	private static List<String> typeNames = new ArrayList<>();
	private static List<String> typeNamesTranslated = new ArrayList<>();
	
	public static <T extends AnimationEvent> void registerAnimationEventType(String id, Class<T> eventClass, AnimationEventGuiParser<T> parser) {
		if (eventTypes.containsKey(id))
			throw new IllegalArgumentException("Animation type '" + id + "' already exists!");
		eventTypes.put(id, eventClass);
		eventTypeInv.put(eventClass, id);
		eventTypeParsers.put(id, parser);
		typeNames.add(id);
		typeNamesTranslated.add("animation.event." + id + ".name");
	}
	
	public static Class<? extends AnimationEvent> getType(String id) {
		return eventTypes.get(id);
	}
	
	public static AnimationEventGuiParser getParser(String id) {
		return eventTypeParsers.get(id);
	}
	
	public static String getId(Class<? extends AnimationEvent> classEvent) {
		return eventTypeInv.get(classEvent);
	}
	
	public static List<String> typeNames() {
		return typeNames;
	}
	
	public static List<String> typeNamestranslated() {
		List<String> translated = new ArrayList<>();
		for (String string : typeNamesTranslated) {
			translated.add(SubGui.translate(string));
		}
		return translated;
	}
	
	public static AnimationEvent create(int tick, String id) {
		Class<? extends AnimationEvent> eventClass = getType(id);
		if (eventClass == null)
			throw new RuntimeException("Found invalid AnimationEvent type '" + id + "'!");
		
		try {
			AnimationEvent event = eventClass.getConstructor(int.class).newInstance(tick);
			return event;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static AnimationEvent loadFromNBT(NBTTagCompound nbt) {
		Class<? extends AnimationEvent> eventClass = getType(nbt.getString("id"));
		if (eventClass == null) {
			System.out.println("Found invalid AnimationEvent type '" + nbt.getString("id") + "'!");
			return null;
		}
		
		try {
			AnimationEvent event = eventClass.getConstructor(int.class).newInstance(nbt.getInteger("tick"));
			event.activated = nbt.getBoolean("activated");
			event.read(nbt);
			return event;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	static {
		registerAnimationEventType("child", ChildActivateEvent.class, new AnimationEventGuiParser<ChildActivateEvent>() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public void createControls(GuiParent parent, @Nullable ChildActivateEvent event, LittlePreviews previews) {
				List<Integer> possibleChildren = new ArrayList<>();
				List<String> children = new ArrayList<>();
				int i = 0;
				for (LittlePreviews child : previews.getChildren()) {
					if (LittleDoor.class.isAssignableFrom(LittleStructureRegistry.getStructureClass(child.getStructureId()))) {
						children.add(SubGuiDoorEvents.getDisplayName(child, i));
						possibleChildren.add(i);
					}
					i++;
				}
				
				GuiComboBox box = new GuiComboBox("child", 38, 0, 100, children);
				if (event != null)
					box.select(event.childId);
				parent.addControl(box);
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			public ChildActivateEvent parse(GuiParent parent, ChildActivateEvent event) {
				GuiComboBox child = (GuiComboBox) parent.get("child");
				try {
					event.childId = Integer.parseInt(child.caption.split(":")[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (child.lines.isEmpty())
					return null;
				return event;
			}
		});
		
		registerAnimationEventType("sound-event", PlaySoundEvent.class, new AnimationEventGuiParser<PlaySoundEvent>() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public void createControls(GuiParent parent, PlaySoundEvent event, LittlePreviews previews) {
				parent.addControl(new GuiStateButton("opening", event != null ? (event.opening ? 0 : 1) : 0, 37, 0, 40, 7, GuiControl.translate("gui.door.open"), GuiControl.translate("gui.door.close")));
				parent.addControl(new GuiPickSoundButton("sound", 86, 0, event));
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			public PlaySoundEvent parse(GuiParent parent, PlaySoundEvent event) {
				GuiPickSoundButton picker = (GuiPickSoundButton) parent.get("sound");
				GuiStateButton opening = (GuiStateButton) parent.get("opening");
				if (picker.selected != null) {
					event.pitch = picker.pitch;
					event.volume = picker.volume;
					event.sound = picker.selected;
					event.opening = opening.getState() == 0;
					return event;
				}
				return null;
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			public int getHeight() {
				return 20;
			}
		});
	}
	
	private int tick;
	private boolean activated = false;
	
	public AnimationEvent(int tick) {
		this.tick = tick;
	}
	
	public int getTick() {
		return tick;
	}
	
	public void reset() {
		activated = false;
	}
	
	public boolean shouldBeProcessed(int tick) {
		return this.tick <= tick && !activated;
	}
	
	public int getMinimumRequiredDuration(LittleStructure structure) {
		return tick + getEventDuration(structure);
	}
	
	public abstract int getEventDuration(LittleStructure structure);
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setString("id", getId(this.getClass()));
		nbt.setInteger("tick", tick);
		nbt.setBoolean("activated", activated);
		write(nbt);
		return nbt;
	}
	
	protected abstract void write(NBTTagCompound nbt);
	
	protected abstract void read(NBTTagCompound nbt);
	
	public boolean process(EntityAnimationController controller) {
		if (run(controller)) {
			activated = true;
			return true;
		}
		return false;
	}
	
	protected abstract boolean run(EntityAnimationController controller);
	
	@SideOnly(Side.CLIENT)
	public void runGui(AnimationGuiHandler handler) {
		
	}
	
	@SideOnly(Side.CLIENT)
	public void prepareInGui(LittlePreviews previews, LittleStructure structure, EntityAnimation animation, AnimationGuiHandler handler) {
		
	}
	
	public void invert(LittleDoor door, int duration) {
		this.tick = duration - getMinimumRequiredDuration(door);
	}
	
	@Override
	public int compareTo(AnimationEvent o) {
		return Integer.compare(this.tick, o.tick);
	}
}
