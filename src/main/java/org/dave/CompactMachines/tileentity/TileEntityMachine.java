package org.dave.compactmachines.tileentity;

import java.util.HashMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.dave.compactmachines.CompactMachines;
import org.dave.compactmachines.handler.ConfigurationHandler;
import org.dave.compactmachines.handler.SharedStorageHandler;
import org.dave.compactmachines.init.ModBlocks;
import org.dave.compactmachines.integration.AbstractHoppingStorage;
import org.dave.compactmachines.integration.AbstractSharedStorage;
import org.dave.compactmachines.integration.appeng.AESharedStorage;
import org.dave.compactmachines.integration.appeng.CMGridBlock;
import org.dave.compactmachines.integration.botania.BotaniaSharedStorage;
import org.dave.compactmachines.integration.bundledredstone.BRSharedStorage;
import org.dave.compactmachines.integration.fluid.FluidSharedStorage;
import org.dave.compactmachines.integration.gas.GasSharedStorage;
import org.dave.compactmachines.integration.item.ItemSharedStorage;
import org.dave.compactmachines.integration.opencomputers.OpenComputersSharedStorage;
import org.dave.compactmachines.integration.redstoneflux.FluxSharedStorage;
import org.dave.compactmachines.integration.thaumcraft.ThaumcraftSharedStorage;
import org.dave.compactmachines.machines.tools.ChunkLoadingTools;
import org.dave.compactmachines.machines.tools.CubeTools;
import org.dave.compactmachines.reference.Names;
import org.dave.compactmachines.reference.Reference;

import appeng.api.movable.IMovableTile;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.api.ProjectRedAPI;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import vazkii.botania.api.mana.IManaPool;

@Optional.InterfaceList({ @Optional.Interface(iface = "appeng.api.networking.IGridHost", modid = "appliedenergistics2"),
    @Optional.Interface(iface = "appeng.api.movable.IMovableTile", modid = "appliedenergistics2"),
    @Optional.Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Transmission"),
    @Optional.Interface(iface = "li.cil.oc.api.network.SidedEnvironment", modid = "OpenComputers"),
    @Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "Mekanism"),
    @Optional.Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "Mekanism"),
    @Optional.Interface(iface = "vazkii.botania.api.mana.IManaPool", modid = "Botania"),
    @Optional.Interface(iface = "thaumcraft.api.aspects.IEssentiaTransport", modid = "Thaumcraft") })
public class TileEntityMachine extends TileEntityCM
    implements ISidedInventory, IFluidHandler, IGasHandler, ITubeConnection, IEnergyHandler, IGridHost, IMovableTile,
    IBundledTile, SidedEnvironment, IManaPool, IEssentiaTransport {

    public int coords = -1;
    public int[] _fluidid;
    public int[] _fluidamount;
    public int[] _gasid;
    public int[] _gasamount;
    public int[] _energy;
    public int[] _aspectid;
    public int[] _aspectamount;

    public int _mana = 0;
    public int meta = 0;

    public int entangledInstance;

    public boolean isUpgraded = false;

    public HashMap<Integer, CMGridBlock> gridBlocks;
    public HashMap<Integer, IGridNode> gridNodes;

    private boolean init;
    private Object[] airHandlers;

    public static final int INVENTORY_SIZE = 6;

    public TileEntityMachine() {
        super();
        // XXX: Should these be initialised to -1, as in TileEntityInterface
        _fluidid = new int[6];
        _fluidamount = new int[6];
        // These need to be initialised to -1 to avoid a crash when no
        // gas-providing mod is installed
        _gasid = new int[] { -1, -1, -1, -1, -1, -1 };
        _gasamount = new int[6];
        _energy = new int[6];
        _mana = 0;
        _aspectid = new int[] { -1, -1, -1, -1, -1, -1 };
        _aspectamount = new int[6];

        gridBlocks = new HashMap<Integer, CMGridBlock>();
        gridNodes = new HashMap<Integer, IGridNode>();

        airHandlers = new Object[6];
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        // Side directly determines the inventory
        return new int[] { side };
    }

    public ItemSharedStorage getStorage(int side) {
        return (ItemSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, "item");
    }

    public FluidSharedStorage getStorageFluid(int side) {
        return (FluidSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, "liquid");
    }

    public GasSharedStorage getStorageGas(int side) {
        return (GasSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, "gas");
    }

    public FluxSharedStorage getStorageFlux(int side) {
        return (FluxSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, "flux");
    }

    public AESharedStorage getStorageAE(int side) {
        return (AESharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, entangledInstance, "appeng");
    }

    public BRSharedStorage getStorageBR(int side) {
        return (BRSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, "bundledRedstone");
    }

    public OpenComputersSharedStorage getStorageOC(int side) {
        return (OpenComputersSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, "OpenComputers");
    }

    public BotaniaSharedStorage getStorageBotania(int side) {
        return (BotaniaSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, 0, "botania");
    }

    public ThaumcraftSharedStorage getStorageThaumcraft(int side) {
        return (ThaumcraftSharedStorage) SharedStorageHandler.instance(worldObj.isRemote)
            .getStorage(this.coords, side, "thaumcraft");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);

        coords = nbtTagCompound.getInteger("coords");
        meta = nbtTagCompound.getInteger("meta");
        isUpgraded = nbtTagCompound.getBoolean("upgraded");
        entangledInstance = nbtTagCompound.getInteger("entangle-id");

        if (isUpgraded && worldObj != null && worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    protected void readSyncNBT(NBTTagCompound tag) {}

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);

        if (!nbtTagCompound.hasKey("meta")) {
            nbtTagCompound.setInteger("meta", meta);
        }

        nbtTagCompound.setInteger("coords", coords);
        nbtTagCompound.setBoolean("upgraded", isUpgraded);
        nbtTagCompound.setInteger("entangle-id", entangledInstance);
    }

    @Override
    protected void writeSyncNBT(NBTTagCompound tag) {}

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    @SideOnly(Side.SERVER)
    public void onChunkUnload() {
        super.onChunkUnload();

        if (worldObj.isRemote) {
            return;
        }

        if (ConfigurationHandler.chunkLoadingMode == 2) {
            ChunkLoadingTools.disableMachine(this);
        }

        deinitialize();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (worldObj.isRemote) {
            return;
        }

        deinitialize();
    }

    public void deinitialize() {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            OpenComputersSharedStorage storageOC = getStorageOC(dir.ordinal());
            if (storageOC != null) {
                Node node = storageOC.getNode();
                if (node != null) {
                    node.remove();
                }
            }

            AESharedStorage storageAE = getStorageAE(dir.ordinal());
            if (storageAE != null) {
                storageAE.destroyMachineNode(entangledInstance);
                CompactMachines.instance.entangleRegistry.removeMachineTile(this);
            }
        }
    }

    public void initialize() {

        if (worldObj.isRemote) {
            return;
        }

        entangledInstance = CompactMachines.instance.entangleRegistry.registerMachineTile(this);
        this.markDirty();

        if (ConfigurationHandler.chunkLoadingMode != 0 && !ChunkLoadingTools.isCoordChunkLoaded(this)) {
            ChunkLoadingTools.forceChunkLoad(this.coords);
        }

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            OpenComputersSharedStorage storageOC = getStorageOC(dir.ordinal());
            if (storageOC != null) {
                Node node = storageOC.getNode();
                if (node != null && node.network() == null) {
                    li.cil.oc.api.Network.joinOrCreateNetwork(this);
                }
            }

            AESharedStorage storageAE = getStorageAE(dir.ordinal());
            if (storageAE != null) {
                getGridNode(dir);
            }
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!init && !isInvalid() && coords != -1) {
            initialize();
            init = true;
        }

        if (Reference.PR_AVAILABLE) {
            updateIncomingSignals();
        }

        if (!worldObj.isRemote) {
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity outside = worldObj
                    .getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                for (AbstractSharedStorage storage : SharedStorageHandler.instance(false)
                    .getAllStorages(coords, dir.ordinal())) {
                    if (!(storage instanceof AbstractHoppingStorage)) {
                        continue;
                    }

                    hopStorage(storage, outside);
                }
            }
        }
    }

    public ChunkCoordinates getChunkCoordinates() {
        int x = coords * ConfigurationHandler.cubeDistance;
        int y = 39;
        int z = 0;

        int size = (Reference.getBoxSize(meta) / 2);

        return new ChunkCoordinates(x + size, y + size, z + size);

    }

    private void hopStorage(AbstractSharedStorage storage, TileEntity outside) {
        if (storage == null || !(storage instanceof AbstractHoppingStorage)) {
            return;
        }

        AbstractHoppingStorage hoppingStorage = (AbstractHoppingStorage) storage;

        if (hoppingStorage.getHoppingMode() == 4 || (hoppingStorage.getHoppingMode() == 2
            || (hoppingStorage.getHoppingMode() == 3 && hoppingStorage.isAutoHoppingToInside() == false))) {
            hoppingStorage.hoppingTick(outside, true);
        }
    }

    private void updateDummyAirHandlers() {}

    @Override
    public int getSizeInventory() {
        return INVENTORY_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        if (coords == -1) {
            return null;
        }
        return getStorage(slotIndex).getStackInSlot(0);
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int decreaseAmount) {
        if (coords == -1) {
            return null;
        }
        return getStorage(slotIndex).decrStackSize(0, decreaseAmount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        if (coords == -1) {
            return null;
        }
        return getStorage(slotIndex).getStackInSlotOnClosing(0);
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        if (coords == -1) {
            return;
        }

        ItemSharedStorage storage = getStorage(slotIndex);
        storage.setAutoHoppingToInside(true);
        storage.setDirty();
        storage.setInventorySlotContents(0, itemStack);
    }

    @Override
    public String getInventoryName() {
        return this.hasCustomName() ? this.getCustomName() : Names.Containers.MACHINE;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.hasCustomName();
    }

    @Override
    public int getInventoryStackLimit() {
        if (coords == -1) {
            return 0;
        }
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return false;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        if (coords == -1) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side) {
        if (coords == -1) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side) {
        if (coords == -1) {
            return false;
        }
        return true;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (coords == -1) {
            return 0;
        }
        FluidSharedStorage fss = getStorageFluid(from.ordinal());
        if (doFill && resource.amount > 0) {
            fss.setAutoHoppingToInside(true);
            fss.setDirty();
        }
        return fss.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return getStorageFluid(from.ordinal()).drain(from, maxDrain, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return getStorageFluid(from.ordinal()).drain(from, resource, doDrain);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        if (coords == -1) {
            return false;
        }
        return getStorageFluid(from.ordinal()).canDrain(from, fluid);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        if (coords == -1) {
            return false;
        }
        return getStorageFluid(from.ordinal()).canFill(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return getStorageFluid(from.ordinal()).getTankInfo(from);
    }

    public FluidStack getFluid(int side) {
        return getStorageFluid(side).getFluid();
    }

    public FluidStack getFluid(ForgeDirection from) {
        return getStorageFluid(from.ordinal()).getFluid();
    }

    @Optional.Method(modid = "Mekanism")
    @Override
    public int receiveGas(ForgeDirection from, GasStack stack) {
        if (coords == -1) {
            return 0;
        }

        if (!ConfigurationHandler.enableIntegrationMekanism) {
            return 0;
        }

        GasSharedStorage gss = getStorageGas(from.ordinal());

        // XXX: Should we test with canReceiveGas first? Or do we rely on
        // suppliers to do this?
        gss.setAutoHoppingToInside(true);
        gss.setDirty();

        return gss.receiveGas(from, stack);
    }

    @Optional.Method(modid = "Mekanism")
    @Override
    public GasStack drawGas(ForgeDirection from, int amount) {
        return getStorageGas(from.ordinal()).drawGas(from, amount);
    }

    @Optional.Method(modid = "Mekanism")
    @Override
    public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer) {
        return this.receiveGas(side, stack);
    }

    @Optional.Method(modid = "Mekanism")
    @Override
    public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer) {
        return this.drawGas(side, amount);
    }

    @Optional.Method(modid = "Mekanism")
    @Override
    public boolean canReceiveGas(ForgeDirection from, Gas type) {
        return getStorageGas(from.ordinal()).canReceiveGas(from, type);
    }

    @Optional.Method(modid = "Mekanism")
    @Override
    public boolean canDrawGas(ForgeDirection from, Gas type) {
        return getStorageGas(from.ordinal()).canDrawGas(from, type);
    }

    @Optional.Method(modid = "Mekanism")
    @Override
    public boolean canTubeConnect(ForgeDirection side) {
        if (!ConfigurationHandler.enableIntegrationMekanism) {
            return false;
        }
        return true;
    }

    @Optional.Method(modid = "Mekanism")
    public GasStack getGasContents(ForgeDirection from) {
        return getStorageGas(from.ordinal()).getGasContents();
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        if (coords == -1) {
            return 0;
        }
        FluxSharedStorage fss = getStorageFlux(from.ordinal());
        if (!simulate && maxReceive > 0) {
            fss.setAutoHoppingToInside(true);
            fss.setDirty();
        }
        return fss.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        if (coords == -1) {
            return 0;
        }
        return getStorageFlux(from.ordinal()).extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        if (coords == -1) {
            return 0;
        }
        return getStorageFlux(from.ordinal()).getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        if (coords == -1) {
            return 0;
        }
        return getStorageFlux(from.ordinal()).getMaxEnergyStored();
    }

    public CMGridBlock getGridBlock(ForgeDirection dir) {
        CMGridBlock gridBlock = gridBlocks.get(dir.ordinal());
        if (gridBlock == null) {
            gridBlock = new CMGridBlock(this);
            gridBlocks.put(dir.ordinal(), gridBlock);
        }

        return gridBlock;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        if (coords == -1) {
            return null;
        }

        if (worldObj.isRemote) {
            return null;
        }

        if (!ConfigurationHandler.enableIntegrationAE2) {
            return null;
        }

        IGridNode gridNode = gridNodes.get(dir.ordinal());

        if (gridNode == null) {
            gridNode = getStorageAE(dir.ordinal()).getMachineNode(getGridBlock(dir), entangledInstance);
            gridNodes.put(dir.ordinal(), gridNode);
        }

        return gridNode;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.DENSE;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public void securityBreak() {}

    public ItemStack getItemDrop() {
        ItemStack stack = new ItemStack(ModBlocks.machine, 1, meta);

        if (isUpgraded) {
            if (stack.stackTagCompound == null) {
                stack.stackTagCompound = new NBTTagCompound();
            }
            // LogHelper.info("Dropping item stack with coords: " + coords);
            stack.stackTagCompound.setInteger("coords", coords);
        }

        if (hasCustomName()) {
            stack.setStackDisplayName(getCustomName());
        }

        return stack;
    }

    public void dropAsItem() {
        ItemStack stack = getItemDrop();

        EntityItem entityitem = new EntityItem(this.getWorldObj(), this.xCoord, this.yCoord + 0.5F, this.zCoord, stack);

        entityitem.lifespan = 1200;
        entityitem.delayBeforeCanPickup = 10;

        float f3 = 0.05F;
        entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3;
        entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + 0.2F;
        entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3;
        this.getWorldObj()
            .spawnEntityInWorld(entityitem);
    }

    @Override
    @Optional.Method(modid = "appliedenergistics2")
    public boolean prepareToMove() {
        if (isUpgraded) {
            return true;
        }
        return false;
    }

    @Override
    @Optional.Method(modid = "appliedenergistics2")
    public void doneMoving() {}

    @Optional.Method(modid = "ProjRed|Transmission")
    private void updateIncomingSignals() {
        if (!ConfigurationHandler.enableIntegrationProjectRed) {
            return;
        }

        boolean needsNotify = false;
        boolean haveChanges = false;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            BRSharedStorage br = getStorageBR(dir.ordinal());
            if (br.machineNeedsNotify) {
                // LogHelper.info("Signal into one of the interfaces changed: " + dir);
                needsNotify = true;
                br.machineNeedsNotify = false;
            }
            byte[] previous = br.machineBundledSignal;
            byte[] current = ProjectRedAPI.transmissionAPI
                .getBundledInput(worldObj, xCoord, yCoord, zCoord, dir.ordinal());

            if (current != null) {
                for (int i = 0; i < current.length; i++) {
                    if (previous[i] != current[i]) {
                        haveChanges = true;
                        previous[i] = current[i];
                    }
                }
            }
            br.setDirty();
        }

        if (haveChanges) {
            WorldServer machineWorld = MinecraftServer.getServer()
                .worldServerForDimension(ConfigurationHandler.dimensionId);
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                Vec3 pos = CubeTools.getInterfacePosition(this.coords, this.meta, dir);

                machineWorld
                    .notifyBlockChange((int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord, ModBlocks.interfaceblock);

            }
        }

        if (needsNotify || haveChanges) {
            worldObj.notifyBlockChange(xCoord, yCoord, zCoord, blockType);
        }
    }

    @Override
    @Optional.Method(modid = "ProjRed|Transmission")
    public byte[] getBundledSignal(int dir) {
        if (!ConfigurationHandler.enableIntegrationProjectRed) {
            return null;
        }

        BRSharedStorage storage = getStorageBR(dir);
        byte[] current = storage.interfaceBundledSignal;

        if (current == null) {
            return null;
        }

        byte[] result = new byte[current.length];
        for (int i = 0; i < current.length; i++) {
            // Machine-Output = Interface-Input unless Interface-Output is made by us
            int a = current[i] & 255;
            int b = storage.interfaceOutputtedSignal[i] & 255;
            int c = a;
            if (b > 0) {
                continue;
            }

            result[i] = (byte) c;
        }

        storage.machineOutputtedSignal = result;
        storage.setDirty();

        // LogHelper.info("Machine outputting to " + ForgeDirection.getOrientation(dir) + ": " + getByteString(result));

        return result;
    }

    @Override
    @Optional.Method(modid = "ProjRed|Transmission")
    public boolean canConnectBundled(int side) {
        if (!ConfigurationHandler.enableIntegrationProjectRed) {
            return false;
        }

        return true;
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public Node sidedNode(ForgeDirection side) {
        if (worldObj.isRemote) {
            return null;
        }

        if (!ConfigurationHandler.enableIntegrationOpenComputers) {
            return null;
        }

        return getStorageOC(side.ordinal()).getNode();
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public boolean canConnect(ForgeDirection side) {
        if (!ConfigurationHandler.enableIntegrationOpenComputers) {
            return false;
        }

        return true;
    }

    @Override
    @Optional.Method(modid = "Botania")
    public boolean isFull() {
        if (coords == -1) {
            return true;
        }

        return getStorageBotania(0).isFull();
    }

    @Override
    @Optional.Method(modid = "Botania")
    public void recieveMana(int mana) {
        if (coords == -1) {
            return;
        }
        getStorageBotania(0).recieveMana(mana);
    }

    @Override
    @Optional.Method(modid = "Botania")
    public boolean canRecieveManaFromBursts() {
        if (coords == -1) {
            return false;
        }
        return getStorageBotania(0).canRecieveManaFromBursts();
    }

    @Override
    @Optional.Method(modid = "Botania")
    public int getCurrentMana() {
        if (coords == -1) {
            return 0;
        }
        return getStorageBotania(0).getCurrentMana();
    }

    @Override
    @Optional.Method(modid = "Botania")
    public boolean isOutputtingPower() {
        if (coords == -1) {
            return false;
        }
        return getStorageBotania(0).isOutputtingPower();
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean isConnectable(ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return false;
        }
        return getStorageThaumcraft(face.ordinal()).isConnectable(face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean canInputFrom(ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return false;
        }
        return getStorageThaumcraft(face.ordinal()).canInputFrom(face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean canOutputTo(ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return false;
        }
        return getStorageThaumcraft(face.ordinal()).canOutputTo(face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public void setSuction(Aspect aspect, int amount) {}

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public Aspect getSuctionType(ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return null;
        }
        return getStorageThaumcraft(face.ordinal()).getSuctionType(face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int getSuctionAmount(ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return 0;
        }
        return getStorageThaumcraft(face.ordinal()).getSuctionAmount(face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int takeEssentia(Aspect aspect, int amount, ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return 0;
        }
        return getStorageThaumcraft(face.ordinal()).takeEssentia(aspect, amount, face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int addEssentia(Aspect aspect, int amount, ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return 0;
        }
        return getStorageThaumcraft(face.ordinal()).addEssentia(aspect, amount, face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public Aspect getEssentiaType(ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return null;
        }
        return getStorageThaumcraft(face.ordinal()).getEssentiaType(face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int getEssentiaAmount(ForgeDirection face) {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return 0;
        }
        return getStorageThaumcraft(face.ordinal()).getEssentiaAmount(face);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int getMinimumSuction() {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return Integer.MAX_VALUE;
        }
        return getStorageThaumcraft(0).getMinimumSuction();
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean renderExtendedTube() {
        if (!ConfigurationHandler.enableIntegrationThaumcraft || coords == -1) {
            return false;
        }
        return getStorageThaumcraft(0).renderExtendedTube();
    }

    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        return;
    }

    private static String roundNumberTo(double value, int decimals) {
        double ret = roundNumberToDouble(value, decimals);
        if (decimals == 0) {
            return "" + (int) ret;
        } else {
            return "" + ret;
        }
    }

    private static double roundNumberToDouble(double value, int decimals) {
        return Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
    }
}
