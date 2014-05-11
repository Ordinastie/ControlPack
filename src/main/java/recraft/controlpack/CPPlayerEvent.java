package recraft.controlpack;

import java.util.Collection;

import net.malisis.core.MalisisCore;
import net.malisis.core.event.user.UserAttackEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CPPlayerEvent
{
	World world;
	EntityClientPlayerMP player;
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		world = Minecraft.getMinecraft().theWorld;
		if(!world.isRemote)
			return;
		player = Minecraft.getMinecraft().thePlayer;
		
		if(event.action == Action.LEFT_CLICK_BLOCK)
		{
			if(CPControls.getAutoSwitchTool())
			{
				autoSwitchTool(event);
			}
		}
		
		if(event.action == Action.RIGHT_CLICK_BLOCK)
		{
			if(CPControls.getAutoSwitchTorch())
			{
				autoSwitchTorch(event);
			}
		}
	}
	

	@SubscribeEvent
	public void onUserAttack(UserAttackEvent event)
	{
		int index = -1;
		player = event.player;
		
		ItemStack stack;
		Item item;
		double damage = 0F;
		for(int i = 0; i < 9; i++)
		{
			if((stack = player.inventory.mainInventory[i]) != null && (item = stack.getItem()) != null)
			{
				if(item instanceof ItemSword)
				{
					Collection<AttributeModifier> m = stack.getAttributeModifiers().get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
					double itemDamage = m.iterator().next().getAmount();
					if(itemDamage > damage)
					{
						index = i;
						damage = itemDamage;
					}
				}
			}
		}
		
		if(index != -1)
		{
			MalisisCore.Message("Attack : " + index);
			player.inventory.currentItem = index;
		}
	}
	
	private void autoSwitchTorch(PlayerInteractEvent event)
	{
		if(!isTool(getPlayerItem(player.inventory.currentItem)))
			return;
	
		ItemStack stack;
		Item item;
		int current = player.inventory.currentItem;
		
		for(int i = 0; i < 9; i++)
		{
			if((stack = player.inventory.mainInventory[i]) != null && (item = stack.getItem()) != null)
			{
				if(item == Item.getItemFromBlock(Blocks.torch))
				{
					player.inventory.currentItem = i;					
					Minecraft mc = Minecraft.getMinecraft();
					mc.playerController.onPlayerRightClick(player, world, stack, event.x, event.y, event.z, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
//					item.onItemUse(stack, player, world, event.x, event.y, event.z, event.face, (float) v.xCoord, (float) v.yCoord, (float) v.zCoord);
					player.inventory.currentItem = current;
					return;
				}
			}
		}
	}
	
	
	private void autoSwitchTool(PlayerInteractEvent event)
	{
		Block block = world.getBlock(event.x, event.y, event.z);
		int index = -1;
		int noToolIndex = -1;
		
		ItemStack stack;
		Item item;
		float speed = 1F;
		for(int i = 0; i < 9; i++)
		{
			if((stack = player.inventory.mainInventory[i]) != null && (item = stack.getItem()) != null)
			{
				if(isTool(item))
				{
					float itemSpeed = item.getDigSpeed(stack, block, world.getBlockMetadata(event.x, event.y, event.z));
					if(itemSpeed > speed)
					{
						index = i;
						speed = itemSpeed;
					}
				}
				else if(noToolIndex == -1)
					noToolIndex = i;
			}
		}
		
		if(index != -1)
		{
			MalisisCore.Message("Chose : " + index);
			player.inventory.currentItem = index;
		}
	}
	
	private boolean isTool(Item item)
	{
		return item != null && (item instanceof ItemTool || item instanceof ItemShears);
	}
	
	private Item getPlayerItem(int index)
	{
		ItemStack stack = player.inventory.mainInventory[index];
		if(stack != null)
			return stack.getItem();
		return null;	
	}
	
}
