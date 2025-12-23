package com.wildermods.multimyth.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class MultimythWindow extends Window {

	public MultimythWindow(String title, Skin skin) {
		super(title, skin);
	}

	public void centerOnStage(Stage stage) {
	    pack(); // ensures width/height are computed

	    setHeight(stage.getHeight() / 2);
	    setWidth(stage.getWidth() / 4);
	    
	    float x = Math.round((stage.getWidth() - getWidth()) / 2f);
	    float y = Math.round((stage.getHeight() - getHeight()) / 2f);
	    
	    setPosition(x, y);
	    invalidateHierarchy();
	    validate();
	}
	
}
