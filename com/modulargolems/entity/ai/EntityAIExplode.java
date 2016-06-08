package com.modulargolems.entity.ai;

import com.modulargolems.blocks.BlockLightProvider;
import com.modulargolems.entity.GolemBase;
import com.modulargolems.main.GolemItems;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIExplode extends EntityAIBase
{
	public final GolemBase golem;
	public final int MIN_RANGE;
	public final int MAX_RANGE;
	public final int FUSE_LEN;
	/** Percent chance to explode while attacking a mob **/
	public final int CHANCE_TO_EXPLODE_WHEN_ATTACKING;

	public boolean isIgnited;
	public int fuseTimer;

	public EntityAIExplode(GolemBase golemBase, int minRange, int maxRange, int fuse, int attackExplodeChance)
	{
		this.setMutexBits(8);
		this.golem = golemBase;
		this.MIN_RANGE = minRange;
		this.MAX_RANGE = maxRange;
		this.FUSE_LEN = fuse;
		this.CHANCE_TO_EXPLODE_WHEN_ATTACKING = attackExplodeChance;
		resetIgnite();
	}

	@Override
	// returning true from here will cause explosion next tick
	public boolean shouldExecute() 
	{
		if(golem.getHealth() <= 1 || golem.deathTime == 1)
		{
			this.ignite();
			this.fuseTimer = 0;
		}

		if(golem.isBurning() || (golem.attackTimer == 10 && golem.getRNG().nextInt(100) < CHANCE_TO_EXPLODE_WHEN_ATTACKING))
		{
			this.ignite();
		}

		if(golem.isWet() || (golem.getAttackTarget() != null && golem.getDistanceSqToEntity(golem.getAttackTarget()) > this.MIN_RANGE * this.MIN_RANGE))
		{
			this.resetIgnite();
		}

		if(this.isIgnited)
		{
			golem.motionX = golem.motionZ = 0;
			this.fuseTimer--;
			// TODO spawn particles?

			if(this.fuseTimer <= 0)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void startExecuting()
	{
		this.explode();
	}

	@Override
	public boolean continueExecuting()
	{
		return false;		
	}

	public boolean ignite()
	{
		if(!this.isIgnited)
		{
			// update info
			this.isIgnited = true;
			this.fuseTimer = this.FUSE_LEN + golem.getRNG().nextInt(Math.floorDiv(FUSE_LEN, 2) + 1);
			// play sounds
			if(!golem.isWet())
			{
				golem.playSound(SoundEvents.entity_creeper_primed, 1.0F, 0.5F);
				return true;
			}
		}
		return false;
	}

	public void resetIgnite()
	{
		this.isIgnited = false;
		this.fuseTimer = this.FUSE_LEN + golem.getRNG().nextInt(Math.floorDiv(FUSE_LEN, 2) + 1);
	}

	public void explode()
	{
		if(!golem.worldObj.isRemote)
		{
			boolean flag = golem.worldObj.getGameRules().getBoolean("mobGriefing");
			float range = this.MAX_RANGE > this.MIN_RANGE ? golem.getRNG().nextInt(this.MAX_RANGE - this.MIN_RANGE) : this.MIN_RANGE;
			golem.worldObj.createExplosion(golem, golem.posX, golem.posY, golem.posZ, range, flag);
			golem.setDead();
		}
	}
}
