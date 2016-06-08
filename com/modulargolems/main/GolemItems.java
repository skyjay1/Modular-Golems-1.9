package com.modulargolems.main;

import com.modulargolems.blocks.BlockGolemHead;
import com.modulargolems.blocks.BlockLightProvider;
import com.modulargolems.blocks.BlockPowerProvider;
import com.modulargolems.blocks.TileEntityMovingLightSource;
import com.modulargolems.blocks.TileEntityMovingPowerSource;
import com.modulargolems.items.ItemGolemPaper;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class GolemItems 
{
	public static Item golemPaper;				
	
	public static Block golemHead;				
	public static Block blockLightSourceFull;	
	public static Block blockLightSourceHalf;
	public static Block blockPowerSource;
	
	public static ItemBlock itemGolemHead;
	public static ItemBlock itemLightSourceFull;
	public static ItemBlock itemLightSourceHalf;
	public static ItemBlock itemPowerSource;

	public static void mainRegistry()
	{
		initBlocks();
		initItemBlocks();
		initItems();

		register(TileEntityMovingLightSource.class, "TileEntityMovingLightSource");
		register(TileEntityMovingPowerSource.class, "TileEntityMovingPowerSource");

		register(golemPaper, "golem_paper");
		register(golemHead, itemGolemHead, "golem_head");
		register(blockLightSourceFull, itemLightSourceFull, "light_provider_full");
		register(blockLightSourceHalf, itemLightSourceHalf, "light_provider_half");
		register(blockPowerSource, itemPowerSource, "power_provider_all");
	}

	private static void initBlocks()
	{
		golemHead = new BlockGolemHead();
		blockLightSourceFull = new BlockLightProvider(1.0F);
		blockLightSourceHalf = new BlockLightProvider(0.5F);
		blockPowerSource = new BlockPowerProvider();
	}
	
	private static void initItemBlocks()
	{
		itemGolemHead = new ItemBlock(golemHead);
		itemLightSourceFull = new ItemBlock(blockLightSourceFull);
		itemLightSourceHalf = new ItemBlock(blockLightSourceHalf);
		itemPowerSource = new ItemBlock(blockPowerSource);
	}

	private static void initItems()
	{
		golemPaper = new ItemGolemPaper();
	}

	private static void register(Item item, String name)
	{
		item.setUnlocalizedName(name).setRegistryName(ModularGolems.MODID, name);
		GameRegistry.register(item);
	}

	private static void register(Block block, ItemBlock itemBlock, String name)
	{
		block.setUnlocalizedName(name).setRegistryName(ModularGolems.MODID, name);
		itemBlock.setUnlocalizedName(name).setRegistryName(ModularGolems.MODID, name);
		GameRegistry.register(block);
		GameRegistry.register(itemBlock);
	}

	private static void register(Class <? extends TileEntity> teClass, String name)
	{
		GameRegistry.registerTileEntity(teClass, ModularGolems.MODID + "." + name);
	}
}
