package com.creativemd.littletiles;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.creativemd.creativecore.transformer.CreativeTransformer;
import com.creativemd.creativecore.transformer.Transformer;
import com.creativemd.creativecore.transformer.TransformerNames;
import com.creativemd.littletiles.common.structure.LittleBed;
import com.creativemd.littletiles.common.structure.LittleStructure;

import lombok.experimental.var;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.DependsOn;

public class LittleTilesTransformer extends CreativeTransformer {

	public LittleTilesTransformer() {
		super("littletiles");
	}

	@Override
	protected void initTransformers() {
		addTransformer(new Transformer("net.minecraft.client.renderer.RenderGlobal") {
			
			@Override
			public void transform(ClassNode classNode) {
				MethodNode m = findMethod(classNode, "loadRenderers", "()V");
				String className = patchClassName("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher");
				boolean isNextLabel = false;
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode node = (AbstractInsnNode) iterator.next();
					if(isNextLabel && node instanceof LabelNode)
					{
						m.instructions.insert(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "onReloadRenderers", patchDESC("(Lnet/minecraft/client/renderer/RenderGlobal;)V"), false));
						m.instructions.insert(node, new VarInsnNode(Opcodes.ALOAD, 0));
						isNextLabel = false;
					}
					if(node instanceof TypeInsnNode && ((TypeInsnNode) node).getOpcode() == Opcodes.NEW && ((TypeInsnNode) node).desc.equals(className))
						((TypeInsnNode) node).desc = "com/creativemd/littletiles/client/render/LittleChunkDispatcher";
					if(node instanceof MethodInsnNode && ((MethodInsnNode) node).name.equals("<init>") && ((MethodInsnNode) node).desc.equals("()V") && ((MethodInsnNode) node).owner.equals(className))
					{
						MethodInsnNode method = (MethodInsnNode) node;
						method.owner = "com/creativemd/littletiles/client/render/LittleChunkDispatcher";
						isNextLabel = true;
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.entity.player.EntityPlayer") {
			
			@Override
			public void transform(ClassNode node) {
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "littleBed", "Lcom/creativemd/littletiles/common/structure/LittleStructure;", null, null));
				
				MethodNode m = findMethod(node, "getBedOrientationInDegrees", "()F");
				if(m != null)
				{
					m.instructions.clear();
					
					m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/structure/LittleBed", "getBedOrientationInDegrees", patchDESC("(Lnet/minecraft/entity/player/EntityPlayer;)F"), false));
					m.instructions.add(new InsnNode(Opcodes.FRETURN));
				}
			}
		});
		addTransformer(new Transformer("net.minecraftforge.client.ForgeHooksClient") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "orientBedCamera",  "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V");
				
				ListIterator<AbstractInsnNode> iterator = m.instructions.iterator();
				
				boolean nextLabel = false;
				while(iterator.hasNext())
				{
					AbstractInsnNode insn = iterator.next();
					if(nextLabel && insn instanceof LabelNode)
					{
						m.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/structure/LittleBed", "setBedDirection", patchDESC("(Lnet/minecraft/entity/Entity;)V"), false));
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ALOAD, 3));
						break;
					}
					if(insn instanceof MethodInsnNode && ((MethodInsnNode) insn).owner.equals(patchDESC("org/lwjgl/opengl/GL11")) && ((MethodInsnNode) insn).name.equals(patchFieldName("glRotatef")))
						nextLabel = true;
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.client.renderer.chunk.RenderChunk") {

			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "resortTransparency", "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
				
				m.instructions.clear();
				
				m.instructions.add(new LabelNode());
				
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.add(new VarInsnNode(Opcodes.FLOAD, 1));
				m.instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
				m.instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "resortTransparency", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V", false));
				
				m.instructions.add(new InsnNode(Opcodes.RETURN));
				m.instructions.add(new LabelNode());
				
			}
			
		});
		addTransformer(new Transformer("net.minecraft.network.NettyCompressionDecoder") {
			
			@Override
			public void transform(ClassNode node) {
				
				String descException = patchDESC("io/netty/handler/codec/DecoderException");
				
				String desc = patchDESC("(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V");
				String name = TransformerNames.patchMethodName("decode", desc, patchClassName("io/netty/handler/codec/ByteToMessageDecoder"));
				MethodNode m = findMethod(node, name, desc);
				
				ListIterator<AbstractInsnNode> iterator = m.instructions.iterator();
				
				int found = 0;
				while(iterator.hasNext())
				{
					AbstractInsnNode insn = iterator.next();
					if(insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.NEW && ((TypeInsnNode) insn).desc.equals(descException))
					{
						found++;
						if(found == 2)
							iterator.remove();
					}else if(found == 2){
						iterator.remove();
						if(insn instanceof InsnNode && insn.getOpcode() == Opcodes.ATHROW)
							break;
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.world.chunk.Chunk") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "onChunkUnload", "()V");
				
				String fieldOwner = patchClassName("net/minecraft/world/chunk/Chunk");
				String fieldName = patchFieldName("chunkTileEntityMap");
				
				boolean found = false;
				
				Iterator<AbstractInsnNode> iterator = m.instructions.iterator();
				while(iterator.hasNext())
				{
					AbstractInsnNode insn = iterator.next();
					if(insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) insn).owner.equals(fieldOwner) && ((FieldInsnNode) insn).name.equals(fieldName))
						found = true;
					if(found)
					{
						iterator.remove();
						if(insn instanceof JumpInsnNode && insn.getOpcode() == Opcodes.GOTO)
							break;
					}
				}
			}
		});
		
		addTransformer(new Transformer("net.minecraft.world.World") {
			
			@Override
			public void transform(ClassNode node) {
				
				String worldClassName = patchClassName("net/minecraft/world/World");
				String fieldDESC = patchDESC("Ljava/util/List;");
				String fieldNameTicking = patchFieldName("tickableTileEntities");
				String fieldNameLoaded = patchFieldName("loadedTileEntityList");
				
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, "tileEntitiesFromChunkToBeRemoved",  patchDESC("Ljava/util/List;"), patchDESC("Ljava/util/List<Lnet/minecraft/tileentity/TileEntity;>;"), null));
				
				Iterator<MethodNode> iterator = node.methods.iterator();
				while(iterator.hasNext())
				{
					AbstractInsnNode toInsert = null;
					MethodNode m = iterator.next();
					if(m.name.equals("<init>"))
					{
						Iterator<AbstractInsnNode> iterator2 = m.instructions.iterator();
						while(iterator2.hasNext())
						{
							AbstractInsnNode insn = iterator2.next();
							if(insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode) insn).owner.equals(worldClassName) && (((FieldInsnNode) insn).name.equals(fieldNameLoaded) || ((FieldInsnNode) insn).name.equals(fieldNameTicking)))
							{
								MethodInsnNode m2 = (MethodInsnNode) insn.getPrevious();
								m2.name = "createTileEntity";
								m2.owner = "com/creativemd/littletiles/common/world/WorldInteractor";
								m2.desc = patchDESC("()Ljava/util/List;");
								m2.setOpcode(Opcodes.INVOKESTATIC);
								if(toInsert == null)
									toInsert = insn;
							}
						}
						if(toInsert != null)
						{
							m.instructions.insert(toInsert, new FieldInsnNode(Opcodes.PUTFIELD, worldClassName, "tileEntitiesFromChunkToBeRemoved", fieldDESC));
							m.instructions.insert(toInsert, new MethodInsnNode(Opcodes.INVOKESPECIAL, patchDESC("java/util/ArrayList"), "<init>", "()V", false));
							m.instructions.insert(toInsert, new InsnNode(Opcodes.DUP));
							m.instructions.insert(toInsert, new TypeInsnNode(Opcodes.NEW, patchDESC("java/util/ArrayList")));
							m.instructions.insert(toInsert, new VarInsnNode(Opcodes.ALOAD, 0));
							m.instructions.insert(new LabelNode());
							toInsert = null;
						}
					}
					
				}
				
				String fieldName = patchFieldName("tileEntitiesToBeRemoved");
				String methodOwner = patchClassName("java/util/List");
				
				MethodNode m = findMethod(node, "updateEntities", "()V");
				Iterator<AbstractInsnNode> iterator2 = m.instructions.iterator();
				AbstractInsnNode toInsert = null;
				while(iterator2.hasNext())
				{
					AbstractInsnNode insn = iterator2.next();
					if(insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) insn).owner.equals(worldClassName) && ((FieldInsnNode) insn).name.equals(fieldName))
					{
						AbstractInsnNode next = insn.getNext();
						if(next instanceof MethodInsnNode && ((MethodInsnNode) next).owner.equals(methodOwner) && ((MethodInsnNode) next).name.equals("isEmpty"))
						{
							toInsert = insn.getPrevious();
						}
					}
				}
				
				m.instructions.insertBefore(toInsert, new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.insertBefore(toInsert, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/world/WorldInteractor", "removeTileEntities", patchDESC("(Lnet/minecraft/world/World;)V"), false));
				
			}
		});
		addTransformer(new Transformer("net.minecraft.network.PacketBuffer") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "readCompoundTag", "()Lnet/minecraft/nbt/NBTTagCompound;");
				
				Iterator<AbstractInsnNode> iterator = m.instructions.iterator();
				while(iterator.hasNext())
				{
					AbstractInsnNode insn = iterator.next();
					if(insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Long)
					{
						m.instructions.remove(insn.getPrevious().getPrevious());
						m.instructions.remove(insn.getPrevious());
						m.instructions.remove(insn.getNext());
						
						
						m.instructions.insert(insn, new FieldInsnNode(Opcodes.GETSTATIC, patchDESC("net/minecraft/nbt/NBTSizeTracker"), "INFINITE", patchDESC("Lnet/minecraft/nbt/NBTSizeTracker;")));
						m.instructions.remove(insn);
						break;
					}
				}
			}
		});
	}

}
