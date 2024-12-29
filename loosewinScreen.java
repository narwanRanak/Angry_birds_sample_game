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

public class loosewinScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture looseTexture; // Texture for the background image

    public loosewinScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load the PNG texture
        looseTexture = new Texture(Gdx.files.internal("loose_img.png")); // Replace with your PNG file

        // Create the Exit button
        TextButton exitButton = new TextButton("Go back to Level Screen", skin);

        // Add listener for the Exit button
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new LevelScreen(game,-1)); // Transition to Level Screen
            }
        });

        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true); // Table will take up the entire screen
        stage.addActor(table);

        // Add the button to the table
        table.add(exitButton).width(250).height(120).padTop(400).center();

        // Add the background last so it renders on top
        Image background = new Image(looseTexture);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Match screen dimensions
        background.setTouchable(Touchable.disabled);
        stage.addActor(background);
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
        looseTexture.dispose();
    }
}
