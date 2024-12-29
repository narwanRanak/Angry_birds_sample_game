package clg.birds.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LevelScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture; // Background texture
    private Texture levelTexture;
    private Texture level1Texture;
    private Texture level2Texture;

    public LevelScreen(Main game, Integer num) {
        this.game = game;
        if (num != -1) {
            game.saveGame(num);
        }
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load the background texture
        backgroundTexture = new Texture(Gdx.files.internal("LevelScreen.png")); // Replace with your PNG file
        levelTexture = new Texture(Gdx.files.internal("level.png"));
        level1Texture = new Texture(Gdx.files.internal("level1.png"));
        level2Texture = new Texture(Gdx.files.internal("level2.png"));


        // Create buttons
        TextButton level1Button = new TextButton("Level 1", skin);
        TextButton level2Button = new TextButton("Level 2", skin);
        TextButton level3Button = new TextButton("Level 3", skin);
        TextButton backButton = new TextButton("Back", skin);

        // Add listeners to buttons
        level1Button.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.goToGameScreen(); // Navigate to Level 1
            }
        });

        level2Button.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new Game2Screen(game)); // Navigate to Level 2
            }
        });

        level3Button.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new Game3Screen((game)));
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new Lwjgl3Launcher.HomePage(game)); // Navigate back to HomePage
            }
        });

        // Set custom positions for buttons
        level1Button.setPosition(250, 450); // x=150, y=400
        level1Button.setSize(300, 75); // Set custom size
        level1Button.getLabel().setFontScale(2.0f); // Increase text size
        level2Button.setPosition(250, 325); // x=150, y=300
        level2Button.setSize(300, 75); // Set custom size
        level2Button.getLabel().setFontScale(2.0f); // Increase text size
        level3Button.setPosition(250, 200); // x=150, y=200
        level3Button.setSize(300, 75); // Set custom size
        level3Button.getLabel().setFontScale(2.0f); // Increase text size
        backButton.setPosition(250, 75);  // x=150, y=100
        backButton.setSize(300, 100); // Set custom size
        backButton.getLabel().setFontScale(2.0f); // Increase text size


        // Add buttons directly to the stage
        stage.addActor(level1Button);
        stage.addActor(level2Button);
        stage.addActor(level3Button);
        stage.addActor(backButton);
        // Add the background image
        Image background = new Image(backgroundTexture);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Match screen dimensions
        background.setTouchable(Touchable.disabled);
        stage.addActor(background); // Add the background first
        Image level = new Image(levelTexture);
        level.setSize(200,100);
        level.setPosition(300,430);
        level.setTouchable(Touchable.disabled);
        stage.addActor(level);
        Image level1 = new Image(level1Texture);
        level1.setSize(200,100);
        level1.setPosition(300,300);
        level1.setTouchable(Touchable.disabled);
        stage.addActor(level1);
        Image level2 = new Image(level2Texture);
        level2.setSize(200,100);
        level2.setPosition(300,175);
        level2.setTouchable(Touchable.disabled);
        stage.addActor(level2);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        stage.dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
        levelTexture.dispose();
        level1Texture.dispose();
        level2Texture.dispose();
    }
}
