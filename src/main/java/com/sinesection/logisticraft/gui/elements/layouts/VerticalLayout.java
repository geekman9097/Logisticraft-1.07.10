package com.sinesection.logisticraft.gui.elements.layouts;

import com.sinesection.logisticraft.api.gui.IGuiElement;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VerticalLayout extends AbstractElementLayout {
	public VerticalLayout(int width) {
		this(0, 0, width);
	}

	public VerticalLayout(int xPos, int yPos, int width) {
		super(xPos, yPos, width, 0);
	}

	public <E extends IGuiElement> E add(E element) {
		elements.add(element);
		element.setParent(this);
		element.setYPosition(height);
		setHeight(height + (element.getHeight() + distance));
		element.onCreation();
		return element;
	}

	public <E extends IGuiElement> E remove(E element) {
		elements.remove(element);
		setHeight(height - (element.getHeight() + distance));
		element.setYPosition(0);
		element.onDeletion();
		return element;
	}

	public void layout() {
		height = 0;
		for (IGuiElement element : elements) {
			element.setYPosition(height);
			setHeight(height + (element.getHeight() + distance));
		}
	}

	@Override
	public int getWidth() {
		if (width > 0) {
			return width;
		}
		int width = 0;
		for (IGuiElement element : elements) {
			int elementWidth = element.getWidth();
			if (elementWidth > width) {
				width = elementWidth;
			}
		}
		return width;
	}
}