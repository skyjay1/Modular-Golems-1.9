package com.modulargolems.entity.ai;

import java.util.ArrayList;
import java.util.List;

import com.modulargolems.entity.GolemBase;
import com.modulargolems.entity.ModularGolem;
import com.modulargolems.main.Config;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBookshelf;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlower.EnumFlowerType;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

public class AIManager 
{	
	protected static Block[] soils = {Blocks.dirt, Blocks.grass, Blocks.mycelium, Blocks.farmland};
	
	/** @return the EntityAIPlaceRandomBlocks that places flowers or bushes **/
	public static EntityAIBase makeFlowerAI(GolemBase golem)
	{
		int plantingFrequency = 240;
		// init list and AI for planting flowers
		List<IBlockState> lFlowers = new ArrayList(EnumFlowerType.values().length);
		for(EnumFlowerType e : BlockFlower.EnumFlowerType.values())
		{
			lFlowers.add(e.getBlockType().getBlock().getStateFromMeta(e.getMeta()));
		}
		for(BlockTallGrass.EnumType e : BlockTallGrass.EnumType.values())
		{
			lFlowers.add(Blocks.tallgrass.getDefaultState().withProperty(BlockTallGrass.TYPE, e));
		}
		IBlockState[] flowers = lFlowers.toArray(new IBlockState[lFlowers.size()]);	
		return new EntityAIPlaceRandomBlocks(golem, plantingFrequency, flowers, soils);
	}
	
	/** @return the EntityAIPlaceRandomBlocks that places red or brown mushrooms **/
	public static EntityAIBase makeShroomAI(GolemBase golem)
	{
		int plantingFrequency = 300;
		IBlockState[] shrooms = new IBlockState[] {Blocks.red_mushroom.getDefaultState(), Blocks.brown_mushroom.getDefaultState()};
		return new EntityAIPlaceRandomBlocks(golem, plantingFrequency, shrooms, soils);
	}
	
	public static EntityAIBase makePotionBuffAI(GolemBase golem, int numBookshelves)
	{
		final int DURATION = 20 * (9 + numBookshelves); // 20 t/sec * x sec
		final int TICKS_BETWEEN_APPLY = 20 * (10 - numBookshelves);
		final Potion[] POTIONS = new Potion[]
		{
			MobEffects.damageBoost, MobEffects.fireResistance, MobEffects.heal,
			MobEffects.invisibility, MobEffects.moveSpeed, MobEffects.regeneration,
			MobEffects.resistance
		};
		return new EntityAIPotionBuffs(golem, DURATION, TICKS_BETWEEN_APPLY, true, POTIONS);
	}
	
	public static EntityAIBase makePotionWeaponAI(GolemBase golem, int numLapis)
	{
		final int DURATION = 20 * (6 + numLapis); // 20 t/sec * x sec
		final int TICKS_BETWEEN_APPLY = 20 * (8 - numLapis);
		final Potion[] POTIONS = new Potion[]
		{
			MobEffects.blindness, MobEffects.harm, MobEffects.poison,
			MobEffects.weakness, MobEffects.wither, MobEffects.glowing,
			MobEffects.moveSlowdown
		};
		return new EntityAIPotionBuffs(golem, DURATION, TICKS_BETWEEN_APPLY, false, POTIONS);
	}
	
	public static EntityAIBase makeStrictLightAI(GolemBase golem)
	{
		return new EntityAIProvideLight(golem, 2, true);
	}
	
	public static EntityAIBase makeLightAI(ModularGolem golem)
	{
		return new EntityAIProvideLight(golem, 2, golem.getLightLevel() > 7);
	}
	
	public static EntityAIBase makePowerAI(GolemBase golem)
	{
		final int POWER = 15;
		return new EntityAIProvidePower(golem, 2, POWER);
	}
	
	public static EntityAIBase makeTeleportAI(GolemBase golem, int numEndstone)
	{
		int teleportDelay = (20 * 10) - (20 * numEndstone);
		double range = 12.0D * (numEndstone + 1);
		return new EntityAITeleportRandomly(golem, range, teleportDelay, true);
	}
	
	public static EntityAIBase makeExplodeAI(GolemBase golem, int numTNT)
	{
		int minRange = 2;
		int maxRange = 1 + numTNT;
		return new EntityAIExplode(golem, minRange, maxRange, 50, 8 + (numTNT * 4));
	}
	
	public static boolean hasAI(EntityLiving golem, EntityAIBase ai)
	{
		for(EntityAITaskEntry e : golem.tasks.taskEntries)
		{
			if(e.action.equals(ai)) return true;
		}
		return false;
	}
	
	public static boolean hasAI(EntityLiving golem, Class<? extends EntityAIBase> clazz)
	{
		for(EntityAITaskEntry e : golem.tasks.taskEntries)
		{
			if(e.action.getClass() == clazz) return true;
		}
		return false;
	}
	
	public static <T extends EntityAIBase> T getAI(EntityLiving golem, Class<T> clazz)
	{
		for(EntityAITaskEntry e : golem.tasks.taskEntries)
		{
			if(e.action.getClass() == clazz) return clazz.cast(e.action);
		}
		return null;
	}
	
	public static void applyAIs(ModularGolem golem)
	{
		EntityAIBase flowerAI = makeFlowerAI(golem);
		EntityAIBase shroomAI = makeShroomAI(golem);
		EntityAIBase lightAI = makeLightAI(golem);
		EntityAIBase powerAI = makePowerAI(golem);
		
		// light AI
		if(golem.getLightLevel() > 0)
		{
			golem.tasks.addTask(0, lightAI);
		}
		// iterate through all blocks, adding AIs when needed
		int numBookshelves = 0;
		int numLapis = 0;
		int numEndstone = 0;
		int numTNT = 0;
		for(IBlockState ibs : golem.getGolemBlocks())
		{
			if(Config.getSwim(ibs))
			{
				golem.setCanSwim(true);
			}	
			if(ibs.getBlock() == Blocks.melon_block && !hasAI(golem, flowerAI))
			{
				golem.tasks.addTask(5, flowerAI);
			}
			if((ibs.getBlock() == Blocks.red_mushroom_block || ibs.getBlock() == Blocks.brown_mushroom_block) && !hasAI(golem, shroomAI))
			{
				golem.tasks.addTask(6, shroomAI);
			}
			if(ibs.getBlock() == Blocks.redstone_block && !hasAI(golem, EntityAIProvidePower.class))
			{
				golem.tasks.addTask(0, powerAI);
			}
			if(ibs.getBlock() == Blocks.nether_brick)
			{
				golem.attackUsesFire = true;
			}
			if(ibs.getBlock()instanceof BlockBookshelf) ++numBookshelves;
			if(ibs.getBlock() == Blocks.lapis_block) ++numLapis;
			if(ibs.getBlock() == Blocks.tnt) ++numTNT;
			if(ibs.getBlock() == Blocks.end_stone || ibs.getBlock() == Blocks.purpur_block)
			{
				++numEndstone;
			}
		}
		// check whether to add PotionBuffs AIs
		if(numBookshelves > 0)
		{
			golem.tasks.addTask(4, makePotionBuffAI(golem, numBookshelves));
		}
		if(numLapis > 0)
		{
			golem.tasks.addTask(3, makePotionWeaponAI(golem, numLapis));
		}
		if(numEndstone > 0)
		{
			golem.tasks.addTask(4, makeTeleportAI(golem, numEndstone));
		}
		if(numTNT > 0)
		{
			golem.tasks.addTask(0, makeExplodeAI(golem, numTNT));
		}
	}
}
