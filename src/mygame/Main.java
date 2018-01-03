/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
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

public class Main extends SimpleApplication implements AnalogListener,
        ActionListener {

    Game game;
    public static void main(String[] args) {
        Main app = new Main();

        app.start();
    }
    public Main() {
        game = new Game();
        stateManager.attach(game);
    }
    

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("EnterExit", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
        inputManager.addListener(this, "EnterExit");
    }

    @Override
    public void simpleInitApp() {
                setupKeys();

    }
    
    // Tagen från JmeTests




    public void onAnalog(String binding, float value, float tpf) {
    }


    
    public void onAction(String binding, boolean value, float tpf) {
        game.onAction(binding, value, tpf);
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        
        //System.out.println(characterObject.characterControl.getPhysicsLocation() + " Character location physics location");
        //System.out.println(characterObject.getLocalTranslation() + " Character location local translation");
        
    
    // Laddar in en terräng från Scenes som är skapad i Scene composer

    }
}
class Game extends BaseAppState {
    protected BulletAppState bulletAppState;
    private SimpleApplication sapp;

    TerrainQuad terrain;
    Material matRock;
    boolean wireframe = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    Geometry collisionMarker;
    
    
    protected ArrayList<Car> cars = new ArrayList<Car>();        // Alla bilar, ( Kanske ska ändras till alla objekt(?))
    private Car carObject;
    private Car collidedCar;
    
    private Character characterObject;                      // Avataren man är 
    private CameraNode camNode;
    protected ArrayList<Character> characters = new ArrayList<Character>();
    
    GameObject currentObject;                               // The object you "are" (or control) at the moment, a car or characer for example. 
    PhysicsControl currentControl;
    
    float airTime = 0;
    
    GhostControl ghostControl;                            // Används för att upptäcka kollisioner

    public void onDisable() {
        
    }
    public void onEnable() {
        bulletAppState = new BulletAppState();
        sapp.getStateManager().attach(bulletAppState);
       // bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        //        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
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
        
        //setupKeys();
        createTerrain();
        buildCar(new Vector3f(-20,0,-20));
        buildCar(new Vector3f(-30,0,-30));
        //buildHouse();
        createCharacter();
        

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(1.0f, 0.94f, 0.8f, 1f).multLocal(1.3f));
        dl.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
        sapp.getRootNode().addLight(dl);

        Vector3f lightDir2 = new Vector3f(0.70518064f, 0.5902297f, -0.39287305f);
        DirectionalLight dl2 = new DirectionalLight();
        dl2.setColor(new ColorRGBA(0.7f, 0.85f, 1.0f, 1f));
        dl2.setDirection(lightDir2);
        sapp.getRootNode().addLight(dl2);
    

    }
    private void createTerrain() {
        Spatial terrain = sapp.getAssetManager().loadModel("Scenes/MyScene.j3o");
        RigidBodyControl rigidBodyControl = new RigidBodyControl(0);
        terrain.addControl(rigidBodyControl);
        //rigidBodyControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        sapp.getRootNode().attachChild(terrain);
        terrain.setLocalScale(1f);
        getPhysicsSpace().addAll(terrain);

    }
    public void cleanup(Application app) {
        
    }
    public void initialize(Application app) {
        sapp = (SimpleApplication) app;
    }
    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }
    
    private Geometry findGeom(Spatial spatial, String name) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (int i = 0; i < node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                Geometry result = findGeom(child, name);
                if (result != null) {
                    return result;
                }
            }
        } else if (spatial instanceof Geometry) {
            if (spatial.getName().startsWith(name)) {
                return (Geometry) spatial;
            }
        }
        return null;
    }
    private void buildHouse(){
        Spatial house = sapp.getAssetManager().loadModel("Models/house1.j3o");
        house.addControl(new RigidBodyControl(0));
        sapp.getRootNode().attachChild(house);
        house.setLocalScale(1f);
        house.setLocalTranslation(0,0,0);
        getPhysicsSpace().addAll(house);
        Material houseWall = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        houseWall.setTexture("ColorMap", sapp.getAssetManager().loadTexture("Textures/housewall1.jpg"));
        //houseWall.scaleTextureCoordinates(new Vector2f(3,6));
        house.setMaterial(houseWall);
        
    }
    
    private void createCharacter() {

          Spatial model = sapp.getAssetManager().loadModel("Models/Sinbad/Sinbad.mesh.xml");   
          model.scale(0.25f);
     
          characterObject = new Character(model, "character node", new Vector3f(0, 1, -5));
          characters.add(characterObject);
          characterObject.addControl(characterObject.characterControl);
          characterObject.addControl(ghostControl);
         
          getPhysicsSpace().add(characterObject.characterControl);
          characterObject.characterControl.setPhysicsLocation(new Vector3f(-5,5,0));
          
          sapp.getRootNode().attachChild(characterObject);
          characterObject.attachChild(model);
          
          // set forward camera node that follows the character
          camNode = new CameraNode("CamNode", sapp.getCamera());
          camNode.setControlDir(ControlDirection.SpatialToCamera);
          camNode.setLocalTranslation(characterObject.camPosition);
          camNode.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);
          characterObject.attachChild(camNode);

          //disable the default 1st-person flyCam (don't forget this!!)
          sapp.getFlyByCamera().setEnabled(false);

          currentObject = characterObject;
          currentControl = characterObject.characterControl;
          
    }
    public Character spawnEntity(String type) {
        if (type.equals("Car")) {
            //TODO
        }
        else {
          
          Spatial model = sapp.getAssetManager().loadModel("Models/Sinbad/Sinbad.mesh.xml");   
          model.scale(0.25f);
     
          characterObject = new Character(model, "character node", new Vector3f(0, 1, -5));
          characters.add(characterObject);
          characterObject.addControl(characterObject.characterControl);
          characterObject.addControl(ghostControl);
         
          getPhysicsSpace().add(characterObject.characterControl);
          characterObject.characterControl.setPhysicsLocation(new Vector3f(-5,5,0));
          
          sapp.getRootNode().attachChild(characterObject);
          characterObject.attachChild(model);
          
          return characterObject;

        }
        return null;
    }
    
    private void buildCar(Vector3f initialPosition) {

        //Load model and get chassis Geometry
        Spatial carNode = sapp.getAssetManager().loadModel("Models/Ferrari/Car.scene");
       
        carNode.setShadowMode(ShadowMode.Cast);  
        carObject = new Car("Car", carNode, new Vector3f(0, 4, 12));
        
        carObject.setLocalTranslation(initialPosition);
        carObject.addControl(carObject.carControl);
        
        getPhysicsSpace().add(carObject.carControl);
        
        carObject.attachChild(carNode);
        sapp.getRootNode().attachChild(carObject);
        
        cars.add(carObject);       

        //disable the default 1st-person flyCam (don't forget this!!)
        sapp.getFlyByCamera().setEnabled(false);
          
    }

    // Byter nuvarande objektet som kameran riktas på 
    private void changeControl(GameObject newObjectToControl){
        currentObject.stopMovement();
        currentObject.detachChild(camNode);
        
        this.currentObject = newObjectToControl;
        
        sapp.getFlyByCamera().setEnabled(false);
             
        camNode.setControlDir(ControlDirection.SpatialToCamera);
      
        currentObject.attachChild(camNode);
       
        camNode.setLocalTranslation(newObjectToControl.camPosition);
  
        camNode.lookAt(currentObject.getLocalTranslation(), Vector3f.UNIT_Y);

    }
    @Override
    public void update(float tpf) {
        List<PhysicsCollisionObject> overLappingObjects = ghostControl.getOverlappingObjects();
  
        
        boolean collide = false;
        for(int i=0; i<cars.size(); i++){
            if(ghostControl.getOverlappingObjects().contains(cars.get(i).getControl(PhysicsControl.class))){
                collidedCar = cars.get(i);
                collide = true;
                //System.out.println("collide with car");   
                
                BitmapFont myFont= sapp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
                BitmapText hudText = new BitmapText(myFont, false);
                hudText.setSize(myFont.getCharSet().getRenderedSize() * 2);
                hudText.setColor(ColorRGBA.White);
                hudText.setText("Press F to enter car");
                hudText.setLocalTranslation(20, 50/*sapp.settings.getHeight()*/-20, 0);
                sapp.getGuiNode().attachChild(hudText);
            }
        }
        if(collide == false){
            collidedCar = null;
            sapp.getGuiNode().detachAllChildren();
        }
     
        
        sapp.getCamera().lookAt(currentObject.getWorldTranslation(), Vector3f.UNIT_Y);
        // Taget från JmeTests
        if(currentObject instanceof Character){
            //System.out.println("Character current");
            Character character = (Character)currentObject;
            Vector3f camDir = sapp.getCamera().getDirection().mult(0.8f);
            Vector3f camLeft = sapp.getCamera().getLeft().mult(0.8f);
            camDir.y = 0;
            camLeft.y = 0;
            
            character.viewDirection.set(camDir);
            character.walkDirection.set(0, 0, 0);
           
            if (character.leftRotate) {
                System.out.println("leftRotate su");
                character.viewDirection.addLocal(camLeft.mult(0.05f));
            } else
            if (character.rightRotate) {
                System.out.println("rightRotate su");
                character.viewDirection.addLocal(camLeft.mult(0.05f).negate());
            }
            if (character.forward) {
                System.out.println("forward su");
                character.walkDirection.addLocal(camDir.mult(0.5f));
            } else
            if (character.backward) {
                System.out.println("backward su");
                character.walkDirection.addLocal(camDir.mult(0.5f).negate());
            }
            character.characterControl.setWalkDirection(character.walkDirection);
            character.characterControl.setViewDirection(character.viewDirection);
        }
        else{

        }
            
    
    }
        public void onAction(String binding, boolean value, float tpf) {
            
        if (binding.equals("EnterExit")){
            if (value) {
                if(!characterObject.isInVehicle){                  
                    if(collidedCar!= null){
                       //System.out.println(collidedCar.toString()+ " collidedCaronAction");                  
                       characterObject.characterControl.setPhysicsLocation(new Vector3f(0,1,-10));
                       characterObject.removeFromParent();                 
                       collidedCar.attachChild(characterObject);
                       //System.out.println(characterObject.getParent().toString() + " PARENT OF CHARACTER");     
                       characterObject.isInVehicle = true;           
                       characterObject.invisible();
                       changeControl(collidedCar);
                    }
                }
                else{
                    //System.out.println(currentObject.getLocalTranslation()+ " carlocal");
                    //System.out.println(characterObject.getLocalTranslation() + " characterlocal");
                  
                    System.out.println(characterObject.getLocalTranslation() + " characterlocal");
                    Node characterParent = characterObject.getParent();
                    Vector3f parentsLocalTranslation = characterParent.getLocalTranslation();
                    characterObject.removeFromParent();
                    characterObject.characterControl.setPhysicsLocation(parentsLocalTranslation.add(-2,1,0));
                    sapp.getRootNode().attachChild(characterObject);
              
//              System.out.println("ContactPoint: " + " X: " + contactPoint.getX() + " Y: " + contactPoint.getY() + " Z: " + contactPoint.getZ());
//              System.out.println("localPoint: " + " X: " + localPoint.getX() + " Y: " + localPoint.getY() + " Z: " + localPoint.getZ()); 
                     
                    characterObject.visible();     
                    changeControl(characterObject);
                    characterObject.isInVehicle = false;
                }           
                
            } else {
              
            }
        }
        else{
            System.out.println("Not exitEnter");
            System.out.println(currentObject.toString());
            System.out.println(binding.toString() +" : " + value);
            currentObject.control(binding, value, tpf);
        }
        
    }

}
