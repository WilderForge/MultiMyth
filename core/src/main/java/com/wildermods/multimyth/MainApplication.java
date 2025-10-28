package com.wildermods.multimyth;

import java.io.IOException;
import java.util.Locale;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wildermods.multimyth.internal.CompileStrictly;
import com.wildermods.multimyth.internal.Steam;
import com.wildermods.multimyth.ui.MainWindow;

@CompileStrictly
public class MainApplication extends ApplicationAdapter {
	
	public static final String VERSION = "@MULTIMYTH_VERSION@";
	
	public static final Gson gson;
	static {
		GsonBuilder builder = new GsonBuilder();
		builder.setFormattingStyle(FormattingStyle.PRETTY.withIndent("\t"));
		gson = builder.create();
	}
	
	private Steam steam;
	private Stage stage;
	private MainWindow mainWindow;
	private Skin skin;

	public static MainApplication INSTANCE;
	
	@Override
	public void create() {
		I18N.supportedLocales().forEach((locale) -> System.out.println(locale));
		try {
			I18N.INSTANCE = new I18N(Locale.JAPAN);
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
		}
		
		for (ObjectMap.Entry<String, Drawable> entry : skin.getAll(Drawable.class)) {
			System.out.println("Drawable: " + entry.key);
		}

		mainWindow = new MainWindow(skin);
		
		stage.addActor(mainWindow);

		Gdx.input.setInputProcessor(stage);
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
	
	public Skin getSkin() {
		return skin;
	}
	
	public MainWindow getMainWindow() {
		return mainWindow;
	}
	
	public Steam getSteam() {
		return steam;
	}
 
}