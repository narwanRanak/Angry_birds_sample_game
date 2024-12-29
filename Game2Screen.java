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

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

public class Game2Screen implements Screen, Serializable {
    private Stage stage;
    private Viewport viewport;
    private Texture backgroundTexture;
    private Texture redTexture;
    private Texture blueTexture;
    private Texture yellowTexture;
    private Texture pigTexture;
    private Texture kingTexture;
    private Texture catTexture;
    private Texture glassTexture;
    private Texture glassLegTexture;
    private Texture WOODTexture;
    private Texture stickTexture;
    private Texture landTexture;
    private Texture triangleTexture;
    private Texture glassTriangleTexture;
    private Skin skin;
    private Main game;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    public static final float PPM = 100f; // 100 pixels per meter
    private Queue<Runnable> deferredActions; // Add this to your class
    private static final float REMOVAL_THRESHOLD_Y = 2.4f;



    // New additions for multiple birds
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

    private List<Bird> birds; // List to hold multiple birds
    private int currentBirdIndex = 0; // Index of the current bird
    private WeldJoint currentBirdJoint; // Joint for the current bird

    // Other game elements
    private Body catapultAnchor;
    private boolean isDragging = false;
    // private boolean isBirdLaunched = false;
    private Image catImage;     // Catapult's image
    private Image glassImage;   // Glass box image
    private Image glassLegImage;
    private Image WOODImage;    // Wood box image
    private Image stickImage;
    private Image newglassImage;
    private Image newstickImage;
    private Image landImage;
    private Image newlandImage;
    private Image triangleImage;
    private Image glassTriangleImage;
    private Array<Vector2> trajectoryPoints;
    private ShapeRenderer shapeRenderer;
    private List<Pig> pigs;
    private boolean isPaused = false;    // Track if the game is paused
    private boolean isFirstBirdLaunched = false; // Track if the first bird is launched
    private Table pauseMenuTable;
    final float initialBirdPosX = 1.3f;
    final float initialBirdPosY = 3.15f;
    final float xOffset = 0.2f; // Variable x offset per bird
    final float yOffset = 0.0f; // Fixed y offset per bird   // Table for pause menu buttons (Resume, Exit)
    private List <Stick> sticks;
    private List <GlassLeg> glassLegs;
    private List <Land> lands;
    private List<WOOD> woods;
    private WOOD wood; // Single WOOD object
    private Glass glass; // Single Glass object

    public Game2Screen(Main game) {
        this.game = game;

        // Initialize the physics world
        world = new World(new Vector2(0, -9.8f), true); // Gravity in the negative Y direction
        debugRenderer = new Box2DDebugRenderer(); // Optional for debugging
        deferredActions = new LinkedList<>();

        // Create floor
        float floorXMin = 5.5f; // World units
        float floorXMax = 7.0f; // World units
        float floorY = 2.5f;    // World units
        createFloor(floorXMin, floorXMax, floorY);


        // Set ContactListener for collision detection
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
                if (isGlassFixture(fixtureA)) {
                    glass.setDynamic(world, deferredActions);
                } else if (isGlassFixture(fixtureB)) {
                    glass.setDynamic(world, deferredActions);
                }
                if (isGlassLegFixture(fixtureA)) {
                    System.out.println("Glass Leg hit");
                    GlassLeg glassLeg = (GlassLeg) fixtureA.getBody().getUserData();
                    glassLeg.setDynamic(world, deferredActions);
                } else if (isGlassLegFixture(fixtureB)) {
                    System.out.println("Glass Leg Hit B");
                    GlassLeg glassLeg = (GlassLeg) fixtureB.getBody().getUserData();
                    glassLeg.setDynamic(world, deferredActions);
                }
                if (isLandFixture(fixtureA)) {
                    Land land = (Land) fixtureA.getBody().getUserData();
                    land.setDynamic(world, deferredActions);
                } else if (isLandFixture(fixtureB)) {
                    Land land = (Land) fixtureB.getBody().getUserData();
                    land.setDynamic(world, deferredActions);
                }
                if (isStickFixture(fixtureA) && isStickFixture(fixtureB)) {
                    System.out.println("Bird hit stick!");
                    Stick stick = (Stick) fixtureB.getBody().getUserData();
                    stick.setDynamic(world, deferredActions); // Defer setting to dynamic
                } else if (isStickFixture(fixtureA) && isBirdFixture(fixtureB)) {
                    System.out.println("Stick hit by Bird!");
                    Stick stick = (Stick) fixtureA.getBody().getUserData();
                    stick.setDynamic(world, deferredActions); // Defer setting to dynamic
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
                // Check if a bird hits a glass or wood object
                if (isBirdFixture(fixtureA) && isGlassLegFixture(fixtureB)) {
                    GlassLeg glass = (GlassLeg) fixtureB.getBody().getUserData();
                    glass.setDynamic(world, deferredActions);
                } else if (isGlassLegFixture(fixtureA) && isBirdFixture(fixtureB)) {
                    GlassLeg glass = (GlassLeg) fixtureA.getBody().getUserData();
                    glass.setDynamic(world, deferredActions);
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



        // Initialize viewport and stage
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600); // World size
        viewport = new StretchViewport(800, 600, camera);
        stage = new Stage(viewport);

        // Load textures
        backgroundTexture = new Texture(Gdx.files.internal("Game2Screen.png"));
        redTexture = new Texture(Gdx.files.internal("redBird.png"));
        blueTexture = new Texture(Gdx.files.internal("blueBIrd.png"));
        yellowTexture = new Texture(Gdx.files.internal("yellowBird.png"));
        pigTexture = new Texture(Gdx.files.internal("pig.png"));
        kingTexture = new Texture(Gdx.files.internal("king.png"));
        catTexture = new Texture(Gdx.files.internal("cat.png"));
        glassTexture = new Texture(Gdx.files.internal("glass.png")); // Texture for glass box
        glassLegTexture = new Texture(Gdx.files.internal("glassLeg.png"));
        WOODTexture = new Texture(Gdx.files.internal("WOOD.png"));   // Texture for WOOD box
        stickTexture = new Texture(Gdx.files.internal("stick.png"));
        landTexture = new Texture(Gdx.files.internal("land.png"));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        triangleTexture = new Texture(Gdx.files.internal("triangle.png"));
        glassTriangleTexture = new Texture(Gdx.files.internal("glassTriangle.png"));

        // Create image actors for game elements
        Image backgroundImage = new Image(backgroundTexture);
        // redBirdImage = new Image(redTexture); // Removed single bird image
        catImage = new Image(catTexture);
        Image kingImage = new Image(kingTexture);

        // Create image actors for glass and WOOD boxes
        glassImage = new Image(glassTexture);
        glassLegImage = new Image(glassLegTexture);
        WOODImage = new Image(WOODTexture);
        stickImage = new Image(stickTexture);
        newstickImage = new Image(stickTexture); // Reuse the existing stick texture
        newglassImage = new Image(glassLegTexture);
        newlandImage = new Image(landTexture);
        landImage = new Image(landTexture);
        triangleImage = new Image(triangleTexture);
        glassTriangleImage = new Image(glassTriangleTexture);

        // Set sizes for images
        // redBirdImage.setSize(25, 25); // Removed single bird image
        catImage.setSize(70, 70);

        kingImage.setSize(25, 30);

        // Set positions and sizes for the glass and WOOD boxes
        glassImage.setPosition(600, 300); // Position glass box around pig
        glassImage.setSize(40, 40);
        glassLegImage.setPosition(630, 250);
        glassLegImage.setSize(10, 40);
        WOODImage.setPosition(660, 300); // Position WOOD box
        WOODImage.setSize(40, 40);
        stickImage.setPosition(600, 250);
        stickImage.setSize(10, 40);
        newstickImage.setPosition(660, 250);
        newstickImage.setSize(10, 40);
        newglassImage.setPosition(690, 250);
        newglassImage.setSize(10, 40);
        landImage.setPosition(600, 290);
        landImage.setSize(100, 10);
        newlandImage.setPosition(600, 340);
        newlandImage.setSize(100, 10);
        triangleImage.setPosition(600, 350);
        triangleImage.setSize(40, 40);
        glassTriangleImage.setPosition(660, 350);
        glassTriangleImage.setSize(40, 40);

        // Add images to the stage in the correct order for layering
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
        stage.addActor(catImage);     // Add catapult last to ensure it is in front

        // Initialize pigs list
        pigs = new ArrayList<>();

        // Define positions for your pigs
        Vector2 pigPosition1 = new Vector2(6.15f, 3.32f); // Example position
        Vector2 pigPosition2 = new Vector2(6.75f, 3.32f); // Example position
        Vector2 kingPosition = new Vector2(6.5f,3.7f); // king pig position

        // Create Pig instances and add them to the list
        Pig pig1 = new Pig(world, pigTexture, pigPosition1, stage, PPM);
        Pig pig2 = new Pig(world, pigTexture, pigPosition2, stage, PPM);
        Pig king = new Pig(world, kingTexture, kingPosition, stage, PPM); // Create king as a pig
        king.isKing = true;


        pigs.add(pig1);
        pigs.add(pig2);
        pigs.add(king);
        sticks = new ArrayList<>();
        Vector2 stickPosition1 = new Vector2(6f, 2.9f); // Example position
        Vector2 stickPosition2 = new Vector2(6.6f, 2.9f); // Example position
        Stick stick1 = new Stick(world, stickTexture, stickPosition1, stage, PPM);
        Stick stick2 = new Stick(world, stickTexture, stickPosition2, stage, PPM);
        sticks.add(stick1);
        sticks.add(stick2);
        glassLegs = new ArrayList<>();
        Vector2 glassLegPosition1 = new Vector2(6.3f, 2.9f); // Example position
        Vector2 glassLegPosition2 = new Vector2(6.9f, 2.9f); // Example position
        GlassLeg glassLeg1 = new GlassLeg(world, glassLegTexture, glassLegPosition1, stage, PPM);
        GlassLeg glassLeg2 = new GlassLeg(world, glassLegTexture, glassLegPosition2, stage, PPM);
        glassLegs.add(glassLeg1);
        glassLegs.add(glassLeg2);
        lands= new ArrayList<>();
        Vector2 landPosition1 = new Vector2(6.45f, 3.1f); // Example position
        Vector2 landPosition2 = new Vector2(6.45f, 3.55f); // Example position
        Land land1 = new Land(world, landTexture, landPosition1, stage, PPM);
        Land land2 = new Land(world, landTexture, landPosition2, stage, PPM);
        lands.add(land1);
        lands.add(land2);
        Vector2 woodPosition = new Vector2(6.15f, 3.35f); // Example position
        wood = new WOOD(world, WOODTexture, woodPosition, stage, PPM);

        Vector2 glassPosition = new Vector2(6.75f, 3.35f); // Example position
        glass = new Glass(world, glassTexture, glassPosition, stage, PPM);

        // Initialize shapeRenderer for drawing the trajectory
        shapeRenderer = new ShapeRenderer();

        // Initialize the array to store trajectory points
        trajectoryPoints = new Array<>();

        // Initialize birds list
        birds = new ArrayList<>();

        // Number of birds you want to add
        int numberOfBirds = 9;

        // Create static body for the catapult anchor
        BodyDef anchorDef = new BodyDef();
        anchorDef.type = BodyDef.BodyType.StaticBody;
        float anchorPosX = 130 / PPM;
        float anchorPosY = 300 / PPM;
        anchorDef.position.set(anchorPosX, anchorPosY);
        catapultAnchor = world.createBody(anchorDef);

        // Initialize birds
        Image birdImage = new Image(blueTexture);

        for (int i = 0; i < numberOfBirds; i++) {
            // Create image for the bird
            if(i % 3 == 0){
                birdImage = new Image(redTexture);
            }
            else if(i % 3 == 1){
                birdImage = new Image(blueTexture);
            }
            else{
                birdImage = new Image(yellowTexture);
            }

            birdImage.setSize(25, 25);

            // Calculate bird's start position in world units
            float birdStartX = initialBirdPosX - i * xOffset; // Adjust direction as needed
            float birdStartY = initialBirdPosY - i * yOffset;
            Vector2 birdStartPos = new Vector2(birdStartX, birdStartY);

            // Create physics body for the bird
            BodyDef birdDef = new BodyDef();
            birdDef.type = BodyDef.BodyType.KinematicBody; // Start as kinematic
            birdDef.position.set(birdStartPos); // Start position on the catapult
            Body birdBody = world.createBody(birdDef);

            // Define shape and fixture for the bird
            CircleShape birdShape = new CircleShape();
            birdShape.setRadius(0.2f); // Adjust size of the bird

            FixtureDef birdFixture = new FixtureDef();
            birdFixture.shape = birdShape;
            birdFixture.density = 1.5f;      // Increased density for more mass
            birdFixture.restitution = 0.5f;  // Increased bounciness
            birdFixture.friction = 0.5f;      // Friction remains the same
            birdBody.createFixture(birdFixture);
            birdShape.dispose();

            // Set bird image position to match the bird body
            birdImage.setPosition(
                birdBody.getPosition().x * PPM - birdImage.getWidth() / 2,
                birdBody.getPosition().y * PPM - birdImage.getHeight() / 2
            );

            // Add bird image to the stage
            stage.addActor(birdImage);

            // Create a Bird instance and add to the list
            Bird bird = new Bird(birdBody, birdImage, birdStartPos);
            birds.add(bird);
        }

        // Position and setup the first bird's joint
        if (!birds.isEmpty()) {
            Bird firstBird = birds.get(currentBirdIndex);
            setupBirdJoint(firstBird);

            // Set the initial position of the first bird image to match the catapult
            Vector2 anchorPos = catapultAnchor != null ? catapultAnchor.getPosition() : new Vector2(1.3f, 3.15f);
            firstBird.image.setPosition(
                anchorPos.x * PPM - firstBird.image.getWidth() / 2,
                anchorPos.y * PPM - firstBird.image.getHeight() / 2
            );
            stage.addActor(firstBird.image);
        }

        // Pause button to toggle game pause/resume
        if (!skin.has("default-font", BitmapFont.class)) {
            skin.add("default-font", new BitmapFont()); // Create a new BitmapFont if not found in the skin
        }

        // Score label
        Label scoreLabel = new Label("Score: 0", skin);
        scoreLabel.setFontScale(3.0f);

        // Create a custom TextButtonStyle for the Pause button
        TextButton.TextButtonStyle pauseButtonStyle = new TextButton.TextButtonStyle();
        pauseButtonStyle.font = skin.getFont("default-font"); // Use the default font from the skin
        pauseButtonStyle.fontColor = Color.WHITE; // Set the font color
        pauseButtonStyle.up = null; // No background when the button is not pressed
        pauseButtonStyle.down = null; // No background when the button is pressed

        // Create the Pause button using the custom style
        TextButton pauseButton = new TextButton("Pause", pauseButtonStyle);
        pauseButton.getLabel().setFontScale(3.0f);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause(); // Call the toggle pause method
            }
        });

        // Create layout with Table
        Table table = new Table();
        table.setFillParent(true);
        table.top();
        table.add(scoreLabel).padTop(20).expandX().left();
        table.add(pauseButton).padTop(20).expandX().right();

        // Initialize the CircleShape for the anchor
        CircleShape anchorShape = new CircleShape();
        anchorShape.setRadius(0.05f); // Small radius in world units

        // Create the fixture definition for the anchor
        FixtureDef anchorFixture = new FixtureDef();
        anchorFixture.shape = anchorShape;

        // Attach the fixture to the anchor body
        catapultAnchor.createFixture(anchorFixture);

        // Dispose of the shape after use
        anchorShape.dispose();

        // Initialize the current bird's joint
        if (!birds.isEmpty()) {
            setupBirdJoint(birds.get(currentBirdIndex));
        }

        // Add input processors to handle dragging and UI
        // Create your custom InputAdapter
        InputAdapter gameInputAdapter = new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (currentBirdIndex >= birds.size()) {
                    System.out.println("All birds are launched!"); // Print message when no more birds are available
                    return false; // Prevent further interaction
                }

                System.out.println("touchDown called");
                Bird currentBird = birds.get(currentBirdIndex);
                if (!currentBird.isLaunched && button == 0 && currentBirdIndex < birds.size()) { // Left mouse button
                    Vector3 worldCoords = viewport.getCamera().unproject(new Vector3(screenX, screenY, 0));
                    System.out.println(worldCoords.x + " " + worldCoords.y);
                    System.out.println(currentBird.body.getPosition().x + " " + currentBird.body.getPosition().y);
                    Vector2 touchPoint = new Vector2(worldCoords.x / PPM, worldCoords.y / PPM);

                    // Check if the click is near the bird
                    if (currentBird.body.getPosition().dst(touchPoint) < 0.5f) {
                        System.out.println("touchpoint print");
                        isDragging = true;
                        return true; // Consume the event
                    }
                }
                return false; // Event not consumed
            }


            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                System.out.println("touchDragged called");
                if (isDragging && currentBirdIndex < birds.size()) {
                    Bird currentBird = birds.get(currentBirdIndex);
                    if (currentBird.isLaunched) {
                        // Bird already launched, do not allow dragging
                        return false;
                    }

                    // Convert screen coordinates to world coordinates
                    Vector3 touchPos = new Vector3(screenX, screenY, 0);
                    viewport.unproject(touchPos);

                    // Log the positions for debugging
                    System.out.println("Screen coordinates: x=" + screenX + ", y=" + screenY);
                    System.out.println("Unprojected world coordinates: x=" + touchPos.x + ", y=" + touchPos.y);

                    // Limit the drag area
                    float maxDragDistance = 0.5f; // Increased from 0.5f to 1.0f
                    Vector2 dragVector = new Vector2(touchPos.x / PPM, touchPos.y / PPM).sub(catapultAnchor.getPosition());

                    if (dragVector.len() > maxDragDistance) {
                        dragVector.nor().scl(maxDragDistance);
                        touchPos.x = (catapultAnchor.getPosition().x + dragVector.x) * PPM;
                        touchPos.y = (catapultAnchor.getPosition().y + dragVector.y) * PPM;
                    }

                    // Update the bird's position
                    currentBird.body.setTransform(touchPos.x / PPM, touchPos.y / PPM, currentBird.body.getAngle());

                    // Log the bird's new position
                    Vector2 newBirdPos = currentBird.body.getPosition();
                    System.out.println("Bird's new position: x=" + newBirdPos.x + ", y=" + newBirdPos.y);

                    return true; // Consume the event
                }
                return false; // Event not consumed
            }



            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                System.out.println("touchUp called");
                if (isDragging && button == 0 && currentBirdIndex < birds.size()) { // Left mouse button
                    // Release the bird
                    Bird currentBird = birds.get(currentBirdIndex);
                    if (currentBird.joint != null) {
                        world.destroyJoint(currentBird.joint);
                        currentBird.joint = null;
                    }

                    // Change bird's body type to Dynamic
                    currentBird.body.setType(BodyDef.BodyType.DynamicBody);

                    // Enable gravity for the bird
                    currentBird.body.setGravityScale(1f);

                    // Calculate launch impulse based on drag distance
                    Vector2 launchDirection = new Vector2(catapultAnchor.getPosition()).sub(currentBird.body.getPosition());

                    // Increase the launch force multiplier here
                    float forceMultiplier = 7f; // Increased multiplier
                    float maxLaunchForce = 10f;   // Increased max launch force
                    float launchForce = launchDirection.len() * forceMultiplier; // Adjusted multiplier

                    if (launchForce > maxLaunchForce) {
                        launchForce = maxLaunchForce;
                    }

                    // Normalize the launch direction and scale by launch force
                    launchDirection.nor().scl(launchForce);

                    // Apply the impulse
                    currentBird.body.applyLinearImpulse(launchDirection, currentBird.body.getWorldCenter(), true);

                    isDragging = false;
                    currentBird.isLaunched = true; // Mark the bird as launched

                    // Move to the next bird after launching
                    currentBirdIndex++;
                    if (currentBirdIndex < birds.size()) {
                        Bird nextBird = birds.get(currentBirdIndex);
                        setupBirdJoint(nextBird);
                        // Reset the next bird's position to its initial position
                        nextBird.body.setTransform(nextBird.initialPosition, nextBird.body.getAngle());
                        nextBird.isLaunched = false; // Reset the flag for the next bird
                    } else {
                        System.out.println("All birds are launched!");
                        // Do not attempt to access birds[currentBirdIndex] anymore
                    }

                    return true; // Consume the event
                }
                return false; // Event not consumed
            }};

        // Set up the InputMultiplexer
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gameInputAdapter); // Add your InputAdapter first
        inputMultiplexer.addProcessor(stage); // Then add the stage
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Add table to stage
        stage.addActor(table);

        // Create pause menu (initially hidden)
        pauseMenuTable = new Table();
        pauseMenuTable.center();
        pauseMenuTable.setFillParent(true);

        // Resume button with custom style
        TextButton.TextButtonStyle resumeButtonStyle = new TextButton.TextButtonStyle();
        resumeButtonStyle.font = skin.getFont("default-font");
        resumeButtonStyle.fontColor = Color.WHITE;

        // Create the Resume button using the custom style
        TextButton resumeButton = new TextButton("Resume", resumeButtonStyle);
        resumeButton.getLabel().setFontScale(2.5f);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause(); // Resumes the game
            }
        });

        // Exit button with custom style
        TextButton exitButton = new TextButton("Exit", resumeButtonStyle);
        exitButton.getLabel().setFontScale(2.5f);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LevelScreen(game,1)); // Go back to Level Screen
            }
        });

        // Add buttons to the pause menu table with spacing
        pauseMenuTable.add(resumeButton).width(200).height(60).padBottom(20).row();
        pauseMenuTable.add(exitButton).width(200).height(60).padBottom(20);

        // Initially hide the pause menu
        pauseMenuTable.setVisible(false);
        stage.addActor(pauseMenuTable);
    }

    /**
     * Sets up a WeldJoint for the specified bird to attach it to the catapult anchor.
     *
     * @param bird The bird to attach.
     */
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
    private void makeComponentsDynamic() {
        // Convert all sticks to dynamic
        for (Stick stick : sticks) {
            stick.body.setType(BodyDef.BodyType.DynamicBody);
            stick.body.setLinearDamping(0.5f); // Optional: Add damping for smoother movement
            stick.body.setAngularDamping(0.5f);
        }

        // Convert all glass legs to dynamic
        for (GlassLeg glassLeg : glassLegs) {
            glassLeg.body.setType(BodyDef.BodyType.DynamicBody);
            glassLeg.body.setLinearDamping(0.5f);
            glassLeg.body.setAngularDamping(0.5f);
        }

        // Convert all lands to dynamic
        for (Land land : lands) {
            land.body.setType(BodyDef.BodyType.DynamicBody);
            land.body.setLinearDamping(0.5f);
            land.body.setAngularDamping(0.5f);
        }

        // Convert WOOD to dynamic
        if (wood != null) {
            wood.body.setType(BodyDef.BodyType.DynamicBody);
            wood.body.setLinearDamping(0.5f);
            wood.body.setAngularDamping(0.5f);
        }

        // Convert Glass to dynamic
        if (glass != null) {
            glass.body.setType(BodyDef.BodyType.DynamicBody);
            glass.body.setLinearDamping(0.5f);
            glass.body.setAngularDamping(0.5f);
        }
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
        isPaused = !isPaused;  // Toggle the pause state
        pauseMenuTable.setVisible(isPaused);  // Show or hide the pause menu based on pause state
    }

    private void calculateTrajectory() {
        trajectoryPoints.clear();

        if (isDragging && currentBirdIndex < birds.size()) {
            Bird currentBird = birds.get(currentBirdIndex);
            if (currentBird.isLaunched) {
                return; // No trajectory to calculate if the bird is already launched
            }

            Vector2 startPos = currentBird.body.getPosition(); // Starting position
            Vector2 launchDirection = new Vector2(catapultAnchor.getPosition()).sub(currentBird.body.getPosition());

            // Use the updated launch force multiplier
            float forceMultiplier = 7f; // Same as touchUp
            float maxLaunchForce = 10f; // Same as touchUp
            float launchForce = launchDirection.len() * forceMultiplier;

            if (launchForce > maxLaunchForce) {
                launchForce = maxLaunchForce;
            }

            // Normalize the launch direction and calculate initial velocity
            Vector2 launchVelocity = launchDirection.nor().scl(launchForce / currentBird.body.getMass());

            // Trajectory simulation parameters
            int numSteps = 60; // Number of trajectory points
            float timeStep = 1 / 60f; // Time increment (smaller = smoother)
            float gravity = Math.abs(world.getGravity().y); // Gravity value from the physics world

            for (int i = 0; i < numSteps; i++) {
                float t = i * timeStep; // Current time
                float dx = launchVelocity.x * t;
                float dy = launchVelocity.y * t - 0.5f * gravity * t * t; // Subtract gravity effect
                Vector2 trajPoint = new Vector2(startPos.x + dx, startPos.y + dy);

                // Stop adding points if the bird hits the ground
                if (trajPoint.y < 0) break;

                trajectoryPoints.add(trajPoint);
            }
        }
    }

    // Helper methods to identify fixtures
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
        System.out.println("stick hit and will be removed!");
        // Implement what happens when a pig is hit, e.g., remove it, play animation, etc.
        stick.dispose();
        sticks.remove(stick);
        // Optionally, update score or other game states
    }
    private boolean isGlassLegFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof GlassLeg;
    }
    private boolean isLandFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Land;
    }
    private boolean isWOODFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof WOOD;
    }

    private void handleGlassLegHit(GlassLeg glassLeg) {
        System.out.println("stick hit and will be removed!");
        // Implement what happens when a pig is hit, e.g., remove it, play animation, etc.
        glassLeg.dispose();
        glassLegs.remove(glassLeg);
        // Optionally, update score or other game states
    }
    private void handleLandHit(Land land) {
        System.out.println("stick hit and will be removed!");
        // Implement what happens when a pig is hit, e.g., remove it, play animation, etc.
        land.dispose();
        lands.remove(land);
        // Optionally, update score or other game states
    }


    // Handle pig being hit
    private void handlePigHit(Pig pig) {
        System.out.println("Pig hit and will be removed!");
        // Implement what happens when a pig is hit, e.g., remove it, play animation, etc.
        pig.dispose();
        pigs.remove(pig);
        // Optionally, update score or other game states
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update physics if the game is not paused
        if (!isPaused) {
            world.step(1 / 60f, 6, 2); // Step the physics world
        }


        // Process deferred actions
        while (!deferredActions.isEmpty()) {
            deferredActions.poll().run(); // Execute each deferred action
        }

        // Calculate trajectory if dragging
        if (isDragging && currentBirdIndex < birds.size()) {
            Bird currentBird = birds.get(currentBirdIndex);
            if (!currentBird.isLaunched) {
                calculateTrajectory();
            }
        } else {
            trajectoryPoints.clear();
        }

        // Synchronize all birds' images with their physics bodies
        for (Bird bird : birds) {
            Vector2 birdPosition = bird.body.getPosition();
            bird.image.setPosition(
                birdPosition.x * PPM - bird.image.getWidth() / 2,
                birdPosition.y * PPM - bird.image.getHeight() / 2
            );
        }

        // Update all pigs
        for (Pig pig : pigs) {
            pig.update();
        }

        for (Stick stick : sticks) {
            stick.update();
        }
        for (GlassLeg glassLeg : glassLegs) {
            glassLeg.update();
        }
        for (Land land : lands) {
            land.update();
        }
        if (wood != null) {
            wood.update();
        }
        if (glass != null) {
            glass.update();
        }



        // Synchronize catapult's image with its physics body
        Vector2 catapultPosition = catapultAnchor.getPosition();
        catImage.setPosition(
            catapultPosition.x * PPM - catImage.getWidth() / 2,
            catapultPosition.y * PPM - catImage.getHeight() / 2
        );
        catImage.setRotation(catapultAnchor.getAngle() * MathUtils.radiansToDegrees);

        // Render Box2D debug (optional)
        debugRenderer.render(world, viewport.getCamera().combined);

        // Draw the stage
        stage.act(delta);
        stage.draw();

    }
    private boolean isGlassFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Glass;
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
        blueTexture.dispose();
        yellowTexture.dispose();
        pigTexture.dispose();
        kingTexture.dispose();
        catTexture.dispose();
        glassTexture.dispose();
        WOODTexture.dispose();
        stickTexture.dispose();
        landTexture.dispose();
        skin.dispose();
        glassLegTexture.dispose();
        world.dispose();
        debugRenderer.dispose();
        triangleTexture.dispose();
        glassTriangleTexture.dispose();
        shapeRenderer.dispose();
        if (wood != null) {
            wood.dispose();
        }


        // Dispose all bird images and bodies
        for (Bird bird : birds) {
            bird.image.remove();
            // bird.image.getTexture().dispose(); // Only if each bird has its own texture
            // Box2D bodies are disposed when the world is disposed
        }

        // Dispose all pigs
        for (Pig pig : pigs) {
            pig.dispose();
        }
        for (Stick stick : sticks) {
            stick.dispose();
        }
        for (GlassLeg glassLeg : glassLegs) {
            glassLeg.dispose();
        }
        for(Land land : lands) {
            land.dispose();
        }
        if (glass != null) {
            glass.dispose();
        }

    }
}


class Pig {
    Body body;
    Image image;
    Stage stage;
    float PPM;
    boolean isHit = false;
    boolean isKing = false; // Flag to differentiate the king
    float health = 1f; // Initial health of the pig (1.0 = 100%)

    public Pig(World world, Texture texture, Vector2 position, Stage stage, float PPM) {
        this.stage = stage;
        this.PPM = PPM;

        // Create the Image
        image = new Image(texture);
        image.setSize(25, 25); // Adjust size as needed
        image.setPosition(position.x * PPM - image.getWidth() / 2, position.y * PPM - image.getHeight() / 2);
        stage.addActor(image);

        // Create the Box2D body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Start as static body
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define the shape (circle for pigs)
        CircleShape shape = new CircleShape();
        shape.setRadius(0.05f); // Radius in meters (25 pixels / 2 / 100 PPM)

        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.8f;
        fixtureDef.restitution = 0.1f; // Bounciness
        fixtureDef.friction = 0.6f;

        body.setUserData(this); // Set UserData to reference the Pig instance
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void setDynamic(World world, Queue<Runnable> deferredActions) {
        if (!isHit) {
            isHit = true;
            deferredActions.add(() -> body.setType(BodyDef.BodyType.DynamicBody));
        }

        // Reduce health by half
        health -= 1f;

        if (health <= 0) {
            // Health depleted, remove pig
            System.out.println("Pig is defeated!");
            deferredActions.add(this::dispose);
        } else {
            System.out.println("Pig's health reduced to: " + health);
        }
    }

    public void update() {
        Vector2 pos = body.getPosition();
        image.setPosition(pos.x * PPM - image.getWidth() / 2, pos.y * PPM - image.getHeight() / 2);
        image.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public void dispose() {
        image.remove();
    }
}


class Stick {
    Body body;
    Image image;
    Stage stage;
    float PPM;
    boolean isHit = false;
    float health= 1f;

    public Stick(World world, Texture texture,Vector2 position, Stage stage, float PPM) {
        this.stage = stage;
        this.PPM = PPM;

        //Creating image
        image = new Image(texture);
        image.setSize(10,40); // change size as and when
        image.setPosition(position.x * PPM - image.getWidth()/2, position.y * PPM - image.getHeight()/2);
        stage.addActor(image);
        // Making Box2d body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Start as static body
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define the shape (circle for pigs)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.25f, 0.25f); // Adjust dimensions to match the stick's image


        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.7f;
        fixtureDef.restitution = 0.5f; // Bounciness
        fixtureDef.friction = 0.5f;

        body.setUserData(this); // Set UserData to reference the Pig instance
        body.createFixture(fixtureDef);
        shape.dispose();
    }
    public void setDynamic(World world, Queue<Runnable> deferredActions) {
        if (!isHit) {
            isHit = true;
            deferredActions.add(() -> this.body.setType(BodyDef.BodyType.DynamicBody));
        }
        health -= 1f;

        if (health <= 0) {
            // Health depleted, remove pig
            System.out.println("Pig is defeated!");
            deferredActions.add(this::dispose);
        } else {
            System.out.println("Pig's health reduced to: " + health);
        }
    }

    public void update() {
        Vector2 pos = body.getPosition();
        image.setPosition(pos.x * PPM - image.getWidth() / 2, pos.y * PPM - image.getHeight() / 2);
        image.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public void dispose() {
        image.remove();
    }
}

class GlassLeg {
    Body body;
    Image image;
    Stage stage;
    float PPM;
    boolean isHit = false;
    float health = 0.8f;

    public GlassLeg(World world, Texture texture,Vector2 position, Stage stage, float PPM) {
        this.stage = stage;
        this.PPM = PPM;

        //Creating image
        image = new Image(texture);
        image.setSize(10,40); // change size as and when
        image.setPosition(position.x * PPM - image.getWidth()/2, position.y * PPM - image.getHeight()/2);
        stage.addActor(image);
        // Making Box2d body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Start as static body
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define the shape (circle for pigs)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.1f, 0.4f); // Half-width and half-height in meters (adjust as needed)

        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.6f;
        fixtureDef.restitution = 0.5f; // Bounciness
        fixtureDef.friction = 0.2f;

        body.setUserData(this); // Set UserData to reference the Pig instance
        body.createFixture(fixtureDef);
        shape.dispose();
    }
    public void setDynamic(World world, Queue<Runnable> deferredActions) {
        if (!isHit) {
            isHit = true;
            deferredActions.add(() -> body.setType(BodyDef.BodyType.DynamicBody));
        }
        health -= 0.8f;

        if (health <= 0) {
            // Health depleted, remove pig
            System.out.println("Pig is defeated!");
            deferredActions.add(this::dispose);
        } else {
            System.out.println("Pig's health reduced to: " + health);
        }
    }

    public void update() {
        Vector2 pos = body.getPosition();
        image.setPosition(pos.x * PPM - image.getWidth() / 2, pos.y * PPM - image.getHeight() / 2);
        image.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public void dispose() {
        image.remove();
    }
}

class Land {
    Body body;
    Image image;
    Stage stage;
    float PPM;
    boolean isHit = false;
    float health = 1.2f;

    public Land(World world, Texture texture,Vector2 position, Stage stage, float PPM) {
        this.stage = stage;
        this.PPM = PPM;

        //Creating image
        image = new Image(texture);
        image.setSize(105,10); // change size as and when
        image.setPosition(position.x * PPM - image.getWidth()/2, position.y * PPM - image.getHeight()/2);
        stage.addActor(image);
        // Making Box2d body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Start as static body
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define the shape (circle for pigs)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1f, 0.2f); // Half-width and half-height in meters (adjust as needed)

        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density =0.6f;
        fixtureDef.restitution = 0.8f; // Bounciness
        fixtureDef.friction = 0.2f;

        body.setUserData(this); // Set UserData to reference the Pig instance
        body.createFixture(fixtureDef);
        shape.dispose();
    }
    public void setDynamic(World world, Queue<Runnable> deferredActions) {
        if (!isHit) {
            isHit = true;
            deferredActions.add(() -> body.setType(BodyDef.BodyType.DynamicBody));
        }
        health -= 1.2f;

        if (health <= 0) {
            // Health depleted, remove pig
            System.out.println("Pig is defeated!");
            deferredActions.add(this::dispose);
        } else {
            System.out.println("Pig's health reduced to: " + health);
        }

    }

    public void update() {
        Vector2 pos = body.getPosition();
        image.setPosition(pos.x * PPM - image.getWidth() / 2, pos.y * PPM - image.getHeight() / 2);
        image.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public void dispose() {
        image.remove();
    }
}
class WOOD {
    Body body;
    Image image;
    Stage stage;
    float PPM;
    boolean isHit = false;
    float health = 1.0f;

    public WOOD(World world, Texture texture, Vector2 position, Stage stage, float PPM) {
        this.stage = stage;
        this.PPM = PPM;

        // Create the image for WOOD
        image = new Image(texture);
        image.setSize(40, 40); // Adjust size as needed
        image.setPosition(position.x * PPM - image.getWidth() / 2, position.y * PPM - image.getHeight() / 2);
        stage.addActor(image);

        // Create the Box2D body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Start as static
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define the shape (rectangle for WOOD)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.2f); // Half-width and half-height in meters (adjust as needed)

        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.5f;
        fixtureDef.restitution = 0.5f; // Bounciness
        fixtureDef.friction = 0.4f;

        body.setUserData(this); // Set UserData to reference the WOOD instance
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void setDynamic(World world, Queue<Runnable> deferredActions) {
        if (!isHit) {
            isHit = true;
            deferredActions.add(() -> body.setType(BodyDef.BodyType.DynamicBody));
        }
        health -= 1f;

        if (health <= 0) {
            // Health depleted, remove pig
            System.out.println("Pig is defeated!");
            deferredActions.add(this::dispose);
        } else {
            System.out.println("Pig's health reduced to: " + health);
        }
    }

    public void update() {
        Vector2 pos = body.getPosition();
        image.setPosition(pos.x * PPM - image.getWidth() / 2, pos.y * PPM - image.getHeight() / 2);
        image.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public void dispose() {
        image.remove();
    }
}

class Glass {
    Body body;
    Image image;
    Stage stage;
    float PPM;
    boolean isHit = false;
    float health = 0.75f;

    public Glass(World world, Texture texture, Vector2 position, Stage stage, float PPM) {
        this.stage = stage;
        this.PPM = PPM;

        // Create the image for Glass
        image = new Image(texture);
        image.setSize(40, 40); // Adjust size as needed
        image.setPosition(position.x * PPM - image.getWidth() / 2, position.y * PPM - image.getHeight() / 2);
        stage.addActor(image);

        // Create the Box2D body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Start as static
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define the shape (rectangle for Glass)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.2f); // Half-width and half-height in meters (adjust as needed)

        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.8f;
        fixtureDef.restitution = 0.4f; // Bounciness
        fixtureDef.friction = 0.4f;

        body.setUserData(this); // Set UserData to reference the Glass instance
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void setDynamic(World world, Queue<Runnable> deferredActions) {
        if (!isHit) {
            isHit = true;
            deferredActions.add(() -> body.setType(BodyDef.BodyType.DynamicBody));
        }
        health -= 0.75f;

        if (health <= 0) {
            // Health depleted, remove pig
            System.out.println("Pig is defeated!");
            deferredActions.add(this::dispose);
        } else {
            System.out.println("Pig's health reduced to: " + health);
        }
    }

    public void update() {
        Vector2 pos = body.getPosition();
        image.setPosition(pos.x * PPM - image.getWidth() / 2, pos.y * PPM - image.getHeight() / 2);
        image.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public void dispose() {
        image.remove();
    }
}
