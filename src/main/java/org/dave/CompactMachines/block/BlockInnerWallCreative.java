package org.dave.compactmachines.block;

import org.dave.compactmachines.creativetab.CreativeTabCM;
import org.dave.compactmachines.reference.Names;

public class BlockInnerWallCreative extends BlockProtected {

    public BlockInnerWallCreative() {
        super();
        this.setBlockName(Names.Blocks.INNERWALL_CREATIVE);
        this.setBlockTextureName(Names.Blocks.INNERWALL);
        this.setCreativeTab(CreativeTabCM.CM_TAB);
    }
}
