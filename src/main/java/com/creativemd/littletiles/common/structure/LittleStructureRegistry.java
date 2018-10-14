package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.type.LittleBed;
import com.creativemd.littletiles.common.structure.type.LittleBed.LittleBedParser;
import com.creativemd.littletiles.common.structure.type.LittleChair;
import com.creativemd.littletiles.common.structure.type.LittleChair.LittleChairParser;
import com.creativemd.littletiles.common.structure.type.LittleDoor;
import com.creativemd.littletiles.common.structure.type.LittleDoor.LittleDoorParser;
import com.creativemd.littletiles.common.structure.type.LittleDoor.LittleDoorPreviewHandler;
import com.creativemd.littletiles.common.structure.type.LittleFixedStructure;
import com.creativemd.littletiles.common.structure.type.LittleFixedStructure.LittleFixedStructureParser;
import com.creativemd.littletiles.common.structure.type.LittleLadder;
import com.creativemd.littletiles.common.structure.type.LittleLadder.LittleLadderParser;
import com.creativemd.littletiles.common.structure.type.LittleNoClipStructure;
import com.creativemd.littletiles.common.structure.type.LittleNoClipStructure.LittleNoClipStructureParser;
import com.creativemd.littletiles.common.structure.type.LittleSlidingDoor;
import com.creativemd.littletiles.common.structure.type.LittleSlidingDoor.LittleSlidingDoorParser;
import com.creativemd.littletiles.common.structure.type.LittleStorage;
import com.creativemd.littletiles.common.structure.type.LittleStorage.LittleStorageParser;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleStructureRegistry {
	
	private static HashMap<String, LittleStructureEntry> structuresID = new LinkedHashMap<String, LittleStructureEntry>();
	private static HashMap<Class<? extends LittleStructure>, LittleStructureEntry> structuresClass = new LinkedHashMap<Class<? extends LittleStructure>, LittleStructureEntry>();
	
	private static List<String> cachedNames = new ArrayList<>();
	
	public static List<String> getStructureTypeNames() {
		return new ArrayList<>(cachedNames);
	}
	
	public static void registerStructureType(String id, Class<? extends LittleStructure> classStructure, LittleStructureAttribute attribute, Class<? extends LittleStructureGuiParser> parser) {
		registerStructureType(id, classStructure, attribute, parser, null);
	}
	
	public static void registerStructureType(String id, Class<? extends LittleStructure> classStructure, LittleStructureAttribute attribute, Class<? extends LittleStructureGuiParser> parser, LittleStructurePreviewHandler handler) {
		LittleStructureEntry entry = new LittleStructureEntry(id, classStructure, parser, attribute, handler);
		registerStructureType(id, entry);
		if (attribute != LittleStructureAttribute.PREMADE)
			cachedNames.add(id);
	}
	
	private static void registerStructureType(String id, LittleStructureEntry entry) {
		if (structuresID.containsKey(id))
			throw new RuntimeException("ID is already taken! id=" + id);
		if (structuresID.containsValue(entry))
			throw new RuntimeException("Already registered class=" + entry);
		
		structuresID.put(id, entry);
		structuresClass.put(entry.structureClass, entry);
	}
	
	public static String getStructureID(Class<? extends LittleStructure> classStructure) {
		LittleStructureEntry entry = structuresClass.get(classStructure);
		if (entry != null)
			return entry.id;
		return null;
	}
	
	public static Class<? extends LittleStructure> getStructureClass(String id) {
		LittleStructureEntry entry = structuresID.get(id);
		if (entry != null)
			return entry.structureClass;
		return null;
	}
	
	public static LittleStructureEntry getStructureEntry(String id) {
		return structuresID.get(id);
	}
	
	public static LittleStructureEntry getStructureEntry(Class<? extends LittleStructure> classStructure) {
		return structuresClass.get(classStructure);
	}
	
	public static void initStructures() {
		registerStructureType("fixed", LittleFixedStructure.class, LittleStructureAttribute.NONE, LittleFixedStructureParser.class);
		registerStructureType("chair", LittleChair.class, LittleStructureAttribute.NONE, LittleChairParser.class);
		registerStructureType("door", LittleDoor.class, LittleStructureAttribute.NONE, LittleDoorParser.class, new LittleDoorPreviewHandler());
		registerStructureType("slidingDoor", LittleSlidingDoor.class, LittleStructureAttribute.NONE, LittleSlidingDoorParser.class);
		registerStructureType("ladder", LittleLadder.class, LittleStructureAttribute.LADDER, LittleLadderParser.class);
		registerStructureType("bed", LittleBed.class, LittleStructureAttribute.NONE, LittleBedParser.class);
		registerStructureType("storage", LittleStorage.class, LittleStructureAttribute.NONE, LittleStorageParser.class);
		registerStructureType("noclip", LittleNoClipStructure.class, LittleStructureAttribute.COLLISION, LittleNoClipStructureParser.class);
		
		LittleStructurePremade.initPremadeStructures();
	}
	
	public static final class LittleStructureEntry {
		
		public final String id;
		public final Class<? extends LittleStructure> structureClass;
		public Class<? extends LittleStructureGuiParser> parser;
		public final LittleStructureAttribute attribute;
		public final LittleStructurePreviewHandler handler;
		
		private LittleStructureEntry(String id, Class<? extends LittleStructure> structureClass, Class<? extends LittleStructureGuiParser> parser, LittleStructureAttribute attribute, @Nullable LittleStructurePreviewHandler handler) {
			this.id = id;
			this.structureClass = structureClass;
			this.parser = parser;
			this.attribute = attribute;
			if (handler == null)
				this.handler = new LittleStructurePreviewHandler();
			else
				this.handler = handler;
		}
		
		@Override
		public boolean equals(Object object) {
			return object instanceof LittleStructureEntry && ((LittleStructureEntry) object).structureClass == this.structureClass;
		}
		
		@Override
		public String toString() {
			return structureClass.toString();
		}
		
		@SideOnly(Side.CLIENT)
		public LittleStructureGuiParser createParser(GuiParent parent) {
			try {
				return parser.getConstructor(String.class, GuiParent.class).newInstance(id, parent);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static class LittleStructurePreviewHandler {
		
		public LittleGridContext getMinContext(LittlePreviews previews) {
			return LittleGridContext.getMin();
		}
		
		public List<PlacePreviewTile> getSpecialTiles(LittlePreviews previews) {
			return new ArrayList<>();
		}
		
	}
}
