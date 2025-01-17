package org.dave.compactmachines.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;

import org.dave.compactmachines.CompactMachines;
import org.dave.compactmachines.creativetab.CreativeTabCM;
import org.dave.compactmachines.handler.ConfigurationHandler;
import org.dave.compactmachines.init.ModItems;
import org.dave.compactmachines.item.ItemPersonalShrinkingDevice;
import org.dave.compactmachines.machines.tools.ChunkLoadingTools;
import org.dave.compactmachines.machines.tools.CubeTools;
import org.dave.compactmachines.machines.tools.TeleportTools;
import org.dave.compactmachines.reference.GuiId;
import org.dave.compactmachines.reference.Names;
import org.dave.compactmachines.reference.Reference;
import org.dave.compactmachines.tileentity.TileEntityMachine;
import org.dave.compactmachines.utility.FluidUtils;
import org.dave.compactmachines.utility.WorldUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMachine extends BlockCM implements ITileEntityProvider {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;
    private IIcon[] iconsUpg;

    public BlockMachine() {
        super();
        this.setBlockName(Names.Blocks.MACHINE);
        this.setBlockTextureName(Names.Blocks.MACHINE);
        this.setHardness(4.0F);
        this.setResistance(6000000.0F);
        this.setCreativeTab(CreativeTabCM.CM_TAB);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icons = new IIcon[6];
        iconsUpg = new IIcon[6];

        for (int i = 0; i < icons.length; i++) {
            icons[i] = iconRegister.registerIcon("compactmachines:machine_" + i);
            iconsUpg[i] = iconRegister.registerIcon("compactmachines:machine_" + i + "_upg");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int metadata) {
        return icons[metadata];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        TileEntityMachine tileEntityMachine = (TileEntityMachine) blockAccess.getTileEntity(x, y, z);
        if (tileEntityMachine == null) {
            return icons[0];
        } else {
            if (tileEntityMachine.isUpgraded) {
                return iconsUpg[tileEntityMachine.meta];
            } else {
                return icons[tileEntityMachine.meta];
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metaData) {
        TileEntityMachine tileEntityMachine = new TileEntityMachine();
        tileEntityMachine.meta = metaData;
        return tileEntityMachine;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, player, stack);

        if (stack.stackTagCompound == null) {
            return;
        }

        if (!(world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
            return;
        }

        TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);
        if (tileEntityMachine.coords != -1) {
            // The machine already has data for some reason
            return;
        }

        if (stack.stackTagCompound.hasKey("coords")) {
            int coords = stack.stackTagCompound.getInteger("coords");
            if (coords != -1) {
                tileEntityMachine.coords = coords;
                tileEntityMachine.isUpgraded = true;
            }

            if (!world.isRemote && ConfigurationHandler.adaptBiomes) {
                CubeTools.setCubeBiome(coords, CubeTools.getMachineBiome(tileEntityMachine));
            }
        }

        if (stack.hasDisplayName()) {
            tileEntityMachine.setCustomName(stack.getDisplayName());
        }

        tileEntityMachine.markDirty();
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item unknown, CreativeTabs tab, List subItems) {
        for (int i = 0; i < 6; i++) {
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (willHarvest && (world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
            final TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);

            boolean result = super.removedByPlayer(world, player, x, y, z, willHarvest);

            if (result) {
                if (!tileEntityMachine.isUpgraded) {
                    CubeTools.harvestMachine(tileEntityMachine, player);
                }

                tileEntityMachine.dropAsItem();
            }

            return result;
        }

        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    };

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if ((world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
            TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);

            // Disable chunk loading and remove it from the worlds NBT table
            if (!CompactMachines.instance.entangleRegistry.hasRemainingMachines(tileEntityMachine.coords)) {
                ChunkLoadingTools.disableMachine(tileEntityMachine);
            }

            world.removeTileEntity(x, y, z);
        }

        WorldUtils.updateNeighborAEGrids(world, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int faceHit, float par7,
        float par8, float par9) {
        if (player.isSneaking()) {
            return false;
        } else {
            if (!world.isRemote && player instanceof EntityPlayerMP) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
                    return false;
                }

                TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);
                ItemStack playerStack = player.getCurrentEquippedItem();
                NBTTagCompound stackNBT = null;
                NBTTagCompound cmNBT = null;

                if (playerStack != null) {

                    if (playerStack.hasTagCompound()) {
                        stackNBT = playerStack.getTagCompound();

                        // The CompactMachines NBT group
                        if (stackNBT.hasKey("CompactMachines")) {
                            cmNBT = stackNBT.getCompoundTag("CompactMachines");
                        }
                    }

                    // XXX: Do we need to do anything for gases here?

                    // First check if the player is right clicking with a shrinker or if the player has an item with
                    // canShrink set as true
                    if (playerStack.getItem() instanceof ItemPersonalShrinkingDevice
                        || ((cmNBT != null && cmNBT.hasKey("canShrink")) && cmNBT.getBoolean("canShrink"))) {
                        // Activated with a PSD
                        if (!ConfigurationHandler.allowEnterWithoutPSD) {
                            player.getEntityData()
                                .setBoolean("isUsingPSD", true);
                        }
                        TeleportTools.teleportPlayerToMachineWorld((EntityPlayerMP) player, tileEntityMachine);
                    } else if (FluidContainerRegistry.isEmptyContainer(playerStack)) {
                        // Activated with an empty bucket
                        FluidUtils.emptyTankIntoContainer(
                            tileEntityMachine,
                            player,
                            tileEntityMachine.getFluid(faceHit),
                            ForgeDirection.getOrientation(faceHit));
                    } else if (FluidContainerRegistry.isFilledContainer(playerStack)) {
                        // Activated with a filled bucket
                        FluidUtils
                            .fillTankWithContainer(tileEntityMachine, player, ForgeDirection.getOrientation(faceHit));
                    } else
                        if (tileEntityMachine.isUpgraded == false && playerStack.getItem() == Reference.upgradeItem) {
                            // Activated with a nether star
                            tileEntityMachine.isUpgraded = true;
                            tileEntityMachine.markDirty();

                            world.markBlockForUpdate(x, y, z);

                            playerStack.stackSize--;
                        } else if (playerStack.getItem() == ModItems.quantumentangler) {

                            if (!ConfigurationHandler.allowEntanglement) {
                                player.addChatMessage(
                                    new ChatComponentTranslation("msg.message_quantum_entanglement_disabled.txt"));
                                return true;
                            }

                            if (stackNBT != null && stackNBT.hasKey("coords") && stackNBT.hasKey("size")) {
                                // quantumEntangler already has a compound
                                if (tileEntityMachine.coords != -1) {
                                    player.addChatMessage(
                                        new ChatComponentTranslation("msg.message_machine_already_in_use.txt"));
                                } else if (tileEntityMachine.isUpgraded == false) {
                                    player.addChatMessage(
                                        new ChatComponentTranslation("msg.message_machine_not_upgraded.txt"));
                                } else {

                                    int size = stackNBT.getInteger("size");
                                    if (size != tileEntityMachine.meta) {
                                        player.addChatMessage(
                                            new ChatComponentTranslation("msg.message_machine_invalid_size.txt"));
                                    } else {
                                        int coords = stackNBT.getInteger("coords");
                                        tileEntityMachine.coords = coords;

                                        if (stackNBT.hasKey("roomname")) {
                                            tileEntityMachine.setCustomName(stackNBT.getString("roomname"));
                                        }
                                        tileEntityMachine.initialize();
                                        tileEntityMachine.markDirty();

                                        WorldUtils.updateNeighborAEGrids(world, x, y, z);

                                        playerStack.stackSize--;
                                    }
                                }

                            } else if (tileEntityMachine.isUpgraded && tileEntityMachine.coords != -1) {
                                // No "coords" tag yet and the machine is in use and upgraded
                                // --> Save the coords
                                NBTTagCompound nbt = new NBTTagCompound();
                                nbt.setInteger("coords", tileEntityMachine.coords);
                                nbt.setInteger("size", tileEntityMachine.meta);
                                nbt.setString("roomname", tileEntityMachine.getCustomName());

                                playerStack.setTagCompound(nbt);
                            } else {
                                if (tileEntityMachine.coords == -1) {
                                    player.addChatMessage(
                                        new ChatComponentTranslation("msg.message_machine_not_in_use.txt"));
                                } else if (!tileEntityMachine.isUpgraded) {
                                    player.addChatMessage(
                                        new ChatComponentTranslation("msg.message_machine_not_upgraded.txt"));
                                }
                            }
                        } else {
                            player.openGui(CompactMachines.instance, GuiId.MACHINE.ordinal(), world, x, y, z);
                        }
                } else {
                    player.openGui(CompactMachines.instance, GuiId.MACHINE.ordinal(), world, x, y, z);
                }
            }

            return true;
        }
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        super.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);

        if (!(world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
            return;
        }

        ((TileEntityMachine) world.getTileEntity(x, y, z)).onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
    }
}
