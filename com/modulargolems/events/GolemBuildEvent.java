package com.modulargolems.events;

import com.modulargolems.blocks.BlockGolemHead;
import com.modulargolems.entity.ModularGolem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

/**
 * Fired after {@link BlockGolemHead} has been placed but before
 * the golem is spawned or blocks are removed. Use this
 * to customize the golem based on its blocks, eg, adding
 * custom EntityAIs
 **/
@HasResult
public class GolemBuildEvent extends Event 
{
	/** The world in which theGolem was built **/
	public final World worldObj;
	/** The X,Y,Z coordinates of the Golem Head block **/
	public final BlockPos headPos;
	/** The blocks that built this golem **/
	public final IBlockState body;
	public final IBlockState legs;
	public final IBlockState arm1;
	public final IBlockState arm2;
	/** Whether the golem's arms are aligned on North-South **/
	public final boolean isGolemAlignedNS;

	/** The Modular Golem built with these blocks **/
	private ModularGolem theGolem;

	public GolemBuildEvent(final ModularGolem golem, final BlockPos pos, final boolean isNSAligned)
	{
		this.setResult(Result.ALLOW);
		this.theGolem = golem;
		this.worldObj = golem.worldObj;
		this.headPos = pos;
		this.isGolemAlignedNS = isNSAligned;
		this.body = golem.getBodyBlock();
		this.legs = golem.getLegsBlock();
		this.arm1 = golem.getArm1Block();
		this.arm2 = golem.getArm2Block();
	}

	/** Assign this event a new ModularGolem to spawn **/
	public void setGolem(ModularGolem golem)
	{
		this.theGolem = golem;
	}

	/** @return the ModularGolem of this event **/
	public ModularGolem getGolem()
	{
		return this.theGolem;
	}
	
	/** @return new IBlockState[] { body, legs, arm1, arm2 } **/
	public IBlockState[] getBlocks()
	{
		return new IBlockState[] {body, legs, arm1, arm2};
	}
}
