package com.modulargolems.main;

import com.modulargolems.entity.ModularGolem;

import net.minecraftforge.fml.common.registry.EntityRegistry;

public class GolemEntityRegister 
{
	private static int golemEntityCount;

	public static void mainRegistry() 
	{
		golemEntityCount = 0;
		registerEntities();
	}
	
	public static void registerEntities()
	{		
		register(ModularGolem.class, "ModularGolem");
	}
	
	/** registers the entity
	 */
	private static void register(Class entityClass, String name)
	{		
		EntityRegistry.registerModEntity(entityClass, name, ++golemEntityCount, ModularGolems.instance, 64, 3, true);
	}
}
