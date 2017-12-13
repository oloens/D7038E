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
    private PhysicsHoverControl hoverControl;
    private Spatial spaceCraft;
    TerrainQuad terrain;
    Material matRock;
    boolean wireframe = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    Geometry collisionMarker;
    
    private VehicleControl player;
    private VehicleWheel fr, fl, br, bl;
    private Node node_fr, node_fl, node_br, node_bl;
    private float wheelRadius;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private Node carNode;

    private CharacterControl physicsCharacter;
    private Node characterNode;
    //private CameraNode camNode;
    
    Node currentObject;
    PhysicsControl currentControl;
    
    float airTime = 0;
    
    private Vector3f walkDirection = new Vector3f(0,0,0);
    private Vector3f viewDirection = new Vector3f(0,0,0);
    boolean  forward = false, backward = false, 
          leftRotate = false, rightRotate = false;
    
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
        buildHouse();
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
//        CharacterControl character;
//        CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 4f);
//        character = new CharacterControl(capsule, 0.01f);
//        Node model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
//        //model.setLocalScale(0.5f);
//        model.addControl(character);
//        character.setPhysicsLocation(new Vector3f(-140, 40, -10));
//        rootNode.attachChild(model);
//        getPhysicsSpace().add(character);

          // Add a physics character to the world
          physicsCharacter = new CharacterControl(new CapsuleCollisionShape(0.5f, 1.8f), .1f);
          //physicsCharacter.setPhysicsLocation(new Vector3f(0, 1, 0));
          characterNode = new Node("character node");
          characterNode.setLocalTranslation(new Vector3f(0, 35, 0));
          Spatial model = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
          model.scale(0.25f);
          characterNode.addControl(physicsCharacter);
          getPhysicsSpace().add(physicsCharacter);
          rootNode.attachChild(characterNode);
          characterNode.attachChild(model);
          

          // set forward camera node that follows the character
          CameraNode camNode = new CameraNode("CamNode", cam);
          camNode.setControlDir(ControlDirection.SpatialToCamera);
          camNode.setLocalTranslation(new Vector3f(0, 1, -5));
          camNode.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);
          characterNode.attachChild(camNode);

          //disable the default 1st-person flyCam (don't forget this!!)
          flyCam.setEnabled(false);
          
          currentObject = characterNode;
          currentControl = physicsCharacter;
          
    }
    
    private void buildCar() {
//        spaceCraft = assetManager.loadModel("Models/Ferrari/Car.mesh.xml");
//        CollisionShape colShape = CollisionShapeFactory.createDynamicMeshShape(spaceCraft);
//        spaceCraft.setShadowMode(ShadowMode.CastAndReceive);
//        spaceCraft.setLocalTranslation(new Vector3f(-140, 50, -23));
//        spaceCraft.setLocalRotation(new Quaternion(new float[]{0, 0.01f, 0}));
//
//        hoverControl = new PhysicsHoverControl(colShape, 500);
//
//        spaceCraft.addControl(hoverControl);
//
//
//        rootNode.attachChild(spaceCraft);
//        getPhysicsSpace().add(hoverControl);
//        hoverControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
//
//        ChaseCamera chaseCam = new ChaseCamera(cam, inputManager);
//        spaceCraft.addControl(chaseCam);
//
//        flyCam.setEnabled(false);
        float stiffness = 120.0f;//200=f1 car
        float compValue = 0.2f; //(lower than damp!)
        float dampValue = 0.3f;
        final float mass = 400;

        //Load model and get chassis Geometry
        carNode = (Node)assetManager.loadModel("Models/Ferrari/Car.scene");
        carNode.setShadowMode(ShadowMode.Cast);
        Geometry chasis = findGeom(carNode, "Car");
        BoundingBox box = (BoundingBox) chasis.getModelBound();
        carNode.setLocalTranslation(new Vector3f(0, 15, -500));

        //Create a hull collision shape for the chassis
        CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(chasis);

        //Create a vehicle control
        player = new VehicleControl(carHull, mass);
        carNode.addControl(player);

        //Setting default values for wheels
        player.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        player.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        player.setSuspensionStiffness(stiffness);
        player.setMaxSuspensionForce(10000);

        //Create four wheels and add them at their locations
        //note that our fancy car actually goes backwards..
        Vector3f wheelDirection = new Vector3f(0, -1, 0);
        Vector3f wheelAxle = new Vector3f(-1, 0, 0);

        Geometry wheel_fr = findGeom(carNode, "WheelFrontRight");
        wheel_fr.center();
        box = (BoundingBox) wheel_fr.getModelBound();
        wheelRadius = box.getYExtent();
        float back_wheel_h = (wheelRadius * 1.7f) - 1f;
        float front_wheel_h = (wheelRadius * 1.9f) - 1f;
        player.addWheel(wheel_fr.getParent(), box.getCenter().add(0, -front_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, true);

        Geometry wheel_fl = findGeom(carNode, "WheelFrontLeft");
        wheel_fl.center();
        box = (BoundingBox) wheel_fl.getModelBound();
        player.addWheel(wheel_fl.getParent(), box.getCenter().add(0, -front_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, true);

        Geometry wheel_br = findGeom(carNode, "WheelBackRight");
        wheel_br.center();
        box = (BoundingBox) wheel_br.getModelBound();
        player.addWheel(wheel_br.getParent(), box.getCenter().add(0, -back_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        Geometry wheel_bl = findGeom(carNode, "WheelBackLeft");
        wheel_bl.center();
        box = (BoundingBox) wheel_bl.getModelBound();
        player.addWheel(wheel_bl.getParent(), box.getCenter().add(0, -back_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        player.getWheel(2).setFrictionSlip(4);
        player.getWheel(3).setFrictionSlip(4);

        rootNode.attachChild(carNode);
        getPhysicsSpace().add(player);
        
//        ChaseCamera chaseCam = new ChaseCamera(cam, inputManager);
//        carNode.addControl(chaseCam);
//
//        flyCam.setEnabled(false);

//        flyCam.setEnabled(false);
//        // Enable a chase cam for this target (typically the player).
//        ChaseCamera chaseCam = new ChaseCamera(cam, carNode, inputManager);
//        chaseCam.setSmoothMotion(true);

        // Disable the default flyby cam
            flyCam.setEnabled(false);
            //create the camera Node
            CameraNode camNode = new CameraNode("Camera Node", cam);
            //This mode means that camera copies the movements of the target:
            camNode.setControlDir(ControlDirection.SpatialToCamera);
            //Attach the camNode to the target:
            carNode.attachChild(camNode);
            //Move camNode, e.g. behind and above the target:
            camNode.setLocalTranslation(new Vector3f(0, 4, 12));
            //Rotate the camNode to look at the target:
            camNode.lookAt(carNode.getLocalTranslation(), Vector3f.UNIT_Y);
            
            currentControl = player;
    }

    

    public void onAnalog(String binding, float value, float tpf) {
    }

//    public void onAction(String binding, boolean value, float tpf) {
//        if (binding.equals("Lefts")) {
//            hoverControl.steer(value ? 50f : 0);
//        } else if (binding.equals("Rights")) {
//            hoverControl.steer(value ? -50f : 0);
//        } else if (binding.equals("Ups")) {
//            hoverControl.accelerate(value ? 100f : 0);
//        } else if (binding.equals("Downs")) {
//            hoverControl.accelerate(value ? -100f : 0);
//        } else if (binding.equals("Reset")) {
//            if (value) {
//                System.out.println("Reset");
//                hoverControl.setPhysicsLocation(new Vector3f(-140, 14, -23));
//                hoverControl.setPhysicsRotation(new Matrix3f());
//                hoverControl.clearForces();
//            } else {
//            }
//        } else if (binding.equals("Space") && value) {
//            //makeMissile();
//        }
//    }
    
    public void onAction(String binding, boolean value, float tpf) {
        if(currentControl instanceof CharacterControl){
            System.out.println("Hej");
            if (binding.equals("Lefts")) {
                if (value) {
                    leftRotate = true;
                } else {
                    leftRotate = false;
                }
            } else if (binding.equals("Rights")) {
                if (value) {
                    rightRotate = true;
                } else {
                    rightRotate = false;
                }
            }
            else if (binding.equals("Ups")) {
                if (value) {
                    forward = true;
                } else {
                    forward = false;
                }
            } else if (binding.equals("Downs")) {
                if (value) {
                    backward = true;
                } else {
                    backward = false;
                }
            } else if (binding.equals("Space")) {
                physicsCharacter.jump();
            }
            
        }
        else if(currentControl instanceof VehicleControl){
            if (binding.equals("Lefts")) {
                if (value) {
                    steeringValue += .2f;
                } else {
                    steeringValue += -.2f;
                }
                player.steer(steeringValue);
            } else if (binding.equals("Rights")) {
                if (value) {
                steeringValue += -.2f;
                } else {
                steeringValue += .2f;
                }
                player.steer(steeringValue);
            } //note that our fancy car actually goes backwards..
            else if (binding.equals("Ups")) {
            if (value) {
                accelerationValue -= 800;
            } else {
                accelerationValue += 800;
            }
            player.accelerate(accelerationValue);
            player.setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(findGeom(currentObject, "Car")));
            } else if (binding.equals("Downs")) {
                if (value) {
                    player.brake(40f);
                } else {
                    player.brake(0f);
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    System.out.println("Reset");
                    player.setPhysicsLocation(Vector3f.ZERO);
                    player.setPhysicsRotation(new Matrix3f());
                    player.setLinearVelocity(Vector3f.ZERO);
                    player.setAngularVelocity(Vector3f.ZERO);
                    player.resetSuspension();
                } else {
                }
            }
        }
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        cam.lookAt(currentObject.getWorldTranslation(), Vector3f.UNIT_Y);
        if(currentControl instanceof CharacterControl){
            Vector3f camDir = cam.getDirection().mult(0.8f);
            Vector3f camLeft = cam.getLeft().mult(0.8f);
            camDir.y = 0;
            camLeft.y = 0;
            viewDirection.set(camDir);
            walkDirection.set(0, 0, 0);
           
            if (leftRotate) {
                viewDirection.addLocal(camLeft.mult(0.02f));
            } else
            if (rightRotate) {
                viewDirection.addLocal(camLeft.mult(0.02f).negate());
            }
            if (forward) {
                walkDirection.addLocal(camDir);
            } else
            if (backward) {
                walkDirection.addLocal(camDir.negate());
            }
            physicsCharacter.setWalkDirection(walkDirection);
            physicsCharacter.setViewDirection(viewDirection);
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
        
//        matRock = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
//        matRock.setBoolean("useTriPlanarMapping", false);
//        matRock.setBoolean("WardIso", true);
//        matRock.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
//        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
//        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
//        grass.setWrap(WrapMode.Repeat);
//        matRock.setTexture("DiffuseMap", grass);
//        matRock.setFloat("DiffuseMap_0_scale", 64);
//        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
//        dirt.setWrap(WrapMode.Repeat);
//        matRock.setTexture("DiffuseMap_1", dirt);
//        matRock.setFloat("DiffuseMap_1_scale", 16);
//        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
//        rock.setWrap(WrapMode.Repeat);
//        matRock.setTexture("DiffuseMap_2", rock);
//        matRock.setFloat("DiffuseMap_2_scale", 128);
//        Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
//        normalMap0.setWrap(WrapMode.Repeat);
//        Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
//        normalMap1.setWrap(WrapMode.Repeat);
//        Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
//        normalMap2.setWrap(WrapMode.Repeat);
//        matRock.setTexture("NormalMap", normalMap0);
//        matRock.setTexture("NormalMap_1", normalMap2);
//        matRock.setTexture("NormalMap_2", normalMap2);
//
//        AbstractHeightMap heightmap = null;
//        try {
//            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
//            heightmap.load();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
//        List<Camera> cameras = new ArrayList<Camera>();
//        cameras.add(getCamera());
//        TerrainLodControl control = new TerrainLodControl(terrain, cameras);
//        terrain.addControl(control);
//        terrain.setMaterial(matRock);
//        terrain.setLocalScale(new Vector3f(2, 2, 2));
//        terrain.setLocked(false); // unlock it so we can edit the height
//
//        terrain.setShadowMode(ShadowMode.CastAndReceive);
//        terrain.addControl(new RigidBodyControl(0));
//        rootNode.attachChild(terrain);
//        getPhysicsSpace().addAll(terrain);

    }
}
