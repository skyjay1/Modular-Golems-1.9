package com.modulargolems.blocks;

import java.util.List;

import com.modulargolems.entity.GolemBase;
import com.modulargolems.entity.ai.AIManager;
import com.modulargolems.entity.ai.EntityAIProvidePower;

public class TileEntityMovingPowerSource extends TileEntityMovingLightSource
{    	
	public TileEntityMovingPowerSource() {}

	@Override
	public void update()
	{
		List<GolemBase> entityList = worldObj.getEntitiesWithinAABB(GolemBase.class, this.getAABBToCheck(this.worldObj, this.getPos()));

		// if no golem was found, delete this tile entity and block
		if(entityList.isEmpty())
		{
			selfDestruct();
		}
		else
		{
			for(GolemBase g : entityList)
			{
				if(AIManager.hasAI(g, EntityAIProvidePower.class))
				{
					return;
				}
			}
			// if it made it to here, no golems had that ai
			selfDestruct();
		}
	}
}
