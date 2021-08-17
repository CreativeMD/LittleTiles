package team.creative.littletiles.common.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.creativemd.littletiles.common.action.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.action.LittleGridContext;
import com.creativemd.littletiles.common.action.LittlePreviews;
import com.creativemd.littletiles.common.action.NBTTagCompound;
import com.creativemd.littletiles.common.action.NBTTagList;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage.ActionMessageObjectType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import team.creative.creativecore.common.network.type.NetworkFieldTypeClass;
import team.creative.creativecore.common.network.type.NetworkFieldTypes;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.math.location.TileLocation;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.tile.group.LittleGroup;

public class LittlePacketTypes {
    
    public static void init() {
        NetworkFieldTypes.register(TileLocation.class, new NetworkFieldTypeClass<TileLocation>() {
            
            @Override
            protected void writeContent(TileLocation content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                buffer.writeBoolean(content.isStructure);
                buffer.writeInt(content.index);
                buffer.writeVarIntArray(content.box.getArray());
                if (content.levelUUID != null) {
                    buffer.writeBoolean(true);
                    buffer.writeUUID(content.levelUUID);
                } else
                    buffer.writeBoolean(false);
            }
            
            @Override
            protected TileLocation readContent(FriendlyByteBuf buffer) {
                BlockPos pos = buffer.readBlockPos();
                boolean isStructure = buffer.readBoolean();
                int index = buffer.readInt();
                int[] boxArray = buffer.readVarIntArray();
                UUID level = null;
                if (buffer.readBoolean())
                    level = buffer.readUUID();
                return new TileLocation(pos, isStructure, index, LittleBox.create(boxArray), level);
            }
            
        });
        NetworkFieldTypes.register(StructureLocation.class, new NetworkFieldTypeClass<StructureLocation>() {
            
            @Override
            protected void writeContent(StructureLocation content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                buffer.writeInt(content.index);
                if (content.levelUUID != null) {
                    buffer.writeBoolean(true);
                    buffer.writeUUID(content.levelUUID);
                } else
                    buffer.writeBoolean(false);
            }
            
            @Override
            protected StructureLocation readContent(FriendlyByteBuf buffer) {
                BlockPos pos = buffer.readBlockPos();
                int index = buffer.readInt();
                UUID level = null;
                if (buffer.readBoolean())
                    level = buffer.readUUID();
                return new StructureLocation(pos, index, level);
            }
            
        });
        
        NetworkFieldTypes.register(LittleGroup.class, new NetworkFieldTypeClass<LittleGroup>() {
            
            @Override
            protected void writeContent(LittleGroup content, FriendlyByteBuf buffer) {
                buf.writeBoolean(previews.isAbsolute());
                buf.writeBoolean(previews.hasStructure());
                if (previews.hasStructure())
                    writeNBT(buf, previews.structureNBT);
                if (previews.isAbsolute())
                    writePos(buf, ((LittleAbsolutePreviews) previews).pos);
                
                writeContext(previews.getContext(), buf);
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setTag("list", LittleNBTCompressionTools.writePreviews(previews));
                
                NBTTagList children = new NBTTagList();
                for (LittlePreviews child : previews.getChildren())
                    children.appendTag(LittlePreview.saveChildPreviews(child));
                nbt.setTag("children", children);
                
                writeNBT(buf, nbt);
            }
            
            @Override
            protected LittleGroup readContent(FriendlyByteBuf buffer) {
                boolean absolute = buf.readBoolean();
                boolean structure = buf.readBoolean();
                
                NBTTagCompound nbt;
                LittlePreviews previews;
                if (absolute) {
                    if (structure)
                        previews = LittleNBTCompressionTools
                                .readPreviews(new LittleAbsolutePreviews(readNBT(buf), readPos(buf), readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
                    else
                        previews = LittleNBTCompressionTools.readPreviews(new LittleAbsolutePreviews(readPos(buf), readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
                } else {
                    if (structure)
                        previews = LittleNBTCompressionTools.readPreviews(new LittlePreviews(readNBT(buf), readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
                    else
                        previews = LittleNBTCompressionTools.readPreviews(new LittlePreviews(readContext(buf)), (nbt = readNBT(buf)).getTagList("list", 10));
                }
                
                NBTTagList list = nbt.getTagList("children", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound child = list.getCompoundTagAt(i);
                    previews.addChild(LittlePreviews.getChild(previews.getContext(), child), child.getBoolean("dynamic"));
                }
                return previews;
            }
            
        });
        NetworkFieldTypes.register(PlacementMode.class, new NetworkFieldTypeClass<PlacementMode>() {
            
            @Override
            protected void writeContent(PlacementMode content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.name);
            }
            
            @Override
            protected PlacementMode readContent(FriendlyByteBuf buffer) {
                return PlacementMode.getModeOrDefault(buffer.readUtf());
            }
            
        });
        NetworkFieldTypes.register(LittleAction.class, new NetworkFieldTypeClass<LittleAction>() {
            
            @Override
            protected void writeContent(LittleAction content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.getClass().getName());
                LittleTiles.NETWORK.getPacketType(content.getClass()).write(content, buffer);
            }
            
            @Override
            protected LittleAction readContent(FriendlyByteBuf buffer) {
                Class clazz = Class.forName(buffer.readUtf());
                return (LittleAction) LittleTiles.NETWORK.getPacketType(clazz).read(buffer);
            }
            
        });
        NetworkFieldTypes.register(LittleGrid.class, new NetworkFieldTypeClass<LittleGrid>() {
            
            @Override
            protected void writeContent(LittleGrid content, FriendlyByteBuf buffer) {
                buffer.writeInt(content.count);
            }
            
            @Override
            protected LittleGrid readContent(FriendlyByteBuf buffer) {
                return LittleGrid.get(buffer.readInt());
            }
            
        });
        NetworkFieldTypes.register(LittleVec.class, new NetworkFieldTypeClass<LittleVec>() {
            
            @Override
            protected void writeContent(LittleVec vec, FriendlyByteBuf buffer) {
                buffer.writeInt(vec.x);
                buffer.writeInt(vec.y);
                buffer.writeInt(vec.z);
            }
            
            @Override
            protected LittleVec readContent(FriendlyByteBuf buffer) {
                return new LittleVec(buffer.readInt(), buffer.readInt(), buffer.readInt());
            }
            
        });
        NetworkFieldTypes.register(LittleVecGrid.class, new NetworkFieldTypeClass<LittleVecGrid>() {
            
            @Override
            protected void writeContent(LittleVecGrid vec, FriendlyByteBuf buffer) {
                buffer.writeInt(vec.getVec().x);
                buffer.writeInt(vec.getVec().y);
                buffer.writeInt(vec.getVec().z);
                buffer.writeInt(vec.getGrid().count);
            }
            
            @Override
            protected LittleVecGrid readContent(FriendlyByteBuf buffer) {
                return new LittleVecGrid(new LittleVec(buffer.readInt(), buffer.readInt(), buffer.readInt()), LittleGrid.get(buffer.readInt()));
            }
            
        });
        NetworkFieldTypes.register(LittleVecAbsolute.class, new NetworkFieldTypeClass<LittleVecAbsolute>() {
            
            @Override
            protected void writeContent(LittleVecAbsolute content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.getPos());
                NetworkFieldTypes.write(LittleVecGrid.class, content.getVecGrid(), buffer);
            }
            
            @Override
            protected LittleVecAbsolute readContent(FriendlyByteBuf buffer) {
                return new LittleVecAbsolute(buffer.readBlockPos(), NetworkFieldTypes.read(LittleVecGrid.class, buffer));
            }
            
        });
        NetworkFieldTypes.register(LittleBox.class, new NetworkFieldTypeClass<LittleBox>() {
            
            @Override
            protected void writeContent(LittleBox content, FriendlyByteBuf buffer) {
                buffer.writeVarIntArray(content.getArray());
            }
            
            @Override
            protected LittleBox readContent(FriendlyByteBuf buffer) {
                return LittleBox.create(buffer.readVarIntArray());
            }
            
        });
        NetworkFieldTypes.register(LittleBoxAbsolute.class, new NetworkFieldTypeClass<LittleBoxAbsolute>() {
            
            @Override
            protected void writeContent(LittleBoxAbsolute content, FriendlyByteBuf buffer) {
                buffer.writeBlockPos(content.pos);
                buffer.writeVarIntArray(content.box.getArray());
                buffer.writeInt(content.getGrid().count);
            }
            
            @Override
            protected LittleBoxAbsolute readContent(FriendlyByteBuf buffer) {
                return new LittleBoxAbsolute(buffer.readBlockPos(), LittleBox.create(buffer.readVarIntArray()), LittleGrid.get(buffer.readInt()));
            }
            
        });
        NetworkFieldTypes.register(LittleBoxes.class, new NetworkFieldTypeClass<LittleBoxes>() {
            
            @Override
            protected void writeContent(LittleBoxes content, FriendlyByteBuf buffer) {
                writePos(buf, boxes.pos);
                writeContext(boxes.context, buf);
                if (boxes instanceof LittleBoxesSimple) {
                    buf.writeBoolean(true);
                    buf.writeInt(boxes.size());
                    for (LittleBox box : boxes.all())
                        writeLittleBox(box, buf);
                } else {
                    buf.writeBoolean(false);
                    HashMapList<BlockPos, LittleBox> map = boxes.generateBlockWise();
                    buf.writeInt(map.size());
                    for (Entry<BlockPos, ArrayList<LittleBox>> entry : map.entrySet()) {
                        writePos(buf, entry.getKey());
                        buf.writeInt(entry.getValue().size());
                        for (LittleBox box : entry.getValue())
                            writeLittleBox(box, buf);
                    }
                }
            }
            
            @Override
            protected LittleBoxes readContent(FriendlyByteBuf buffer) {
                BlockPos pos = readPos(buf);
                LittleGridContext context = readContext(buf);
                if (buf.readBoolean()) {
                    LittleBoxes boxes = new LittleBoxesSimple(pos, context);
                    int length = buf.readInt();
                    for (int i = 0; i < length; i++)
                        boxes.add(readLittleBox(buf));
                    return boxes;
                } else {
                    int posCount = buf.readInt();
                    HashMapList<BlockPos, LittleBox> map = new HashMapList<>();
                    for (int i = 0; i < posCount; i++) {
                        BlockPos posList = readPos(buf);
                        int boxCount = buf.readInt();
                        List<LittleBox> blockBoxes = new ArrayList<>();
                        for (int j = 0; j < boxCount; j++)
                            blockBoxes.add(readLittleBox(buf));
                        map.add(posList, blockBoxes);
                    }
                    return new LittleBoxesNoOverlap(pos, context, map);
                }
            }
            
        });
        NetworkFieldTypes.register(TileSelector.class, new NetworkFieldTypeClass<TileSelector>() {
            
            @Override
            protected void writeContent(TileSelector content, FriendlyByteBuf buffer) {
                buffer.writeNbt(content.write(new CompoundTag()));
            }
            
            @Override
            protected TileSelector readContent(FriendlyByteBuf buffer) {
                return TileSelector.loadSelector(buffer.readNbt());
            }
            
        });
        
        NetworkFieldTypes.register(ActionMessage.class, new NetworkFieldTypeClass<ActionMessage>() {
            
            @Override
            protected void writeContent(ActionMessage content, FriendlyByteBuf buffer) {
                buffer.writeUtf(content.text);
                buffer.writeInt(content.objects.length);
                for (int i = 0; i < content.objects.length; i++) {
                    ActionMessageObjectType type = ActionMessage.getType(content.objects[i]);
                    buffer.writeInt(type.index());
                    type.write(content.objects[i], buffer);
                }
            }
            
            @Override
            protected ActionMessage readContent(FriendlyByteBuf buffer) {
                String text = buffer.readUtf();
                Object[] objects = new Object[buffer.readInt()];
                for (int i = 0; i < objects.length; i++) {
                    ActionMessageObjectType type = ActionMessage.getType(buffer.readInt());
                    objects[i] = type.read(buffer);
                }
                return new ActionMessage(text, objects);
            }
            
        });
    }
}
