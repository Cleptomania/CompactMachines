package org.dave.compactmachines.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.dave.compactmachines.creativetab.CreativeTabCM;
import org.dave.compactmachines.reference.Reference;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCM extends Item {

    public ItemCM() {
        super();
        this.setCreativeTab(CreativeTabCM.CM_TAB);
    }

    @Override
    public String getUnlocalizedName() {
        return String.format(
            "item.%s%s",
            Reference.MOD_ID.toLowerCase() + ":",
            getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return String.format(
            "item.%s%s",
            Reference.MOD_ID.toLowerCase() + ":",
            getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        itemIcon = iconRegister.registerIcon(
            this.getUnlocalizedName()
                .substring(
                    this.getUnlocalizedName()
                        .indexOf(".") + 1));
    }

    protected String getUnwrappedUnlocalizedName(String unlocalizedName) {
        return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
    }
}
