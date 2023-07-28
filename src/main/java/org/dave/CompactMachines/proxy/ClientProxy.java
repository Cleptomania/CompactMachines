package org.dave.compactmachines.proxy;

import net.minecraftforge.client.MinecraftForgeClient;

import org.dave.compactmachines.client.render.RenderPersonalShrinkingDevice;
import org.dave.compactmachines.handler.ConfigurationHandler;
import org.dave.compactmachines.init.ModItems;
import org.dave.compactmachines.reference.Textures;

import cpw.mods.fml.common.registry.VillagerRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerVillagerSkins() {
        VillagerRegistry.instance()
            .registerVillagerSkin(ConfigurationHandler.villagerId, Textures.Entities.VILLAGER);
    }

    @Override
    public void registerRenderers() {
        MinecraftForgeClient.registerItemRenderer(ModItems.psd, new RenderPersonalShrinkingDevice());
    }

    @Override
    public boolean isClient() {
        return true;
    }
}
