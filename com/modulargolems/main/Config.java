package com.modulargolems.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	protected static Map<Block, Block> aliasMap;
	protected static List<Block> useDefaultStateList;
	
	public static final double BASE_HEALTH = 100D;
	public static final double BASE_SPEED = 0.22D;
	public static final double BASE_ATTACK = 7D;

	public static void mainRegistry(Configuration config)
	{
		healthMap = new HashMap(40);
		speedMap = new HashMap(40);
		attackMap = new HashMap(40);
		swimMap = new HashMap(40);
		aliasMap = new HashMap<Block, Block>(10);
		useDefaultStateList = new ArrayList<Block>(40);

		addBlockAttributes();
		addAliases();
	}

	protected static void addBlockAttributes()
	{
		addBlock(Blocks.bookshelf, 28.0D, 1.5D, BASE_SPEED, true);
		addBlock(Blocks.clay, 20.0D, 2.0D, 0.2D, false);
		addBlock(Blocks.coal_block, 14.0D, 2.5D, BASE_SPEED, false);
		addBlock(Blocks.crafting_table, 24.0D, 2.0D, 0.26, true);
		addBlock(Blocks.diamond_block, 220.0D, 20.0D, BASE_SPEED, false);
		addBlock(Blocks.emerald_block, 190.0D, 18.0D, BASE_SPEED, false);
		addBlock(Blocks.end_stone, 50.0D, 8.0D, 0.28, false);
		addBlock(Blocks.glass, 8.0D, 13.0D, 0.26D, false);
		addBlock(Blocks.glowstone, 8.0D, 12.0D, 0.26D, false);
		addBlock(Blocks.gold_block, 80.0D, 8.0D, 0.19D, false);
		addBlock(Blocks.hardened_clay, 22.0D, 4.0D, BASE_SPEED, false);
		addBlock(Blocks.packed_ice, 18.0D, 6.0D, BASE_SPEED, true);
		addBlock(Blocks.lapis_block, 45.0D, 2.5D, BASE_SPEED, false);
		addBlock(Blocks.leaves, 6.0D, 0.5D, 0.30D, true);
		addBlock(Blocks.melon_block, 18.0D, 1.5D, 0.25D, true);
		addBlock(Blocks.brown_mushroom_block, 30.0D, 3.0D, 0.26D, true);
		addBlock(Blocks.red_mushroom_block, 30.0D, 3.0D, 0.26D, true);
		addBlock(Blocks.nether_brick, 25.0D, 6.5D, BASE_SPEED, true);
		addBlock(Blocks.obsidian, 120.0D, 18.0D, BASE_SPEED, false);
		addBlock(Blocks.prismarine, 24.0D, 8.0D, BASE_SPEED, false); // TODO should prismarine float?
		addBlock(Blocks.quartz_block, 85.0D, 8.5D, BASE_SPEED, false);
		addBlock(Blocks.red_sandstone, 15.0D, 4.0D, BASE_SPEED, false);
		addBlock(Blocks.redstone_block, 18.0D, 2.0D, BASE_SPEED, false);
		addBlock(Blocks.sandstone, 15.0D, 4.0D, BASE_SPEED, false);
		addBlock(Blocks.sea_lantern, 26.0D, 5.0D, BASE_SPEED, false); // TODO should sea lantern float?
		addBlock(Blocks.slime_block, 75.0D, 3.5D, 0.26D, true);
		addBlock(Blocks.sponge, 20.0D, 1.5D, 0.27D, true);
		addBlock(Blocks.stained_hardened_clay, 26.0D, 3.0D, BASE_SPEED, false);
		addBlock(Blocks.stained_glass, 9.0D, 12.0D, 0.26D, false);
		addBlock(Blocks.hay_block, 10.0D, 1.0D, 0.27D, true);
		addBlock(Blocks.tnt, 14.0D, 2.5D, 0.26D, true);
		// TODO add wool and wood
	}
	
	protected static void addAliases()
	{
		// add block aliases
		addAlias(Blocks.ice, Blocks.packed_ice);
		addAlias(Blocks.leaves2, Blocks.leaves);
		addAlias(Blocks.log2, Blocks.log);
	}
	
	public static void addBlock(Block block, double healthModifier, double attackModifier, double speedModifier, boolean floatsInWater)
	{
		addBlock(block.getDefaultState(), healthModifier, attackModifier, speedModifier, floatsInWater, true);
	}

	public static void addBlock(IBlockState block, double healthModifier, double attackModifier, double speedModifier, boolean floatsInWater, boolean subDefaultState)
	{
		healthMap.put(block, new Double(healthModifier));
		attackMap.put(block, new Double(attackModifier));
		speedMap.put(block, new Double(speedModifier));
		swimMap.put(block, new Boolean(floatsInWater));
		if(subDefaultState)
		{
			useDefaultStateList.add(block.getBlock());
		}
	}

	public static void removeBlock(IBlockState block)
	{
		healthMap.remove(block);
		attackMap.remove(block);
		speedMap.remove(block);
		swimMap.remove(block);
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
	
	public static boolean isBlockMapped(Block toFind)
	{
		return toFind != null && isBlockMapped(toFind.getDefaultState());
	}
	
	/** @return true if the passed IBlockState (or its alias) is mapped **/
	public static boolean isBlockMapped(IBlockState toFind)
	{
		return toFind != null && (attackMap.containsKey(toFind) || attackMap.containsKey(getAlias(toFind)));
	}
	
	/**
	 * Associates a Block as being another for the purpose of
	 * building the golem (and simplifying textures).
	 * Any time {@link #getAlias(Block)} is called on <b>in</b>, 
	 * it will return <b>alias</b> instead
	 */
	public static boolean addAlias(Block in, Block alias)
	{
		return !aliasMap.containsKey(in) && aliasMap.put(in, alias) != null;
	}
	
	/** 
	 * Finds a Block that is equivalent to this one.
	 * For instance, passing ice will return packed ice.
	 * @return an equivalent Block if found, else the passed Block
	 **/
	public static Block getAlias(Block in)
	{
		return aliasMap.containsKey(in) ? aliasMap.get(in) : in;
	}
	
	/** 
	 * Finds a Block that is equivalent to this one.
	 * For instance, passing ice will return packed ice.
	 * @return an equivalent Block's default state if found, else the passed IBlockState
	 **/
	public static IBlockState getAlias(IBlockState in)
	{
		if(useDefaultStateList.contains(in.getBlock()))
		{
			return in.getBlock().getDefaultState();
		}
		
		return aliasMap.containsKey(in.getBlock()) ? aliasMap.get(in.getBlock()).getDefaultState() : in;
	}
}