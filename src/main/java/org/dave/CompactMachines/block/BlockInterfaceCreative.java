package org.dave.compactmachines.block;

import org.dave.compactmachines.creativetab.CreativeTabCM;
import org.dave.compactmachines.reference.Names;

public class BlockInterfaceCreative extends BlockProtected {

    public BlockInterfaceCreative() {
        super();
        this.setBlockName(Names.Blocks.INTERFACE_CREATIVE);
        this.setBlockTextureName(Names.Blocks.INTERFACE);
        this.setCreativeTab(CreativeTabCM.CM_TAB);
    }
}
