package com.wildermods.multimyth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wildermods.multimyth.internal.CompileStrictly;
import com.wildermods.multimyth.internal.GsonHelper;
import com.wildermods.multimyth.internal.Steam;
import com.wildermods.multimyth.ui.MainWindow;
import com.wildermods.multimyth.ui.NewInstanceWindow;
import com.wildermods.thrixlvault.utils.OS;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;

@CompileStrictly
public class MainApplication extends ApplicationAdapter {
	
	public static final String VERSION = "@MULTIMYTH_VERSION@";
	public static final Path MULTIMYTH_DIR = Path.of(System.getProperty("user.home")).resolve("multimyth");
	public static final Path SAVE_DIR = MULTIMYTH_DIR.resolve("instances");
	static {
		try {
			Files.createDirectories(SAVE_DIR);
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static final Gson gson;
	static {
		GsonBuilder b = GsonHelper.GSON_BUILDER; //gsonHelper must remain java 8 compatible
		gson = b.create();
	}
	
	private Steam steam = new Steam() {

		@Override
		public String username() {
			return "wilderforge";
		}
		
	};
	private Stage stage;
	private MainWindow mainWindow;
	private Skin skin;

	public static MainApplication INSTANCE;
	
	@Override
	public void create() {
		I18N.supportedLocales().forEach((locale) -> System.out.println(locale));
		try {
			I18N.INSTANCE = new I18N();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				I18N.INSTANCE = new I18N();
			} catch (IOException e1) {
				throw new Error(e1);
			}
		}
		if(INSTANCE == null) {
			INSTANCE = this;
		}
		else {
			throw new IllegalStateException();
		}
		
/*		try (ServerSocket server = new ServerSocket(61218)) {
			Socket socket = server.accept();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(reader.readLine() != null) {
				System.out.println(reader.readLine());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}*/
		

		
		FileHandle skinDir = Gdx.files.internal("ui/");
		FileHandle[] files = skinDir.list((dir, name) -> name.endsWith(".json"));

		for (FileHandle file : files) {
			System.out.println("Found skin: " + file.path());
		}
		
		stage = new Stage(new ScreenViewport());
		
		//TODO: this is a really shit way of doing this. Need to learn how to properly set styles
		skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
		BitmapFont font = I18N.getFont();
		Window.WindowStyle windowStyle = skin.get(Window.WindowStyle.class);
		if(font != null) {
			skin.add("default", font, BitmapFont.class);
			skin.add("font", font, BitmapFont.class);
			skin.add("list", font, BitmapFont.class);
			skin.add("subtitle", font, BitmapFont.class);
			skin.add("window", font, BitmapFont.class);
			Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
			ImageTextButtonStyle buttonStyle = skin.get(ImageTextButtonStyle.class);
			labelStyle.font = font;
			buttonStyle.font = font;
			windowStyle.titleFont = font;

		}
		windowStyle.background.setTopHeight(25);
		windowStyle.background.setLeftWidth(10);
		windowStyle.background.setRightWidth(10);
		windowStyle.background.setBottomHeight(10);
		
		for (ObjectMap.Entry<String, Drawable> entry : skin.getAll(Drawable.class)) {
			System.out.println("Drawable: " + entry.key);
		}

		mainWindow = new MainWindow(skin);
		
		stage.addActor(mainWindow);

		Gdx.input.setInputProcessor(stage);
		Gdx.graphics.setWindowedMode(Gdx.graphics.getDisplayMode().width / 2, Gdx.graphics.getDisplayMode().height / 2);
		
		for(WildermythManifest manifest : WildermythManifest.manifestStream(OS.getOS()).sorted().collect(Collectors.toList())) {
			System.out.println(manifest.asVersion().version());
		}
	}

	@Override
	public void render() {
		ScreenUtils.clear(0f, 0f, 0f, 1f);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		if(width <= 0 || height <= 0) return;

		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
	}
	
	public void fireNewInstance() {
		NewInstanceWindow popup = new NewInstanceWindow(skin);
		stage.addActor(popup);
		popup.centerOnStage(stage);
	}
	
	public Skin getSkin() {
		return skin;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public void addAndCenterWindow(Window window) {
		this.getStage().addActor(window);
		centerOnStage(window, getStage());
	}
	
	public void addAndCenterWindow(Window window, boolean resize) {
		this.getStage().addActor(window);
		centerOnStage(window, -1, -1, getStage());
	}
	
	private void centerOnStage(Window window, Stage stage) {
	    window.pack(); // ensures width/height are computed
	    float height = stage.getHeight() / 2;
	    float width = stage.getWidth() / 4;
	    centerOnStage(window, width, height, stage);
	}
	
	private void centerOnStage(Window window, float width, float height, Stage stage) {
	    window.pack(); // ensures width/height are computed

	    if(width > 0f) {
	    	window.setHeight(width);
	    }
	    if(height > 0f) {
	    	window.setWidth(height);
	    }
	    
	    float x = Math.round((stage.getWidth() - window.getWidth()) / 2f);
	    float y = Math.round((stage.getHeight() - window.getHeight()) / 2f);
	    
	    window.setPosition(x, y);
	    window.invalidateHierarchy();
	    window.validate();
	}
	
	public MainWindow getMainWindow() {
		return mainWindow;
	}
	
	public Steam getSteam() {
		return steam;
	}
 
}