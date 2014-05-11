package recraft.controlpack.asm;

import static org.objectweb.asm.Opcodes.*;
import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.MalisisClassTransformer;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class CPTransformer extends MalisisClassTransformer
{

	@Override
	public void registerMappings()
	{
	
	}
	
	@Override
	public void registerHooks()
	{
		register(smartLadder());
		register(smartLadder2());
		register(smartSwimHook());
		register(smartSwimHook2());
		register(autorunHook());
	}
	

	/**
	 * Hook to handle smart ladder movement
	 * @return
	 */
	public AsmHook smartLadder()
	{
		AsmHook ah = new AsmHook("net.minecraft.entity.EntityLivingBase", "moveEntityWithHeading", "(FF)V");
		
		//flag = CPControls.getSmartLadder(this);
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 0)); // this
		insert.add(new VarInsnNode(FLOAD, 2)); // par2
		insert.add(new VarInsnNode(ALOAD, 0)); // this
		insert.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/EntityLivingBase", "isJumping", "Z")); //.isJumping
		insert.add(new MethodInsnNode(INVOKESTATIC, "recraft/controlpack/CPControls", "ladderMovement", "(Lnet/minecraft/entity/EntityLivingBase;FZ)Z"));//CPControls.getSmartSwim()
		insert.add(new VarInsnNode(ISTORE, 9)); //flag
		

		//this.isSneaking() && this instanceof EntityPlayer;
		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 0)); // this
		match.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/EntityLivingBase", "isSneaking", "()Z")); //.isSneaking()
		match.add(new JumpInsnNode(IFEQ, null)); //L52
		match.add(new VarInsnNode(ALOAD, 0)); // this
		match.add(new TypeInsnNode(INSTANCEOF, "net/minecraft/entity/player/EntityPlayer")); //instanceof EntityPlayer
		match.add(new JumpInsnNode(IFEQ, null)); //L52

		
		ah.jumpAfter(match).jump(8).insert(insert);
		
		return ah;	
	}
	
	/**
	 * Hook to cancel going down while on ladder
	 * @return
	 */
	public AsmHook smartLadder2()
	{
		AsmHook ah = new AsmHook("net.minecraft.entity.EntityLivingBase", "moveEntityWithHeading", "(FF)V");

		//CPControls.smartLadder(this);
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 0)); // this
		insert.add(new MethodInsnNode(INVOKESTATIC, "recraft/controlpack/CPControls", "cancelLadderFall", "(Lnet/minecraft/entity/EntityLivingBase;)V"));//CPControls.getSmartSwim()

		//this.isSneaking() && this instanceof EntityPlayer;
		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 0)); // this
		match.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/EntityLivingBase", "isCollidedHorizontally", "Z")); //.isCollidedHorizontally
		match.add(new JumpInsnNode(IFEQ, null)); //L52
		match.add(new VarInsnNode(ALOAD, 0)); // this
		match.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/EntityLivingBase", "isOnLadder", "()Z")); //isOnLadder()
		match.add(new JumpInsnNode(IFEQ, null)); //L52

		
		ah.jumpAfter(match).jump(5).insert(insert);
		
		return ah;	
	}
	
	
	/**
	 * Hook to prevent sinking while in water
	 * @return
	 */
	public AsmHook smartSwimHook()
	{
		AsmHook ah = new AsmHook("net.minecraft.entity.EntityLivingBase", "moveEntityWithHeading", "(FF)V");
		
		LabelNode falseLabel = new LabelNode();
		
		InsnList insert = new InsnList();
		insert.add(new MethodInsnNode(INVOKESTATIC, "recraft/controlpack/CPControls", "getSmartSwim", "()Z"));//CPControls.getSmartSwim()
		insert.add(new JumpInsnNode(IFNE, falseLabel));
		
		//this.motionY -= 0.02D;
		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 0)); // this
		match.add(new InsnNode(DUP));
		match.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/EntityLivingBase", "motionY", "D")); //.motionY
		match.add(new LdcInsnNode(0.02));
		match.add(new InsnNode(DSUB));
		
		ah.jumpTo(match).insert(insert).jump(6).insert(falseLabel);		
		
		return ah;		
	}
	
	/**
	 * Hook to handle smart swimming movement
	 * @return
	 */
	public AsmHook smartSwimHook2()
	{
		AsmHook ah = new AsmHook("net.minecraft.entity.EntityLivingBase", "moveEntityWithHeading", "(FF)V");
		
		LabelNode trueLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 0)); // this
		insert.add(new VarInsnNode(FLOAD, 1)); // par1
		insert.add(new VarInsnNode(FLOAD, 2)); // par2
		insert.add(new VarInsnNode(ALOAD, 0)); // this
		insert.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/EntityLivingBase", "isJumping", "Z")); //.isJumping
		insert.add(new MethodInsnNode(INVOKESTATIC, "recraft/controlpack/CPControls", "handleWaterMovement", "(Lnet/minecraft/entity/EntityLivingBase;FFZ)Z"));//CPControls.getSmartSwim()
		insert.add(new JumpInsnNode(IFNE, trueLabel));
		
	
		//this.motionY -= 0.02D;
		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 0)); // this
		match.add(new VarInsnNode(FLOAD, 1)); // par1
		match.add(new VarInsnNode(FLOAD, 2)); // par2
		match.add(new VarInsnNode(ALOAD, 0)); // this
		match.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/EntityLivingBase", "isAIEnabled", "()Z")); //.isAIEnabled()
		
		ah.jumpTo(match).insert(insert).jump(14).insert(trueLabel);		
		
		return ah;		
	}
	
	
	/**
	 * Hook to handle auto run movement
	 * @return
	 */
	public AsmHook autorunHook()
	{
		AsmHook ah = new AsmHook("net.minecraft.util.MovementInputFromOptions", "updatePlayerMoveState", "()V");
//		if (CPHooks.gameSettings.keyBindForward.getIsKeyPressed() || CPControls.getAutorun())

		LabelNode trueLabel = new LabelNode();
		
		//|| CPControls.getAutorun()
		InsnList insert1 = new InsnList();
		insert1.add(new JumpInsnNode(IFNE, trueLabel));
		insert1.add(new MethodInsnNode(INVOKESTATIC, "recraft/controlpack/CPControls", "getAutorun", "()Z"));
		
		//if (this.gameSettings.keyBindForward.getIsKeyPressed()
		InsnList match1 = new InsnList();
		match1.add(new VarInsnNode(ALOAD, 0)); // this
		match1.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/MovementInputFromOptions", "gameSettings",
				"Lnet/minecraft/client/settings/GameSettings;")); //.gameSettings
		match1.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/settings/GameSettings", "keyBindForward",
				"Lnet/minecraft/client/settings/KeyBinding;")); //.keyBindForward
		match1.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/settings/KeyBinding", "getIsKeyPressed", "()Z")); //.getIsKeyPressed()
		
		//if (this.gameSettings.keyBindBack.getIsKeyPressed()
		InsnList match2 = new InsnList();
		match2.add(new VarInsnNode(ALOAD, 0)); // this
		match2.add(new FieldInsnNode(GETFIELD, "net/minecraft/util/MovementInputFromOptions", "gameSettings",
				"Lnet/minecraft/client/settings/GameSettings;")); //.gameSettings
		match2.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/settings/GameSettings", "keyBindBack",
				"Lnet/minecraft/client/settings/KeyBinding;")); //.keyBindBack
		match2.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/settings/KeyBinding", "getIsKeyPressed", "()Z")); //.getIsKeyPressed()
		
	
		return ah.jumpAfter(match1).insert(insert1).next().insert(trueLabel);
	}


	
}
