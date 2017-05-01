package com.creativemd.littletiles;

import java.util.Iterator;
import java.util.ListIterator;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
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
				
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "littleTiles", patchDESC("Ljava/util/List;"), patchDESC("Ljava/util/List<Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;>;"), null));
				
				String ownerBefore = patchClassName("net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher");
				String nameBefore = TransformerNames.patchFieldName("instance", ownerBefore);
				
				String ownerAfter = patchClassName("net/minecraft/client/renderer/chunk/VisGraph");
				String descAfter = patchDESC("()Lnet/minecraft/client/renderer/chunk/SetVisibility;");
				String nameAfter = TransformerNames.patchMethodName("computeVisibility", descAfter, ownerAfter);
				
				MethodNode m = findMethod(node, "rebuildChunk", "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
				
				int tilesLocalID = 14;
				LocalVariableNode tilesLocal = null;
				for (Iterator<LocalVariableNode> iterator = m.localVariables.iterator(); iterator.hasNext();) {
					LocalVariableNode variable = iterator.next();
					tilesLocalID = Math.max(tilesLocalID, variable.index);
				}
				
				LabelNode first = new LabelNode();
				LabelNode last = new LabelNode();
				
				tilesLocal = new LocalVariableNode("tiles", patchDESC("Ljava/util/List;"), patchDESC("Ljava/util/List<Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;>;"), first, last, tilesLocalID);
				
				tilesLocalID++;
				
				m.localVariables.add(tilesLocal);
				
				m.instructions.insert(m.instructions.getFirst(), new VarInsnNode(Opcodes.ASTORE, tilesLocalID));
				m.instructions.insert(m.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESPECIAL, patchDESC("java/util/ArrayList"), "<init>", "()V", false));
				m.instructions.insert(m.instructions.getFirst(), new InsnNode(Opcodes.DUP));
				m.instructions.insert(m.instructions.getFirst(), new TypeInsnNode(Opcodes.NEW, patchDESC("java/util/ArrayList")));
				
				m.instructions.insert(m.instructions.getFirst(), first);
				
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					if(insn instanceof FieldInsnNode && ((FieldInsnNode) insn).owner.equals(ownerBefore) && ((FieldInsnNode) insn).name.equals(nameBefore))
					{
						insn = insn.getPrevious();
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, tilesLocalID));
						int index = 17;
						String varDesc = patchDESC("Lnet/minecraft/tileentity/TileEntity;");
						for (Iterator<LocalVariableNode> iterator2 = m.localVariables.iterator(); iterator2.hasNext();) {
							LocalVariableNode local = iterator2.next();
							if(local.desc.equals(varDesc))
								index = local.index;
						}
						System.out.println("index: " + index + " desc:" + varDesc);
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, index));
						m.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "addTileEntity", patchDESC("(Ljava/util/List;Lnet/minecraft/tileentity/TileEntity;)V"), false));
						//m.instructions.insertBefore(insn, last);
					}else if(insn instanceof MethodInsnNode && ((MethodInsnNode) insn).desc.equals(descAfter) && ((MethodInsnNode) insn).owner.equals(ownerAfter) && ((MethodInsnNode) insn).name.equals(nameAfter)){
						AbstractInsnNode before = insn.getPrevious().getPrevious();
						m.instructions.insertBefore(before, new VarInsnNode(Opcodes.ALOAD, 0));
						m.instructions.insertBefore(before, new VarInsnNode(Opcodes.ALOAD, tilesLocalID));
						m.instructions.insertBefore(before, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "onDoneRendering", patchDESC("(Lnet/minecraft/client/renderer/chunk/RenderChunk;Ljava/util/List;)V"), false));
						
					}
				}
				m.instructions.insert(m.instructions.getLast(), last);
			}
		});
	}

}
