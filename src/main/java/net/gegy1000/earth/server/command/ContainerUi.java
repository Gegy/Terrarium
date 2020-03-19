package net.gegy1000.earth.server.command;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class ContainerUi {
    public static final int WIDTH = 9;
    public static final int PADDING = 1;
    public static final int PADDED_WIDTH = WIDTH - PADDING * 2;

    private final EntityPlayerMP player;
    private final ITextComponent title;
    private final Element[] elements;
    private final int rows;

    private ContainerUi(EntityPlayerMP player, ITextComponent title, Element[] elements) {
        this.player = player;
        this.title = title;
        this.elements = elements;
        this.rows = MathHelper.ceil((double) elements.length / PADDED_WIDTH);
        this.resolveRows();
    }

    public static Builder builder(EntityPlayerMP player) {
        return new Builder(player);
    }

    private void resolveRows() {
        for (int row = 0; row < this.rows; row++) {
            Element[] resolved = this.resolveRow(row);
            int minColumn = (WIDTH - resolved.length) / 2;
            for (int column = 0; column < resolved.length; column++) {
                resolved[column].setPosition(row, column + minColumn);
            }
        }
    }

    private Element[] resolveRow(int row) {
        int minId = Integer.MAX_VALUE;
        int maxId = Integer.MIN_VALUE;
        int rowStart = row * PADDED_WIDTH;
        int rowEnd = Math.min(rowStart + PADDED_WIDTH, this.elements.length);
        for (int id = rowStart; id < rowEnd; id++) {
            if (this.elements[id] != null) {
                if (id < minId) {
                    minId = id;
                }
                if (id > maxId) {
                    maxId = id;
                }
            }
        }
        Element[] resolved = new Element[(maxId - minId) + 1];
        System.arraycopy(this.elements, minId, resolved, 0, resolved.length);
        return resolved;
    }

    public ContainerUiInventory createInventory() {
        return new ContainerUiInventory(this.player, this.title, this.elements, this.rows);
    }

    public static class Builder {
        private final EntityPlayerMP player;
        private final List<Element> elements = new ArrayList<>();
        private ITextComponent title = new TextComponentString("Container");

        private Builder(EntityPlayerMP player) {
            this.player = player;
        }

        public Builder addElement(Item icon, String name, Runnable clickHandler) {
            Element element = new Element(this.elements.size(), icon, name, clickHandler);
            this.elements.add(element);
            return this;
        }

        public Builder addElement(Block icon, String name, Runnable clickHandler) {
            return this.addElement(Item.getItemFromBlock(icon), name, clickHandler);
        }

        public Builder setTitle(ITextComponent title) {
            this.title = title;
            return this;
        }

        public ContainerUi build() {
            return new ContainerUi(this.player, this.title, this.elements.toArray(new Element[0]));
        }
    }

    public static class Element {
        private final int id;
        private final Item icon;
        private final String name;
        private final Runnable clickHandler;

        private int row;
        private int column;

        private Element(int id, Item icon, String name, Runnable clickHandler) {
            this.id = id;
            this.icon = icon;
            this.name = name;
            this.clickHandler = clickHandler;
        }

        public void setPosition(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getId() {
            return this.id;
        }

        public int getRow() {
            return this.row;
        }

        public int getColumn() {
            return this.column;
        }

        public ItemStack createStack() {
            ItemStack stack = new ItemStack(this.icon, 1);
            stack.setStackDisplayName(this.name);
            return stack;
        }

        public void handleClick() {
            this.clickHandler.run();
        }
    }
}
