//package clg.birds.lwjgl3;
//
//import com.badlogic.gdx.Game;
//
//public class Main extends Game {
//    @Override
//    public void create() {
//        setScreen(new Lwjgl3Launcher.HomePage(this)); // Start with the HomePage screen
//    }
//
//    // Transition to Level Screen
//    public void goToLevelScreen() {
//        setScreen(new LevelScreen(this));
//    }
//
//    // Transition to Game Screen
//    public void goToGameScreen() {
//        setScreen(new GameScreen(this));
//    }
//    public void goToGame2Screen() { setScreen(new Game2Screen(this));
//    }
//    public void goTowinlooseScreen() {setScreen (new winlooseScreen(this));
//    }
//    public void goToloosewinScreen() {setScreen(new loosewinScreen(this));
//    }
//    public void goToGame3Screen () {setScreen(new Game3Screen(this));}
//
//
//    @Override
//    public void render() {
//        super.render(); // Delegate rendering to the active screen
//    }
//
//    @Override
//    public void dispose() {
//        super.dispose();
//        // Additional cleanup can be added here if shared resources are used
//    }
//
//}



















package clg.birds.lwjgl3;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import java.io.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends Game {
    private GameScreen gs = null;
    private Game2Screen g2s = null;

    @Override
    public void create() {
        setScreen(new HomePage(this)); // Start with the HomePage screen
    }

    public GameScreen loadGameScreen(Integer num){
        String filename = Gdx.files.getLocalStoragePath() + "storedGame_" + num + ".ser";

        // Deserialization
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            GameScreen object1 = (GameScreen)in.readObject();
            in.close();
            file.close();

            System.out.println("Object has been deserialized");
            return object1;
        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
            return null;
        }

        catch(ClassNotFoundException ex)
        {
            System.out.println("ClassNotFoundException is caught");
            return null;
        }
    }

    public Game2Screen loadGame2Screen(Integer num){
        String filename = Gdx.files.getLocalStoragePath() + "storedGame_" + num + ".ser";
        System.out.println(filename);
        // Deserialization
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            Game2Screen object1 = (Game2Screen)in.readObject();
            in.close();
            file.close();

            System.out.println("Object has been deserialized");
            return object1;
        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
            return null;
        }

        catch(ClassNotFoundException ex)
        {
            System.out.println("ClassNotFoundException is caught");
            return null;
        }
    }

    void saveGame(Integer num){
        String filename = Gdx.files.getLocalStoragePath() + "storedGame_" + num + ".ser";
        System.out.println(filename);
        try
        {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            if(num == 0){
                out.writeObject(this.gs);
            }else{
                out.writeObject(this.g2s);
            }



            out.close();
            file.close();

            System.out.println("Object has been serialized");

        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
        }
    }

    // Transition to Level Screen
    public void goToLevelScreen() {
        setScreen(new LevelScreen(this, -1));
    }

    // Transition to Game Screen
    public void goToGameScreen() {


        if(this.gs == null){
            this.gs = (GameScreen)this.loadGameScreen(0);
            if( this.gs == null){
                this.gs = new GameScreen(this);
            }
        }

        setScreen(this.gs);
    }
    public void goTowinlooseScreen() {setScreen (new winlooseScreen(this));
    }
    public void goToloosewinScreen() {setScreen(new loosewinScreen(this));
    }
    public void goToGame3Screen () {setScreen(new Game3Screen(this));}


    @Override
    public void render() {
        super.render(); // Delegate rendering to the active screen
    }

    @Override
    public void dispose() {
        super.dispose();
        // Additional cleanup can be added here if shared resources are used
    }

    public void goToGame2Screen() {
        if(this.g2s == null){
            this.g2s = (Game2Screen)this.loadGame2Screen(1);
            if (this.g2s == null){
                this.g2s = new Game2Screen(this);
            }
        }

        setScreen(this.g2s);
    }
}

// HomePage class which implements the Screen interface
class HomePage implements Screen {
    private Stage stage;
    private Skin skin;
    private Main game;
    private Texture homePageImgTexture;
    private Viewport viewport;

    public HomePage(Main game) {
        this.game = game;

        // Initialize the viewport and stage
        viewport = new StretchViewport(800, 600);
        stage = new Stage(viewport);

        // Load the background image
        homePageImgTexture = new Texture(Gdx.files.internal("MainScreen.png"));

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Title label with larger font scale
//            Label titleLabel = new Label("Angry Birds Game", skin);
//            titleLabel.setFontScale(7.0f);

        // Play button with larger font scale
        BitmapFont font = new BitmapFont();
        font.getData().setScale(3.0f);
        TextButton.TextButtonStyle playButtonStyle = new TextButton.TextButtonStyle();
        playButtonStyle.font = font;
        playButtonStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
//            TextButton playButton = new TextButton("Play", playButtonStyle);

        // Level selection button
        TextButton levelButton = new TextButton("Play", playButtonStyle);
        levelButton.getLabel().setFontScale(3.0f);
        levelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.goToLevelScreen(); // Navigate to the level screen
            }
        });

        // Settings button
        TextButton.TextButtonStyle settingButtonStyle = new TextButton.TextButtonStyle();
        settingButtonStyle.font = font;
        settingButtonStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
//            TextButton settingButton = new TextButton("Settings", settingButtonStyle);

        Table table = new Table();
        table.center();
        table.setFillParent(true);

//            table.add(titleLabel).padBottom(20).row();
//            table.add(playButton).padBottom(10).row();
        table.add(levelButton).padBottom(10).row();  // Added level button to the home page
//            table.add(settingButton).padBottom(10).row();

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getBatch().begin();
        stage.getBatch().draw(homePageImgTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        stage.getBatch().end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        homePageImgTexture.dispose();
    }
}
