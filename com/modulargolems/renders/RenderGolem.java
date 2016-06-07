package com.modulargolems.renders;

import com.modulargolems.entity.ModularGolem;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGolem extends RenderLiving<ModularGolem> 
{
	private ResourceLocation texture;

	public RenderGolem(RenderManager renderManagerIn)
	{
		super(renderManagerIn, new ModelGolem(), 0.5F);
	}
	
	@Override
	public void doRender(ModularGolem golem, double x, double y, double z, float f0, float f1)
	{
		// GL11 settings
		GlStateManager.pushMatrix();
		if(golem.hasTransparency())
		{
			GlStateManager.enableNormalize();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);       
		}
		
		// render body first
		this.texture = golem.getBodyResource();
		if(this.texture != null)
		{
			super.doRender(golem, x, y, z, f0, f1);
		}
		// render legs
		this.texture = golem.getLegsResource();
		if(this.texture != null)
		{
			super.doRender(golem, x, y, z, f0, f1);
		}
		// render arm1
		this.texture = golem.getArm1Resource();
		if(this.texture != null)
		{
			super.doRender(golem, x, y, z, f0, f1);
		}
		// render arm2
		this.texture = golem.getArm2Resource();
		if(this.texture != null)
		{
			super.doRender(golem, x, y, z, f0, f1);
		}

		// return GL11 settings to normal
		if(golem.hasTransparency())
		{
			GlStateManager.disableBlend();
			GlStateManager.disableNormalize();
		}
		GlStateManager.popMatrix();   
	}

	@Override
	protected void rotateCorpse(ModularGolem golem, float p_77043_2_, float p_77043_3_, float partialTicks)
	{
		super.rotateCorpse(golem, p_77043_2_, p_77043_3_, partialTicks);

		if ((double)golem.limbSwingAmount >= 0.01D)
		{
			float f = 13.0F;
			float f1 = golem.limbSwing - golem.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
			float f2 = (Math.abs(f1 % f - f * 0.5F) - f * 0.25F) / (f * 0.25F);
			GlStateManager.rotate(6.5F * f2, 0.0F, 0.0F, 1.0F);
		}
	}
	
	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(ModularGolem entity) 
	{
		return this.texture != null ? this.texture : ModularGolem.TEST_TEXTURE;
	}
}
