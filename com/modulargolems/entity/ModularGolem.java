package com.modulargolems.entity;

import java.util.List;

import com.google.common.base.Optional;
import com.modulargolems.entity.ai.AIManager;
import com.modulargolems.entity.ai.EntityAIExplode;
import com.modulargolems.entity.ai.EntityAITeleportRandomly;
import com.modulargolems.main.Config;
import com.modulargolems.main.ModularGolems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBookshelf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModularGolem extends GolemBase
{
	protected static final DataParameter<Optional<IBlockState>> DATA_ARM1 = EntityDataManager.createKey(ModularGolem.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	protected static final DataParameter<Optional<IBlockState>> DATA_ARM2 = EntityDataManager.createKey(ModularGolem.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	protected static final DataParameter<Optional<IBlockState>> DATA_BODY = EntityDataManager.createKey(ModularGolem.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	protected static final DataParameter<Optional<IBlockState>> DATA_LEGS = EntityDataManager.createKey(ModularGolem.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	protected static final DataParameter<Byte> DATA_LIGHT = EntityDataManager.createKey(ModularGolem.class, DataSerializers.BYTE);

	public static final String KEY_ARM1 = "GolemBlockArm1";
	public static final String KEY_ARM2 = "GolemBlockArm2";
	public static final String KEY_BODY = "GolemBlockBody";
	public static final String KEY_LEGS = "GolemBlockLegs";

	/** The golem's texture locations */
	protected ResourceLocation rArm1, rArm2, rBody, rLegs;
	protected boolean hasTransparency;

	public boolean attackUsesFire = false;

	public ModularGolem(World world)
	{
		super(world);
	}

	public ModularGolem(World world, IBlockState arm1, IBlockState arm2, IBlockState body, IBlockState legs)
	{
		this(world);
		this.setBlocks(arm1, arm2, body, legs);
		applyBlockAttributes();
		initResourceLocations();
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.getDataManager().register(DATA_ARM1, Optional.<IBlockState>absent());
		this.getDataManager().register(DATA_ARM2, Optional.<IBlockState>absent());
		this.getDataManager().register(DATA_BODY, Optional.<IBlockState>absent());
		this.getDataManager().register(DATA_LEGS, Optional.<IBlockState>absent());
		this.getDataManager().register(DATA_LIGHT, new Byte((byte)0));
	}

	public void applyBlockAttributes()
	{	
		// set modifiers
		double attack = calculateAttack(getArm1Block(), getArm2Block(), getBodyBlock(), getLegsBlock());
		double health = calculateHealth(getArm1Block(), getArm2Block(), getBodyBlock(), getLegsBlock());
		double speed = calculateSpeed(getArm1Block(), getArm2Block(), getBodyBlock(), getLegsBlock());
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attack);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);
		// calculate light level and transparency
		this.setLightLevel(this.calculateLight());
		this.hasTransparency = this.calculateHasTransparency();	
		// apply AIs that are based on which blocks make up this golem
		AIManager.applyAIs(this);

		// debug:
		String out = "\nMy blocks: "
				+ "\n\tArm1 - " + getArm1Block().getBlock().getRegistryName().getResourcePath() + " (" + Config.getAttack(getArm1Block()) + ")"
				+ "\n\tArm2 - " + getArm2Block().getBlock().getRegistryName().getResourcePath() + " (" + Config.getAttack(getArm2Block()) + ")"
				+ "\n\tBody - " + getBodyBlock().getBlock().getRegistryName().getResourcePath() + " (" + Config.getHealth(getBodyBlock()) + ")"
				+ "\n\tLegs - " + getLegsBlock().getBlock().getRegistryName().getResourcePath() + " (" + Config.getSpeed(getLegsBlock()) + ")"
				+ "\nMy atributes: "
				+ "\n\tHealth - " + health + "\n\tAttack - " + attack + "\n\tSpeed - " + speed;
		System.out.println(out);
	}

	/** Called from applyEntityAttributes. Use this to adjust health, speed, knockback resistance, etc. **/
	@Override
	protected void applyAttributes()
	{

	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		if(this.ticksExisted == 2 && this.worldObj.isRemote)
		{
			this.initResourceLocations();
		}
		if(this.worldObj.isRemote && AIManager.hasAI(this, EntityAITeleportRandomly.class))
		{
			for(int i = 0; i < 2; ++i)
			{
				double x = this.posX + (this.getRNG().nextDouble() - 0.5D) * (double)this.width;
				double y = this.posY + this.getRNG().nextDouble() * (double)this.height - 0.25D;
				double z = this.posZ + (this.getRNG().nextDouble() - 0.5D) * (double)this.width;
				double motionX = (this.getRNG().nextDouble() - 0.5D) * 2.0D;
				double motionY = -this.getRNG().nextDouble();
				double motionZ = (this.getRNG().nextDouble() - 0.5D) * 2.0D;
				this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, motionX, motionY, motionZ, new int[0]);
			}
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entity)
	{
		if(super.attackEntityAsMob(entity))
		{
			if(this.attackUsesFire)
			{
				entity.setFire(2 + rand.nextInt(4));
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Called when the mob's health reaches 0.
	 */
	@Override
	public void onDeath(DamageSource src)
	{
		super.onDeath(src);
		if(AIManager.hasAI(this, EntityAIExplode.class))
		{
			//AIManager.getAI(this, EntityAIExplode.class).explode();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setBoolean("PlayerCreated", this.isPlayerCreated());
		writeIBlockState(nbt, getArm1Block(), KEY_ARM1);
		writeIBlockState(nbt, getArm2Block(), KEY_ARM2);
		writeIBlockState(nbt, getBodyBlock(), KEY_BODY);
		writeIBlockState(nbt, getLegsBlock(), KEY_LEGS);
		// debug:
		String ids = nbt.getShort(KEY_ARM1) + ", " + nbt.getShort(KEY_ARM2) + 
				", " + nbt.getShort(KEY_BODY) + ", " + nbt.getShort(KEY_LEGS);
		System.out.println("NBT block ids set: { " + ids + " }");
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.setPlayerCreated(nbt.getBoolean("PlayerCreated"));
		// set blocks and data
		IBlockState arm1, arm2, body, legs;
		arm1 = readIBlockState(nbt, KEY_ARM1);
		arm2 = readIBlockState(nbt, KEY_ARM2);
		body = readIBlockState(nbt, KEY_BODY);
		legs = readIBlockState(nbt, KEY_LEGS);
		// apply behavior to other important functions
		this.setBlocks(arm1, arm2, body, legs);
		this.applyBlockAttributes();
		this.initResourceLocations();
		// debug:
		String ids = nbt.getShort(KEY_ARM1) + ", " + nbt.getShort(KEY_ARM2) + 
				", " + nbt.getShort(KEY_BODY) + ", " + nbt.getShort(KEY_LEGS);
		System.out.println("NBT block ids parsed: { " + ids + " }");
	}

	@Override
	public void addGolemDrops(List<ItemStack> drops, boolean recentlyHit, int lootingLevel)
	{
		final int DROP_THRESHOLD = 3;

		ItemStack iArm1 = new ItemStack(getArm1Block().getBlock().getItemDropped(getArm1Block(), rand, lootingLevel));
		ItemStack iArm2 = new ItemStack(getArm2Block().getBlock().getItemDropped(getArm2Block(), rand, lootingLevel));
		ItemStack iBody = new ItemStack(getBodyBlock().getBlock().getItemDropped(getBodyBlock(), rand, lootingLevel));
		ItemStack iLegs = new ItemStack(getLegsBlock().getBlock().getItemDropped(getLegsBlock(), rand, lootingLevel));

		if(rand.nextInt(DROP_THRESHOLD) > lootingLevel) drops.add(iArm1);
		if(rand.nextInt(DROP_THRESHOLD) > lootingLevel) drops.add(iArm2);
		if(rand.nextInt(DROP_THRESHOLD) > lootingLevel) drops.add(iBody);
		if(rand.nextInt(DROP_THRESHOLD) > lootingLevel) drops.add(iLegs);
	}

	@Override
	public SoundEvent getGolemSound() 
	{
		return this.getLegsBlock() != null ? this.getLegsBlock().getBlock().getStepSound().getStepSound() : SoundEvents.block_stone_step;
	}

	//////////////// MAKE TEXTURES /////////////////////

	public void initResourceLocations()
	{
		this.rArm1 = makeArm1Loc();
		this.rArm2 = makeArm2Loc();
		this.rBody = makeBodyLoc();
		this.rLegs = makeLegsLoc();
		String out = "Made ResourceLocations:\n\t"
				+ rArm1.toString() + "\n\t" + rArm2.toString() + "\n\t"
				+ rBody.toString() + "\n\t" + rLegs.toString();
		System.out.println(out);
	}

	protected ResourceLocation makeArm1Loc()
	{
		String sArm1 = getArm1Block().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(getModId() + ":textures/entity/arm1/" + sArm1 + ".png");
	}

	protected ResourceLocation makeArm2Loc()
	{
		String sArm2 = getArm2Block().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(getModId() + ":textures/entity/arm2/" + sArm2 + ".png");
	}

	protected ResourceLocation makeBodyLoc()
	{
		String sBody = getBodyBlock().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(getModId() + ":textures/entity/body/" + sBody + ".png");
	}

	protected ResourceLocation makeLegsLoc()
	{
		String sLegs = getLegsBlock().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(getModId() + ":textures/entity/legs/" + sLegs + ".png");
	}

	/////////////////// OTHER //////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float f)
	{
		return this.getLightLevel() > 0 ? 15728880 : super.getBrightnessForRender(f);
	}

	/** Gets how bright this entity is **/
	@Override
	public float getBrightness(float f)
	{
		return this.getLightLevel() > 0 ? this.getLightLevel() : super.getBrightness(f);
	}

	public int calculateLight()
	{
		int maxLight = 0;
		for(IBlockState b : this.getGolemBlocks())
		{
			int light = b.getBlock().getLightValue(b);
			if(light > maxLight)
			{
				maxLight = light;
			}
		}
		return maxLight;
	}

	public boolean calculateHasTransparency()
	{
		if(!this.worldObj.isRemote) return false;

		for(IBlockState b : this.getGolemBlocks())
		{
			if(b.isTranslucent()) return true;
		}
		return false;
	}

	public double calculateAttack(IBlockState arm1, IBlockState arm2, IBlockState body, IBlockState legs)
	{
		final double WEIGHT_BODY = 0.1D;
		final double WEIGHT_LEGS = 0.05D;
		double dArms = (Config.getAttack(arm1) + Config.getAttack(arm2)) * 0.5D;
		double dBody = (Config.getAttack(body) * WEIGHT_BODY);
		double dLegs = (Config.getAttack(legs) * WEIGHT_LEGS);
		if(dBody < dArms * WEIGHT_BODY) dBody *= -1;
		if(dLegs < dArms * WEIGHT_LEGS) dLegs *= -1;
		return dArms + dBody + dLegs;
	}

	public double calculateHealth(IBlockState arm1, IBlockState arm2, IBlockState body, IBlockState legs)
	{
		final double WEIGHT_ARMS = 0.2D;
		final double WEIGHT_LEGS = 0.1D;
		double dBody = Config.getHealth(body);
		double dArms = (Config.getHealth(arm1) + Config.getHealth(arm2)) * 0.5D * WEIGHT_ARMS;
		double dLegs = Config.getHealth(legs) * WEIGHT_LEGS;
		if(dArms < dBody * WEIGHT_ARMS) dArms *= -1;
		if(dLegs < dBody * WEIGHT_LEGS) dLegs *= -1;
		return dBody + dArms + dLegs;
	}

	public double calculateSpeed(IBlockState arm1, IBlockState arm2, IBlockState body, IBlockState legs)
	{
		final double WEIGHT_BODY = 0.15D;
		final double WEIGHT_ARMS = 0.05D;
		double dLegs = Config.getSpeed(legs);
		double dArms = (Config.getSpeed(arm1) + Config.getSpeed(arm2)) * 0.5D * WEIGHT_ARMS;
		double dBody = Config.getSpeed(legs) * WEIGHT_BODY;
		if(dArms < dLegs * WEIGHT_ARMS) dArms *= -1;
		if(dBody < dLegs * WEIGHT_BODY) dBody *= -1;
		return dLegs + dArms + dBody;
	}

	public NBTTagCompound writeIBlockState(NBTTagCompound nbt, IBlockState state, final String KEY)
	{
		nbt.setShort(KEY, (short)Block.getIdFromBlock(state.getBlock()));
		nbt.setShort(KEY.concat("Data"), (short)state.getBlock().getMetaFromState(state));
		return nbt;
	}

	public IBlockState readIBlockState(NBTTagCompound nbt, final String KEY)
	{
		return Block.getBlockById(nbt.getShort(KEY)).getStateFromMeta(nbt.getShort(KEY.concat("Data")) & 65535);
	}

	/////////////////// SETTERS AND GETTERS /////////////////////
	/*
	public void setBlocks(Block arm1, Block arm2, Block body, Block legs)
	{
		setBlocks(arm1.getDefaultState(), arm2.getDefaultState(), body.getDefaultState(), legs.getDefaultState());
	}
	 */
	public void setBlocks(IBlockState arm1, IBlockState arm2, IBlockState body, IBlockState legs)
	{
		arm1 = Config.getAlias(arm1);
		arm2 = Config.getAlias(arm2);
		body = Config.getAlias(body);
		legs = Config.getAlias(legs);
		this.getDataManager().set(DATA_ARM1, Optional.fromNullable(arm1));
		this.getDataManager().set(DATA_ARM2, Optional.fromNullable(arm2));
		this.getDataManager().set(DATA_BODY, Optional.fromNullable(body));
		this.getDataManager().set(DATA_LEGS, Optional.fromNullable(legs));
	}

	public ResourceLocation getArm1Resource()
	{
		return this.rArm1;
	}

	public ResourceLocation getArm2Resource()
	{
		return this.rArm2;
	}

	public ResourceLocation getBodyResource()
	{
		return this.rBody;
	}

	public ResourceLocation getLegsResource()
	{
		return this.rLegs;
	}

	public IBlockState getArm1Block()
	{
		return (IBlockState)((Optional)this.dataWatcher.get(DATA_ARM1)).orNull();
	}

	public IBlockState getArm2Block()
	{
		return (IBlockState)((Optional)this.dataWatcher.get(DATA_ARM2)).orNull();
	}

	public IBlockState getBodyBlock()
	{
		return (IBlockState)((Optional)this.dataWatcher.get(DATA_BODY)).orNull();
	}

	public IBlockState getLegsBlock()
	{
		return (IBlockState)((Optional)this.dataWatcher.get(DATA_LEGS)).orNull();
	}

	public IBlockState[] getGolemBlocks()
	{
		return new IBlockState[] {getArm1Block(), getArm2Block(), getBodyBlock(), getLegsBlock()};
	}

	public void setLightLevel(int toSet)
	{
		this.getDataManager().set(DATA_LIGHT, new Byte((byte)toSet));
	}

	public int getLightLevel()
	{
		return this.getDataManager().get(DATA_LIGHT).intValue();
	}

	public boolean hasTransparency()
	{
		return this.hasTransparency;
	}

	public String getModId()
	{
		return ModularGolems.MODID;
	}
}
