package com.modulargolems.entity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.modulargolems.entity.ai.EntityAIDefendAgainstMonsters;
import com.modulargolems.main.ModularGolems;
import com.modulargolems.main.GolemItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
/**
 * Base class for all golems in this mod.
 **/
public abstract class GolemBase extends EntityCreature implements IAnimals 
{
	protected int attackTimer;
	protected boolean isPlayerCreated;
	protected ResourceLocation textureLoc;
	protected boolean hasHome = false;
	protected ItemStack creativeReturn;
	Village villageObj;
	/** deincrements, and a distance-to-home check is done at 0 */
	private int homeCheckTimer = 70;

	// customizable variables with default values //
	protected double knockbackY = 0.4000000059604645D;
	protected boolean takesFallDamage = false;
	protected boolean isLeashable = true;
	
	public static final ResourceLocation TEST_TEXTURE = makeGolemTexture("test");

	/////////////// CONSTRUCTORS /////////////////

	public GolemBase(World world) 
	{
		super(world);
		this.setSize(1.4F, 2.9F);
		this.setCanTakeFallDamage(false);
		this.setCanSwim(false);
	}

	////////////// BEHAVIOR OVERRIDES //////////////////

	@Override
	protected void initEntityAI()
	{
		this.tasks.addTask(1, new EntityAIAttackMelee(this, this.getBaseMoveSpeed() * 4.0D, true));
		this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, this.getBaseMoveSpeed() * 3.75D, 32.0F));
		this.tasks.addTask(3, new EntityAIMoveThroughVillage(this, this.getBaseMoveSpeed() * 2.25D, true));
		this.tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, this.getBaseMoveSpeed() * 4.0D));
		this.tasks.addTask(5, new EntityAIWander(this, this.getBaseMoveSpeed() * 2.25D));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIDefendAgainstMonsters(this));
		this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, false, true, new Predicate<EntityLiving>()
		{
			public boolean apply(EntityLiving e)
			{
				return e != null && IMob.VISIBLE_MOB_SELECTOR.apply(e) && !(e instanceof EntityCreeper);
			}
		}));
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.setTextureType(this.makeGolemTexture("test"));
	}

	@Override
	protected void applyEntityAttributes() 
	{
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22D);
		this.applyAttributes();
	}

	/**
	 * main AI tick function, replaces updateEntityActionState
	 */
	@Override
	protected void updateAITasks()
	{
		if (--this.homeCheckTimer <= 0)
		{
			this.homeCheckTimer = 70 + this.rand.nextInt(50);
			this.villageObj = this.worldObj.getVillageCollection().getNearestVillage(new BlockPos(this), 32);

			if (this.villageObj == null)
			{
				this.detachHome();
			}
			else
			{
				BlockPos blockpos = this.villageObj.getCenter();
				this.setHomePosAndDistance(blockpos, (int)((float)this.villageObj.getVillageRadius() * 0.6F));
			}
		}

		super.updateAITasks();
	}	

	/**
	 * Decrements the entity's air supply when underwater
	 */
	@Override
	protected int decreaseAirSupply(int i)
	{
		return i;
	}

	@Override
	public boolean canBeLeashedTo(EntityPlayer player)
	{
		return this.isLeashable && super.canBeLeashedTo(player);
	}

	@Override
	protected void collideWithEntity(Entity entityIn)
	{
		if (entityIn instanceof IMob && !(entityIn instanceof EntityCreeper) && this.getRNG().nextInt(20) == 0)
		{
			this.setAttackTarget((EntityLivingBase)entityIn);
		}

		super.collideWithEntity(entityIn);
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		if (this.attackTimer > 0)
		{
			--this.attackTimer;
		}

		if (this.motionX * this.motionX + this.motionZ * this.motionZ > 2.500000277905201E-7D && this.rand.nextInt(5) == 0)
		{
			int i = MathHelper.floor_double(this.posX);
			int j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
			int k = MathHelper.floor_double(this.posZ);
			IBlockState iblockstate = this.worldObj.getBlockState(new BlockPos(i, j, k));

			if (iblockstate.getMaterial() != Material.air)
			{
				this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, 4.0D * ((double)this.rand.nextFloat() - 0.5D), 0.5D, ((double)this.rand.nextFloat() - 0.5D) * 4.0D, new int[] {Block.getStateId(iblockstate)});
			}
		}	
	}

	/**
	 * Returns true if this entity can attack entities of the specified class.
	 */
	@Override
	public boolean canAttackClass(Class <? extends EntityLivingBase > cls)
	{
		return this.isPlayerCreated() && EntityPlayer.class.isAssignableFrom(cls) ? false : (cls == EntityCreeper.class ? false : super.canAttackClass(cls));
	}

	@Override
	public boolean attackEntityAsMob(Entity entity)
	{
		if(!(entity instanceof EntityLivingBase)) return super.attackEntityAsMob(entity);

		// (0.0 ~ 1.0] lower number results in less variance
		final float VARIANCE = 0.8F; 
		// calculate damage based on base attack damage and variance
		float baseAttack = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		float damage = baseAttack + (float)(rand.nextDouble() - 0.5D) * VARIANCE * baseAttack;

		// calculate luck and unluck to possibly increase or decrease damage
		int myLuck = this.getActivePotionEffect(MobEffects.luck) != null ? 10 : 0;
		int myUnluck = this.getActivePotionEffect(MobEffects.unluck) != null ? 10 : 0;
		int unluck = ((EntityLivingBase)entity).getActivePotionEffect(MobEffects.unluck) != null ? 10 : 0;

		// percent chance of multiplying damage
		final float CRITICAL_CHANCE = 5 + myLuck + unluck - myUnluck; 
		// try to increase damage if random critical chance succeeds
		if(rand.nextInt(100) < CRITICAL_CHANCE)
		{
			damage *= 2.5F;
		}

		this.attackTimer = 10;
		this.worldObj.setEntityState(this, (byte)4);
		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);

		if (flag)
		{
			entity.motionY += knockbackY;
			this.applyEnchantments(this, entity);
			// debug:
			//System.out.print("[Extra Golems] Base damage = " + this.getAttackDamage() + "; damage = " + damage + "\n");
		}

		this.playSound(this.getThrowSound(), 1.0F, 1.0F);
		return flag;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte b)
	{
		if (b == 4)
		{
			this.attackTimer = 10;
			this.playSound(this.getThrowSound(), 1.0F, 1.0F);
		}
		else
		{
			super.handleStatusUpdate(b);
		}
	}

	@SideOnly(Side.CLIENT)
	public int getAttackTimer()
	{
		return this.attackTimer;
	}

	/**
	 * Called when the mob is falling. Calculates and applies fall damage.
	 */
	@Override
	public void fall(float distance, float damageMultiplier) 
	{
		if(this.canTakeFallDamage())
		{
			super.fall(distance, damageMultiplier);
		}
	}

	@Override
	public int getMaxFallHeight()
	{
		return this.canTakeFallDamage() ? super.getMaxFallHeight() : 64;
	}

	/**
	 * Plays sound of golem walking
	 */
	@Override
	protected void playStepSound(BlockPos pos, Block block)
	{
		this.playSound(this.getWalkingSound(), 0.7F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.1F);
	}

	/**
	 * Determines if an entity can be despawned, used on idle far away entities
	 */
	@Override
	protected boolean canDespawn()
	{
		return false;
	}

	/**
	 * Get number of ticks, at least during which the living entity will be silent.
	 */
	@Override
	public int getTalkInterval()
	{
		return 120;
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	@Override
	public void onDeath(DamageSource src)
	{
		if (!this.isPlayerCreated() && this.attackingPlayer != null && this.villageObj != null)
		{
			this.villageObj.setReputationForPlayer(this.attackingPlayer.getName(), -5);
		}

		super.onDeath(src);
	}

	///////////////// ITEM DROP LOGIC /////////////////////

	/**
	 * Drop items of this living's type. 
	 * @param recentlyHit - Whether this entity has recently been hit by a player. 
	 * @param lootingLevel - Level of Looting used to kill this mob.
	 */
	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel)
	{
		// WeightedRandomChestContent(Item, metadata, minimum amount, maximum amount
		List<ItemStack> drops = new ArrayList();

		this.addGolemDrops(drops, recentlyHit, lootingLevel);

		for(ItemStack i : drops)
		{
			if(i != null)
			{
				this.entityDropItem(i, 0.0F);
			}
			
		}
	}

	/////////////// NON-OVERRIDEN GETTERS/SETTERS /////////////////

	public void setTextureType(ResourceLocation texturelocation)
	{
		this.textureLoc = texturelocation;
	}

	public ResourceLocation getTextureType()
	{
		return this.textureLoc;
	}

	/** Sets the attack damage this golem has **/
	private void setBaseAttackDamage(float f)
	{
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(f);
	}

	public float getBaseAttackDamage()
	{
		return (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
	}

	public double getBaseMoveSpeed()
	{
		return this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
	}

	public Village getVillage() 
	{
		return this.villageObj;
	}

	public void setCanTakeFallDamage(boolean toSet)
	{
		this.takesFallDamage = toSet;
	}

	public boolean canTakeFallDamage()
	{
		return this.takesFallDamage;
	}

	public void setCanSwim(boolean canSwim)
	{
		if(canSwim)
		{
			((PathNavigateGround)this.getNavigator()).setCanSwim(false);
			this.tasks.addTask(0, new EntityAISwimming(this));
		}
		else
		{
			((PathNavigateGround)this.getNavigator()).setCanSwim(true);
			this.tasks.removeTask(new EntityAISwimming(this));
		}
	}

	public void setPlayerCreated(boolean bool)
	{
		this.isPlayerCreated = bool;
	}

	public boolean isPlayerCreated() 
	{
		return this.isPlayerCreated;
	}

	/** Not used in this project. Will be used in the WAILA addon **/
	public boolean doesInteractChangeTexture()
	{
		return false;
	}

	/////////////// TEXTURE HELPERS //////////////////

	/** Makes a texture on the assumption that MODID is 'golems' **/
	public static ResourceLocation makeGolemTexture(String texture)
	{
		return makeGolemTexture(ModularGolems.MODID, texture);
	}

	/** 
	 * Makes a ResourceLocation using the passed mod id and part of the texture name.
	 * Texture should be at 'assets/<b>MODID</b>/textures/entity/golem_<b>suffix</b>.png'
	 * @see {@link #applyTexture()}
	 **/
	public static ResourceLocation makeGolemTexture(final String MODID, String texture)
	{
		return new ResourceLocation(MODID + ":textures/entity/golem_" + texture + ".png");
	}

	///////////////////// SOUND OVERRIDES ////////////////////

	@Override
	protected SoundEvent getAmbientSound()
	{
		return getGolemSound();
	}

	protected SoundEvent getWalkingSound()
	{
		return getGolemSound();
	}

	/** Returns the sound this mob makes when it attacks */
	public SoundEvent getThrowSound() 
	{
		return getGolemSound();
	}

	@Override
	protected SoundEvent getHurtSound()
	{
		return getGolemSound();
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return getGolemSound();
	}

	////////////////////////////////////////////////////////////
	// Override ALL OF THE FOLLOWING FUNCTIONS FOR EACH GOLEM //
	////////////////////////////////////////////////////////////

	/** Called from applyEntityAttributes. Use this to adjust health, speed, knockback resistance, etc. **/
	protected abstract void applyAttributes();

	/** 
	 * Called each time a golem dies. Passes a list of drops already containing some defaults.
	 **/
	public abstract void addGolemDrops(List<ItemStack> dropList, boolean recentlyHit, int lootingLevel);

	/** @return A SoundEvent to play when the golem is attacking, walking, hurt, and on death **/
	public abstract SoundEvent getGolemSound();
}