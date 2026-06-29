package com.joshuastar.world;

public class Inventory {

    public static final int HOTBAR_SIZE = 9;
    public static final int MAIN_SIZE = 27;
    public static final int TOTAL_SIZE = HOTBAR_SIZE + MAIN_SIZE;

    private final short[] itemId = new short[TOTAL_SIZE];
    private final int[] itemCount = new int[TOTAL_SIZE];

    private int selectedHotbarSlot = 0;

    public Inventory() {
        itemId[0] = 1;
        itemCount[0] = 64;
        itemId[1] = 2;
        itemCount[1] = 64;
        itemId[2] = 3;
        itemCount[2] = 64;
        itemId[3] = 7;
        itemCount[3] = 64;
        itemId[4] = 8;
        itemCount[4] = 64;
    }

    public short getItemId(int slot) {
        return itemId[slot];
    }

    public int getItemCount(int slot) {
        return itemCount[slot];
    }

    public void setSlot(int slot, short id, int count) {
        itemId[slot] = id;
        itemCount[slot] = count;
    }

    public int getSelectedHotbarSlot() {
        return selectedHotbarSlot;
    }

    public void setSelectedHotbarSlot(int slot) {
        if (slot < 0 || slot >= HOTBAR_SIZE) {
            return;
        }
        selectedHotbarSlot = slot;
    }

    public short getSelectedItemId() {
        return itemId[selectedHotbarSlot];
    }
}