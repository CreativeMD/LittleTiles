package com.creativemd.littletiles;

import java.util.Iterator;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
		addTransformer(new Transformer("net.minecraft.client.renderer.VertexBuffer") {
			
			@Override
			public void transform(ClassNode node) {
				
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "littleTilesAdded", "Z", null, Boolean.FALSE));
				
				MethodNode m = findMethod(node, "reset", "()V");
				
				AbstractInsnNode start = m.instructions.getFirst();
				
				m.instructions.insertBefore(start, new LabelNode());
				m.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_0));
				m.instructions.insertBefore(start, new FieldInsnNode(Opcodes.PUTFIELD, patchClassName("net/minecraft/client/renderer/VertexBuffer"), "littleTilesAdded", "Z"));

				
			}
		});
		
		//Remove packet limits
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
						
						m.instructions.insert(insn, new FieldInsnNode(Opcodes.GETSTATIC, patchDESC("net/minecraft/nbt/NBTSizeTracker"), TransformerNames.patchFieldName("INFINITE", patchClassName("net/minecraft/nbt/NBTSizeTracker")), patchDESC("Lnet/minecraft/nbt/NBTSizeTracker;")));
						m.instructions.remove(insn);
						break;
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.util.math.AxisAlignedBB") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "intersectsWith", "(Lnet/minecraft/util/math/AxisAlignedBB;)Z");
				
				String axisClassName = patchClassName("net/minecraft/util/math/AxisAlignedBB");
				String methodDesc = "(L" +  axisClassName + ";)Z";
				
				LabelNode label = (LabelNode) m.instructions.getFirst();
				m.instructions.insertBefore(label, new LabelNode());
				m.instructions.insertBefore(label, new VarInsnNode(Opcodes.ALOAD, 1));
				m.instructions.insertBefore(label, new TypeInsnNode(Opcodes.INSTANCEOF, "com/creativemd/creativecore/common/collision/CreativeAxisAlignedBB"));
				m.instructions.insertBefore(label, new JumpInsnNode(Opcodes.IFEQ, label));
				m.instructions.insertBefore(label, new LabelNode());
				m.instructions.insertBefore(label, new VarInsnNode(Opcodes.ALOAD, 1));
				m.instructions.insertBefore(label, new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.insertBefore(label, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, axisClassName, patchMethodName("intersectsWith", methodDesc), methodDesc, false));
				m.instructions.insertBefore(label, new InsnNode(Opcodes.IRETURN));
				
				
				String methodDESC2 = patchDESC("(Lnet/minecraft/util/math/AxisAlignedBB;D)D");
				String methodName = patchMethodName("calculateYOffset", methodDESC2);
				
				m = new MethodNode(Opcodes.ACC_PUBLIC, "calculateYOffsetStepUp", patchDESC("(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/util/math/AxisAlignedBB;D)D"), null, null);
				LabelNode l0 = new LabelNode();
				m.instructions.add(l0);
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				m.instructions.add(new VarInsnNode(Opcodes.DLOAD, 3));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, axisClassName, methodName, methodDESC2, false));
				m.instructions.add(new InsnNode(Opcodes.DRETURN));
				LabelNode l1 = new LabelNode();
				m.instructions.add(l1);
				
				m.localVariables.add(new LocalVariableNode("this", "L" + axisClassName + ";", null, l0, l1, 0));
				m.localVariables.add(new LocalVariableNode("other", "L" + axisClassName + ";", null, l0, l1, 1));
				m.localVariables.add(new LocalVariableNode("otherY", "L" + axisClassName + ";", null, l0, l1, 2));
				m.localVariables.add(new LocalVariableNode("offset", "D", null, l0, l1, 3));
				
				m.visitMaxs(4, 5);
				
				node.methods.add(m);
			}
		});
		addTransformer(new Transformer("net.minecraftforge.common.ForgeHooks") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "isLivingOnLadder", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityLivingBase;)Z");
				
				m.instructions.clear();
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/structure/LittleLadder", "isLivingOnLadder", patchDESC("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityLivingBase;)Z"), false));
				m.instructions.add(new InsnNode(Opcodes.IRETURN));
			}
		});
		addTransformer(new Transformer("net.minecraft.entity.Entity") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "move", "(Lnet/minecraft/entity/MoverType;DDD)V");
				
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					if(insn instanceof VarInsnNode && insn.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) insn).var == 32)
					{
						MethodInsnNode methodInsn = (MethodInsnNode) insn.getNext().getNext();
						methodInsn.name = "calculateYOffsetStepUp";
						methodInsn.desc = patchDESC("(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/util/math/AxisAlignedBB;D)D");
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ALOAD, 31));
						
						//((VarInsnNode) insn).var = 31;
						return ;
					}
				}
			}
		});
		addTransformer(new Transformer("DynamicLight") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "updateChunkLight", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;Ljava/util/Set;Ljava/util/Set;)V");
				
				String className = patchClassName("net/minecraft/client/renderer/chunk/RenderChunk");
				String methodName = TransformerNames.patchMethodName("setNeedsUpdate", "(Z)V", className);
				
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					
					if(insn instanceof MethodInsnNode && insn.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode) insn).name.equals(methodName) && ((MethodInsnNode) insn).owner.equals(className))
					{
						AbstractInsnNode before = insn.getPrevious().getPrevious();
						m.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/client/render/LittleChunkDispatcher", "onOptifineMarksChunkRenderUpdateForDynamicLights", "(Lnet/minecraft/client/renderer/chunk/RenderChunk;)V", false));
						
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ALOAD, ((VarInsnNode) before).var));
	
						return ;						
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.client.Minecraft") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = findMethod(node, "middleClickMouse", "()V");
				String className = patchClassName("net/minecraft/client/Minecraft");
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					if(insn instanceof MethodInsnNode && insn.getOpcode() == Opcodes.INVOKESTATIC && ((MethodInsnNode) insn).owner.equals("net/minecraftforge/common/ForgeHooks") && ((MethodInsnNode) insn).name.equals("onPickBlock") && ((MethodInsnNode) insn).desc.equals(patchDESC("(Lnet/minecraft/util/math/RayTraceResult;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;)Z")))
					{
						m.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/events/LittleEvent", "onMouseWheelClick", patchDESC("(Lnet/minecraft/util/math/RayTraceResult;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;)Z"), false));
						m.instructions.insertBefore(insn, new JumpInsnNode(Opcodes.IFNE, findNextLabel(insn)));
						m.instructions.insertBefore(insn, new LabelNode());
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
						m.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETFIELD, className, patchFieldName("objectMouseOver"), patchDESC("Lnet/minecraft/util/math/RayTraceResult;")));
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
						m.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETFIELD, className, patchFieldName("player"), patchDESC("Lnet/minecraft/client/entity/EntityPlayerSP;")));
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
						m.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETFIELD, className, patchFieldName("world"), patchDESC("Lnet/minecraft/client/multiplayer/WorldClient;")));
						break;
					}
				}
			}
		});
		addTransformer(new Transformer("net.minecraft.util.ITickable") {
			
			@Override
			public void transform(ClassNode node) {
				MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "shouldLTUpdate", "()Z", null, null);
				node.version = Opcodes.V1_8;
				LabelNode start = new LabelNode();
				LabelNode end = new LabelNode();
				m.instructions.add(start);
				m.instructions.add(new InsnNode(Opcodes.ICONST_1));
				m.instructions.add(new InsnNode(Opcodes.IRETURN));
				m.instructions.add(end);
				m.localVariables.add(new LocalVariableNode("this", patchDESC("Lnet/minecraft/util/ITickable;"), null, start, end, 0));
				m.maxLocals = 1;
				m.maxStack = 1;
				node.methods.add(m);
			}
		});
		addTransformer(new Transformer("net.minecraft.world.World") {
			
			@Override
			public void transform(ClassNode node) {
				node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "tickstatechanged", "Ljava/util/List;", "Ljava/util/List<Lnet/minecraft/tileentity/TileEntity;>;", null));
				
				String className = patchClassName("net/minecraft/util/ITickable");
				
				MethodNode m = findMethod(node, "addTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z");
				
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					
					if(insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.INSTANCEOF && ((TypeInsnNode) insn).desc.equals(className))
					{
						int varIndex = ((VarInsnNode) insn.getPrevious()).var;
						LabelNode elseNode = ((JumpInsnNode) insn.getNext()).label;
						
						insn = insn.getNext();
						m.instructions.insert(insn, new JumpInsnNode(Opcodes.IFEQ, elseNode));
						m.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKEINTERFACE, className, "shouldLTUpdate", "()Z", true));
						m.instructions.insert(insn, new TypeInsnNode(Opcodes.CHECKCAST, className));
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ALOAD, varIndex));
						break;
					}
				}
				
				m = findMethod(node, "updateEntities", "()V");
				
				String worldClass = patchClassName("net/minecraft/world/World");
				String fieldName = patchFieldName("processingLoadedTiles");
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					
					if(insn instanceof FieldInsnNode && insn.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode) insn).name.equals(fieldName) && ((FieldInsnNode) insn).owner.equals(worldClass))
					{
						insn = insn.getNext();
						
						m.instructions.insertBefore(insn, new LabelNode());
						m.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
						m.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/events/LittleEvent", "processStateUpdates", "(L" + worldClass + ";)V", false));
						break;
					}
				}
			}
		});
		/*addTransformer(new Transformer("net.minecraft.world.World") {
		
			@Override
			public void transform(ClassNode node) {
				String className = patchClassName("net/minecraft/tileentity/TileEntity");
				String methodName = TransformerNames.patchMethodName("onLoad", "()V", className);
				
				MethodNode m = findMethod(node, "addTileEntity", "(Lnet/minecraft/tileentity/TileEntity;)Z");
				
				for (Iterator iterator = m.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = (AbstractInsnNode) iterator.next();
					
					if(insn instanceof MethodInsnNode && insn.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode) insn).name.equals(methodName) && ((MethodInsnNode) insn).owner.equals(className))
					{
						VarInsnNode before = (VarInsnNode) insn.getPrevious();
						m.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/littletiles/common/utils/converting/ChiselAndBitsConveration", "onAddedTileEntity", patchDESC("(Lnet/minecraft/tileentity/TileEntity;)V"), false));
						m.instructions.insert(insn, new VarInsnNode(Opcodes.ALOAD, before.var));
						return ;
					}
				}
			}
		});*/
	}

}
