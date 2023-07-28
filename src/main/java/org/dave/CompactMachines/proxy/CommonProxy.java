package org.dave.compactmachines.proxy;

import org.dave.compactmachines.reference.Names;
import org.dave.compactmachines.tileentity.TileEntityInterface;
import org.dave.compactmachines.tileentity.TileEntityMachine;

import cpw.mods.fml.common.registry.GameRegistry;

public abstract class CommonProxy implements IProxy {

    @Override
    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityMachine.class, Names.Blocks.MACHINE);
        GameRegistry.registerTileEntity(TileEntityInterface.class, Names.Blocks.INTERFACE);
    }

    @Override
    public void registerHandlers() {}

    @Override
    public void registerVillagerSkins() {}

    @Override
    public void registerRenderers() {}

    @Override
    public boolean isClient() {
        return false;
    }
}
