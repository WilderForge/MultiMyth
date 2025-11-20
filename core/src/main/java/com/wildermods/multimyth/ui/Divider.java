package com.wildermods.multimyth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.graphics.g2d.Batch;

public class Divider extends Widget {

	public enum Orientation {
		HORIZONTAL, VERTICAL
	}

	private final Orientation orientation;
	private final Color color;
	private final float thickness;

	/**
	 * Creates a colored divider.
	 *
	 * @param color	   The color of the divider.
	 * @param thickness   The thickness in pixels (height for horizontal, width for vertical)
	 * @param orientation Orientation.HORIZONTAL or Orientation.VERTICAL
	 */
	public Divider(Orientation orientation, Color color, float thickness) {
		this.color = new Color(color);
		this.orientation = orientation;
		this.thickness = thickness;
	}
	
	@Override
	public float getPrefWidth() {
	    return orientation == Orientation.VERTICAL ? thickness : 1;
	}

	@Override
	public float getPrefHeight() {
	    return orientation == Orientation.HORIZONTAL ? thickness : 1;
	}
	
	public Divider(Orientation orientation) {
		this(orientation, Color.WHITE, 2);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		Color old = batch.getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		batch.draw(whitePixel(), getX(), getY(), getWidth(), getHeight());
		batch.setColor(old);
	}

	// A 1x1 white pixel texture. Lazy-load it once.
	private static Texture white;
	private static Texture whitePixel() {
		if (white == null) {
			white = new Texture(1, 1, Pixmap.Format.RGBA8888);
			Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
			pixmap.setColor(Color.WHITE);
			pixmap.fill();
			white.draw(pixmap, 0, 0);
			pixmap.dispose();
		}
		return white;
	}
}
