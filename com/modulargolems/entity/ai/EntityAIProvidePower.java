package com.modulargolems.entity.ai;

import com.modulargolems.blocks.BlockLightProvider;
import com.modulargolems.blocks.BlockPowerProvider;
import com.modulargolems.entity.GolemBase;
import com.modulargolems.main.GolemItems;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIProvidePower extends EntityAIBase
{
	public final GolemBase golem;
	public final int tickDelay;
	
	protected final int power;
	
	public EntityAIProvidePower(GolemBase golemBase, int ticksBetweenPlacing, int powerLevel)
	{
		this.setMutexBits(8);
		this.golem = golemBase;
		this.tickDelay = ticksBetweenPlacing;
		this.power = powerLevel > 15 || powerLevel < 0 ? 15 : powerLevel;
	}
	
	@Override
	public boolean shouldExecute() 
	{
		return tickDelay <= 1 || golem.ticksExisted % tickDelay == 0;
	}
	
	@Override
	public void startExecuting()
	{
		placePowerNearby();
	}
	
	@Override
	public boolean continueExecuting()
	{
		return false;
	}
	
	/** Finds air blocks nearby and replaces them with BlockMovingPowerSource **/
	protected boolean placePowerNearby() 
	{
		int x = MathHelper.floor_double(golem.posX);
		int y = MathHelper.floor_double(golem.posY - 0.20000000298023224D); // y-pos of block below golem
		int z = MathHelper.floor_double(golem.posZ);
		
		// power 3 layers at golem location
		for(int k = -1; k < 3; ++k)
		{	
			BlockPos at = new BlockPos(x, y + k, z);
			// if the block is air, make it a power block
			if(golem.worldObj.isAirBlock(at))
			{
				IBlockState powerBlock = GolemItems.blockPowerSource.getDefaultState().withProperty(BlockPowerProvider.POWER, this.power);
				golem.worldObj.setBlockState(at, powerBlock);
			}
		}
		return true;
	}
}
