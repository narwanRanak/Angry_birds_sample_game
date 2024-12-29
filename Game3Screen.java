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
import java.util.*;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Game3Screen implements Screen, Serializable {
    private Stage stage;
    private Viewport viewport;
    private Texture backgroundTexture;
    //    private Texture redTexture;
//    private Texture blueTexture;
    private Texture yellowTexture;
    //    private Texture pigTexture;
//    private Texture kingTexture;
    private Texture fattyTexture;
    private Texture catTexture;
    private Texture glassTexture;
    private Texture glassLegTexture;
    //    private Texture WOODTexture;
//    private Texture stickTexture;
//    private Texture landTexture;
//    private Texture triangleTexture;
//    private Texture glassTriangleTexture;
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
    //    private Image WOODImage;    // Wood box image
//    private Image stickImage;
//    private Image newglassImage;
//    private Image newstickImage;
//    private Image landImage;
//    private Image newlandImage;
//    private Image triangleImage;
//    private Image glassTriangleImage;
    private Array<Vector2> trajectoryPoints;
    private ShapeRenderer shapeRenderer;
    private List<Fatty> fatties;
    private boolean isPaused = false;  // Track if the game is paused
    private Table pauseMenuTable;
    final float initialBirdPosX = 1.3f;
    final float initialBirdPosY = 3.15f;
    final float xOffset = 0.2f; // Variable x offset per bird
    final float yOffset = 0.0f; // Fixed y offset per bird   // Table for pause menu buttons (Resume, Exit)
    //    private List <Stick> sticks;
    private List <GlassLeg> glassLegs;
    private List <Land> lands;
    private List<WOOD> woods;
    private WOOD wood; // Single WOOD object
    private Glass glass; // Single Glass object
    private boolean isFirstBirdLaunched = false; // Track if the first bird has been launched


    public Game3Screen(Main game) {
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
//                if (isWOODFixture(fixtureA)) {
//                    wood.setDynamic(world, deferredActions);
//                } else if (isWOODFixture(fixtureB)) {
//                    wood.setDynamic(world, deferredActions);
//                }
                if (isGlassFixture(fixtureA)) {
                    Glass glass = (Glass) fixtureA.getBody().getUserData();
                    glass.setDynamic(world, deferredActions);
                } else if (isGlassFixture(fixtureB)) {
                    Glass glass= (Glass) fixtureB.getBody().getUserData();
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
                // Check if a bird has hit a pig
                if (isFattyFixture(fixtureA)) {
                    Fatty fatty = (Fatty) fixtureA.getBody().getUserData();
                    fatty.setDynamic(world, deferredActions); // Defer setting to dynamic
                } else if (isFattyFixture(fixtureB)) {
                    Fatty fatty = (Fatty) fixtureB.getBody().getUserData();
                    fatty.setDynamic(world, deferredActions); // Defer setting to dynamic
                }

                // Check if a bird hits a glass or wood object
                if (isBirdFixture(fixtureA) && isGlassLegFixture(fixtureB)) {
                    GlassLeg glass = (GlassLeg) fixtureB.getBody().getUserData();
                    glass.setDynamic(world, deferredActions);
                } else if (isGlassLegFixture(fixtureA) && isBirdFixture(fixtureB)) {
                    GlassLeg glass = (GlassLeg) fixtureA.getBody().getUserData();
                    glass.setDynamic(world, deferredActions);
                }

//                if (isBirdFixture(fixtureA) && isWOODFixture(fixtureB)) {
//                    WOOD wood = (WOOD) fixtureB.getBody().getUserData();
//                    wood.setDynamic(world, deferredActions);
//                } else if (isWOODFixture(fixtureA) && isBirdFixture(fixtureB)) {
//                    WOOD wood = (WOOD) fixtureA.getBody().getUserData();
//                    wood.setDynamic(world, deferredActions);
//                }
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
        backgroundTexture = new Texture(Gdx.files.internal("Game3Screen.png"));
//        redTexture = new Texture(Gdx.files.internal("redBird.png"));
//        blueTexture = new Texture(Gdx.files.internal("blueBIrd.png"));
        yellowTexture = new Texture(Gdx.files.internal("yellowBird.png"));
//        pigTexture = new Texture(Gdx.files.internal("pig.png"));
//        kingTexture = new Texture(Gdx.files.internal("king.png"));
        fattyTexture = new Texture(Gdx.files.internal("fatty.png"));
        catTexture = new Texture(Gdx.files.internal("cat.png"));
        glassTexture = new Texture(Gdx.files.internal("glass.png")); // Texture for glass box
        glassLegTexture = new Texture(Gdx.files.internal("glassLeg.png"));
//        WOODTexture = new Texture(Gdx.files.internal("WOOD.png"));   // Texture for WOOD box
//        stickTexture = new Texture(Gdx.files.internal("stick.png"));
//        landTexture = new Texture(Gdx.files.internal("land.png"));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
//        triangleTexture = new Texture(Gdx.files.internal("triangle.png"));
//        glassTriangleTexture = new Texture(Gdx.files.internal("glassTriangle.png"));

        // Create image actors for game elements
        Image backgroundImage = new Image(backgroundTexture);
        // redBirdImage = new Image(redTexture); // Removed single bird image
        catImage = new Image(catTexture);
//        Image kingImage = new Image(kingTexture);
        Image fattyImage = new Image(fattyTexture);

        // Create image actors for glass and WOOD boxes
        glassImage = new Image(glassTexture);
        glassLegImage = new Image(glassLegTexture);
//        WOODImage = new Image(WOODTexture);
//        stickImage = new Image(stickTexture);
//        newstickImage = new Image(stickTexture); // Reuse the existing stick texture
//        newglassImage = new Image(glassLegTexture);
//        newlandImage = new Image(landTexture);
//        landImage = new Image(landTexture);
//        triangleImage = new Image(triangleTexture);
//        glassTriangleImage = new Image(glassTriangleTexture);

        // Set sizes for images
        // redBirdImage.setSize(25, 25); // Removed single bird image
        catImage.setSize(70, 70);

//        kingImage.setSize(25, 30);
        fattyImage.setSize(35,35);

        // Set positions and sizes for the glass and WOOD boxes
//        glassImage.setPosition(600, 300); // Position glass box around pig
//        glassImage.setSize(40, 40);
//        glassLegImage.setPosition(630, 250);
//        glassLegImage.setSize(10, 40);
//        WOODImage.setPosition(660, 300); // Position WOOD box
//        WOODImage.setSize(40, 40);
//        stickImage.setPosition(600, 250);
//        stickImage.setSize(10, 40);
//        newstickImage.setPosition(660, 250);
//        newstickImage.setSize(10, 40);
//        newglassImage.setPosition(690, 250);
//        newglassImage.setSize(10, 40);
//        landImage.setPosition(600, 290);
//        landImage.setSize(100, 10);
//        newlandImage.setPosition(600, 340);
//        newlandImage.setSize(100, 10);
//        triangleImage.setPosition(600, 350);
//        triangleImage.setSize(40, 40);
//        glassTriangleImage.setPosition(660, 350);
//        glassTriangleImage.setSize(40, 40);

        // Add images to the stage in the correct order for layering
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Add actors to the stage
        stage.addActor(catImage);     // Add catapult last to ensure it is in front

        // Initialize pigs list
        // Initialize pigs (fatty objects)
        fatties = new ArrayList<>();

// Define positions for your Fatty instances
        Vector2 fattyPosition1 = new Vector2(6.15f, 3.32f); // Example position
        Vector2 fattyPosition2 = new Vector2(6.75f, 3.32f); // Example position
        Vector2 fattyPosition3 = new Vector2(6.5f, 3.7f);   // "King" fatty position

// Create Fatty instances and add them to the list
        Fatty fatty1 = new Fatty(world, fattyTexture, fattyPosition1, stage, PPM);
        Fatty fatty2 = new Fatty(world, fattyTexture, fattyPosition2, stage, PPM);
        Fatty fatty3 = new Fatty(world, fattyTexture, fattyPosition3, stage, PPM);


        fatties.add(fatty1);
        fatties.add(fatty2);
        fatties.add(fatty3);

        glassLegs = new ArrayList<>();
        Vector2 glassLegPosition1 = new Vector2(6.3f, 2.5f); // Example position
        Vector2 glassLegPosition2 = new Vector2(6.6f, 2.5f); // Example position
        Vector2 glassLegPosition3 = new Vector2(6.9f, 2.5f); // Example position
        Vector2 glassLegPosition4 = new Vector2(6f, 2.5f); // Example position
        Vector2 glassLegPosition5 = new Vector2(6.3f, 2.5f); // Example position
        Vector2 glassLegPosition6 = new Vector2(6.9f, 3.3f); // Example position
        Vector2 glassLegPosition7 = new Vector2(6.6f, 3.3f); // Example position
        Vector2 glassLegPosition8 = new Vector2(6.3f, 3.3f); // Example position
        Vector2 glassLegPosition9 = new Vector2(6f, 3.3f); // Example position
        Vector2 glassLegPosition10 = new Vector2(5.7f, 2.5f); // Example position


        GlassLeg glassLeg1 = new GlassLeg(world, glassLegTexture, glassLegPosition1, stage, PPM);
        GlassLeg glassLeg2 = new GlassLeg(world, glassLegTexture, glassLegPosition2, stage, PPM);
        GlassLeg glassLeg3 = new GlassLeg(world, glassLegTexture, glassLegPosition3, stage, PPM);
        GlassLeg glassLeg4 = new GlassLeg(world, glassLegTexture, glassLegPosition4, stage, PPM);
        GlassLeg glassLeg5 = new GlassLeg(world, glassLegTexture, glassLegPosition5, stage, PPM);
        GlassLeg glassLeg6 = new GlassLeg(world, glassLegTexture, glassLegPosition6, stage, PPM);
        GlassLeg glassLeg7 = new GlassLeg(world, glassLegTexture, glassLegPosition7, stage, PPM);
        GlassLeg glassLeg8 = new GlassLeg(world, glassLegTexture, glassLegPosition8, stage, PPM);
        GlassLeg glassLeg9 = new GlassLeg(world, glassLegTexture, glassLegPosition9, stage, PPM);
        GlassLeg glassLeg10 = new GlassLeg(world, glassLegTexture, glassLegPosition10, stage, PPM);


        glassLegs.add(glassLeg1);
        glassLegs.add(glassLeg2);
        glassLegs.add(glassLeg3);
        glassLegs.add(glassLeg4);
        glassLegs.add(glassLeg5);
        glassLegs.add(glassLeg6);
        glassLegs.add(glassLeg7);
        glassLegs.add(glassLeg8);
        glassLegs.add(glassLeg9);
        glassLegs.add(glassLeg10);

        // Declare glass as a list
        List<Glass> glass;

// Initialize as an ArrayList
        glass = new ArrayList<>();

// Add Glass objects to the list
        Vector2 glassPosition1 = new Vector2(5.85f, 2.9f); // Example position
        Vector2 glassPosition2 = new Vector2(6.15f, 2.9f);
        Vector2 glassPosition3 = new Vector2(6.45f, 2.9f);
        Vector2 glassPosition4 = new Vector2(6.75f, 2.9f);
        Vector2 glassPosition5 = new Vector2(6.45f, 3.7f);
        Vector2 glassPosition6 = new Vector2(6.15f, 3.7f);
        Vector2 glassPosition7 = new Vector2(6.75f, 3.7f);
        Vector2 glassPosition8 = new Vector2(6.45f, 4.1f);
        Vector2 glassPosition9 = new Vector2(6.75f, 4.1f);
        Glass glass1 = new Glass(world, glassTexture, glassPosition1, stage, PPM);
        Glass glass2 = new Glass(world, glassTexture, glassPosition2, stage, PPM);
        Glass glass3 = new Glass(world, glassTexture, glassPosition3, stage, PPM);
        Glass glass4 = new Glass(world, glassTexture, glassPosition4, stage, PPM);
        Glass glass5 = new Glass(world, glassTexture, glassPosition5, stage, PPM);
        Glass glass6 = new Glass(world, glassTexture, glassPosition6, stage, PPM);
        Glass glass7 = new Glass(world, glassTexture, glassPosition7, stage, PPM);
        Glass glass8 = new Glass(world, glassTexture, glassPosition8, stage, PPM);
        Glass glass9 = new Glass(world, glassTexture, glassPosition9, stage, PPM);

        glass.add(glass1);
        glass.add(glass2);
        glass.add(glass3);
        glass.add(glass4);
        glass.add(glass5);
        glass.add(glass6);
        glass.add(glass7);
        glass.add(glass8);
        glass.add(glass9);


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
        Image birdImage = new Image(yellowTexture);

        for (int i = 0; i < numberOfBirds; i++) {
            // Create image for the bird
            if(i % 3 == 0){
                birdImage = new Image(yellowTexture);
            }
            else if(i % 3 == 1){
                birdImage = new Image(yellowTexture);
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
//        Label scoreLabel = new Label("Score: 0", skin);
//        scoreLabel.setFontScale(3.0f);

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
//        table.add(scoreLabel).padTop(20).expandX().left();
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
                game.setScreen(new LevelScreen(game,2)); // Go back to Level Screen
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

//    private void removeOffScreenObjects() {
//        Bird currentBird = (currentBirdIndex < birds.size()) ? birds.get(currentBirdIndex) : null;
//
//        // Iterate over pigs
//        for (int i = pigs.size() - 1; i >= 0; i--) { // Iterate backwards to avoid index issues
//            Pig pig = pigs.get(i);
//            if (pig.body.getPosition().y < REMOVAL_THRESHOLD_Y) { // Check if Y-coordinate is below threshold
//                pig.dispose(); // Remove visuals (image) from the stage
//                world.destroyBody(pig.body); // Destroy the physics body
//                pigs.remove(i); // Remove from the pigs list
//                System.out.println("Pig removed for falling out of bounds.");
//            }
//        }
//
//        // Iterate over birds
//        for (int i = birds.size() - 1; i >= 0; i--) { // Iterate backwards to avoid index issues
//            Bird bird = birds.get(i);
//            if (bird.body.getPosition().y < -2.0f) { // Check if Y-coordinate is below threshold
//                bird.image.remove(); // Remove visuals (image) from the stage
//                world.destroyBody(bird.body); // Destroy the physics body
//                birds.remove(i); // Remove from the birds list
//                System.out.println("Bird removed for falling out of bounds.");
//            }
//        }
//    }


    private void calculateTrajectory() {
        trajectoryPoints.clear();

        if (isDragging && currentBirdIndex < birds.size()) {
            Bird currentBird = birds.get(currentBirdIndex);
            if (currentBird.isLaunched) {
                // Bird already launched, no trajectory to calculate
                return;
            }

            Vector2 startPos = new Vector2(currentBird.body.getPosition());

            // Calculate the launch velocity
            Vector2 launchDirection = new Vector2(catapultAnchor.getPosition()).sub(currentBird.body.getPosition());

            // Use the updated launch force multiplier
            float forceMultiplier = 7f; // Same as touchUp
            float maxLaunchForce = 10f;   // Same as touchUp
            float launchForce = launchDirection.len() * forceMultiplier; // Adjusted multiplier

            if (launchForce > maxLaunchForce) {
                launchForce = maxLaunchForce;
            }

            // Normalize the launch direction and calculate initial velocities
            Vector2 launchVelocity = launchDirection.nor().scl(launchForce / currentBird.body.getMass());

            // Increase the number of steps for a smoother trajectory
            int numSteps = 60; // Increased from 30 to 60
            float timeStep = 1 / 60f; // Smaller timestep for better accuracy

            float gravity = Math.abs(world.getGravity().y); // Ensure gravity is positive for calculations

            for (int i = 0; i < numSteps; i++) {
                float t = i * timeStep;
                float dx = launchVelocity.x * t;
                float dy = launchVelocity.y * t - 0.5f * gravity * t * t; // Subtract gravity effect
                Vector2 trajPoint = new Vector2(startPos.x + dx, startPos.y + dy);
                trajectoryPoints.add(trajPoint);
            }
        }
    }

    // Helper methods to identify fixtures
    private boolean isBirdFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Bird;
    }
    private boolean isFattyFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Fatty;
    }
    private boolean isGlassLegFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof GlassLeg;
    }
//    private boolean isLandFixture(Fixture fixture) {
//        return fixture.getBody().getUserData() instanceof Land;
//    }
//    private boolean isWOODFixture(Fixture fixture) {
//        return fixture.getBody().getUserData() instanceof WOOD;
//    }

    private void handleGlassLegHit(GlassLeg glassLeg) {
        System.out.println("stick hit and will be removed!");
        // Implement what happens when a pig is hit, e.g., remove it, play animation, etc.
        glassLeg.dispose();
        glassLegs.remove(glassLeg);
        // Optionally, update score or other game states
    }
//    private void handleLandHit(Land land) {
//        System.out.println("stick hit and will be removed!");
//        // Implement what happens when a pig is hit, e.g., remove it, play animation, etc.
//        land.dispose();
//        lands.remove(land);
//        // Optionally, update score or other game states
//    }


    // Handle pig being hit
    // Handle Fatty being hit
    private void handleFattyHit(Fatty fatty) {
        System.out.println("Fatty hit and will be removed!");
        fatty.dispose();
        fatties.remove(fatty); // Remove from the list
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
        Iterator<Fatty> fattyIterator = fatties.iterator();
        while (fattyIterator.hasNext()) {
            Fatty fatty = fattyIterator.next();
            fatty.update();
            if (fatty.health <= 0) {
                fatty.dispose();
                fattyIterator.remove(); // Safe removal
            }
        }


//        for (Stick stick : sticks) {
//            stick.update();
//        }
        for (GlassLeg glassLeg : glassLegs) {
            glassLeg.update();
        }
//        for (Land land : lands) {
//            land.update();
//        }
        if (wood != null) {
            wood.update();
        }
        if (glass != null) {
            glass.update();
        }
        // Check if all pigs are destroyed
        if (areAllFattiesDestroyed()) {
            game.setScreen(new winlooseScreen(game)); // Transition to WinLooseScreen
        }
        if (areAllBirdsLaunched() && !areAllFattiesDestroyed()) {
            System.out.println("All birds launched but not all pigs destroyed!");
            game.setScreen(new loosewinScreen(game)); // Transition to loosewinScreen
            return; // Exit render method
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

        // Render the trajectory after drawing the stage
        if (isDragging && currentBirdIndex < birds.size()) {
            Bird currentBird = birds.get(currentBirdIndex);
            if (!currentBird.isLaunched && trajectoryPoints.size > 0) {
                // Use the camera's combined matrix for accurate positioning
                shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.RED);

                for (Vector2 point : trajectoryPoints) {
                    // Convert world coordinates to screen coordinates by scaling with PPM
                    float screenX = point.x * PPM;
                    float screenY = point.y * PPM;
                    shapeRenderer.circle(screenX, screenY, 3f); // Fixed radius in pixels
                }

                shapeRenderer.end();
            }
        }
    }
    private boolean isGlassFixture(Fixture fixture) {
        return fixture.getBody().getUserData() instanceof Glass;
    }

    private boolean areAllFattiesDestroyed() {
        for (Fatty fatty : fatties) {
            if (!fatty.isHit) { // Or other logic to check if the pig is still active
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
//        redTexture.dispose();
//        blueTexture.dispose();
        yellowTexture.dispose();
//        pigTexture.dispose();
//        kingTexture.dispose();
        catTexture.dispose();
        glassTexture.dispose();
//        WOODTexture.dispose();
//        stickTexture.dispose();
//        landTexture.dispose();
        skin.dispose();
        glassLegTexture.dispose();
        world.dispose();
        debugRenderer.dispose();
//        triangleTexture.dispose();
//        glassTriangleTexture.dispose();
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
        for (Fatty fatty : fatties) {
            fatty.dispose();
        }
//        for (Stick stick : sticks) {
//            stick.dispose();
//        }
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


class Fatty {
    Body body;
    Image image;
    Stage stage;
    float PPM;
    boolean isHit = false;
    float health = 1f;

    public Fatty(World world, Texture texture, Vector2 position, Stage stage, float PPM) {
        this.stage = stage;
        this.PPM = PPM;

        // Create the image for Fatty
        image = new Image(texture);
        image.setSize(40, 40); // Adjust size as needed
        image.setPosition(position.x * PPM - image.getWidth() / 2, position.y * PPM - image.getHeight() / 2);
        stage.addActor(image);

        // Create the Box2D body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Start as static
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        // Define the shape (rectangle for Fatty)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.2f); // Half-width and half-height in meters (adjust as needed)

        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.restitution = 0.3f; // Bounciness
        fixtureDef.friction = 0.5f;

        body.setUserData(this); // Set UserData to reference the Fatty instance
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void setDynamic(World world, Queue<Runnable> deferredActions) {
        if (!isHit) {
            isHit = true;
            deferredActions.add(() -> body.setType(BodyDef.BodyType.DynamicBody));
        }

        // Reduce health by half
        health -= 0.05f;

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

