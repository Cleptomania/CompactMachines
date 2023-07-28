package org.dave.compactmachines.block;

import org.dave.compactmachines.creativetab.CreativeTabCM;
import org.dave.compactmachines.reference.Names;

public class BlockResizingCube extends BlockCM {

    public BlockResizingCube() {
        super();
        this.setBlockName(Names.Blocks.RESIZINGCUBE);
        this.setBlockTextureName(Names.Blocks.RESIZINGCUBE);
        this.setHardness(16.0F);
        this.setResistance(20.0F);
        this.setCreativeTab(CreativeTabCM.CM_TAB);
    }
}
