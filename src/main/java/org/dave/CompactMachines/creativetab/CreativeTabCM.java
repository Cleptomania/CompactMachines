package org.dave.compactmachines.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import org.dave.compactmachines.init.ModItems;
import org.dave.compactmachines.reference.Reference;

public class CreativeTabCM {

    public static final CreativeTabs CM_TAB = new CreativeTabs(Reference.MOD_ID.toLowerCase()) {

        @Override
        public Item getTabIconItem() {
            return ModItems.psd;
        }
    };
}
