/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Cylinder;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * This application originated from testing around with some JMETest examples. Parts of some methods are borrowed from JMEtests, but 
 * over 90% of all code has been written from scratch. 
 * 
 * 
 * 
 * @author Olof E, Anton E, Jonathan O
 */
public class Main extends SimpleApplication implements AnalogListener,
        ActionListener {

    Game game;
    public static void main(String[] args) {
        Main app = new Main();

        app.start();
    }
    public Main() {
        game = new Game(false);
        stateManager.attach(game);
    }
    
    //key setup
    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("EnterExit", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "EnterExit");
    }

    @Override
    public void simpleInitApp() {
                setupKeys();

    }
    




    public void onAnalog(String binding, float value, float tpf) {
    }


    
    public void onAction(String binding, boolean value, float tpf) {
        game.onAction(binding, value, tpf);
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        

    }
}
class Game extends BaseAppState {
    protected BulletAppState bulletAppState;
    protected SimpleApplication sapp;
    protected boolean isClient;
    private int objectCounter = 0;

    TerrainQuad terrain;
    Material matRock;
    boolean wireframe = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    Geometry collisionMarker;
    
    
    protected ArrayList<Car> cars = new ArrayList<Car>();        // all the cars, not really used though
    private Car carObject;
    private Car collidedCar;
    protected ArrayList<GameObject> entities = new ArrayList<GameObject>();
    
    private Character characterObject;                      // Your avatar
    private CameraNode camNode;
    protected ArrayList<Character> characters = new ArrayList<Character>();
    
    GameObject currentObject;                               // The object you "are" (or control) at the moment, a car or characer for example. 
    PhysicsControl currentControl;
    
    float airTime = 0;
    
    DirectionalLight dl = new DirectionalLight(); //different lights that get changed
    Vector3f lightDir2 = new Vector3f(0.70518064f, 0.5902297f, -0.39287305f);
    DirectionalLight dl2 = new DirectionalLight();
    
    GhostControl ghostControl;                            // Collision detection 
    public Game(boolean isClient) {
        this.isClient=isClient;
    }
    
    public void onDisable() {
        
    }
    /**
     * Some elements of the initial scene setup is derived from JMEtests
     */
    public void onEnable() {
        bulletAppState = new BulletAppState();
        sapp.getStateManager().attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setAccuracy(1f/30f);
     
       
        PssmShadowRenderer pssmr = new PssmShadowRenderer(sapp.getAssetManager(), 2048, 3);
        pssmr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
        pssmr.setLambda(0.55f);
        pssmr.setShadowIntensity(0.6f);
        pssmr.setCompareMode(CompareMode.Hardware);
        pssmr.setFilterMode(FilterMode.Bilinear);
        sapp.getViewPort().addProcessor(pssmr);

        ghostControl = new GhostControl(new SphereCollisionShape(0.7f));
        getPhysicsSpace().add(ghostControl);
        
        createTerrain(); // 
        buildCar(new Vector3f(-120,0,-20)); // create our cars, if you want more cars in the game just follow the pattern here
        buildCar(new Vector3f(-130,0,-30));
        buildCar(new Vector3f(-140,0,-40));

        

        initLight(); //set up initial lighting
    
        
        AudioNode background_noise = new AudioNode(sapp.getAssetManager(), "Sounds/wind01.wav", DataType.Stream);
        sapp.getRootNode().attachChild(background_noise);
        background_noise.setPositional(false); // background noise setup
        background_noise.setLooping(true);
        background_noise.setVolume(1);
        background_noise.setName("background_noise");

        

    }
    //sets up the initial lighting of the scene
    private void initLight(){

        dl.setColor(new ColorRGBA(1.0f, 0.9f, 0.9f, 1f).multLocal(1.3f));
        dl.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
        sapp.getRootNode().addLight(dl);
        dl.setName("color1");

        dl2.setColor(new ColorRGBA(1.0f, 0.9f, 0.9f, 1f));
        dl2.setDirection(lightDir2);
        sapp.getRootNode().addLight(dl2);
        dl2.setName("color2");
}
    /**
     * Derived from JMEtests
     */
    private void createTerrain() {
        Spatial terrain = sapp.getAssetManager().loadModel("Scenes/MyScene.j3o");
        RigidBodyControl rigidBodyControl = new RigidBodyControl(0);
        terrain.addControl(rigidBodyControl);
        sapp.getRootNode().attachChild(terrain);
        terrain.setLocalScale(1f);
        getPhysicsSpace().addAll(terrain);

    }
    public void cleanup(Application app) {
        
    }
    public void initialize(Application app) {
        sapp = (SimpleApplication) app;
    }
    protected PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }
    
    /**
     * the numbers below are values for lighting. dayA-dayD are the final values for daylight,
     * and nightA-nightD are final values for nighttime.
     * 
     * a-d are just the current values. 
     */
    float a = 1.0f;
    float b = 0.9f;
    float c = 0.9f;
    float d = 1.0f;
    boolean DayOrNight = false;
    final float timeFactor = 20;
    
    final float dayA = 1.0f;
    final float dayD = 1.0f;
    final float dayB = 0.9f;
    final float dayC = 0.9f;
    
    final float nightA = 0.8f;
    final float nightB = 0.8f;
    final float nightC = 2.0f;
    final float nightD = 2.0f;
    
    private void updateLight(){ //updates lighting regularly. depending on if its going towards day or night, the values change
                                //for faster day/night cycles, use a lower timeFactor than 20, and for slower, use a higher one.
        
        if (!DayOrNight) { 
            if (!(a <= 0.8f)){
                a = a + ((nightA-dayA)/timeFactor);

            }
            if (!(b <= 0.8f)){
               b = b + ((nightB-dayB)/timeFactor);

            }
            if (!(c >= 2f)){
                //c += 0.05f;
                c = c + ((nightC-dayC)/timeFactor);

            }
            if (!(d >= 2f)){
                d = d + ((nightD-dayD)/timeFactor);

            }
            if( a <= 0.8f && b <= 0.8f && c >= 2f && d >= 2f ){
                DayOrNight = true;
                a = 0.800001f;
                b = 0.800001f;
                c = 1.999999f;
                d = 1.999999f;
            }
        }
        else if( DayOrNight){
            if (!(a >= 1f)){
                a = a + ((dayA-nightA)/timeFactor);
            }
            if (!(b >= 0.9f)){
                b = b + ((dayB-nightB)/timeFactor);
            }
            if (!(c <= 0.9f)){
                c = c + ((dayC-nightC)/timeFactor);
            }
            if (!(d <= 1.0f)){
                d = d + ((dayD-nightD)/timeFactor);
            }
            if( a >= 1f && b >= 0.9f && c <= 0.9f && d <= 1f ){
                DayOrNight = false;
                a = 0.999999f;
                b = 0.899999f;
                c = 0.900001f;
                d = 1.000001f;
            }
        }
        sapp.getRootNode().removeLight(dl);
        dl.setColor(new ColorRGBA(a, b, c, d));
        sapp.getRootNode().addLight(dl);
        
        sapp.getRootNode().removeLight(dl2);
        dl2.setColor(new ColorRGBA(a, b, c, d));
        sapp.getRootNode().addLight(dl2);
        
 
}
    /**
     * Creation of camera and the way it follows the character is derived from JMEtests
     * 
     * 
     * The server's creation of a character. Adds character control and a camera attached to it (needed for rotation)
     */
    protected int serverMakeCharacter() {
        objectCounter++;
          Spatial model = sapp.getAssetManager().loadModel("Models/Sinbad/Sinbad.mesh.xml");   
          model.scale(0.25f);
     
          characterObject = new Character(model, "character node", new Vector3f(0, 1, -5), !isClient);
          characters.add(characterObject);
          characterObject.setID(objectCounter);
          entities.add(characterObject);
          if (!isClient) {
              characterObject.addControl(characterObject.characterControl);
              getPhysicsSpace().add(characterObject.characterControl);
              characterObject.characterControl.setPhysicsLocation(new Vector3f(-115,5,0));
          }
          else {
              characterObject.setLocalTranslation(new Vector3f(-115,5,0));
          }
          GhostControl ctrl = new GhostControl(new SphereCollisionShape(0.7f));
          getPhysicsSpace().add(ctrl);
          characterObject.addControl(ctrl);
          characterObject.ghostControl = ctrl;
      
          sapp.getRootNode().attachChild(characterObject);
          characterObject.attachChild(model);
          
          // set forward camera node that follows the character
          Camera cam = sapp.getCamera().clone();
          characterObject.camNode = new CameraNode("CamNode", cam);
          characterObject.camNode.setControlDir(ControlDirection.SpatialToCamera);
          characterObject.camNode.setLocalTranslation(characterObject.camPosition);
          characterObject.camNode.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);
          characterObject.attachChild(characterObject.camNode);
          

          sapp.getFlyByCamera().setEnabled(false);

          characterObject.currentObject = characterObject;
          if (!isClient) {
             currentControl = characterObject.characterControl; 
          }
          
          
          return characterObject.getID();
    }
    /**
     * Creation of camera and the way it follows the character is derived from JMEtests
     * 
     * 
     * The Clients way of making your avatar, sets up the character and attaches your camera to it.
     */
    protected void clientMakeCharacter(int id) {
        Spatial model = sapp.getAssetManager().loadModel("Models/Sinbad/Sinbad.mesh.xml");   
          model.scale(0.25f);
     
          characterObject = new Character(model, "character node", new Vector3f(0, 1, -5), !isClient);
          characters.add(characterObject);
          characterObject.setID(id);
          entities.add(characterObject);
          if (!isClient) {
              characterObject.addControl(characterObject.characterControl);
              getPhysicsSpace().add(characterObject.characterControl);
              characterObject.characterControl.setPhysicsLocation(new Vector3f(-115,5,0));
          }
          else {
              characterObject.setLocalTranslation(new Vector3f(-115,5,0));
          }
          characterObject.addControl(ghostControl);
         

          
          sapp.getRootNode().attachChild(characterObject);
          characterObject.attachChild(model);
          
          // set forward camera node that follows the character
          camNode = new CameraNode("CamNode", sapp.getCamera());
          camNode.setControlDir(ControlDirection.SpatialToCamera);
          camNode.setLocalTranslation(characterObject.camPosition);
          camNode.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);
          characterObject.attachChild(camNode);
          

          sapp.getFlyByCamera().setEnabled(false);

          characterObject.currentObject = characterObject;
          currentObject = characterObject;
          if (!isClient) {
             currentControl = characterObject.characterControl; 
          }
          
    
    }
    
    
    // Used by client to add other players' avatars to the scene
    public Character spawnEntity(int id) {       
        Spatial model = sapp.getAssetManager().loadModel("Models/Sinbad/Sinbad.mesh.xml");   
        model.scale(0.25f);

        Character characterObject = new Character(model, "character node", new Vector3f(0, 1, -5), !isClient);
        characters.add(characterObject);
        entities.add(characterObject);
        characterObject.setID(id);
        sapp.getRootNode().attachChild(characterObject);
        characterObject.attachChild(model);

        return characterObject;

    }
    //creates a car, all the details of creating a car is in Car.java
    //here it just creates a Car object (the big chunk of code creating a car is called in Car constructor)
    // Also sets up the honk audio and attaches it as positional sound to the Car.
    private void buildCar(Vector3f initialPosition) {
        objectCounter++;

        //Load model and get chassis Geometry
        Spatial carNode = sapp.getAssetManager().loadModel("Models/Ferrari/Car.scene");
       
        carNode.setShadowMode(ShadowMode.Cast);  
        carObject = new Car("Car", carNode, new Vector3f(0, 4, 12));
        carObject.setID(objectCounter);
        carObject.setLocalTranslation(initialPosition);
        getPhysicsSpace().add(carObject.carControl);  
        carObject.addControl(carObject.carControl);

        
        carObject.attachChild(carNode);
        sapp.getRootNode().attachChild(carObject);
        
        cars.add(carObject);       
        entities.add(carObject);

        AudioNode audioHonk = new AudioNode(sapp.getAssetManager(), "Sounds/car_horn.ogg", DataType.Buffer);
        audioHonk.setPositional(true);
        audioHonk.setLooping(false);
        audioHonk.setVolume(2);
        audioHonk.setName("Audio");
        carObject.attachChild(audioHonk);
        
        
        sapp.getFlyByCamera().setEnabled(false);
          
    }
    //hopefully self explanatory
    protected Character getCharacterById(int id) {
        for (int i=0; i<characters.size(); i++) {
            if (id==characters.get(i).getID()) {
                return characters.get(i);
            }
        }
        System.out.println("Character not found in getCharacterById()");
        return null;
    }
    //hopefully self explanatory
    protected GameObject getEntityById(int id) {
        for (int i=0; i<entities.size(); i++) {
            if (id==entities.get(i).getID()) {
                return entities.get(i);
            }
        }
        System.out.println("Entity not found in getEntityById())");
        return null;
    }

    //the clients way of changing control from character to a car, a lot of trial and error here so it is not super clean
    protected void changeControlClient(Character c1, GameObject newObjectToControl) {
      
        if (newObjectToControl instanceof Car) {
            c1.removeFromParent();                 
            newObjectToControl.attachChild(c1);
            c1.isInVehicle = true;           
            c1.invisible();

            
        }
        else {
            System.out.println(c1.getLocalTranslation() + " characterlocal");
            Node characterParent = c1.getParent();
            Vector3f parentsLocalTranslation = characterParent.getLocalTranslation();
            characterObject.removeFromParent();
            c1.setLocalTranslation(parentsLocalTranslation.add(-2,1,0));
            sapp.getRootNode().attachChild(c1);

            c1.visible();     
            c1.isInVehicle = false;            
        }
        
        currentObject.stopMovement();
        currentObject.detachChild(camNode);
        
        currentObject = newObjectToControl;
        
        sapp.getFlyByCamera().setEnabled(false);
             
        camNode.setControlDir(ControlDirection.SpatialToCamera);
      
        currentObject.attachChild(camNode);
       
        camNode.setLocalTranslation(newObjectToControl.camPosition);
  
        camNode.lookAt(currentObject.getLocalTranslation(), Vector3f.UNIT_Y);

    }

    // Byter nuvarande objektet som kameran riktas på 
    private GameObject changeControl(Character c1, GameObject newObjectToControl){
        c1.currentObject = newObjectToControl;
        newObjectToControl.camNode = c1.camNode;
        
        return c1.currentObject;

    }
    float serverTimeSinceLightMessage = 0; // adds tpf onto it, when it reaches 5 seconds its time for a weather update
    /**
     * server's update method. checks for proximity to cars so that characters may enter them, 
     * handles character movement and so on
     * 
     * @param tpf 
     */
    public void serverUpdate(float tpf) {
        serverTimeSinceLightMessage+=tpf;
        
        for (int j=0; j<characters.size(); j++) {
            GameObject currentObject = characters.get(j).currentObject;
            if (currentObject.ghostControl!=null){
                List<PhysicsCollisionObject> overLappingObjects = currentObject.ghostControl.getOverlappingObjects();
        
                boolean collide = false;
                for(int i=0; i<cars.size(); i++){
                    if(currentObject.ghostControl.getOverlappingObjects().contains(cars.get(i).getControl(PhysicsControl.class))){
                        currentObject.collidedCar = cars.get(i);
                        collide = true;

                    }
                }
                if(collide == false){
                    currentObject.collidedCar = null;
                    sapp.getGuiNode().detachAllChildren();
                }
            }



            currentObject.camNode.lookAt(currentObject.getWorldTranslation(), Vector3f.UNIT_Y);
            // Some elements of character and car movement originated from JMEtests
            if(currentObject instanceof Character){
                Character character = (Character)currentObject;
                Vector3f camDir = character.camNode.getCamera().getDirection().mult(0.8f);
                Vector3f camLeft = character.camNode.getCamera().getLeft().mult(0.8f);
                camDir.y = 0;
                camLeft.y = 0;

                character.viewDirection.set(camDir);
                character.walkDirection.set(0, 0, 0);

                if (character.leftRotate) {
                character.viewDirection.addLocal(camLeft.mult(0.05f));
                } else
                if (character.rightRotate) {
                    character.viewDirection.addLocal(camLeft.mult(0.05f).negate());
                }
                if (character.forward) {
                    character.walkDirection.addLocal(camDir.mult(0.5f));
                } else
                if (character.backward) {
                    character.walkDirection.addLocal(camDir.mult(0.5f).negate());
                 }
                if (!isClient) {
                    character.characterControl.setWalkDirection(character.walkDirection);
                    character.characterControl.setViewDirection(character.viewDirection);  
                }

            }
            else{

            }
            }
    }
    
    /**
     * calls for light updates, and if its a server, calls serverUpdate and then returns.
     * 
     */
    float light = 0;
    @Override
    public void update(float tpf) {
        light += tpf;
        if(light >= 0.5f){
           updateLight(); 
           light = 0;
}
        
        if (!isClient) {
            serverUpdate(tpf);
            return;
        }
        
        List<PhysicsCollisionObject> overLappingObjects = ghostControl.getOverlappingObjects();
        if (characters.isEmpty()) {
            //only do automatic stuff
            return;
        }
     
        
        // Some elements of character and car movement originated from JMEtests
        if(currentObject instanceof Character){
            Character character = (Character)currentObject;
            Vector3f camDir = sapp.getCamera().getDirection().mult(0.8f);
            Vector3f camLeft = sapp.getCamera().getLeft().mult(0.8f);
            camDir.y = 0;
            camLeft.y = 0;
            
            character.viewDirection.set(camDir);
            character.walkDirection.set(0, 0, 0);
           
            if (character.leftRotate) {
                character.viewDirection.addLocal(camLeft.mult(0.05f));
            } else
            if (character.rightRotate) {
                character.viewDirection.addLocal(camLeft.mult(0.05f).negate());
            }
            if (character.forward) {
                character.walkDirection.addLocal(camDir.mult(0.5f));
            } else
            if (character.backward) {
                character.walkDirection.addLocal(camDir.mult(0.5f).negate());
            }
            if (!isClient) {
                character.characterControl.setWalkDirection(character.walkDirection);
                character.characterControl.setViewDirection(character.viewDirection);  
            }
 
        }
        else{

        }
            
    
    }
        public void onAction(String binding, boolean value, float tpf) {
            
    }
        //handles going in and out of cars, and calls for control of the character
    public void characterAction(String binding, boolean value, float tpf, Character character) {
                    
        if (binding.equals("EnterExit")){
            if (value) {
                if(!character.isInVehicle){                  
                    if(character.collidedCar!= null){
                        if (character.collidedCar.occupied) {
                            return;
                        }
                        character.stopMovement();
                        
                        
                       character.collidedCar.occupied = true;
                       character.characterControl.setPhysicsLocation(new Vector3f(0,1,-10));
                       character.removeFromParent();                 
                       character.collidedCar.attachChild(character);
                       character.isInVehicle = true;           
                       character.invisible();
                       changeControl(character, character.collidedCar);
                    }
                }
                else{
                    ((Car) character.getParent()).occupied=false;
                    ((Car) character.getParent()).stopMovement();
                    System.out.println(character.getLocalTranslation() + " characterlocal");
                    Node characterParent = character.getParent();
                    Vector3f parentsLocalTranslation = characterParent.getLocalTranslation();
                    character.removeFromParent();
                    character.characterControl.setPhysicsLocation(parentsLocalTranslation.add(-2,1,0));
                    sapp.getRootNode().attachChild(character);
              
                     
                    character.visible();     
                    changeControl(character, character);
                    character.isInVehicle = false;
                }           
                
            } else {
              
            }
        }
        else{
            System.out.println("Not exitEnter");
            System.out.println(currentObject.toString());
            System.out.println(binding.toString() +" : " + value);
            character.control(binding, value, tpf);
        }
    }
}

