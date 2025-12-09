package com.wildermods.multimyth.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.wildermods.masshash.exception.IntegrityException;
import com.wildermods.multimyth.I18N;
import com.wildermods.multimyth.MainApplication;
import com.wildermods.multimyth.ui.Divider.Orientation;
import com.wildermods.thrixlvault.ChrysalisizedVault;
import com.wildermods.thrixlvault.MassDownloadWeaver;
import com.wildermods.thrixlvault.Vault;
import com.wildermods.thrixlvault.exception.DatabaseError;
import com.wildermods.thrixlvault.exception.MissingVersionException;
import com.wildermods.thrixlvault.exception.VersionParsingException;
import com.wildermods.thrixlvault.steam.CompletedDownload;
import com.wildermods.thrixlvault.steam.FailedDownload;
import com.wildermods.thrixlvault.steam.ISteamDownload;
import com.wildermods.thrixlvault.steam.SkippedDownload;
import com.wildermods.thrixlvault.utils.OS;
import com.wildermods.thrixlvault.utils.version.Version;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;

public class NewInstanceWindow extends Window {

	private static final Logger LOGGER = LogManager.getLogger(NewInstanceWindow.class);
	boolean nameEdited = false;
	WildermythManifest selectedVersion = null;
	TextField nameField = null;
	
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

	    setHeight(stage.getHeight() / 2);
	    setWidth(stage.getWidth() / 4);
	    
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
		nameField = new TextField("", skin);
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
				select(WildermythManifest.getLatest());
			}
		});
		select(WildermythManifest.getLatest());
		topPanel.add(resetNameButton);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		
		List<WildermythManifest> manifests = 
			WildermythManifest.manifestStream(OS.getOS()).
			filter((manifest) -> {
				return 
					(manifest.isPublic() && manifest.isVersionKnown()) 
					||
					manifest.isLatest("unstable");
			})
			.collect(Collectors.toList());
		
		manifests.sort((Comparator)Comparator.naturalOrder().reversed()); //cast needed to make the compiler shut up
		
		Version release;
		try {
			release = Version.parse("1.0.0");
		} catch (VersionParsingException e) {
			throw new AssertionError(e);
		}
		
		manifests.forEach((manifest) -> {
			middle.row();
			VersionSelectButton versionButton = new VersionSelectButton(manifest);
			MultimythTable versionTable = new MultimythTable(skin);
			versionTable.add(manifest.version().replace('+', '.')).expandX().fillX().left();
			List<String> types = new ArrayList<>();
			try {
				if(manifest.isLatest()) {
					types.add(I18N.translate("release.latest"));
				}
				else {
					types.add(I18N.translate("release.old"));
				}
				if(manifest.isBranch("unstable")){
					types.add(I18N.translate("release.unstable"));
				}
				if(manifest.isPublic()) {
					
					if(release.compareTo(Version.parse(manifest.version())) <= 0) {
						types.add(I18N.translate("release.release"));
					}
					else {
						types.add(I18N.translate("release.beta"));
					}
				}
				StringBuilder b = new StringBuilder();
				Iterator<String> ti = types.iterator();
				while(ti.hasNext()) {
					b.append(ti.next());
					if(ti.hasNext()) {
						b.append(' ');
					}
				}
				String typeString = b.toString();
				if(typeString.isEmpty()) {
					typeString = I18N.translate("release.unknown");
				}
				versionTable.add(typeString).right();
			}
			catch(VersionParsingException e) {
				e.printStackTrace();
			}
			versionButton.add(versionTable).expandX().fillX();
			middle.add(versionButton).expandX().fillX();
		});
		
		MultimythTable right = new MultimythTable(skin);
		
		mainPanel.add(left);
		mainPanel.add(versionPane).expand().fill();
		mainPanel.add(right);
	}
	
	
	private void setupBottomPanel(MultimythTable table, Skin skin) {
		MultimythTable bottomPanel = new MultimythTable(skin);
		TextButton cancelButton = new TextButton("Cancel", skin);
		TextButton confirmButton = new TextButton("OK", skin);
		
		cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewInstanceWindow.this.remove();
            }
		});
		
		confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                create();
            }
		});
		
		bottomPanel.add().expandX().fillX();
		bottomPanel.add(cancelButton.pad(4f)).right();
		bottomPanel.add().width(5f);
		bottomPanel.add(confirmButton.pad(4f)).right();
		table.add(bottomPanel).padTop(10f).expandX().fillX();
	}
	
	private void select(WildermythManifest manifest) {
		selectedVersion = manifest;
		nameField.setText(selectedVersion.version());
	}
	
	public void create() {
		if(selectedVersion == null) {
			LOGGER.warn("No version selected?!");
			return;
		}
		ChrysalisizedVault chrysalis;
		
		try {
			if(!Vault.DEFAULT.hasChrysalis(selectedVersion)) {
				try {
					MassDownloadWeaver downloader = new MassDownloadWeaver(MainApplication.INSTANCE.getSteam().username(), Set.of(selectedVersion));
					Set<ISteamDownload> results = downloader.run(); //TODO: Make it so the program doesn't go unresponsive while downloading
					if(results.size() == 1) {
						ISteamDownload result = results.iterator().next();
						if(result instanceof FailedDownload) {
							throw new IOException("Download failed", ((FailedDownload) result).failReason());
						}
						else if (result instanceof SkippedDownload) {
							throw new IOException("Download was skipped: " + ((SkippedDownload)result).downloadBlockedReason());
						}
						else if (result instanceof CompletedDownload) {
							//NO-OP
						}
						else {
							throw new AssertionError("Unknown result type: " + result.getClass());
						}
					}
					else {
						throw new AssertionError("Expected 1 download result, got " + results.size());
					}
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}
			
			if(Vault.DEFAULT.hasChrysalis(selectedVersion)) {
				try {
					chrysalis = Vault.DEFAULT.chrysalisize(selectedVersion);
					try {
						chrysalis.export(Path.of("/tmp/multimyth/" + nameField.getText()), true);
					} catch (IntegrityException e) {
						throw new IOException(e); //shouldn't ever occur unless the user's hard drive is failing?
					}
				} catch (MissingVersionException e) {
					throw new AssertionError(e); //should be impossible
				} catch(IOException | InterruptedException | ExecutionException e) {
					throw new DatabaseError(e);
				}
			}
			else {
				throw new AssertionError(selectedVersion.name() + " was downloaded, but isn't vaulted?! Error in logic?");
			}
		}
		catch(IOException e) {
			e.printStackTrace(); //TODO: popup
		}
		catch(DatabaseError e) {
			e.printStackTrace(); //TODO: popup
		}
	}
	
	private class VersionSelectButton extends Button {
		
		private final WildermythManifest manifest;
		
		public VersionSelectButton(WildermythManifest manifest) {
			super(NewInstanceWindow.this.getSkin());
			this.manifest = manifest;
			this.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					NewInstanceWindow.this.select(manifest);
				}
			});
			
		}
		
		public WildermythManifest getManifest() {
			return manifest;
		}
	}
	
}
