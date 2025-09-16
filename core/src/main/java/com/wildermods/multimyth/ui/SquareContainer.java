package com.wildermods.multimyth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

public class SquareContainer<T extends Actor> extends WidgetGroup {
	private final T child;

	public SquareContainer(T child) {
		this.child = child;
		addActor(child);
	}

	@Override
	public void layout() {
		super.layout();
		float size = Math.min(getWidth(), getHeight());
		child.setBounds(0, 0, size, size);
	}

}
