package org.dave.compactmachines.integration.opencomputers;

import org.dave.compactmachines.handler.SharedStorageHandler;
import org.dave.compactmachines.integration.AbstractSharedStorage;

import cpw.mods.fml.common.Optional;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")
public class OpenComputersSharedStorage extends AbstractSharedStorage implements Environment {

    public Node node;

    public OpenComputersSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
        super(storageHandler, coord, side);

        this.side = side;
        this.coord = coord;
    }

    public Node getNode() {
        if (node == null) {
            // LogHelper.info("Creating new node for side: " + ForgeDirection.getOrientation(side));
            node = li.cil.oc.api.Network.newNode(this, Visibility.Network)
                .withConnector()
                .create();
        }
        return node;
    }

    @Override
    public String type() {
        return "OpenComputers";
    }

    @Override
    public Node node() {
        return getNode();
    }

    @Override
    public void onConnect(Node node) {}

    @Override
    public void onDisconnect(Node node) {}

    @Override
    public void onMessage(Message message) {}
}
