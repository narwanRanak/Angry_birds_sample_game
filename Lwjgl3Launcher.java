package clg.birds.lwjgl3;

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

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {

    // Main method added as the entry point of the application
    public static void main(String[] args) {
        createApplication();  // Call to create the application and start the game
    }

    // This method creates and launches the LibGDX application
    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    // Configuration settings for the game (window size, title, etc.)
    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
//        configuration.setTitle("Angry Birds Game");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        configuration.setWindowedMode(800, 600);  // Set window size to 800x600
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }

    // HomePage class which implements the Screen interface
    public static class HomePage implements Screen {
        private Stage stage;
        private Skin skin;
        private Main game;
        private Texture homePageImgTexture;
        private Viewport viewport;
        private Texture playTexture;

        public HomePage(Main game) {
            this.game = game;

            // Initialize the viewport and stage
            viewport = new StretchViewport(800, 600);
            stage = new Stage(viewport);

            // Load the background image
            homePageImgTexture = new Texture(Gdx.files.internal("MainScreen.png"));
            playTexture = new Texture(Gdx.files.internal("play.png"));

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
            levelButton.getLabel().setFontScale(20.0f);
            levelButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.goToLevelScreen(); // Navigate to the level screen
                }
            });



            Table table = new Table();
            table.center();
            table.setFillParent(true);


            table.add(levelButton).padBottom(10).row();  // Added level button to the home page
            Image background = new Image(homePageImgTexture);
            background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Match screen dimensions
            background.setTouchable(Touchable.disabled);
            stage.addActor(table);
            stage.addActor(background); // Add the background first
            Image play = new Image(playTexture);
            play.setSize(150,150);
            play.setPosition(300,100);
            play.setTouchable(Touchable.disabled);
            stage.addActor(play);
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
}
