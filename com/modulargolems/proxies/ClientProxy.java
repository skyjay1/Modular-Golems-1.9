package com.modulargolems.proxies;

import com.modulargolems.entity.ModularGolem;
import com.modulargolems.main.GolemItems;
import com.modulargolems.renders.RenderGolem;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy 
{	
	@Override
	public void preInitRenders()
	{
		registerBlockRenders();
		registerItemRenders();
		registerEntityRenders();
	}
	
	public void registerBlockRenders()
	{
		// manually add golem head using pumpkin models
		register(GolemItems.itemGolemHead, "minecraft:pumpkin");
		register(GolemItems.itemLightSourceFull);
		register(GolemItems.itemLightSourceHalf);
	}

	public void registerItemRenders()
	{		
		register(GolemItems.golemPaper);	
	}

	public void registerEntityRenders()
	{
		register(ModularGolem.class);
	}

	/**	Registers an entity with the RenderGolem rendering class */
	public static void register(Class<? extends ModularGolem> golem)
	{
		RenderingRegistry.registerEntityRenderingHandler(golem, new IRenderFactory<ModularGolem>() 
		{
			@Override
			public Render<? super ModularGolem> createRenderFor(RenderManager manager) 
			{
				return new RenderGolem(manager);
			}
		});
	}
	
	private void register(Item i, String name, int... meta)
	{
		if(meta.length < 1) meta = new int[] {0};
		for(int m : meta)
		{
			ModelLoader.setCustomModelResourceLocation(i, m, new ModelResourceLocation(name, "inventory"));
		}
	}

	private void register(Item i, int... meta)
	{
		register(i, i.getRegistryName().toString(), meta);
	}

	private void register(Block b, int... meta)
	{
		Item i = Item.getItemFromBlock(b);
		if(i != null)
		{
			register(i, meta);
		} else System.out.println("Tried to register render for a null ItemBlock. Skipping.");
	}
}
