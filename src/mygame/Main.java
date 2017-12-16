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

    private BulletAppState bulletAppState;
   

    TerrainQuad terrain;
    Material matRock;
    boolean wireframe = false;
    protected BitmapText hintText;
    PointLight pl;
    Geometry lightMdl;
    Geometry collisionMarker;
    
    
    ArrayList<Car> carsStore = new ArrayList<Car>();
    private Car carObject;
    private Car collidedCar;
    //private CharacterControl physicsCharacter;
    private Character characterObject;
    private CameraNode camNode;
    
    GameObject currentObject;                               // The object you "are" (or control) at the moment, a car or characer for example. 
    PhysicsControl currentControl;
    
    float airTime = 0;
    
    GhostControl ghostControl; 
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

        ghostControl = new GhostControl(new SphereCollisionShape(0.7f));
        getPhysicsSpace().add(ghostControl);
        
        setupKeys();
        createTerrain();
        buildCar(new Vector3f(-20,0,-20));
        buildCar(new Vector3f(-30,0,-30));
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

          //CustomGhostControl ghost = new CustomGhostControl(bulletAppState);
          //physicsCharacter.setPhysicsLocation(new Vector3f(0, 1, 0));
          Spatial model = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
          Spatial model2 = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
          model.scale(0.25f);
          model2.scale(0.25f);
          Character characterObject2 = new Character(model2, "character node", new Vector3f(0,1,-5));
          characterObject2.attachChild(model2);
          //rootNode.attachChild(characterObject2);
          characterObject2.setLocalTranslation(0,1,0);
//        Cylinder cylinderMesh = new Cylinder(100,100, 1f, 1f, true);
//        Geometry cylinderGeo = new Geometry("Cylinder", cylinderMesh);   
//        Material cylinderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        cylinderMat.setColor("Color", ColorRGBA.Red);
//        cylinderGeo.setMaterial(cylinderMat);
       // Node  cylinderNode = new Node("cylinder");
        //cylinderNode.attachChild(characterObject2);
        //cylinderNode.setLocalTranslation(new Vector3f(-2, 0,0));   
          
          
          //characterObject2.setLocalTranslation(0,20,0);
          characterObject = new Character(model, "character node", new Vector3f(0, 1, -5));
          //characterObject.attachChild(cylinderNode);
          //characterObject.setLocalTranslation(new Vector3f(0, 5, 0));
          
          characterObject.addControl(characterObject.characterControl);
          characterObject.addControl(ghostControl);
          //carObject.addControl(ghost);
          
          //ghost.setPhysicsLocation(new Vector3f(0,5,0));
          //ghost.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
          //ghost.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
          
          //characterObject.setLocalTranslation(new Vector3f(-5, 5, 0));
          //characterObject.characterControl.setPhysicsLocation(new Vector3f(-5,5,0));
          getPhysicsSpace().add(characterObject.characterControl);
          characterObject.characterControl.setPhysicsLocation(new Vector3f(-5,5,0));
          //getPhysicsSpace().add(ghost);
          
          //characterObject.characterControl.setEnabled(false);
          //getPhysicsSpace().remove(characterObject.characterControl);
          
         
          
          
          
          rootNode.attachChild(characterObject);
          characterObject.attachChild(model);
          //characterObject.attachChild(characterObject2);
          //rootNode.attachChild(characterObject2);
          

          // set forward camera node that follows the character
          camNode = new CameraNode("CamNode", cam);
          camNode.setControlDir(ControlDirection.SpatialToCamera);
          camNode.setLocalTranslation(characterObject.camPosition);
          camNode.lookAt(model.getLocalTranslation(), Vector3f.UNIT_Y);
          characterObject.attachChild(camNode);

          //disable the default 1st-person flyCam (don't forget this!!)
          flyCam.setEnabled(false);
          
          
          
          
          currentObject = characterObject;
          currentControl = characterObject.characterControl;
          
         
          
    }
    
    private void buildCar(Vector3f initialPosition) {

        
        //CustomGhostControl ghost = new CustomGhostControl(bulletAppState);
        //Load model and get chassis Geometry
        Spatial carNode = assetManager.loadModel("Models/Ferrari/Car.scene");
        //carNode.scale(0.25f);
        carNode.setShadowMode(ShadowMode.Cast);
         
        carObject = new Car("Car", carNode, new Vector3f(0, 4, 12));
        
        carObject.setLocalTranslation(initialPosition);
        carObject.addControl(carObject.carControl);
        //carObject.addControl(ghostControl);
        //carObject.addControl(ghost);
        
//        ghost.setPhysicsLocation(new Vector3f(-20,0,-20));
//        ghost.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
//        ghost.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
        
        getPhysicsSpace().add(carObject.carControl);
        //getPhysicsSpace().add(ghost);
        
        carObject.attachChild(carNode);
        rootNode.attachChild(carObject);
        
        carsStore.add(carObject);
        
        
        
        

//        // Disable the default flyby cam
//            flyCam.setEnabled(false);
//            //create the camera Node
//            camNode = new CameraNode("Camera Node", cam);
//            //This mode means that camera copies the movements of the target:
//            camNode.setControlDir(ControlDirection.SpatialToCamera);
//            
//            //Move camNode, e.g. behind and above the target:
//            camNode.setLocalTranslation(new Vector3f(0, 4, 12));
//            //Rotate the camNode to look at the target:
//            camNode.lookAt(carNode.getLocalTranslation(), Vector3f.UNIT_Y);
//            //Attach the camNode to the target:
//            carObject.attachChild(camNode);
//            
//            currentControl = carObject.carControl;
//            currentObject = carObject;
            
            

          //disable the default 1st-person flyCam (don't forget this!!)
          flyCam.setEnabled(false);
          
    }

   
    private void changeControl(GameObject newObjectToControl){
        currentObject.stopMovement();
        currentObject.detachChild(camNode);
        
        this.currentObject = newObjectToControl;
        
        flyCam.setEnabled(false);
        
        //This mode means that camera copies the movements of the target:
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        //Attach the camNode to the target:
        currentObject.attachChild(camNode);
        //Move camNode, e.g. behind and above the target:
        //camNode.setLocalTranslation(new Vector3f(0, 4, 12));
        camNode.setLocalTranslation(newObjectToControl.camPosition);
        //Rotate the camNode to look at the target:
        camNode.lookAt(currentObject.getLocalTranslation(), Vector3f.UNIT_Y);
        //this.currentControl
    }

    public void onAnalog(String binding, float value, float tpf) {
    }


    
    public void onAction(String binding, boolean value, float tpf) {
            
        
        if (binding.equals("EnterExit")){
            if (value) {
                
                if(!characterObject.isInVehicle){
                    
                    if(collidedCar!= null){
                      
                       System.out.println(collidedCar.toString()+ " collidedCaronAction");
                       //Vector3f localPoint = new Vector3f();
                       //collidedCar.worldToLocal(collidedCar.getWorldTranslation(), localPoint);
                       
                       //characterObject.removeFromParent();
                       
                       //collidedCar.attachChild(characterObject);
                        // System.out.println(characterObject.getParent().toString() + " character parent");
                       //characterObject.setLocalTranslation(localPoint);
                       
                       //characterObject.setWorldTranslation(collidedCar.getWorldTranslation());
                       
                       //Vector3f localPoint = new Vector3f();
                       //collidedCar.worldToLocal(characterObject.characterControl.getPhysicsLocation(), localPoint);
                       
                       //characterObject.setLocalTranslation(new Vector3f(0,0,-10));
                       characterObject.characterControl.setPhysicsLocation(new Vector3f(0,1,-10));
                       characterObject.removeFromParent();
                       
                       //Node character = new Node("Car");
                       //character.attachChild(characterObject);
                       
                       collidedCar.attachChild(characterObject);
                       System.out.println(characterObject.getParent().toString() + " PARENT OF CHARACTER");
                       //characterObject.characterControl.setPhysicsLocation(localPoint);
                      // character.setLocalTranslation(localPoint);
                       //System.out.println(localPoint.toString());
                       //System.out.println(characterObject.getWorldTranslation());
//
//
//                        System.out.println("ContactPoint: " + " X: " + contactPoint.getX() + " Y: " + contactPoint.getY() + " Z: " + contactPoint.getZ());
//                        System.out.println("localPoint: " + " X: " + localPoint.getX() + " Y: " + localPoint.getY() + " Z: " + localPoint.getZ()); 
//
//                        hitObjectNode.attachChild(snowBall);
//                        snowBall.setLocalTranslation(localPoint);
                       
                       
                       characterObject.isInVehicle = true;
                      // characterObject.stopMovement();
                       characterObject.invisible();
                       changeControl(collidedCar);
                       //collidedCar.attachChild(characterObject);
                       
//                       hitObjectNode.worldToLocal(contactPoint, localPoint);
//              
// 
//                        System.out.println("ContactPoint: " + " X: " + contactPoint.getX() + " Y: " + contactPoint.getY() + " Z: " + contactPoint.getZ());
//                        System.out.println("localPoint: " + " X: " + localPoint.getX() + " Y: " + localPoint.getY() + " Z: " + localPoint.getZ()); 
//
//                        hitObjectNode.attachChild(snowBall);
//                        snowBall.setLocalTranslation(localPoint);
                    }
                    
                }
                else{
                    
                    System.out.println(currentObject.getLocalTranslation()+ " carlocal");
                    System.out.println(characterObject.getLocalTranslation() + " characterlocal");
                    //characterObject.setLocalTranslation(new Vector3f(20,5,20));
                    //rootNode.attachChild(characterObject);
                    
                    System.out.println(characterObject.getLocalTranslation() + " characterlocal");
                    Node characterParent = characterObject.getParent();
                    Vector3f parentsLocalTranslation = characterParent.getLocalTranslation();
                    characterObject.removeFromParent();
                    
                    characterObject.characterControl.setPhysicsLocation(parentsLocalTranslation.add(-2,1,0));
                    Vector3f localPoint = new Vector3f();
                   //characterObject.worldToLocal(characterObject.characterControl.getPhysicsLocation(),localPoint);
                    rootNode.attachChild(characterObject);
                    //characterObject.setLocalTranslation(localPoint);
//              hitObjectNode.worldToLocal(contactPoint, localPoint);
//              
// 
//              System.out.println("ContactPoint: " + " X: " + contactPoint.getX() + " Y: " + contactPoint.getY() + " Z: " + contactPoint.getZ());
//              System.out.println("localPoint: " + " X: " + localPoint.getX() + " Y: " + localPoint.getY() + " Z: " + localPoint.getZ()); 
// 
//              hitObjectNode.attachChild(currentObject);
//              currentObject.setLocalTranslation(localPoint);
                    
                     characterObject.visible();
                    
                    changeControl(characterObject);
                    //System.out.pricharacterObject.getLocalTranslation()
                    
                    //Node parrent = characterObject.getParent();
                    //System.out.println(parrent);
                    //parrent.detachChildAt(0);
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
        
        //System.out.println(characterObject.characterControl.getPhysicsLocation() + " Character location physics location");
        //System.out.println(characterObject.getLocalTranslation() + " Character location local translation");
        List<PhysicsCollisionObject> overLappingObjects = ghostControl.getOverlappingObjects();
        //System.out.println(overLappingObjects);
        
        boolean collide = false;
        for(int i=0; i<carsStore.size(); i++){
            if(ghostControl.getOverlappingObjects().contains(carsStore.get(i).getControl(PhysicsControl.class))){
                collidedCar = carsStore.get(i);
                collide = true;
                //System.out.println("collide with car");   
                
                BitmapFont myFont= assetManager.loadFont("Interface/Fonts/Console.fnt");
                BitmapText hudText = new BitmapText(myFont, false);
                hudText.setSize(myFont.getCharSet().getRenderedSize() * 2);
                hudText.setColor(ColorRGBA.White);
                hudText.setText("Press F to enter car");
                hudText.setLocalTranslation(20, settings.getHeight()-20, 0);
                guiNode.attachChild(hudText);
            }
        }
        if(collide == false){
            collidedCar = null;
            guiNode.detachAllChildren();
        }
        
        if(ghostControl.getOverlappingObjects().contains(carObject.getControl(PhysicsControl.class))){
            
        }
        
//        CollisionResults results = new CollisionResults();
//            // 2. Aim the ray from cam loc to cam direction.
//            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
//            // 3. Collect intersections between Ray and Shootables in results list.
//            carObject.collideWith(ray, results);
//            // 4. Print the results
//            System.out.println("----- Collisions? " + results.size() + "-----");
//            for (int i = 0; i < results.size(); i++) {
//              // For each hit, we know distance, impact point, name of geometry.
//              float dist = results.getCollision(i).getDistance();
//              Vector3f pt = results.getCollision(i).getContactPoint();
//              String hit = results.getCollision(i).getGeometry().getName();
//              System.out.println("* Collision #" + i);
//              System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
//            }
//            // 5. Use the results (we mark the hit object)
//            //Node snowBall = createSnowBall();
//            if (results.size() > 0) {
//              // The closest collision point is what was truly hit:
//              CollisionResult closest = results.getClosestCollision();
//              // Let's interact - we mark the hit with a red dot.
//              Geometry hitObjectGeo = closest.getGeometry();
//              System.out.println(hitObjectGeo.getName());
//              Node hitObjectNode = hitObjectGeo.getParent();
//              System.out.println(hitObjectNode.getName());
//              //Vector3f hitObjectNodePoint = hitObjectNode.getWorldTranslation();
//              Vector3f contactPoint = closest.getContactPoint();
//              Vector3f localPoint = new Vector3f();
//              hitObjectNode.worldToLocal(contactPoint, localPoint);
//              
// 
//              System.out.println("ContactPoint: " + " X: " + contactPoint.getX() + " Y: " + contactPoint.getY() + " Z: " + contactPoint.getZ());
//              System.out.println("localPoint: " + " X: " + localPoint.getX() + " Y: " + localPoint.getY() + " Z: " + localPoint.getZ()); 
// 
//              hitObjectNode.attachChild(currentObject);
//              currentObject.setLocalTranslation(localPoint);
//              
//            }
        
        cam.lookAt(currentObject.getWorldTranslation(), Vector3f.UNIT_Y);
        if(currentObject instanceof Character){
            //System.out.println("Character current");
            Character character = (Character)currentObject;
            Vector3f camDir = cam.getDirection().mult(0.8f);
            Vector3f camLeft = cam.getLeft().mult(0.8f);
            camDir.y = 0;
            camLeft.y = 0;
            
            character.viewDirection.set(camDir);
            character.walkDirection.set(0, 0, 0);
           
            if (character.leftRotate) {
                System.out.println("leftRotate su");
                character.viewDirection.addLocal(camLeft.mult(0.02f));
            } else
            if (character.rightRotate) {
                System.out.println("rightRotate su");
                character.viewDirection.addLocal(camLeft.mult(0.02f).negate());
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
//            System.out.println("hdeuhduehd");
//             characterObject.walkDirection.set(0, 0, 0);
//             characterObject.characterControl.setWalkDirection(characterObject.walkDirection);
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
        RigidBodyControl rigidBodyControl = new RigidBodyControl(0);
        terrain.addControl(rigidBodyControl);
        //rigidBodyControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        rootNode.attachChild(terrain);
        terrain.setLocalScale(1f);
        getPhysicsSpace().addAll(terrain);

    }
}
