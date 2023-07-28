package org.dave.compactmachines.init;

import org.dave.compactmachines.block.BlockCM;
import org.dave.compactmachines.block.BlockInnerWall;
import org.dave.compactmachines.block.BlockInnerWallCreative;
import org.dave.compactmachines.block.BlockInnerWallDecorative;
import org.dave.compactmachines.block.BlockInterface;
import org.dave.compactmachines.block.BlockInterfaceCreative;
import org.dave.compactmachines.block.BlockInterfaceDecorative;
import org.dave.compactmachines.block.BlockMachine;
import org.dave.compactmachines.block.BlockResizingCube;
import org.dave.compactmachines.item.ItemBlockMachine;
import org.dave.compactmachines.reference.Names;
import org.dave.compactmachines.reference.Reference;

import cpw.mods.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks {

    public static final BlockCM machine = new BlockMachine();
    public static final BlockCM interfaceblock = new BlockInterface();
    public static final BlockCM interfaceblockdecor = new BlockInterfaceDecorative();
    public static final BlockCM interfaceblockcreative = new BlockInterfaceCreative();
    public static final BlockCM innerwall = new BlockInnerWall();
    public static final BlockCM innerwalldecor = new BlockInnerWallDecorative();
    public static final BlockCM innerwallcreative = new BlockInnerWallCreative();
    public static final BlockCM resizingcube = new BlockResizingCube();

    public static void init() {
        GameRegistry.registerBlock(machine, ItemBlockMachine.class, Names.Blocks.MACHINE);
        GameRegistry.registerBlock(interfaceblock, Names.Blocks.INTERFACE);
        GameRegistry.registerBlock(innerwall, Names.Blocks.INNERWALL);
        GameRegistry.registerBlock(innerwalldecor, Names.Blocks.INNERWALL_DECORATIVE);
        GameRegistry.registerBlock(resizingcube, Names.Blocks.RESIZINGCUBE);

        GameRegistry.registerBlock(interfaceblockdecor, Names.Blocks.INTERFACE_DECORATIVE);
        GameRegistry.registerBlock(interfaceblockcreative, Names.Blocks.INTERFACE_CREATIVE);
        GameRegistry.registerBlock(innerwallcreative, Names.Blocks.INNERWALL_CREATIVE);
    }
}
