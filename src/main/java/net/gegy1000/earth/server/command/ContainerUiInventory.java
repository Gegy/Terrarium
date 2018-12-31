package net.gegy1000.earth.server.command;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextComponent;
import net.minecraft.util.DefaultedList;

public class ContainerUiInventory implements Inventory {
    private final ServerPlayerEntity player;
    private final TextComponent title;

    private final Int2ObjectMap<ContainerUi.Element> elements;
    private final int rows;

    private final DefaultedList<ItemStack> stacks;

    public ContainerUiInventory(ServerPlayerEntity player, TextComponent title, ContainerUi.Element[] elements, int rows) {
        this.player = player;
        this.title = title;
        this.rows = rows;

        this.stacks = DefaultedList.create(this.getInvSize(), ItemStack.EMPTY);

        this.elements = new Int2ObjectOpenHashMap<>();
        for (ContainerUi.Element element : elements) {
            int slot = element.getColumn() + element.getRow() * ContainerUi.WIDTH;
            this.elements.put(slot, element);
            this.stacks.set(slot, element.createStack());
        }
    }

    @Override
    public int getInvSize() {
        return this.rows * ContainerUi.WIDTH;
    }

    @Override
    public boolean isInvEmpty() {
        return false;
    }

    @Override
    public ItemStack getInvStack(int index) {
        return this.stacks.get(index);
    }

    @Override
    public ItemStack takeInvStack(int index, int count) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeInvStack(int index) {
        this.handleElementClick(index);
        return ItemStack.EMPTY;
    }

    private void handleElementClick(int index) {
        this.player.inventory.setCursorStack(ItemStack.EMPTY);
        ContainerUi.Element element = this.elements.get(index);
        if (element != null) {
            this.player.closeGui();
            element.handleClick();
        }
    }

    @Override
    public void setInvStack(int index, ItemStack stack) {
    }

    @Override
    public int getInvMaxStackAmount() {
        return 1;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity player) {
        return true;
    }

    @Override
    public void onInvOpen(PlayerEntity player) {
    }

    @Override
    public void onInvClose(PlayerEntity player) {
    }

    @Override
    public boolean isValidInvStack(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getInvProperty(int id) {
        return 0;
    }

    @Override
    public void setInvProperty(int id, int value) {
    }

    @Override
    public int getInvPropertyCount() {
        return 0;
    }

    @Override
    public void clearInv() {
    }

    @Override
    public TextComponent getName() {
        return this.title;
    }
}
