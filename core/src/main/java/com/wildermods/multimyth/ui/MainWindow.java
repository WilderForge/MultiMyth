package com.wildermods.multimyth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.wildermods.multimyth.internal.CompileStrictly;

@CompileStrictly
public class MainWindow extends MultimythTable {
	
	private MultimythTable topPanel;
	private MultimythTable sidePanel;
	private MultimythTable mainPanel;
	private MultimythTable bottomPanel;
	
	private Cell<MultimythTable> topPanelCell;
	private Cell<MultimythTable> sidePanelCell;
	private Cell<MultimythTable> mainPanelCell;
	private Cell<MultimythTable> bottomPanelCell;
	
	public MainWindow(Skin skin) {
		super(skin);
		debug();
		setSkin(skin);
		
		topPanel = new MultimythTable(skin);
		sidePanel = new MultimythTable(skin);
		mainPanel = new MultimythTable(skin);
		bottomPanel = new MultimythTable(skin);
		
        setFillParent(true);
        pad(16f);
		this.setBackground(skin.getDrawable("window"));
		
		sidePanel = new MultimythTable(skin);
		
		topPanelCell = this.add(topPanel).colspan(2);
		topPanelCell.expandX().fill().row();
		addTopPanelStuff(topPanel);

		topPanel.setBackground(createSolidColorDrawable(Color.RED));
		
		
		this.add(mainPanel).expand().fill();
		mainPanel.setBackground(createSolidColorDrawable(Color.WHITE));
		
		this.add(sidePanel).width(Value.percentWidth(0.1f, this)).fillY();
		sidePanel.background(createSolidColorDrawable(Color.BLUE));
		
		this.row();
		
		this.add(bottomPanel).colspan(2).expandX().fill();
		bottomPanel.background(createSolidColorDrawable(Color.GREEN));
		addBottomPanelStuff(bottomPanel);


	}
	
	@Override
	public void act(float delta) {
	    super.act(delta);
	}
	
	private void addTopPanelStuff(MultimythTable topPanel) {
		topPanel.add(new ImageTextButton("New Instance", getSkin()).pad(5f)).space(5f);
		topPanel.add(new ImageTextButton("Folders", getSkin()).pad(5f)).space(5f);
		topPanel.add(new ImageTextButton("Settings", getSkin()).pad(5f)).space(5f);
		topPanel.add(new ImageTextButton("Update", getSkin()).pad(5f)).space(5f);
		topPanel.add(new ImageTextButton("Help", getSkin()).pad(5f)).space(5f);
		topPanel.add().expandX().fillX();
		topPanel.add(new Label("Not logged in to steam.", getSkin()));
	}
	
	private void addMainPanelStuff(MultimythTable mainPanel) {
		
	}
	
	private void addBottomPanelStuff(MultimythTable bottomPanel) {
		bottomPanel.add(new ImageTextButton("Lorem Ipsum", getSkin()).pad(5f)).space(5f);
		bottomPanel.add().expandX().fillX();
		bottomPanel.add(new Label("Multimyth version ", getSkin()));
	}
	
	private void addInstanceToUI() {
		
	}
	
	public MainWindow() {
		this(null);
	}
	
	private static Drawable createSolidColorDrawable(Color color) {
	    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
	    pixmap.setColor(color);
	    pixmap.fill();
	    Texture texture = new Texture(pixmap);
	    pixmap.dispose();
	    return new TextureRegionDrawable(new TextureRegion(texture));
	}

}
