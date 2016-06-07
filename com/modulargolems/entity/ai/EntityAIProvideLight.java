package com.modulargolems.entity.ai;

import com.modulargolems.blocks.BlockLightProvider;
import com.modulargolems.entity.GolemBase;
import com.modulargolems.main.GolemItems;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIProvideLight extends EntityAIBase
{
	public final GolemBase golem;
	public final int tickDelay;
	
	protected final Block lightBlock;
	
	public EntityAIProvideLight(GolemBase golemBase, int ticksBetweenPlacing, boolean fullLight)
	{
		this.setMutexBits(8);
		this.golem = golemBase;
		this.tickDelay = ticksBetweenPlacing;
		this.lightBlock = fullLight ? GolemItems.blockLightSourceFull : GolemItems.blockLightSourceHalf;
	}
	
	@Override
	public boolean shouldExecute() 
	{
		return tickDelay <= 1 || golem.ticksExisted % tickDelay == 0;
	}
	
	@Override
	public void startExecuting()
	{
		placeLightBlock();
	}
	
	@Override
	public boolean continueExecuting()
	{
		return false;
	}
	
	private boolean placeLightBlock() 
	{
		int x = MathHelper.floor_double(golem.posX);
		int y = MathHelper.floor_double(golem.posY - 0.20000000298023224D);
		int z = MathHelper.floor_double(golem.posZ);
		int[][] validPos = {{x,z},{x+1,z},{x-1,z},{x,z+1},{x,z-1},{x+1,z+1},{x-1,z+1},{x+1,z-1},{x-1,z-1}};
		for(int[] coord : validPos)
		{
			int xPos = coord[0];
			int zPos = coord[1];
			for(int k = 0; k < 3; ++k)
			{	
				int yPos = y + k + 1;
				BlockPos pos = new BlockPos(xPos, yPos, zPos);
				IBlockState state = golem.worldObj.getBlockState(pos);
				Block at = state.getBlock();
				if(golem.worldObj.isAirBlock(pos))
				{
					return golem.worldObj.setBlockState(pos, this.lightBlock.getDefaultState(), 2);
				}
				else if(at instanceof BlockLightProvider)
				{
					return false;
				}
			}
		}
		return false;
	}
}
