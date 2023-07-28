package org.dave.compactmachines.block;

import org.dave.compactmachines.creativetab.CreativeTabCM;
import org.dave.compactmachines.reference.Names;

public class BlockInterfaceDecorative extends BlockCM {

    public BlockInterfaceDecorative() {
        super();
        this.setBlockName(Names.Blocks.INTERFACE_DECORATIVE);
        this.setBlockTextureName(Names.Blocks.INTERFACE);
        this.setHardness(8.0F);
        this.setResistance(20.0F);
        this.setCreativeTab(CreativeTabCM.CM_TAB);
    }
}
