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
	
}
