package org.dave.compactmachines.utility;

import net.minecraft.util.ResourceLocation;

import org.dave.compactmachines.reference.Reference;

public class ResourceLocationHelper {

    public static ResourceLocation getResourceLocation(String modId, String path) {
        return new ResourceLocation(modId, path);
    }

    public static ResourceLocation getResourceLocation(String path) {
        return getResourceLocation(Reference.MOD_ID.toLowerCase(), path);
    }
}
