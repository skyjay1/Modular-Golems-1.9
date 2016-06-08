package com.modulargolems.events.handlers;

import com.modulargolems.entity.GolemBase;
import com.modulargolems.events.GolemBuildEvent;

import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Handles events added specifically from this mod **/
public class GolemCommonEventHandler 
{	
	@SubscribeEvent
	public void onBuildGolem(GolemBuildEvent event)
	{
		
	}
	
	@SubscribeEvent
	public void onLivingSpawned(EntityJoinWorldEvent event)
	{
		// add custom 'attack golem' AI to zombies. They already have this for regular iron golems
		if(event.getEntity() instanceof EntityZombie && !(event.getEntity() instanceof EntityPigZombie))
		{
			EntityZombie zombie = (EntityZombie)event.getEntity();
			zombie.targetTasks.addTask(3, new EntityAINearestAttackableTarget(zombie, GolemBase.class, true));
		}
	}
	
	
}
