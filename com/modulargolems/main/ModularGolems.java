package com.modulargolems.main;

import com.modulargolems.events.handlers.GolemClientEventHandler;
import com.modulargolems.events.handlers.GolemCommonEventHandler;
import com.modulargolems.proxies.CommonProxy;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod(modid = ModularGolems.MODID, name = ModularGolems.NAME, version = ModularGolems.VERSION, acceptedMinecraftVersions = ModularGolems.MCVERSION)
public class ModularGolems 
{	
	public static final String MODID = "modulargolems";
	public static final String NAME = "Modular Golems";
	public static final String VERSION = "1.01";
	public static final String MCVERSION = "1.9";
	
	@SidedProxy(clientSide = "com." + MODID + ".proxies.ClientProxy", serverSide = "com." + MODID + ".proxies.CommonProxy")
	public static CommonProxy proxy;
	
	@Mod.Instance(ModularGolems.MODID)
	public static ModularGolems instance;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) 
	{	
		Config.mainRegistry(new Configuration(event.getSuggestedConfigurationFile()));
		GolemItems.mainRegistry();
		GolemEntityRegister.mainRegistry();
		proxy.preInitRenders();
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event) 
	{		
		registerCrafting();
		MinecraftForge.EVENT_BUS.register(new GolemCommonEventHandler());
		if(event.getSide() == Side.CLIENT)
		{
			MinecraftForge.EVENT_BUS.register(new GolemClientEventHandler());
		}
	}
	
	public static void registerCrafting()
	{
		GameRegistry.addShapelessRecipe(new ItemStack(GolemItems.golemPaper, 1), Items.writable_book,Items.redstone);
		GameRegistry.addShapelessRecipe(new ItemStack(GolemItems.golemHead, 1), GolemItems.golemPaper,Blocks.pumpkin);
	}
}

