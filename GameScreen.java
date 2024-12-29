package clg.birds.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.io.Serializable;
import java.util.Queue;
import java.util.LinkedList;
import clg.birds.lwjgl3.Pig;
import clg.birds.lwjgl3.Stick;
import clg.birds.lwjgl3.WOOD;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen, Serializable {
    private Stage stage;
    private Viewport viewport;
    private Texture backgroundTexture;
    private Texture redTexture;
    private Texture blueTexture;
    private Texture yellowTexture;
    private Texture pigTexture;
    private Texture kingTexture;
    private Texture catTexture;
    private Texture WOODTexture;
    private Texture stickTexture;
    private Skin skin;
    private Main game;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    public static final float PPM = 100f; // 100 pixels per meter
    private Queue<Runnable> deferredActions;
    private static final float REMOVAL_THRESHOLD_Y = 2.4f;

    private class Bird {
        Body body;
        Image image;
        WeldJoint joint;
        boolean isLaunched;
        Vector2 initialPosition;

        public Bird(Body body, Image image, Vector2 initialPosition) {
            this.body = body;
            this.image = image;
            this.joint = null;
            this.isLaunched = false;
            this.initialPosition = initialPosition;
            body.setUserData(this); // Set UserData to reference the Bird instance
        }
    }

    private List<Bird> birds;
    private int currentBirdIndex = 0;
    private WeldJoint currentBirdJoint;

    private Body catapultAnchor;
    private boolean isDragging = false;
    private Image catImage;
    private Image WOODImage;
    private Image stickImage;
    private Image newstickImage;
    private Array<Vector2> trajectoryPoints;
    private ShapeRenderer shapeRenderer;
    private List<Pig> pigs;
    private boolean isPaused = false;
    private Table pauseMenuTable;
    final float initialBirdPosX = 1.3f;
    final float initialBirdPosY = 2.1f;
    final float xOffset = 0.2f;
    final float yOffset = 0.0f;
    private List<Stick> sticks;
    private Stick stick;
    private List<WOOD> woods;
    private Pig pig;
    private WOOD wood;
    private boolean isFirstBirdLaunched = false;

    public GameScreen(Main game) {
        this.game = game;

        world = new World(new Vector2(0, -9.8f), true);
        debugRenderer = new Box2DDebugRenderer();
        deferredActions = new LinkedList<>();

        float floorXMin = 5.5f;
        float floorXMax = 7.0f;
        float floorY = 2.5f;
        createFloor(floorXMin, floorXMax, floorY);

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();
                if (isWOODFixture(fixtureA)) {
                    wood.setDynamic(world, deferredActions);
                } else if (isWOODFixture(fixtureB)) {
                    wood.setDynamic(world, deferredActions);
                }
                if (isStickFixture(fixtureA)) {
                    System.out.println("Stick Leg hit");
                    Stick stick = (Stick) fixtureA.getBody().getUserData();
                    stick.setDynamic(world, deferredActions);
                } else if (isStickFixture(fixtureB)) {
                    System.out.println("Stick Leg Hit B");
                    Stick stick = (Stick) fixtureB.getBody().getUserData();
                    stick.setDynamic(world, deferredActions);
                }
                // Check if a bird has hit a pig
                if (isBirdFixture(fixtureA) && isPigFixture(fixtureB)) {
                    System.out.println("Bird hit Pig!");
                    Pig pig = (Pig) fixtureB.getBody().getUserData();
                    pig.setDynamic(world, deferredActions); // Defer setting to dynamic
                } else if (isPigFixture(fixtureA) && isBirdFixture(fixtureB)) {
                    System.out.println("Pig hit by Bird!");
                    Pig pig = (Pig) fixtureA.getBody().getUserData();
                    pig.setDynamic(world, deferredActions); // Defer setting to dynamic
                }

                if (isBirdFixture(fixtureA) && isWOODFixture(fixtureB)) {
                    WOOD wood = (WOOD) fixtureB.getBody().getUserData();
                    wood.setDynamic(world, deferredActions);
                } else if (isWOODFixture(fixtureA) && isBirdFixture(fixtureB)) {
                    WOOD wood = (WOOD) fixtureA.getBody().getUserData();
                    wood.setDynamic(world, deferredActions);
                }
            }

            @Override
            public void endContact(Contact contact) {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        viewport = new StretchViewport(800, 600, camera);
        stage = new Stage(viewport);

        backgroundTexture = new Texture(Gdx.files.internal("level_1_screen.png"));
        redTexture = new Texture(Gdx.files.internal("redBird.png"));
        blueTexture = new Texture(Gdx.files.internal("blueBIrd.png"));
        yellowTexture = new Texture(Gdx.files.internal("yellowBird.png"));
        pigTexture = new Texture(Gdx.files.internal("pig.png"));
        kingTexture = new Texture(Gdx.files.internal("king.png"));
        catTexture = new Texture(Gdx.files.internal("cat.png"));
        WOODTexture = new Texture(Gdx.files.internal("WOOD.png"));
        stickTexture = new Texture(Gdx.files.internal("stick.png"));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Image backgroundImage = new Image(backgroundTexture);
        catImage = new Image(catTexture);
        catImage.setSize(70, 70); // Ensure a reasonable size
        catImage.setPosition(100, 150); // Update to match your desired position
        Image kingImage = new Image(kingTexture);

        WOODImage = new Image(WOODTexture);
        stickImage = new Image(stickTexture);
        newstickImage = new Image(stickTexture);



//        catImage.setSize(70, 70);
//        kingImage.setSize(25, 30);
//        WOODImage.setPosition(660, 300);
//        WOODImage.setSize(40, 40);
//        stickImage.setPosition(600, 250);
//        stickImage.setSize(10, 40);
//        newstickImage.setPosition(660, 250);
//        newstickImage.setSize(10, 40);

        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
//        stage.addActor(catImage);

        pigs = new ArrayList<>();
        Vector2 pigPosition1 = new Vector2(6.15f, 1.6f);
        Vector2 pigPosition2 = new Vector2(6.75f, 1.6f);
        Vector2 kingPosition = new Vector2(6.15f, 2.35f);

        Pig pig1 = new Pig(world, pigTexture, pigPosition1, stage, PPM);
        Pig pig2 = new Pig(world, pigTexture, pigPosition2, stage, PPM);
        Pig kingPig = new Pig(world, kingTexture, kingPosition, stage, PPM);
        kingPig.isKing = true;

        pigs.add(pig1);
        pigs.add(pig2);
        pigs.add(kingPig);

        sticks = new ArrayList<>();
        Vector2 stickPosition1 = new Vector2(6f, 1.7f);
        Vector2 stickPosition2 = new Vector2(6.3f, 1.7f);
        Stick stick1 = new Stick(world, stickTexture, stickPosition1, stage, PPM);
        Stick stick2 = new Stick(world, stickTexture, stickPosition2, stage, PPM);
        sticks.add(stick1);
        sticks.add(stick2);

        Vector2 woodPosition = new Vector2(6.15f, 2.1f);
        wood = new WOOD(world, WOODTexture, woodPosition, stage, PPM);

        shapeRenderer = new ShapeRenderer();
        trajectoryPoints = new Array<>();

        birds = new ArrayList<>();
        int numberOfBirds = 6;

        BodyDef anchorDef = new BodyDef();
        anchorDef.type = BodyDef.BodyType.StaticBody;
        float anchorPosX = 120 / PPM;
        float anchorPosY = 200 / PPM;
        anchorDef.position.set(anchorPosX, anchorPosY);
        catapultAnchor = world.createBody(anchorDef);
        stage.addActor(catImage);

        Image birdImage;

        for (int i = 0; i < numberOfBirds; i++) {
            if (i % 3 == 0) {
                birdImage = new Image(redTexture);
            } else if (i % 3 == 1) {
                birdImage = new Image(blueTexture);
            } else {
                birdImage = new Image(yellowTexture);
            }

            birdImage.setSize(25, 25);

            float birdStartX = initialBirdPosX - i * xOffset;
            float birdStartY = initialBirdPosY - i * yOffset;
            Vector2 birdStartPos = new Vector2(birdStartX, birdStartY);

            BodyDef birdDef = new BodyDef();
            birdDef.type = BodyDef.BodyType.KinematicBody;
            birdDef.position.set(birdStartPos);
            Body birdBody = world.createBody(birdDef);

            CircleShape birdShape = new CircleShape();
            birdShape.setRadius(0.2f);

            FixtureDef birdFixture = new FixtureDef();
            birdFixture.shape = birdShape;
            birdFixture.density = 1.5f;
            birdFixture.restitution = 0.5f;
            birdFixture.friction = 0.5f;
            birdBody.createFixture(birdFixture);
            birdShape.dispose();


            birdImage.setPosition(
                birdBody.getPosition().x * PPM - birdImage.getWidth() / 2,
                birdBody.getPosition().y * PPM - birdImage.getHeight() / 2
            );

            stage.addActor(birdImage);

            Bird bird = new Bird(birdBody, birdImage, birdStartPos);
            birds.add(bird);
            System.out.println("Initializing bird " + i);
            System.out.println("Position: " + birdStartPos + ", Body Type: " + birdBody.getType());

        }

        if (!birds.isEmpty()) {
            Bird firstBird = birds.get(currentBirdIndex);
            setupBirdJoint(firstBird);

            Vector2 anchorPos = catapultAnchor != null ? catapultAnchor.getPosition() : new Vector2(1.3f, 3.15f);
            firstBird.image.setPosition(
                anchorPos.x * PPM - firstBird.image.getWidth() / 2,
                anchorPos.y * PPM - firstBird.image.getHeight() / 2
            );
//            stage.addActor(firstBird.image);
        }

        if (!skin.has("default-font", BitmapFont.class)) {
            skin.add("default-font", new BitmapFont());
        }

        TextButton.TextButtonStyle pauseButtonStyle = new TextButton.TextButtonStyle();
        pauseButtonStyle.font = skin.getFont("default-font");
        pauseButtonStyle.fontColor = Color.WHITE;

        TextButton pauseButton = new TextButton("Pause", pauseButtonStyle);
        pauseButton.getLabel().setFontScale(3.0f);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.top();
        table.add(pauseButton).padTop(20).expandX().right();

        CircleShape anchorShape = new CircleShape();
        anchorShape.setRadius(0.05f);

        FixtureDef anchorFixture = new FixtureDef();
        anchorFixture.shape = anchorShape;
        catapultAnchor.createFixture(anchorFixture);
        anchorShape.dispose();

        if (!birds.isEmpty()) {
            setupBirdJoint(birds.get(currentBirdIndex));
        }

        InputAdapter gameInputAdapter = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (currentBirdIndex >= birds.size()) {
                    System.out.println("All birds are launched!");
                    return false;
                }

                Bird currentBird = birds.get(currentBirdIndex);
                if (!currentBird.isLaunched && button == 0 && currentBirdIndex < birds.size()) {
                    Vector3 worldCoords = viewport.getCamera().unproject(new Vector3(screenX, screenY, 0));
                    Vector2 touchPoint = new Vector2(worldCoords.x / PPM, worldCoords.y / PPM);

                    if (currentBird.body.getPosition().dst(touchPoint) < 0.5f) {
                        isDragging = true;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (isDragging && currentBirdIndex < birds.size()) {
                    Bird currentBird = birds.get(currentBirdIndex);
                    if (currentBird.isLaunched) {
                        return false;
                    }

                    Vector3 touchPos = new Vector3(screenX, screenY, 0);
                    viewport.unproject(touchPos);

                    float maxDragDistance = 0.5f;
                    Vector2 dragVector = new Vector2(touchPos.x / PPM, touchPos.y / PPM).sub(catapultAnchor.getPosition());

                    if (dragVector.len() > maxDragDistance) {
                        dragVector.nor().scl(maxDragDistance);
                        touchPos.x = (catapultAnchor.getPosition().x + dragVector.x) * PPM;
                        touchPos.y = (catapultAnchor.getPosition().y + dragVector.y) * PPM;
                    }

                    currentBird.body.setTransform(touchPos.x / PPM, touchPos.y / PPM, currentBird.body.getAngle());
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (isDragging && button == 0 && currentBirdIndex < birds.size()) {
                    Bird currentBird = birds.get(currentBirdIndex);

                    // Destroy the joint to release the bird
                    if (currentBird.joint != null) {
                        System.out.println("Releasing bird: " + currentBirdIndex);
                        world.destroyJoint(currentBird.joint);
                        currentBird.joint = null;
                    }

                    // Specific fix for the first bird
                    if (currentBirdIndex == 0) {
                        System.out.println("Applying first bird-specific fixes.");
                        currentBird.body.setAwake(true);
                        currentBird.body.setLinearVelocity(Vector2.Zero);
                        currentBird.body.setAngularVelocity(0f);
                    }

                    // Make the bird dynamic and enable gravity
                    currentBird.body.setType(BodyDef.BodyType.DynamicBody);
                    currentBird.body.setGravityScale(1f);

                    // Apply launch force
                    Vector2 launchDirection = new Vector2(catapultAnchor.getPosition()).sub(currentBird.body.getPosition());
                    float forceMultiplier = 7f;
                    float maxLaunchForce = 10f;
                    float launchForce = launchDirection.len() * forceMultiplier;

                    if (launchForce > maxLaunchForce) {
                        launchForce = maxLaunchForce;
                    }

                    // Debug logs for force and direction
                    System.out.println("Bird " + currentBirdIndex + " Launch Direction: " + launchDirection);
                    System.out.println("Bird " + currentBirdIndex + " Launch Force: " + launchForce);

                    launchDirection.nor().scl(launchForce);
                    currentBird.body.applyLinearImpulse(launchDirection, currentBird.body.getWorldCenter(), true);

                    // Update state
                    isDragging = false;
                    currentBird.isLaunched = true;

                    // Move to the next bird
                    currentBirdIndex++;
                    if (currentBirdIndex < birds.size()) {
                        Bird nextBird = birds.get(currentBirdIndex);
                        setupBirdJoint(nextBird);
                        nextBird.body.setTransform(nextBird.initialPosition, nextBird.body.getAngle());
                        nextBird.isLaunched = false;
                    } else {
                        System.out.println("All birds launched!");
                    }
                    return true;
                }
                return false;
            }
        };

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gameInputAdapter);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        stage.addActor(table);

        pauseMenuTable = new Table();
        pauseMenuTable.center();
        pauseMenuTable.setFillParent(true);

        TextButton.TextButtonStyle resumeButtonStyle = new TextButton.TextButtonStyle();
        resumeButtonStyle.font = skin.getFont("default-font");
        resumeButtonStyle.fontColor = Color.WHITE;

        TextButton resumeButton = new TextButton("Resume", resumeButtonStyle);
        resumeButton.getLabel().setFontScale(2.5f);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
            }
        });

        TextButton exitButton = new TextButton("Exit", resumeButtonStyle);
        exitButton.getLabel().setFontScale(2.5f);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelScreen(game,0));
            }
        });

        pauseMenuTable.add(resumeButton).width(200).height(60).padBottom(20).row();
        pauseMenuTable.add(exitButton).width(200).height(60).padBottom(20);
        pauseMenuTable.setVisible(false);
        stage.addActor(pauseMenuTable);
    }

    private void setupBirdJoint(Bird bird) {
        WeldJointDef jointDef = new WeldJointDef();
        jointDef.bodyA = catapultAnchor;
        jointDef.bodyB = bird.body;
        jointDef.collideConnected = false;
        jointDef.localAnchorA.set(0, 0);
        jointDef.localAnchorB.set(0, 0);
        bird.joint = (WeldJoint) world.createJoint(jointDef);
        currentBirdJoint = bird.joint;
    }

    private void createFloor(float xMin, float xMax, float y) {
        BodyDef floorBodyDef = new BodyDef();
        floorBodyDef.type = BodyDef.BodyType.StaticBody;
        floorBodyDef.position.set((xMin + xMax) / 2, y);

        Body floorBody = world.createBody(floorBodyDef);

        PolygonShape floorShape = new PolygonShape();
        floorShape.setAsBox((xMax - xMin) / 2, 0.1f);

        FixtureDef floorFixtureDef = new FixtureDef();
        floorFixtureDef.shape = floorShape;
        floorFixtureDef.friction = 0.5f;
        floorFixtureDef.restitution = 0.0f;

        floorBody.createFixture(floorFixtureDef);
        floorShape.dispose();
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseMenuTable.setVisible(isPaused);
    }

    private void calculateTrajectory() {
        trajectoryPoints.clear();

        if (isDragging && currentBirdIndex < birds.size()) {
            Bird currentBird = birds.get(currentBirdIndex);
            if (currentBird.isLaunched) {
                return;
            }

            Vector2 startPos = new Vector2(currentBird.body.getPosition());
            Vector2 launchDirection = new Vector2(catapultAnchor.getPosition()).sub(currentBird.body.getPosition());
            float forceMultiplier = 7f;
            float maxLaunchForce = 10f;
            float launchForce = launchDirection.len() * forceMultiplier;

            if (launchForce > maxLaunchForce) {
                launchForce = maxLaunchForce;
            }

            Vector2 launchVelocity = launchDirection.nor().scl(launchForce / currentBird.body.getMass());
            int numSteps = 60;
            float timeStep = 1 / 60f;
            float gravity = Math.abs(world.getGravity().y);

            for (int i = 0; i < numSteps; i++) {
                float t = i * timeStep;
                float dx = launchVelocity.x * t;
                float dy = launchVelocity.y * t - 0.5f * gravity * t * t;
                Vector2 trajPoint = new Vector2(startPos.x + dx, startPos.y + dy);
                trajectoryPoints.add(trajPoint);
            }
        }
    }
    private boolean isAnyBirdInMotion() {
        for (Bird bird : birds) {
            if (bird.isLaunched) {
                Vector2 velocity = bird.body.getLinearVelocity();
                float speed = velocity.len(); // Calculate speed
                if (speed > 0.1f) { // Adjust threshold as needed
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isBirdFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Bird;
    }

    private boolean isPigFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Pig;
    }

    private boolean isStickFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Stick;
    }

    private void handleStickHit(Stick stick) {
        stick.dispose();
        sticks.remove(stick);
    }

    private boolean isWOODFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof WOOD;
    }

    private void handlePigHit(Pig pig) {
        pig.dispose();
        pigs.remove(pig);
    }
    @Override
    public void show() {
        // Called when the screen is set.
        System.out.println("GameScreen is now visible.");
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update the physics world if the game is not paused
        if (!isPaused) {
            world.step(1 / 60f, 6, 2);
        }

        // Execute any deferred actions (collisions or other game logic updates)
        while (!deferredActions.isEmpty()) {
            deferredActions.poll().run();
        }

        // If dragging a bird, calculate its trajectory
        if (isDragging && currentBirdIndex < birds.size()) {
            Bird currentBird = birds.get(currentBirdIndex);
            if (!currentBird.isLaunched) {
                calculateTrajectory();
            }
        } else {
            trajectoryPoints.clear();
        }

        // Update the positions of birds based on their body physics
        for (Bird bird : birds) {
            Vector2 birdPosition = bird.body.getPosition();
            bird.image.setPosition(
                birdPosition.x * PPM - bird.image.getWidth() / 2,
                birdPosition.y * PPM - bird.image.getHeight() / 2
            );
        }

        // Update pigs' state (e.g., if they are hit or destroyed)
        for (Pig pig : pigs) {
            pig.update();
        }

        // Check for win/lose conditions after birds have finished moving
        if (!isAnyBirdInMotion()) {
            if (areAllPigsDestroyed()) {
                System.out.println("All pigs destroyed! Transitioning to win screen.");
                game.setScreen(new winlooseScreen(game));
                return;
            }

            if (areAllBirdsLaunched() && !areAllPigsDestroyed()) {
                System.out.println("All birds launched but pigs remain! Transitioning to lose screen.");
                game.setScreen(new loosewinScreen(game));
                return;
            }
        }

        // Render the physics debug lines for troubleshooting
        debugRenderer.render(world, viewport.getCamera().combined);

        // Process the stage (UI and other elements)
        stage.act(delta);
        stage.draw();

        // Draw trajectory points for the current bird if dragging
        if (isDragging && currentBirdIndex < birds.size()) {
            Bird currentBird = birds.get(currentBirdIndex);
            if (!currentBird.isLaunched && trajectoryPoints.size > 0) {
                shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.RED);

                for (Vector2 point : trajectoryPoints) {
                    float screenX = point.x * PPM;
                    float screenY = point.y * PPM;
                    shapeRenderer.circle(screenX, screenY, 3f);
                }

                shapeRenderer.end();
            }
        }
    }


    private boolean areAllPigsDestroyed() {
        for (Pig pig : pigs) {
            if (!pig.isHit) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllBirdsLaunched() {
        for (Bird bird : birds) {
            if (!bird.isLaunched) {
                return false;
            }
        }
        return true;
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
        backgroundTexture.dispose();
        redTexture.dispose();
        pigTexture.dispose();
        kingTexture.dispose();
        catTexture.dispose();
        WOODTexture.dispose();
        stickTexture.dispose();
        skin.dispose();
        world.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();

        if (wood != null) {
            wood.dispose();
        }

        for (Bird bird : birds) {
            bird.image.remove();
        }

        for (Pig pig : pigs) {
            pig.dispose();
        }

        for (Stick stick : sticks) {
            stick.dispose();
        }
    }
}
