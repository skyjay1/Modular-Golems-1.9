package com.modulargolems.blocks;

import com.modulargolems.entity.ModularGolem;
import com.modulargolems.main.Config;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockGolemHead extends BlockHorizontal
{
	public BlockGolemHead() 
	{
		super(Material.ground);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setStepSound(SoundType.WOOD);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(FACING).getHorizontalIndex();
	}

	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {FACING});
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		super.onBlockAdded(world, pos, state);
		IBlockState down1 = world.getBlockState(pos.down(1));
		IBlockState down2 = world.getBlockState(pos.down(2));
		IBlockState west = world.getBlockState(pos.down(1).west(1));
		IBlockState east = world.getBlockState(pos.down(1).east(1));
		IBlockState north = world.getBlockState(pos.down(1).north(1));
		IBlockState south = world.getBlockState(pos.down(1).south(1));
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if(Config.isBlockMapped(down1) && Config.isBlockMapped(down2))
		{
			boolean flagNS = Config.isBlockMapped(north) && Config.isBlockMapped(south);
			boolean flagWE = Config.isBlockMapped(east) && Config.isBlockMapped(west);
			
			if(flagNS || flagWE)
			{
				IBlockState arm1 = flagNS ? north : east;
				IBlockState arm2 = flagNS ? south : west;
				ModularGolem golem = new ModularGolem(world, arm1, arm2, down1, down2);
				
				// clear the area where the golem blocks were
				removeGolemBlocks(world, pos, flagWE);
				
				golem.setPlayerCreated(true);
				golem.moveToBlockPosAndAngles(pos.down(2), 0.0F, 0.0F);
				if(!world.isRemote)
				{
					world.spawnEntityInWorld(golem);
				}
			}	
		}
	}

	/** Replaces golem blocks with air **/
	public static void removeGolemBlocks(World world, BlockPos head, boolean alignedWE)
	{
		world.setBlockToAir(head);
		world.setBlockToAir(head.down(1));
		world.setBlockToAir(head.down(2));
		if(alignedWE)
		{
			world.setBlockToAir(head.down(1).west(1));
			world.setBlockToAir(head.down(1).east(1));
		}
		else
		{
			world.setBlockToAir(head.down(1).north(1));
			world.setBlockToAir(head.down(1).south(1));
		}
	}
}
