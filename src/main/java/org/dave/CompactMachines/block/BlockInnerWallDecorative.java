package org.dave.compactmachines.block;

import org.dave.compactmachines.creativetab.CreativeTabCM;
import org.dave.compactmachines.reference.Names;

public class BlockInnerWallDecorative extends BlockCM {

    public BlockInnerWallDecorative() {
        super();
        this.setBlockName(Names.Blocks.INNERWALL_DECORATIVE);
        this.setBlockTextureName(Names.Blocks.INNERWALL);
        this.setHardness(8.0F);
        this.setResistance(20.0F);
        this.setCreativeTab(CreativeTabCM.CM_TAB);
    }

}
