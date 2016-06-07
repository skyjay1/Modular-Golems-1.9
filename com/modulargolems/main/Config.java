package com.modulargolems.main;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;

/** Registers the config settings to adjust aspects of this mod **/
public class Config 
{
	protected static Map<IBlockState, Double> healthMap;
	protected static Map<IBlockState, Double> speedMap;
	protected static Map<IBlockState, Double> attackMap;
	protected static Map<IBlockState, Boolean> swimMap;

	public static final double BASE_HEALTH = 100D;
	public static final double BASE_SPEED = 0.22D;
	public static final double BASE_ATTACK = 7D;

	public static void mainRegistry(Configuration config)
	{
		healthMap = new HashMap(40);
		speedMap = new HashMap(40);
		attackMap = new HashMap(40);

		addBlockAttributes();
	}

	protected static void addBlockAttributes()
	{
		//addBlock(Blocks.redstone_block, 90D, 4D, 0.30D);
		addBlock(Blocks.bookshelf, 28.0D, 1.5D, BASE_SPEED, true);
		addBlock(Blocks.clay, 12D, 1.5D, 0.2D, false);
		addBlock(Blocks.diamond_block, 220D, 21D, BASE_SPEED, false);
		addBlock(Blocks.emerald_block, 190D, 20D, BASE_SPEED, false);
		addBlock(Blocks.glass, 8D, 12D, 0.26D, false);
		addBlock(Blocks.obsidian, 180D, 10D, BASE_SPEED, false);
	}

	public static void addBlock(Block block, double healthModifier, double attackModifier, double speedModifier, boolean floatsInWater)
	{
		addBlock(block.getDefaultState(), healthModifier, attackModifier, speedModifier, floatsInWater);
	}
	
	public static void addBlock(IBlockState block, double healthModifier, double attackModifier, double speedModifier, boolean floatsInWater)
	{
		healthMap.put(block, new Double(healthModifier));
		attackMap.put(block, new Double(attackModifier));
		speedMap.put(block, new Double(speedModifier));
		swimMap.put(block, new Boolean(floatsInWater));
	}

	public static double getSpeed(IBlockState in)
	{
		if(in != null && in.getBlock() != null)
		{
			if(speedMap.containsKey(in))
			{
				return speedMap.get(in).doubleValue();
			} else System.out.println("Did not find speed modifier for block " + in.getBlock().getRegistryName());
		} else System.out.println("Tried to find speed modifier for null. That's not even a block!");
		return 0;
	}

	public static double getHealth(IBlockState in)
	{
		if(in != null && in.getBlock() != null)
		{
			if(healthMap.containsKey(in))
			{
				return healthMap.get(in).doubleValue();
			} else System.out.println("Did not find health modifier for block " + in.getBlock().getRegistryName());
		} else System.out.println("Tried to find health modifier for null. That's not even a block!");
		return 0;
	}

	public static double getAttack(IBlockState in)
	{
		if(in != null && in.getBlock() != null)
		{
			if(attackMap.containsKey(in))
			{
				return attackMap.get(in).doubleValue();
			} else System.out.println("Did not find attack modifier for block " + in.getBlock().getRegistryName());
		} else System.out.println("Tried to find attack modifier for null. That's not even a block!");
		return 0;
	}
	
	public static boolean getSwim(IBlockState in)
	{
		if(in != null && in.getBlock() != null)
		{
			if(swimMap.containsKey(in))
			{
				return swimMap.get(in).booleanValue();
			} else System.out.println("Did not find swim modifier for block " + in.getBlock().getRegistryName());
		} else System.out.println("Tried to find swim modifier for null. That's not even a block!");
		return false;
	}
	
	public static boolean isBlockMapped(Block toCheck)
	{
		return isBlockMapped(toCheck.getDefaultState());
	}

	public static boolean isBlockMapped(IBlockState toFind)
	{
		return toFind != null && attackMap.containsKey(toFind);
	}
}