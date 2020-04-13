package com.creativemd.littletiles.common.structure.registry;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.type.LittleBed;
import com.creativemd.littletiles.common.structure.type.LittleBed.LittleBedParser;
import com.creativemd.littletiles.common.structure.type.LittleChair;
import com.creativemd.littletiles.common.structure.type.LittleChair.LittleChairParser;
import com.creativemd.littletiles.common.structure.type.LittleFixedStructure;
import com.creativemd.littletiles.common.structure.type.LittleFixedStructure.LittleFixedStructureParser;
import com.creativemd.littletiles.common.structure.type.LittleLadder;
import com.creativemd.littletiles.common.structure.type.LittleLadder.LittleLadderParser;
import com.creativemd.littletiles.common.structure.type.LittleNoClipStructure;
import com.creativemd.littletiles.common.structure.type.LittleNoClipStructure.LittleNoClipStructureParser;
import com.creativemd.littletiles.common.structure.type.LittleStorage;
import com.creativemd.littletiles.common.structure.type.LittleStorage.LittleStorageParser;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorBase;

public class LittleStructureRegistry {
	
	private static HashMap<String, LittleStructureType> structures = new LinkedHashMap<String, LittleStructureType>();
	private static HashMap<Class<? extends LittleStructure>, LittleStructureType> structuresClass = new LinkedHashMap<Class<? extends LittleStructure>, LittleStructureType>();
	
	private static PairList<String, PairList<String, Class<? extends LittleStructureGuiParser>>> craftables = new PairList<>();
	
	public static LittleStructureType registerStructureType(String id, String category, Class<? extends LittleStructure> classStructure, int attribute, Class<? extends LittleStructureGuiParser> parser) {
		return registerStructureType(id, new LittleStructureType(id, category, classStructure, attribute), parser);
	}
	
	public static void registerGuiParser(String id, String category, Class<? extends LittleStructureGuiParser> parser) {
		category = "structure.category." + category;
		PairList<String, Class<? extends LittleStructureGuiParser>> categoryList = craftables.getValue(category);
		if (categoryList == null) {
			categoryList = new PairList<>();
			craftables.add(category, categoryList);
		}
		categoryList.add("structure." + id + ".name", parser);
	}
	
	public static PairList<String, PairList<String, Class<? extends LittleStructureGuiParser>>> getCraftables() {
		return craftables;
	}
	
	public static Class<? extends LittleStructureGuiParser> getParserClass(String id) {
		for (Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>> pair : craftables) {
			Class<? extends LittleStructureGuiParser> parser = pair.value.getValue(id);
			if (parser != null)
				return parser;
		}
		return null;
	}
	
	public static LittleStructureGuiParser getParser(GuiParent parent, AnimationGuiHandler handler, Class<? extends LittleStructureGuiParser> clazz) {
		try {
			if (clazz == null)
				return null;
			return clazz.getConstructor(GuiParent.class, AnimationGuiHandler.class).newInstance(parent, handler);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static LittleStructureGuiParser getParser(GuiParent parent, AnimationGuiHandler handler, String id) {
		try {
			Class<? extends LittleStructureGuiParser> clazz = getParserClass(id);
			if (clazz == null)
				return null;
			return clazz.getConstructor(GuiParent.class, AnimationGuiHandler.class).newInstance(parent, handler);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static LittleStructureType registerStructureType(String id, LittleStructureType entry, Class<? extends LittleStructureGuiParser> parser) {
		if (structures.containsKey(id))
			throw new RuntimeException("ID is already taken! id=" + id);
		
		if (parser != null)
			registerGuiParser(id, entry.category, parser);
		
		structures.put(id, entry);
		structuresClass.put(entry.structureClass, entry);
		return entry;
	}
	
	public static String getStructureId(Class<? extends LittleStructure> classStructure) {
		LittleStructureType entry = structuresClass.get(classStructure);
		if (entry != null)
			return entry.id;
		return null;
	}
	
	public static Class<? extends LittleStructure> getStructureClass(String id) {
		LittleStructureType entry = structures.get(id);
		if (entry != null)
			return entry.structureClass;
		return null;
	}
	
	public static LittleStructureType getStructureType(String id) {
		return structures.get(id);
	}
	
	public static LittleStructureType getStructureType(Class<? extends LittleStructure> classStructure) {
		return structuresClass.get(classStructure);
	}
	
	public static void initStructures() {
		registerStructureType("fixed", "simple", LittleFixedStructure.class, LittleStructureAttribute.NONE, LittleFixedStructureParser.class);
		
		registerStructureType("ladder", "simple", LittleLadder.class, LittleStructureAttribute.LADDER, LittleLadderParser.class);
		
		registerStructureType("bed", "simple", LittleBed.class, LittleStructureAttribute.NONE, LittleBedParser.class);
		registerStructureType("chair", "simple", LittleChair.class, LittleStructureAttribute.NONE, LittleChairParser.class);
		
		registerStructureType("storage", "simple", LittleStorage.class, LittleStructureAttribute.NONE, LittleStorageParser.class);
		registerStructureType("noclip", "simple", LittleNoClipStructure.class, LittleStructureAttribute.NOCOLLISION, LittleNoClipStructureParser.class);
		
		LittleDoorBase.initDoors();
		
		LittleStructurePremade.initPremadeStructures();
	}
}
