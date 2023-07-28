package org.dave.compactmachines.integration.bundledredstone;

import net.minecraft.nbt.NBTTagCompound;

import org.dave.compactmachines.handler.SharedStorageHandler;
import org.dave.compactmachines.integration.AbstractBufferedStorage;

public class BRSharedStorage extends AbstractBufferedStorage {

    public int coord;
    public int side;

    public boolean machineNeedsNotify = false;

    public byte[] machineBundledSignal = new byte[16];
    public byte[] machineOutputtedSignal = new byte[16];

    public byte[] interfaceBundledSignal = new byte[16];
    public byte[] interfaceOutputtedSignal = new byte[16];

    public BRSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
        super(storageHandler, coord, side);

        this.side = side;
        this.coord = coord;
    }

    @Override
    public String type() {
        return "bundledRedstone";
    }

    @Override
    public NBTTagCompound saveToTag() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setByteArray("machineBundledSignal", machineBundledSignal);
        compound.setByteArray("machineOutputtedSignal", machineOutputtedSignal);
        compound.setByteArray("interfaceBundledSignal", interfaceBundledSignal);
        compound.setByteArray("interfaceOutputtedSignal", interfaceOutputtedSignal);
        return compound;
    }

    @Override
    public void loadFromTag(NBTTagCompound tag) {
        machineBundledSignal = tag.getByteArray("machineBundledSignal");
        machineOutputtedSignal = tag.getByteArray("machineOutputtedSignal");
        interfaceBundledSignal = tag.getByteArray("interfaceBundledSignal");
        interfaceOutputtedSignal = tag.getByteArray("interfaceOutputtedSignal");

        if (machineBundledSignal == null || machineBundledSignal.length < 16) {
            machineBundledSignal = new byte[16];
        }

        if (machineOutputtedSignal == null || machineOutputtedSignal.length < 16) {
            machineOutputtedSignal = new byte[16];
        }

        if (interfaceBundledSignal == null || interfaceBundledSignal.length < 16) {
            interfaceBundledSignal = new byte[16];
        }

        if (interfaceOutputtedSignal == null || interfaceOutputtedSignal.length < 16) {
            interfaceOutputtedSignal = new byte[16];
        }
    }

    private static String getByteString(byte[] arr) {
        String resString = "[";
        for (int i = 0; i < arr.length; i++) {
            resString += (arr[i] & 255) + ",";
        }
        return resString.substring(0, resString.length() - 1) + "]";
    }

}
