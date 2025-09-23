package com.wildermods.multimyth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AutoFocusingScrollPane extends ScrollPane {

	public AutoFocusingScrollPane(Actor actor, Skin skin) {
		super(actor, skin);
		this.addListener(new InputListener() {
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				getStage().setScrollFocus(AutoFocusingScrollPane.this);
			}
			
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				getStage().setScrollFocus(AutoFocusingScrollPane.this);
			}
		});
	}
	
}
