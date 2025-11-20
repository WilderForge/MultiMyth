package com.wildermods.multimyth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIHelper {

	public static Drawable createSolidColorDrawable(Color color) {
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(color);
		pixmap.fill();
		Texture texture = new Texture(pixmap);
		pixmap.dispose();
		return new TextureRegionDrawable(new TextureRegion(texture));
	}
	
	public static SpriteDrawable createSolidColorDrawable(Drawable base, Color color) {
		// Use min width/height for Pixmap size
		int width = Math.max(1, (int) base.getMinWidth());
		int height = Math.max(1, (int) base.getMinHeight());

		Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		pixmap.setColor(color);
		pixmap.fill();

		Texture texture = new Texture(pixmap);
		pixmap.dispose();

		Sprite sprite = new Sprite(texture);
		SpriteDrawable drawable = new SpriteDrawable(sprite);

		// Copy all sizing values from base
		drawable.setLeftWidth(base.getLeftWidth());
		drawable.setRightWidth(base.getRightWidth());
		drawable.setTopHeight(base.getTopHeight());
		drawable.setBottomHeight(base.getBottomHeight());
		drawable.setMinWidth(base.getMinWidth());
		drawable.setMinHeight(base.getMinHeight());

		return drawable;
	}
	
}
