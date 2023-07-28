package org.dave.compactmachines.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;

import org.dave.compactmachines.CompactMachines;
import org.dave.compactmachines.machines.tools.CubeTools;
import org.dave.compactmachines.reference.GuiId;
import org.dave.compactmachines.reference.Names;
import org.dave.compactmachines.tileentity.TileEntityInterface;
import org.dave.compactmachines.utility.FluidUtils;

public class BlockInterface extends BlockProtected implements ITileEntityProvider {

    public BlockInterface() {
        super();
        this.setBlockName(Names.Blocks.INTERFACE);
        this.setBlockTextureName(Names.Blocks.INTERFACE);
        this.setLightOpacity(1);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityInterface();
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    // Prevent blocks from being placed by players
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        world.setBlockToAir(x, y, z);
        return;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        super.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);

        if (!(world.getTileEntity(x, y, z) instanceof TileEntityInterface)) {
            return;
        }

        ((TileEntityInterface) world.getTileEntity(x, y, z)).onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return CubeTools.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int faceHit, float par7,
        float par8, float par9) {
        if (player.isSneaking()) {
            return false;
        } else {
            if (!(world.getTileEntity(x, y, z) instanceof TileEntityInterface)) {
                return false;
            }

            TileEntityInterface tank = (TileEntityInterface) world.getTileEntity(x, y, z);

            ItemStack playerStack = player.inventory.getCurrentItem();

            // XXX: Do we need to do anything for gases here?

            if (playerStack != null) {
                if (FluidContainerRegistry.isEmptyContainer(playerStack)) {
                    FluidUtils.emptyTankIntoContainer(tank, player, tank.getFluid());
                    world.markBlockForUpdate(x, y, z);
                    return true;
                } else if (FluidContainerRegistry.isFilledContainer(playerStack)) {
                    FluidUtils.fillTankWithContainer(tank, player);
                    world.markBlockForUpdate(x, y, z);
                    return true;
                }
            }

            if (!world.isRemote && player instanceof EntityPlayerMP) {
                player.openGui(CompactMachines.instance, GuiId.INTERFACE.ordinal(), world, x, y, z);
            }

            return true;
        }
    }
}
