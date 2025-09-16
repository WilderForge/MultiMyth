package com.wildermods.multimyth.ui;

import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.wildermods.multimyth.MainApplication;
import com.wildermods.multimyth.internal.CompileStrictly;
import com.wildermods.thrixlvault.exception.VersionParsingException;
import com.wildermods.thrixlvault.utils.OS;
import com.wildermods.thrixlvault.utils.version.Version;
import com.wildermods.thrixlvault.utils.version.VersionParser;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;

@CompileStrictly
public class MainWindow extends MultimythTable {
	
	private static final int MIN_COLUMNS = 5;
	private static final int MAX_COLUMNS = 16;
	
	private MultimythTable topPanel;
	private MultimythTable sidePanel;
	private ScrollPane mainPanel;
	private MultimythTable bottomPanel;
	
	private Grid grid = new Grid(64);
	private Cell<MultimythTable> topPanelCell;
	private Cell<MultimythTable> sidePanelCell;
	private Cell<MultimythTable> bottomPanelCell;
	
	public MainWindow(Skin skin) {
		super(skin);
		debug();
		setSkin(skin);
		
		topPanel = new MultimythTable(skin);
		sidePanel = new MultimythTable(skin);
		
		// Create the grid and wrap it in a Container for proper alignment
		grid = new Grid(128);
		Table gridContainer = new Table();
		gridContainer.add(grid).align(Align.topLeft).expandX().fillX();
		gridContainer.top().left(); // Force top-left alignment
		
		mainPanel = new ScrollPane(gridContainer, skin);
		bottomPanel = new MultimythTable(skin);
		
		setFillParent(true);
		pad(16f);
		this.setBackground(skin.getDrawable("window"));
		
		topPanelCell = this.add(topPanel).colspan(2);
		topPanelCell.expandX().fill().row();
		addTopPanelStuff(topPanel);

		topPanel.setBackground(createSolidColorDrawable(Color.RED));
		
		// Add the scroll pane directly
		this.add(mainPanel).expand().fillX().align(Align.topLeft);
		mainPanel.debugAll();
		mainPanel.setScrollingDisabled(true, false);
		mainPanel.setFadeScrollBars(false);
		mainPanel.setScrollbarsVisible(true);
		
		this.add(sidePanel).width(256).fillY();
		sidePanel.background(createSolidColorDrawable(Color.BLUE));
		addMainPanelStuff(grid);
		
		this.row();
		
		this.add(bottomPanel).colspan(2).expandX().fill();
		bottomPanel.background(createSolidColorDrawable(Color.GREEN));
		addBottomPanelStuff(bottomPanel);

		System.out.println("test");
	}
	
	private void addMainPanelStuff(Grid actor) {
		List<WildermythManifest> manifests = WildermythManifest.getManifests().stream()
			.filter(WildermythManifest::isPublic) //only public releases
			.filter((manifest) -> {
				return manifest.os() == OS.getOS(); //for this OS
			})
			.filter((manifest) -> { 
				return !manifest.version().contains("r"); //that are not special re-releases
			})
			.sorted((manifest1, manifest2) -> {
				try { 
					Version version1 = VersionParser.parse(manifest1.version().replace('+', '.').replace('r', '.'), false); 
					Version version2 = VersionParser.parse(manifest2.version().replace('+', '.').replace('r', '.'), false); 
					return version1.compareTo(version2); } catch(VersionParsingException e) 
				{ 
					throw new RuntimeException(e); 
				}
			}).collect(Collectors.toList());
		
		for(WildermythManifest manifest : manifests) { 
			System.out.println(manifest); 
			actor.addActor(new SquareContainer<GameInstanceButton>(new GameInstanceButton(manifest.version(), getSkin())));
		}
	}

	@Override
	public void layout() {
		super.layout();
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
	
	private void addBottomPanelStuff(MultimythTable bottomPanel) {
		bottomPanel.add(new ImageTextButton("Lorem Ipsum", getSkin()).pad(5f)).space(5f);
		bottomPanel.add().expandX().fillX();
		bottomPanel.add(new Label("Multimyth version " + MainApplication.VERSION, getSkin()));
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
