package com.sinesection.logisticraft.gui.buttons;

import net.minecraft.util.ResourceLocation;

public enum StandardButtonTextureSets implements IButtonTextureSet {
	LARGE_BUTTON(0, 0, 20, 200),
	SMALL_BUTTON(0, 80, 15, 200),
	LOCKED_BUTTON(224, 0, 16, 16),
	UNLOCKED_BUTTON(240, 0, 16, 16),
	SMALL_BLANK_BUTTON(208, 48, 16, 16),
	ARROW_DOWN_BUTTON(224, 48, 16, 16),
	ARROW_UP_BUTTON(240, 48, 16, 16),
	DOWN_BUTTON(224, 96, 10, 16),
	UP_BUTTON(240, 96, 10, 16),
	LEFT_BUTTON(204, 0, 16, 10),
	RIGHT_BUTTON(214, 0, 16, 10),
	LEFT_BUTTON_SMALL(238, 220, 12, 9),
	RIGHT_BUTTON_SMALL(247, 220, 12, 9);
	private final int x, y, height, width;

	StandardButtonTextureSets(int x, int y, int height, int width) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public ResourceLocation getTexture() {
		return GuiBetterButton.TEXTURE;
	}
}