package com.wildermods.multimyth.ui;

import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class GameInstanceButton extends ImageTextButton {
	
	public GameInstanceButton(String text, Skin skin) {
		this(text, skin.get(ImageTextButtonStyle.class));
		setSkin(skin);
		this.debugAll();
	}
	
	public GameInstanceButton(String text, Skin skin, String styleName) {
		this(text, skin.get(styleName, ImageTextButtonStyle.class));
		this.debugAll();
	}
	
	public GameInstanceButton(String text, ImageTextButtonStyle style) {
		super(text, style);
		this.reset();
		this.add(getImage()).expand().fill();
		this.row();
		this.add(getLabel());
	}
	/*
	@Override
	public float getPrefWidth() {
	float imageW = (getImage() != null) ? getImage().getPrefWidth() : 0f;
	float labelW = (getLabel() != null) ? getLabel().getPrefWidth() : 0f;
	return Math.max(imageW, labelW);
	}

	@Override
	public float getPrefHeight() {
	float imageH = (getImage() != null) ? getImage().getPrefHeight() : 0f;
	float labelH = (getLabel() != null) ? getLabel().getPrefHeight() : 0f;
	return imageH + labelH;
	}*/
	
}
