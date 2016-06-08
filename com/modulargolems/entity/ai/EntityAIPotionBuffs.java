package com.modulargolems.entity.ai;

import com.modulargolems.entity.GolemBase;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class EntityAIPotionBuffs extends EntityAIBase
{
	public final GolemBase golem;
	public final Potion[] potions;
	/** True: potions will be applied to golem. False: potions will be applied to hostiles **/
	public final boolean targetSelf;
	public final int duration;
	public final int restingTicks;
	
	public EntityAIPotionBuffs(GolemBase golemBase, int avgDuration, int ticksBetweenBuffing, boolean applyToSelf, Potion[] aPotions)
	{
		this.setMutexBits(8);
		this.golem = golemBase;
		this.duration = avgDuration;
		this.restingTicks = ticksBetweenBuffing;
		this.targetSelf = applyToSelf;
		this.potions = aPotions;
	}
	
	@Override
	public boolean shouldExecute() 
	{
		// execute if target has no potion effects and/or it has long enough
		int bound = getTarget() != null && getTarget().getActivePotionEffects().isEmpty() ? 10 : restingTicks;
		return golem.getRNG().nextInt(bound) == 0;
	}
	
	@Override
	public void startExecuting()
	{
		updateTask();
	}
	
	@Override
	public boolean continueExecuting()
	{
		return getTarget() != null && getTarget().getActivePotionEffects().isEmpty();
	}
	
	@Override
	public void updateTask()
    {
		// percent chance to have level 2 potion instead of level 1
		final int AMP_CHANCE = 25;
		EntityLivingBase target = this.targetSelf ? golem : golem.getAttackTarget();
		if(target != null && this.potions.length > 0)
		{
			Potion potion = this.potions[golem.getRNG().nextInt(this.potions.length)];
			int variance = golem.getRNG().nextInt(1 + this.duration / 8) - golem.getRNG().nextInt(1 + this.duration / 8);
			int len = potion.isInstant() ? 1 : this.duration + variance;
			int amp = golem.getRNG().nextInt(100) < AMP_CHANCE ? 2 : 1;
			target.addPotionEffect(new PotionEffect(potion, len, amp));
		}
    }
	
	protected EntityLivingBase getTarget()
	{
		return this.targetSelf ? golem : golem.getAttackTarget();
	}
}
