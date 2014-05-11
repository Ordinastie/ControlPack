package recraft.controlpack;

import static org.lwjgl.input.Keyboard.*;
import net.malisis.core.MalisisCore;
import net.malisis.core.event.user.KeyboardEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;

public class CPControls
{
	private boolean autorun = false;
	private boolean autoSwitchTool = true;
	private boolean autoSwitchTorch = true;
	private boolean smartSwim = true;
	private boolean smartLadder = true;

	public static KeyBinding kbAutorun;
	public static KeyBinding kbToggleSmartLadder;
	public static KeyBinding kbToggleSmartSwim;

	public CPControls()
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			kbAutorun = new KeyBinding("key.autorun", KEY_A, "key.categories." + ControlPack.modid);
			kbToggleSmartLadder = new KeyBinding("key.toggleSmartLadder", KEY_L, "key.categories." + ControlPack.modid);
			kbToggleSmartSwim = new KeyBinding("key.toggleSmartSwim", KEY_M, "key.categories." + ControlPack.modid);
	
			ClientRegistry.registerKeyBinding(kbAutorun);
			ClientRegistry.registerKeyBinding(kbToggleSmartLadder);
			ClientRegistry.registerKeyBinding(kbToggleSmartSwim);
		}
	}
	
	public void loadConfiguration(Configuration config)
	{
		this.autoSwitchTool = config.get(Configuration.CATEGORY_GENERAL, "autoSwitchTool", true).getBoolean(true);
		this.autoSwitchTorch = config.get(Configuration.CATEGORY_GENERAL, "autoSwitchTorch", true).getBoolean(true);
		this.smartSwim = config.get(Configuration.CATEGORY_GENERAL, "smartSwim", true).getBoolean(true);
		this.smartLadder = config.get(Configuration.CATEGORY_GENERAL, "smartLadder", true).getBoolean(true);
	}

	private static boolean keyPressed(KeyBinding key)
	{
		return keyPressed(key.getKeyCode());
	}

	private static boolean keyPressed(int key)
	{
		return Keyboard.getEventKey() == key && Keyboard.getEventKeyState();
	}

	@SubscribeEvent
	public void onKeyPressed(KeyboardEvent event)
	{
		if (keyPressed(Minecraft.getMinecraft().gameSettings.keyBindTogglePerspective) && toggleThirdPersonView())
		{
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event)
	{
		GameSettings gs = Minecraft.getMinecraft().gameSettings;
		//Auto run
		if (kbAutorun.isPressed())
		{
			autorun = !autorun;
			return;
		}
		if(gs.keyBindForward.isPressed() || gs.keyBindBack.isPressed())
		{
			autorun = false;
			return;
		}
		//Smart ladder toggle
		if (kbToggleSmartLadder.isPressed())
		{
			smartLadder = !smartLadder;
			MalisisCore.Message(String.format("Smart ladder %s", smartLadder ? "activated" : "deactivated"));
			return;
		}
		//Smart swim toggle
		if (kbToggleSmartSwim.isPressed())
		{
			smartSwim = !smartSwim;
			MalisisCore.Message(String.format("Smart swimming %s", smartSwim ? "activated" : "deactivated"));
			return;
		}
	}

	public static boolean ladderMovement(EntityLivingBase player, float moveForward, boolean jumping)
	{
		if (!(player instanceof EntityPlayer) || !ControlPack.controls.smartLadder)
			return false;

		player.motionY = 0;
		if (moveForward != 0 || player.isSneaking() || jumping)
		{
			int dir = (int) (player.rotationPitch * moveForward);
			if (dir > 0 || player.isSneaking())
				player.motionY = -0.2D;
			if (dir < 0 || jumping)
				player.motionY = 0.2D;
		}

		return false;
	}

	public static void cancelLadderFall(EntityLivingBase player)
	{
		if (!(player instanceof EntityPlayer) || !ControlPack.controls.smartLadder)
			return;

		player.motionY = 0;
	}
	
	public static boolean handleWaterMovement(EntityLivingBase player, float moveStrafing, float moveForward, boolean jumping)
	{
		if (!(player instanceof EntityPlayer) || !getSmartSwim() || player.onGround)
			return false;
		
		if(jumping)
		{
			player.motionY -= 0.0098D;
			return false;
		}
		if(player.isSneaking())
		{
			player.motionY -= 0.04D;
			player.motionX *= 1.2D;
			player.motionZ *= 1.2D;
			return false;
		}
		
		Vec3 look = player.getLookVec();
		player.motionX += moveForward * look.xCoord * 0.04F;
		player.motionY += moveForward * look.yCoord * 0.04F;
		player.motionZ += moveForward * look.zCoord * 0.04F;
		
//		look.rotateAroundY(player.rotationYaw / 90);
//		player.motionX += moveStrafing * look.xCoord * 0.04F;
//		player.motionZ += moveStrafing * look.zCoord * 0.04F;
		
		

	/*	float movement = moveStrafing * moveStrafing + moveForward * moveForward;
		if (movement >= 0.0001F)
		{
			movement = MathHelper.sqrt_float(movement);
			if (movement < 1.0F)
				movement = 1.0F;
			movement = 0.02F / movement;			
		
			float factor = player.rotationPitch / 90F;
			float moveUp = moveForward * movement;
			//MalisisCore.Message(player.rotationPitch);
			factor = 0;
			
			moveStrafing *= movement;
			moveForward *= movement  * (1 - Math.abs(factor));

			float f4 = MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F);
			float f5 = MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F);

			player.motionX += (double) (moveStrafing * f5 - moveForward * f4);
			player.motionZ += (double) (moveForward * f5 + moveStrafing * f4);
			player.motionY += (double) (moveUp * -factor);

		}
		
		if (player.isSneaking())
		{
			player.motionY -= 0.04D;
			player.motionX *= 1.2D;
			player.motionZ *= 1.2D;

		}
*/
		return true;

	}

	public static boolean getAutorun()
	{
		return ControlPack.controls.autorun;
	}

	public static boolean getAutoSwitchTool()
	{
		return ControlPack.controls.autoSwitchTool;
	}

	public static boolean getAutoSwitchTorch()
	{
		return ControlPack.controls.autoSwitchTorch;
	}

	public static boolean getSmartSwim()
	{
		return ControlPack.controls.smartSwim;
	}

	

	public boolean toggleThirdPersonView()
	{
		GameSettings gs = Minecraft.getMinecraft().gameSettings;
		if (gs.thirdPersonView >= 2)
		{
			gs.debugCamEnable = !gs.debugCamEnable;
			return gs.debugCamEnable;
		}
		return false;
	}
}
