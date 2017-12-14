/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author Anton
 */
public class Car extends GameObject{
    
    
    VehicleControl carControl;
    
    private float wheelRadius;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    
    float stiffness = 120.0f;//200=f1 car
    float compValue = 0.2f; //(lower than damp!)
    float dampValue = 0.3f;
    final float mass = 400;
    
    public Car(String name, Node carNode){
        super(name);
        
        createCar(carNode);
    }
    
    private void createCar(Node carNode){
        carNode.setShadowMode(RenderQueue.ShadowMode.Cast);  
        Geometry chasis = findGeom(carNode, "Car"); 
        BoundingBox box = (BoundingBox) chasis.getModelBound();
        carNode.setLocalTranslation(new Vector3f(0, 15, -500));
        
         //Create a hull collision shape for the chassis
        CollisionShape carHull = CollisionShapeFactory.createDynamicMeshShape(chasis);

        //Create a vehicle control
        carControl = new VehicleControl(carHull, mass);
        
        //Setting default values for wheels
        carControl.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        carControl.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        carControl.setSuspensionStiffness(stiffness);
        carControl.setMaxSuspensionForce(10000);

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
        carControl.addWheel(wheel_fr.getParent(), box.getCenter().add(0, -front_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, true);

        Geometry wheel_fl = findGeom(carNode, "WheelFrontLeft");
        wheel_fl.center();
        box = (BoundingBox) wheel_fl.getModelBound();
        carControl.addWheel(wheel_fl.getParent(), box.getCenter().add(0, -front_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, true);

        Geometry wheel_br = findGeom(carNode, "WheelBackRight");
        wheel_br.center();
        box = (BoundingBox) wheel_br.getModelBound();
        carControl.addWheel(wheel_br.getParent(), box.getCenter().add(0, -back_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        Geometry wheel_bl = findGeom(carNode, "WheelBackLeft");
        wheel_bl.center();
        box = (BoundingBox) wheel_bl.getModelBound();
        carControl.addWheel(wheel_bl.getParent(), box.getCenter().add(0, -back_wheel_h, 0),
                wheelDirection, wheelAxle, 0.2f, wheelRadius, false);

        carControl.getWheel(2).setFrictionSlip(4);
        carControl.getWheel(3).setFrictionSlip(4);
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
    
    
    public void control(String binding, boolean value, float tpf){
        
//        if (binding.equals("Lefts")) {
//                if (value) {
//                    steeringValue += .2f;
//                } else {
//                    steeringValue += -.2f;
//                }
//                player.steer(steeringValue);
//            } else if (binding.equals("Rights")) {
//                if (value) {
//                steeringValue += -.2f;
//                } else {
//                steeringValue += .2f;
//                }
//                player.steer(steeringValue);
//            } //note that our fancy car actually goes backwards..
//            else if (binding.equals("Ups")) {
//            if (value) {
//                accelerationValue -= 800;
//            } else {
//                accelerationValue += 800;
//            }
//            player.accelerate(accelerationValue);
//            player.setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(findGeom(currentObject, "Car")));
//            } else if (binding.equals("Downs")) {
//                if (value) {
//                    player.brake(40f);
//                } else {
//                    player.brake(0f);
//                }
//            } else if (binding.equals("Reset")) {
//                if (value) {
//                    System.out.println("Reset");
//                    player.setPhysicsLocation(Vector3f.ZERO);
//                    player.setPhysicsRotation(new Matrix3f());
//                    player.setLinearVelocity(Vector3f.ZERO);
//                    player.setAngularVelocity(Vector3f.ZERO);
//                    player.resetSuspension();
//                } else {
//                }
//            }
        
    }
    
}
