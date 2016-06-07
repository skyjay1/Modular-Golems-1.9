package com.modulargolems.entity;

import java.util.List;

import com.google.common.base.Optional;
import com.modulargolems.entity.ai.EntityAIProvideLight;
import com.modulargolems.main.Config;
import com.modulargolems.main.ModularGolems;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.BlockRenderLayer;
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
	
	public static final String KEY_ARM1 = "GolemBlockArm1";
	public static final String KEY_ARM2 = "GolemBlockArm2";
	public static final String KEY_BODY = "GolemBlockBody";
	public static final String KEY_LEGS = "GolemBlockLegs";

	/** The golem's texture locations */
	private ResourceLocation rArm1, rArm2, rBody, rLegs;

	protected int light;
	protected boolean hasTransparency;

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
		//this.writeToNBT(this.getEntityData());
		if(this.getLight() > 0)
		{
			this.tasks.addTask(2, new EntityAIProvideLight(this, 2, this.getLight() > 12));
		}
	}
	
	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.getDataManager().register(DATA_ARM1, Optional.<IBlockState>absent());
		this.getDataManager().register(DATA_ARM2, Optional.<IBlockState>absent());
		this.getDataManager().register(DATA_BODY, Optional.<IBlockState>absent());
		this.getDataManager().register(DATA_LEGS, Optional.<IBlockState>absent());
	}

	public void applyBlockAttributes()
	{	
		// set modifiers
		double attack = (Config.getAttack(getArm1Block()) + Config.getAttack(getArm2Block())) / 2;
		double health = Config.getHealth(getBodyBlock());
		double speed = Config.getSpeed(getLegsBlock());
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attack);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);
		// calculate light level and transparency
		this.light = this.calculateLight();
		this.hasTransparency = this.calculateHasTransparency();	
		// add AIs based on blocks
		if(this.getLight() > 0)
		{
			this.tasks.addTask(2, new EntityAIProvideLight(this, 2, this.getLight() > 12));
		}
		for(IBlockState ibs : this.getGolemBlocks())
		{
			if(Config.isBlockMapped(ibs) && Config.getSwim(ibs))
			{
				this.setCanSwim(true);
				break;
			}
		}
		
		// debug:
		String out = "\nMy blocks: "
				+ "\n\tArm1 - " + getArm1Block().getBlock().getRegistryName().getResourcePath() + "(" + Config.getAttack(getArm1Block()) + ")"
				+ "\n\tArm2 - " + getArm2Block().getBlock().getRegistryName().getResourcePath() + "(" + Config.getAttack(getArm2Block()) + ")"
				+ "\n\tBody - " + getBodyBlock().getBlock().getRegistryName().getResourcePath() + "(" + Config.getHealth(getBodyBlock()) + ")"
				+ "\n\tLegs - " + getLegsBlock().getBlock().getRegistryName().getResourcePath() + "(" + Config.getSpeed(getLegsBlock()) + ")";
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
		if(this.ticksExisted == 2 && worldObj.isRemote)
		{
			this.initResourceLocations();
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
		ItemStack iArm1 = new ItemStack(getArm1Block().getBlock());
		ItemStack iArm2 = new ItemStack(getArm2Block().getBlock());
		ItemStack iBody = new ItemStack(getBodyBlock().getBlock());
		ItemStack iLegs = new ItemStack(getLegsBlock().getBlock());

		if(rand.nextBoolean()) drops.add(iArm1);
		if(rand.nextBoolean()) drops.add(iArm2);
		if(rand.nextBoolean()) drops.add(iBody);
		if(rand.nextBoolean()) drops.add(iLegs);
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
		String out = "Made ResourceLocations:\n"
				+ rArm1.toString() + "\n" + rArm2.toString() + "\n"
				+ rBody.toString() + "\n" + rLegs.toString();
		System.out.println(out);
	}

	protected ResourceLocation makeArm1Loc()
	{
		String sArm1 = getArm1Block().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(ModularGolems.MODID + ":textures/entity/arm1/" + sArm1 + ".png");
	}

	protected ResourceLocation makeArm2Loc()
	{
		String sArm2 = getArm2Block().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(ModularGolems.MODID + ":textures/entity/arm2/" + sArm2 + ".png");
	}

	protected ResourceLocation makeBodyLoc()
	{
		String sBody = getBodyBlock().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(ModularGolems.MODID + ":textures/entity/body/" + sBody + ".png");
	}

	protected ResourceLocation makeLegsLoc()
	{
		String sLegs = getLegsBlock().getBlock().getRegistryName().getResourcePath();
		return new ResourceLocation(ModularGolems.MODID + ":textures/entity/legs/" + sLegs + ".png");
	}

	/////////////////// OTHER //////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float f)
	{
		return this.getLight() > 0 ? 15728880 : super.getBrightnessForRender(f);
	}

	/** Gets how bright this entity is **/
	@Override
	public float getBrightness(float f)
	{
		return this.getLight() > 0 ? this.light : super.getBrightness(f);
	}

	public int calculateLight()
	{
		int maxLight = 0;
		for(IBlockState b : this.getGolemBlocks())
		{
			if(b != null)
			{
				int light = b.getBlock().getLightValue(b);
				if(light > maxLight)
				{
					maxLight = light;
				}
			}
		}

		return maxLight;
	}

	public boolean calculateHasTransparency()
	{
		if(!this.worldObj.isRemote) return false;

		boolean transp = false;
		for(IBlockState b : this.getGolemBlocks())
		{
			transp = transp || b.getBlock().getBlockLayer() == BlockRenderLayer.TRANSLUCENT;
		}
		return transp;
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

	public int getLight()
	{
		return this.light;
	}

	public boolean hasTransparency()
	{
		return this.hasTransparency;
	}
}
