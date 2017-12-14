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

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.util.CollisionShapeFactory;
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
import com.jme3.scene.control.CameraControl.ControlDirection;
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

    private BulletAppState bulletAppState;
   

    TerrainQuad terrain;
    Material matRock;
    boolean wireframe = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    Geometry collisionMarker;
    
    
    

    //private CharacterControl physicsCharacter;
    private Character characterObject;
    private CameraNode camNode;
    
    GameObject currentObject;                               // The object you "are" (or control) at the moment, a car or characer for example. 
    PhysicsControl currentControl;
    
    float airTime = 0;
    
//    private Vector3f walkDirection = new Vector3f(0,0,0);
//    private Vector3f viewDirection = new Vector3f(0,0,0);
//    boolean  forward = false, backward = false, 
//          leftRotate = false, rightRotate = false;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {
//        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
//        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
//        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
//        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
//        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
//        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
//        inputManager.addListener(this, "Lefts");
//        inputManager.addListener(this, "Rights");
//        inputManager.addListener(this, "Ups");
//        inputManager.addListener(this, "Downs");
//        inputManager.addListener(this, "Space");
//        inputManager.addListener(this, "Reset");
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
//        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        bulletAppState.getPhysicsSpace().setAccuracy(1f/30f);
        //rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        PssmShadowRenderer pssmr = new PssmShadowRenderer(assetManager, 2048, 3);
        pssmr.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
        pssmr.setLambda(0.55f);
        pssmr.setShadowIntensity(0.6f);
        pssmr.setCompareMode(CompareMode.Hardware);
        pssmr.setFilterMode(FilterMode.Bilinear);
        viewPort.addProcessor(pssmr);

        setupKeys();
        createTerrain();
        //buildCar();
        //buildHouse();
        createCharacter();

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(1.0f, 0.94f, 0.8f, 1f).multLocal(1.3f));
        dl.setDirection(new Vector3f(-0.5f, -0.3f, -0.3f).normalizeLocal());
        rootNode.addLight(dl);

        Vector3f lightDir2 = new Vector3f(0.70518064f, 0.5902297f, -0.39287305f);
        DirectionalLight dl2 = new DirectionalLight();
        dl2.setColor(new ColorRGBA(0.7f, 0.85f, 1.0f, 1f));
        dl2.setDirection(lightDir2);
        rootNode.addLight(dl2);
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
        Spatial house = assetManager.loadModel("Models/house1.j3o");
        house.addControl(new RigidBodyControl(0));
        rootNode.attachChild(house);
        house.setLocalScale(1f);
        house.setLocalTranslation(0,0,0);
        getPhysicsSpace().addAll(house);
        Material houseWall = new Material( assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        houseWall.setTexture("ColorMap", assetManager.loadTexture("Textures/housewall1.jpg"));
        //houseWall.scaleTextureCoordinates(new Vector2f(3,6));
        house.setMaterial(houseWall);
        
    }
    
    private void createCharacter() {

          
          //physicsCharacter.setPhysicsLocation(new Vector3f(0, 1, 0));
          Spatial model = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
          model.scale(0.25f);
          characterObject = new Character(model, "character node");
          characterObject.setLocalTranslation(new Vector3f(0, 35, 0));
          characterObject.addControl(characterObject.characterControl);
          getPhysicsSpace().add(characterObject.characterControl);
          rootNode.attachChild(characterObject);
          characterObject.attachChild(model);
          
          

          // set forward camera node that follows the character
          camNode = new CameraNode("CamNode", cam);
          camNode.setControlDir(ControlDirection.SpatialToCamera);
          camNode.setLocalTranslation(new Vector3f(0, 1, -5));
          camNode.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);
          characterObject.attachChild(camNode);

          //disable the default 1st-person flyCam (don't forget this!!)
          flyCam.setEnabled(false);
          
          
          
          
          currentObject = characterObject;
          currentControl = characterObject.characterControl;
          
    }
    
    private void buildCar() {

        

        //Load model and get chassis Geometry
        Node carNode = (Node)assetManager.loadModel("Models/Ferrari/Car.scene");
        carNode.setShadowMode(ShadowMode.Cast);
         
        Car carObject = new Car("Car", carNode);
        
        carNode.setLocalTranslation(new Vector3f(0, 15, -500));
        carObject.addControl(carObject.carControl);
        getPhysicsSpace().add(carObject.carControl);
        
        carObject.attachChild(carNode);
        rootNode.attachChild(carObject);
        
        
        

        // Disable the default flyby cam
            flyCam.setEnabled(false);
            //create the camera Node
            camNode = new CameraNode("Camera Node", cam);
            //This mode means that camera copies the movements of the target:
            camNode.setControlDir(ControlDirection.SpatialToCamera);
            
            //Move camNode, e.g. behind and above the target:
            camNode.setLocalTranslation(new Vector3f(0, 1, -5));
            //Rotate the camNode to look at the target:
            camNode.lookAt(carObject.getLocalTranslation(), Vector3f.UNIT_Y);
            //Attach the camNode to the target:
            carObject.attachChild(camNode);
            
            currentControl = carObject.carControl;
            currentObject = carObject;
            
            

          //disable the default 1st-person flyCam (don't forget this!!)
          flyCam.setEnabled(false);
          
    }

   
    private void changeControl(GameObject newObjectToControl){
        currentObject.detachChild(camNode);
        
        this.currentObject = newObjectToControl;
        
        flyCam.setEnabled(false);
        
        //This mode means that camera copies the movements of the target:
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        //Attach the camNode to the target:
        currentObject.attachChild(camNode);
        //Move camNode, e.g. behind and above the target:
        camNode.setLocalTranslation(new Vector3f(0, 4, 12));
        //Rotate the camNode to look at the target:
        camNode.lookAt(currentObject.getLocalTranslation(), Vector3f.UNIT_Y);
        //this.currentControl
    }

    public void onAnalog(String binding, float value, float tpf) {
    }


    
    public void onAction(String binding, boolean value, float tpf) {
        
        currentObject.control(binding, value, tpf);
        
//        if(currentControl instanceof CharacterControl){
//            
//            
//        }
//        else if(currentControl instanceof VehicleControl){
//            
//        }
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        cam.lookAt(currentObject.getWorldTranslation(), Vector3f.UNIT_Y);
        if(currentObject instanceof Character){
            Character character = (Character)currentObject;
            Vector3f camDir = cam.getDirection().mult(0.8f);
            Vector3f camLeft = cam.getLeft().mult(0.8f);
            camDir.y = 0;
            camLeft.y = 0;
            
            character.viewDirection.set(camDir);
            character.walkDirection.set(0, 0, 0);
           
            if (character.leftRotate) {
                character.viewDirection.addLocal(camLeft.mult(0.02f));
            } else
            if (character.rightRotate) {
                character.viewDirection.addLocal(camLeft.mult(0.02f).negate());
            }
            if (character.forward) {
                character.walkDirection.addLocal(camDir);
            } else
            if (character.backward) {
                character.walkDirection.addLocal(camDir.negate());
            }
            character.characterControl.setWalkDirection(character.walkDirection);
            character.characterControl.setViewDirection(character.viewDirection);
        }
            
    }

   
//    public void updateCamera() {
//        rootNode.updateGeometricState();
//
//        Vector3f pos = spaceCraft.getWorldTranslation().clone();
//        Quaternion rot = spaceCraft.getWorldRotation();
//        Vector3f dir = rot.getRotationColumn(2);
//
//        // make it XZ only
//        Vector3f camPos = new Vector3f(dir);
//        camPos.setY(0);
//        camPos.normalizeLocal();
//
//        // negate and multiply by distance from object
//        camPos.negateLocal();
//        camPos.multLocal(15);
//
//        // add Y distance
//        camPos.setY(2);
//        camPos.addLocal(pos);
//        cam.setLocation(camPos);
//
//        Vector3f lookAt = new Vector3f(dir);
//        lookAt.multLocal(7); // look at dist
//        lookAt.addLocal(pos);
//        cam.lookAt(lookAt, Vector3f.UNIT_Y);
//    }

//    @Override
//    public void simpleUpdate(float tpf) {
//    }

    private void createTerrain() {
        Spatial terrain = assetManager.loadModel("Scenes/MyScene.j3o");
        terrain.addControl(new RigidBodyControl(0));
        rootNode.attachChild(terrain);
        terrain.setLocalScale(1f);
        getPhysicsSpace().addAll(terrain);

    }
}
