package org.dave.compactmachines.init;

import org.dave.compactmachines.item.ItemAtomEnlarger;
import org.dave.compactmachines.item.ItemAtomShrinker;
import org.dave.compactmachines.item.ItemEntangler;
import org.dave.compactmachines.item.ItemInterface;
import org.dave.compactmachines.item.ItemPersonalShrinkingDevice;
import org.dave.compactmachines.reference.Names;
import org.dave.compactmachines.reference.Reference;

import cpw.mods.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModItems {

    public static final ItemPersonalShrinkingDevice psd = new ItemPersonalShrinkingDevice();
    public static final ItemAtomShrinker shrinker = new ItemAtomShrinker();
    public static final ItemAtomEnlarger enlarger = new ItemAtomEnlarger();
    public static final ItemInterface interfaceitem = new ItemInterface();
    public static final ItemEntangler quantumentangler = new ItemEntangler();

    public static void init() {
        GameRegistry.registerItem(psd, Names.Items.PSD);
        GameRegistry.registerItem(shrinker, Names.Items.SHRINKER);
        GameRegistry.registerItem(enlarger, Names.Items.ENLARGER);
        GameRegistry.registerItem(interfaceitem, Names.Items.INTERFACEITEM);
        GameRegistry.registerItem(quantumentangler, Names.Items.QUANTUMENTANGLER);
    }
}
