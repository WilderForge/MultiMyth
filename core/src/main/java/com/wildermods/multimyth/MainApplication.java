package com.wildermods.multimyth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.wildermods.multimyth.internal.CompileStrictly;
import com.wildermods.multimyth.ui.MainWindow;

@CompileStrictly
public class MainApplication extends ApplicationAdapter {
    private Stage stage;
    private MainWindow mainWindow;
    private Skin skin;

    public static MainApplication INSTANCE;
    
    @Override
    public void create() {
    	if(INSTANCE == null) {
    		INSTANCE = this;
    	}
    	else {
    		throw new IllegalStateException();
    	}
    	
    	try (ServerSocket server = new ServerSocket(61218)) {
			Socket socket = server.accept();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(reader.readLine() != null) {
				System.out.println(reader.readLine());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    	

    	
    	FileHandle skinDir = Gdx.files.internal("ui/");
    	FileHandle[] files = skinDir.list((dir, name) -> name.endsWith(".json"));

    	for (FileHandle file : files) {
    	    System.out.println("Found skin: " + file.path());
    	}
    	
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        
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
 
}