package com.wildermods.multimyth.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.file.PathUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import com.wildermods.multimyth.I18N;
import com.wildermods.multimyth.MainApplication;
import com.wildermods.multimyth.internal.Install;
import com.wildermods.multimyth.internal.JVMInstance;

public class MainWindow extends MultimythTable {
	
	private static final int MIN_COLUMNS = 5;
	private static final int MAX_COLUMNS = 16;
	
	private MultimythTable topPanel;
	private SidePanel sidePanel;
	private ScrollPane mainPanel;
	private MultimythTable bottomPanel;
	
	private Grid grid = new Grid(64);
	private Cell<MultimythTable> topPanelCell;
	private Cell<MultimythTable> sidePanelCell;
	private Cell<MultimythTable> bottomPanelCell;
	
	public MainWindow(Skin skin) {
		super(skin);
		//debug();
		setSkin(skin);
		build();
	}
	
	public void build() {
		
		// Create the grid and wrap it in a Container for proper alignment
		topPanel = new MultimythTable(this.getSkin());
		sidePanel = new SidePanel(this.getSkin());
		
		grid = new Grid(128);
		Table gridContainer = new Table();
		gridContainer.add(grid).align(Align.topLeft).expandX().fillX();
		gridContainer.top().left();
		
		mainPanel = new AutoFocusingScrollPane(gridContainer, this.getSkin());
		bottomPanel = new MultimythTable(this.getSkin());
		
		setFillParent(true);
		pad(16f);
		this.setBackground(this.getSkin().getDrawable("window"));
		
		topPanelCell = this.add(topPanel).colspan(2);
		topPanelCell.expandX().fill().row();
		addTopPanelStuff(topPanel);
		topPanel.setBackground(UIHelper.createSolidColorDrawable(Color.RED));
		
		this.add(mainPanel).expand().fillX().align(Align.topLeft);
		mainPanel.setScrollingDisabled(true, false);
		mainPanel.setFadeScrollBars(false);
		mainPanel.setScrollbarsVisible(true);
		
		this.add(sidePanel).width(256).fillY();
		sidePanel.background(UIHelper.createSolidColorDrawable(Color.BLUE));
		
		this.row();
		
		this.add(bottomPanel).colspan(2).expandX().fill();
		bottomPanel.background(UIHelper.createSolidColorDrawable(Color.GREEN));
		addBottomPanelStuff(bottomPanel);
		
		sidePanel.select(null);
		
		// Add main panel stuff AFTER the UI structure is built
		addMainPanelStuff(grid);
		
		// Force initial layout
		invalidateHierarchy();
		validate();
	}
	
	private void addMainPanelStuff(Grid actor) {
		
		List<Path> subdirs = new ArrayList<>();
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(MainApplication.SAVE_DIR)) {
			for(Path entry : stream) {
				if(Files.isDirectory(entry)) {
					subdirs.add(entry);
					Path metadata = entry.resolve("instance.mm");
					if(Files.exists(metadata)) {
						try {
							Install install = Install.fromFile(metadata);
							GameInstanceButton gameButton = new GameInstanceButton(install.name(), getSkin());
							gameButton.addListener(new ClickListener() {
								@Override
								public void clicked (InputEvent event, float x, float y) {
									MainWindow.this.sidePanel.select(install);
								}
							});
							actor.addActor(new SquareContainer<GameInstanceButton>(gameButton));
						}
						catch(Throwable t) {
							actor.addActor(new SquareContainer<GameInstanceButton>(new GameInstanceButton(I18N.translate("instance.error"), getSkin())));
							new IOException("Could not read installation metadata: " + metadata, t).printStackTrace();
						}
					}
					else {
						System.err.println(entry);
					}
				}
			}
		}
		catch(IOException e) {
			throw new Error(e); //the launcher is unusable, so Error should be thrown
		}
		
		/*List<WildermythManifest> manifests = WildermythManifest.getManifests().stream()
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
		}*/
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
		ImageTextButton newInstanceButton = new ImageTextButton(I18N.translate("topPanel.newInstance"), getSkin());
		topPanel.add(newInstanceButton.pad(5f)).space(5f);
		newInstanceButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				MainApplication.INSTANCE.fireNewInstance();
			}
		});
		
		topPanel.add(new ImageTextButton(I18N.translate("topPanel.folders"), getSkin()).pad(5f)).space(5f);
		topPanel.add(new ImageTextButton(I18N.translate("topPanel.settings"), getSkin()).pad(5f)).space(5f);
		topPanel.add(new ImageTextButton(I18N.translate("topPanel.update"), getSkin()).pad(5f)).space(5f);
		topPanel.add(new ImageTextButton(I18N.translate("topPanel.help"), getSkin()).pad(5f)).space(5f);
		topPanel.add().expandX().fillX();
		topPanel.add(new Label(I18N.translate("topPanel.steam.notLoggedIn"), getSkin()));
	}
	
	private void addBottomPanelStuff(MultimythTable bottomPanel) {
		bottomPanel.add(new ImageTextButton("Lorem Ipsum", getSkin()).pad(5f)).space(5f);
		bottomPanel.add().expandX().fillX();
		bottomPanel.add(new Label(I18N.translate("bottomPanel.version", MainApplication.VERSION), getSkin()));
	}
	
	private void addInstanceToUI() {
		
	}
	
	public MainWindow() {
		this(null);
	}
	
	private static class SidePanel extends MultimythTable {

		private Install selected = null;
		private GameInstanceButton imageButton = new GameInstanceButton(I18N.translate("noInstance"), getSkin());
		private TextButton launchButton = new TextButton(I18N.translate("launch"), getSkin());
		private TextButton editInstanceButton = new TextButton(I18N.translate("edit"), getSkin());
		private TextButton viewModsButton = new TextButton(I18N.translate("viewMods"), getSkin());
		private TextButton viewFolderButton = new TextButton(I18N.translate("installFolder"), getSkin());
		private TextButton configButton = new TextButton(I18N.translate("configFolder"), getSkin());
		private TextButton deleteButton = new TextButton(I18N.translate("shortcut"), getSkin());
		private TextButton copyButton = new TextButton(I18N.translate("clone"), getSkin());
		
		public SidePanel(Skin skin) {
			super(skin);
			build();
		}
		
		@Override
		public void build() {
			align(Align.top);
			defaults().expandX().fillX().padBottom(1);
			System.out.println("DEFAULTS: " + this.defaults().getExpandX());
			add(imageButton); //TODO: make image not ass
			row();
			add(launchButton);
			row();
			add(editInstanceButton);
			row();
			add(viewModsButton);
			row();
			add(viewFolderButton);
			row();
			add(configButton);
			row();
			add(deleteButton);
			row();
			
			addListeners();
		}
		
		public void select(Install install) {
			this.selected = install;
			if(install != null) {
				imageButton.setDisabled(false);
				launchButton.setDisabled(false);
				deleteButton.setDisabled(false);
			}
			else {
				imageButton.setDisabled(true);
				launchButton.setDisabled(true);
				editInstanceButton.setDisabled(true);
				viewModsButton.setDisabled(true);
				viewFolderButton.setDisabled(true);
				configButton.setDisabled(true);
				deleteButton.setDisabled(true);
				copyButton.setDisabled(true);
			}
			pack();
		}
		
		private void addListeners() {
			imageButton.addListener(new ClickListener() {
				public void clicked (InputEvent event, float x, float y) {
					if(selected != null) {
						if(selected.isCoremodded()) {
							try {
								JVMInstance.fromPath(selected.java().getJVMLocation(), selected.installPath());
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
			
			launchButton.addListener(new ClickListener() {
				public void clicked (InputEvent event, float x, float y) {
					if(selected != null) {
						try {
							
							ArrayList<String> command = new ArrayList<>();
							ProcessBuilder processBuilder = new ProcessBuilder();
							if(selected.isCoremodded()) {

								JVMInstance jvm = JVMInstance.fromPath(selected.java().getJVMLocation(), selected.installPath());
								command.add(jvm.jvmLocation.toRealPath(LinkOption.NOFOLLOW_LINKS).toAbsolutePath().toString());
								
								command.add("-cp");
								
								Properties properties = jvm.getProperties();
								//String classpath = properties.getProperty("java.class.path");
								String classpath = "*" + File.pathSeparator + "." + File.separator + "fabric" + File.separator + "*";
								
								command.add(classpath);
								
								command.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
								
								{ //DO NOT EXECUTE commandString, it is a security vulnerability to do so! For debug output ONLY!
									StringBuilder commandString = new StringBuilder();
									Iterator<String> argIterator = command.iterator();
									for(String s : command) {
										commandString.append(s);
										commandString.append(' ');
									}
									System.out.println("Executing " + commandString);
								}
								
							}
							else {
								command.add("java");
								command.add("-jar");
								command.add("wildermyth.jar");
							}
							
							processBuilder.directory(selected.installPath().toFile());
							processBuilder.command(command);
							processBuilder.inheritIO();
							processBuilder.start();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			editInstanceButton.addListener(new ClickListener() {
				
			});
			
			viewModsButton.addListener(new ClickListener() {
				
			});
			
			viewFolderButton.addListener(new ClickListener() {
				
			});
			
			configButton.addListener(new ClickListener() {
				
			});
			
			deleteButton.addListener(new ClickListener() {
				public void clicked (InputEvent event, float x, float y) {
						Dialog dialog = new Dialog(I18N.translate("instance.popup.delete.title"), getSkin());
						dialog.add(I18N.translate("instance.popup.delete.dialog", selected.name()));
						dialog.getContentTable().row();
						
						TextButton cancelButton = new TextButton(I18N.translate("cancel"), getSkin());
						cancelButton.addListener(new ClickListener() {
							public void clicked (InputEvent event, float x, float y) {
								dialog.remove();
							}
						});
						
						TextButton deleteButton = new TextButton(I18N.translate("delete"), getSkin());
						deleteButton.addListener(new ClickListener() {
							public void clicked (InputEvent event, float x, float y) {
								try {
									PathUtils.delete(selected.installPath());
									dialog.remove();
									Dialog successDialog = new Dialog(I18N.translate("instance.popup.delete.success"), getSkin());
									successDialog.add(I18N.translate("instance.popup.delete.success.dialog", selected.name()));
									successDialog.row();
									successDialog.add(new TextButton(I18N.translate("ok"), getSkin()));
									successDialog.addListener(new ClickListener() {
										public void clicked (InputEvent event, float x, float y) {
											successDialog.remove();
										}
									});
								}
								catch(IOException e) {
									e.printStackTrace();
									dialog.setVisible(false);
									Dialog errDialog = new Dialog(I18N.translate("error.unexpected", ""), getSkin());
									errDialog.add(I18N.translate("error.delete.fail", selected.installPath()));
									errDialog.row();
									if(e.getMessage() != null && !e.getMessage().isBlank()) {
										errDialog.add(e.getMessage());
										errDialog.row();
									}
									TextButton okButton = new TextButton(I18N.translate("ok"), getSkin());
									okButton.addListener(new ClickListener() {
										public void clicked (InputEvent event, float x, float y) {
											try {
												okButton.remove();
											}
											finally {
												dialog.setVisible(true); //so we don't soft lock the UI if an exception occurs
											}
										}
									});
								}
							}
						});
						
						
						dialog.getContentTable().add(cancelButton).align(Align.left);
						dialog.getContentTable().add(deleteButton).align(Align.right);
						
				}
			});
			
			copyButton.addListener(new ClickListener() {
				
			});
		}
		
	}

}
