package org.dave.compactmachines.proxy;

public interface IProxy {

    public abstract void registerTileEntities();

    public abstract void registerHandlers();

    public abstract void registerVillagerSkins();

    public abstract void registerRenderers();

    public abstract boolean isClient();
}
