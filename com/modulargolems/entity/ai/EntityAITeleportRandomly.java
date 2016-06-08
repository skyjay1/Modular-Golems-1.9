package com.modulargolems.entity.ai;

import com.modulargolems.entity.GolemBase;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;

public class EntityAITeleportRandomly extends EntityAIBase
{
	public final GolemBase golem;
	public final double RANGE;
	public final int restingTicks;
	public final boolean isHurtByWater;
	
	public EntityAITeleportRandomly(GolemBase golemBase, double maxDistance, int teleportDelay, boolean waterHurts)
	{
		this.setMutexBits(8);
		this.golem = golemBase;
		this.RANGE = maxDistance;
		this.restingTicks = teleportDelay;
		this.isHurtByWater = waterHurts;
	}
	
	@Override
	public boolean shouldExecute() 
	{
		boolean hurtByWaterFlag = this.isHurtByWater ? golem.isWet() && golem.attackEntityFrom(DamageSource.generic, 0.5F) : false;
		return hurtByWaterFlag || (golem.getAttackTarget() == null && golem.getRNG().nextInt(this.restingTicks) == 0);
	}
	
	@Override
	public void startExecuting()
	{
		for(int i = 0; i < 64; i++)
		{
			if(teleportRandomly()) return;
		}
	}
	
	@Override
	public boolean continueExecuting()
	{
		return false;
	}

    /**
     * Teleport the golem
     **/
    private boolean teleportTo(double x, double y, double z)
    {
        net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(golem, x, y, z, 0);
        if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) 
        {
        	return false;
        }
        boolean flag = golem.teleportTo_(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        if (flag)
        {
            golem.worldObj.playSound((EntityPlayer)null, golem.prevPosX, golem.prevPosY, golem.prevPosZ, SoundEvents.entity_endermen_teleport, golem.getSoundCategory(), 1.0F, 1.0F);
            golem.playSound(SoundEvents.entity_endermen_teleport, 1.0F, 1.0F);
        }

        return flag;
    }
    
    protected boolean teleportRandomly()
    {
    	// debug:
    	System.out.println("random teleport time");
        double d0 = golem.posX + (golem.getRNG().nextDouble() - 0.5D) * RANGE;
        double d1 = golem.posY + (golem.getRNG().nextDouble() - 0.5D) * RANGE * 0.5D;
        double d2 = golem.posZ + (golem.getRNG().nextDouble() - 0.5D) * RANGE;
        return this.teleportTo(d0, d1, d2);
    }
}
