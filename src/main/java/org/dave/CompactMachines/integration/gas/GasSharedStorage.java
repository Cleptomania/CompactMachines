package org.dave.compactmachines.integration.gas;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.compactmachines.handler.ConfigurationHandler;
import org.dave.compactmachines.handler.SharedStorageHandler;
import org.dave.compactmachines.integration.AbstractHoppingStorage;
import org.dave.compactmachines.reference.Reference;

import cpw.mods.fml.common.Optional;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;

@Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "Mekanism")
public class GasSharedStorage extends AbstractHoppingStorage implements IGasHandler {

    private ExtendedGasTank tank;

    public GasSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
        super(storageHandler, coord, side);

        if (Reference.MEK_AVAILABLE) {
            tank = new ExtendedGasTank() {

                @Override
                public void onGasChanged() {
                    setDirty();
                }
            };
        }

        max_cooldown = ConfigurationHandler.cooldownGas;
    }

    public GasStack getGasContents() {
        if (!ConfigurationHandler.enableIntegrationMekanism) {
            return null;
        }

        GasStack gas = tank.getGas();

        if (gas != null) {
            // Return a copy so tank contents cannot be externally modified
            gas = gas.copy();
        }

        return gas;
    }

    @Override
    public int receiveGas(ForgeDirection side, GasStack stack) {
        if (!ConfigurationHandler.enableIntegrationMekanism) {
            return 0;
        }

        return tank.receive(stack, true);
    }

    @Override
    public GasStack drawGas(ForgeDirection side, int amount) {
        if (!ConfigurationHandler.enableIntegrationMekanism) {
            return null;
        }
        return tank.draw(amount, true);
    }

    @Override
    public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer) {
        return this.receiveGas(side, stack);
    }

    @Override
    public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer) {
        return this.drawGas(side, amount);
    }

    @Override
    public boolean canReceiveGas(ForgeDirection side, Gas gas) {
        if (!ConfigurationHandler.enableIntegrationMekanism) {
            return false;
        }
        return tank.canReceive(gas);
    }

    @Override
    public boolean canDrawGas(ForgeDirection side, Gas gas) {
        if (!ConfigurationHandler.enableIntegrationMekanism) {
            return false;
        }
        return tank.canDraw(gas);
    }

    @Override
    public String type() {
        return "gas";
    }

    @Override
    public NBTTagCompound saveToTag() {
        NBTTagCompound compound = super.saveToTag();
        compound.setTag("tank", tank.write(new NBTTagCompound()));

        return compound;
    }

    @Override
    public void loadFromTag(NBTTagCompound tag) {
        super.loadFromTag(tag);
        tank.read(tag.getCompoundTag("tank"));
    }

    @Override
    public void hopToTileEntity(TileEntity te, boolean opposite) {
        GasStack stack = tank.getGas();

        if (stack == null || stack.amount == 0) {
            return;
        }

        stack = stack.copy();

        if (te instanceof IGasHandler) {
            IGasHandler gh = (IGasHandler) te;

            ForgeDirection hoppingSide = ForgeDirection.getOrientation(side);

            if (opposite) {
                hoppingSide = hoppingSide.getOpposite();
            }

            if (gh.canReceiveGas(hoppingSide, stack.getGas())) {
                int received = gh.receiveGas(hoppingSide, stack, true);

                if (received > 0) {
                    this.tank.draw(received, true);

                    te.markDirty();
                }
            }

        }

    }
}
