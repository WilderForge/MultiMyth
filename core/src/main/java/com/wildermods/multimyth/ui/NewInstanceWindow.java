package com.wildermods.multimyth.ui;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.wildermods.multimyth.I18N;
import com.wildermods.multimyth.ui.Divider.Orientation;
import com.wildermods.thrixlvault.exception.UnknownVersionException;
import com.wildermods.thrixlvault.exception.VersionParsingException;
import com.wildermods.thrixlvault.utils.OS;
import com.wildermods.thrixlvault.utils.version.Version;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;

public class NewInstanceWindow extends Window {

	boolean nameEdited = false;
	
	public NewInstanceWindow(Skin skin) {
		super(I18N.translate("topPanel.newInstance"), skin);
		
		//debugAll();
		
		this.setBackground(UIHelper.createSolidColorDrawable(getBackground(), Color.MAROON));
		setModal(true);
		setMovable(true);
		setResizable(true);
		
		setupTitleBar(skin);
		
		MultimythTable table = new MultimythTable(skin);
		add(table).expand().fillX().top();
		
		setupTopPanel(table, skin);
		table.row();
		table.add(new Divider(Orientation.HORIZONTAL)).expand().fill().pad(4);
		table.row();
		setupMainPanel(table, skin);
		table.row();
		setupBottomPanel(table, skin);
		
		//this.background(UIHelper.createSolidColorDrawable(Color.CORAL));
	}

	public void centerOnStage(Stage stage) {
	    pack(); // ensures width/height are computed

	    float x = Math.round((stage.getWidth() - getWidth()) / 2f);
	    float y = Math.round((stage.getHeight() - getHeight()) / 2f);

	    setPosition(x, y);
	    invalidateHierarchy();
	    validate();
	}
	
	private void setupTitleBar(Skin skin) {
		TextButton closeButton = new TextButton("x", skin);
		closeButton.getLabel().setFontScale(1.2f);
		closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewInstanceWindow.this.remove();
            }
		});
		
		getTitleTable().add(closeButton).prefWidth(4).prefHeight(4);
	}
	
	private void setupTopPanel(MultimythTable table, Skin skin) {
		MultimythTable topPanel = new MultimythTable(skin);
		table.add(topPanel).expandX().fillX();
		topPanel.add(new Label(I18N.translate("instance.create.label.window"), skin)).pad(2);
		TextField nameField = new TextField("", skin);
		nameField.setTextFieldListener(new TextField.TextFieldListener() {

			@Override
			public void keyTyped(TextField textField, char c) {
				nameEdited = true;
			}
			
		});
		topPanel.add(nameField).pad(2).expandX().fillX();
		topPanel.add().width(10);
		TextButton resetNameButton = new TextButton(I18N.translate("reset"), skin);
		resetNameButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				nameEdited = false;
				try {
					nameField.setText(WildermythManifest.getLatest().version());
				} catch (UnknownVersionException e) {
					throw new RuntimeException(e);
				}
			}
		});
		topPanel.add(resetNameButton);
	}
	
	private void setupMainPanel(MultimythTable table, Skin skin) {
		
		MultimythTable mainPanel = new MultimythTable(skin);
		table.add(mainPanel).expandX().fillX();
		MultimythTable left = new MultimythTable(skin);
		MultimythTable middle = new MultimythTable(skin);
		AutoFocusingScrollPane versionPane = new AutoFocusingScrollPane(middle, skin);
		//versionPane.debugAll();
		MultimythTable header = new MultimythTable(skin);
		header.add(new Label(I18N.translate("instance.create.header.version"), skin)).left().expandX().fillX();
		header.add(new Label(I18N.translate("instance.create.header.type"), skin)).right();
		middle.add(header).expand().fill();
		
		List<WildermythManifest> manifests = WildermythManifest.manifestStream(OS.getOS()).filter(WildermythManifest::isCurrentOS).filter(WildermythManifest::isPublic).collect(Collectors.toList());
		manifests.sort(Comparator.naturalOrder());
		
		Version release;
		try {
			release = Version.parse("1.0.0");
		} catch (VersionParsingException e) {
			throw new AssertionError(e);
		}
		
		manifests.forEach((manifest) -> {
			middle.row();
			MultimythTable versionTable = new MultimythTable(skin);
			versionTable.add(manifest.version().replace('+', '.')).expandX().fillX().left();
			try {
				if(release.compareTo(Version.parse(manifest.version())) <= 0) {
					versionTable.add("release").right();
				}
				else {
					versionTable.add("beta").right();
				}
			}
			catch(VersionParsingException e) {
				e.printStackTrace();
			}
			middle.add(versionTable).expandX().fillX();
		});
		
		MultimythTable right = new MultimythTable(skin);
		
		mainPanel.add(left);
		mainPanel.add(versionPane).expand().fill();
		mainPanel.add(right);
	}
	
	
	private void setupBottomPanel(MultimythTable table, Skin skin) {
		MultimythTable bottomPanel = new MultimythTable(skin);
		table.add(bottomPanel);
	}
	
}
