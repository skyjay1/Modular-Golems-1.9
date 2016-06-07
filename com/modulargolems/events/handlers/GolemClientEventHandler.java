package com.modulargolems.events.handlers;

import com.modulargolems.events.GolemPaperAddInfoEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GolemClientEventHandler 
{
	@SubscribeEvent
	public void onAddInfo(GolemPaperAddInfoEvent event)
	{
		// debug:
		//event.infoList.add("test");
	}
}
