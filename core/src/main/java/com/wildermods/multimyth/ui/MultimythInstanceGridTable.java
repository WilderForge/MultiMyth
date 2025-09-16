package com.wildermods.multimyth.ui;

import java.util.Collection;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class MultimythInstanceGridTable extends MultimythTable {

	public MultimythInstanceGridTable(Collection<Actor> actors, Skin skin) {
		super(skin);
		super.add(actors.toArray(new Actor[]{}));
	}

	
	
}
