package com.modulargolems.events;

import java.util.List;

import com.modulargolems.items.ItemGolemPaper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * Fired after {@link ItemGolemPaper} has added information to itself.
 * Only activates when player is holding SHIFT key.
 **/
@SideOnly(Side.CLIENT)
public class GolemPaperAddInfoEvent extends Event
{
	public final ItemStack itemStack;
	public final EntityPlayer player;
	public final List infoList;
	
	public GolemPaperAddInfoEvent(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List)
	{
		this.itemStack = par1ItemStack;
		this.player = par2EntityPlayer;
		this.infoList = par3List;
	}
}
