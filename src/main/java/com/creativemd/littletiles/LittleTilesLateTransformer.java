package com.creativemd.littletiles;

import java.io.PrintWriter;
import java.util.Iterator;

import org.objectweb.asm.Opcodes;
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
import org.objectweb.asm.util.TraceClassVisitor;

import com.creativemd.creativecore.transformer.CreativeTransformer;
import com.creativemd.creativecore.transformer.Transformer;
import com.creativemd.creativecore.transformer.TransformerNames;

public class LittleTilesLateTransformer extends CreativeTransformer {

	public LittleTilesLateTransformer() {
		super("littletiles");
	}

	@Override
	protected void initTransformers() {
		addTransformer(new Transformer("net.minecraft.client.renderer.chunk.RenderChunk") {
			
			@Override
			public void transform(ClassNode node) {
				boolean obfuscated = TransformerNames.obfuscated;
				TransformerNames.obfuscated = false;
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "littleTiles", "Ljava/util/List;", "Ljava/util/List<Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;>;", null));
				
				String ownerBefore = "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher";
				String nameBefore = obfuscated ? "field_147556_a" : "instance";
				
				String ownerAfter = patchClassName("net/minecraft/client/renderer/chunk/VisGraph");
				String descAfter = patchDESC("()Lnet/minecraft/client/renderer/chunk/SetVisibility;");
				String nameAfter = obfuscated ? "func_178607_a" : "computeVisibility";
				
				MethodNode m = findMethod(node, obfuscated ? "func_178581_b" : "rebuildChunk", "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V");
				
				int tilesLocalID = 14;
				LocalVariableNode tilesLocal = null;
				for (Iterator<LocalVariableNode> iterator = m.localVariables.iterator(); iterator.hasNext();) {
					LocalVariableNode variable = iterator.next();
					tilesLocalID = Math.max(tilesLocalID, variable.index);
				}
				
				LabelNode first = new LabelNode();
				LabelNode last = new LabelNode();
				
				tilesLocal = new LocalVariableNode("tiles", "Ljava/util/List;", "Ljava/util/List<Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;>;", first, last, tilesLocalID);
				
				tilesLocalID++;
				
				m.localVariables.add(tilesLocal);
				
				m.instructions.insert(m.instructions.getFirst(), new VarInsnNode(Opcodes.ASTORE, tilesLocalID));
				m.instructions.insert(m.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false));
				m.instructions.insert(m.instructions.getFirst(), new InsnNode(Opcodes.DUP));
				m.instructions.insert(m.instructions.getFirst(), new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
				
				m.instructions.insert(m.instructions.getFirst(), first);
				
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					if(insn instanceof FieldInsnNode && ((FieldInsnNode) insn).owner.equals(ownerBefore) && ((FieldInsnNode) insn).name.equals(nameBefore))
					{
						insn = insn.getPrevious();
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, tilesLocalID));
						int index = 17;
						String varDesc = "Lnet/minecraft/tileentity/TileEntity;";
						for (Iterator<LocalVariableNode> iterator2 = m.localVariables.iterator(); iterator2.hasNext();) {
							LocalVariableNode local = iterator2.next();
							if(local.desc.equals(varDesc))
								index = local.index;
						}
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, index));
						m.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "addTileEntity", "(Ljava/util/List;Lnet/minecraft/tileentity/TileEntity;)V", false));
						//m.instructions.insertBefore(insn, last);
					}else if(insn instanceof MethodInsnNode && ((MethodInsnNode) insn).desc.equals(descAfter) && ((MethodInsnNode) insn).owner.equals(ownerAfter) && ((MethodInsnNode) insn).name.equals(nameAfter)){
						AbstractInsnNode before = insn.getPrevious().getPrevious();
						m.instructions.insertBefore(before, new VarInsnNode(Opcodes.ALOAD, 0));
						m.instructions.insertBefore(before, new VarInsnNode(Opcodes.ALOAD, tilesLocalID));
						m.instructions.insertBefore(before, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "onDoneRendering", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;Ljava/util/List;)V", false));
						
					}
				}
				m.instructions.insert(m.instructions.getLast(), last);
				
				TransformerNames.obfuscated = obfuscated;
			}
		});
	}

}
