package org.dave.compactmachines.network;

import net.minecraftforge.common.DimensionManager;

import org.dave.compactmachines.handler.ConfigurationHandler;
import org.dave.compactmachines.machines.world.WorldProviderMachines;
import org.dave.compactmachines.utility.LogHelper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageConfiguration implements IMessage, IMessageHandler<MessageConfiguration, IMessage> {

    public MessageConfiguration() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        LogHelper.info("Receiving configuration from server");
        ConfigurationHandler.isServerConfig = true;
        ConfigurationHandler.dimensionId = buf.readInt();
        ConfigurationHandler.capacityRF = buf.readInt();
        ConfigurationHandler.capacityFluid = buf.readInt();
        ConfigurationHandler.capacityGas = buf.readInt();
        ConfigurationHandler.capacityMana = buf.readInt();
        ConfigurationHandler.allowRespawning = buf.readBoolean();

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ConfigurationHandler.dimensionId);
        buf.writeInt(ConfigurationHandler.capacityRF);
        buf.writeInt(ConfigurationHandler.capacityFluid);
        buf.writeInt(ConfigurationHandler.capacityGas);
        buf.writeInt(ConfigurationHandler.capacityMana);
        buf.writeBoolean(ConfigurationHandler.allowRespawning);
    }

    @Override
    public IMessage onMessage(MessageConfiguration message, MessageContext ctx) {
        LogHelper.info("Registering dimension " + ConfigurationHandler.dimensionId + " on client side");
        DimensionManager.registerProviderType(ConfigurationHandler.dimensionId, WorldProviderMachines.class, true);
        DimensionManager.registerDimension(ConfigurationHandler.dimensionId, ConfigurationHandler.dimensionId);
        return null;
    }

}
