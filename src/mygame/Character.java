/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author Anton
 */
public class Character extends GameObject{
    
    Vector3f walkDirection = new Vector3f(0,0,0);
    Vector3f viewDirection = new Vector3f(0,0,0);
    boolean forward = false, backward = false, 
    leftRotate = false, rightRotate = false;
    Spatial model;
    CharacterControl characterControl;
    
    boolean isInVehicle = false;
    
    public Character(Spatial model, String name, Vector3f camPosition){
        super(name, camPosition);
        this.model = model;
        //this.attachChild(model);
        characterControl = new CharacterControl(new CapsuleCollisionShape(0.5f, 1.8f), .1f);
        characterControl.setApplyPhysicsLocal(true);
        //characterControl.setPhysicsLocation(new Vector3f(0,1,0));
        
        
    }
    
    public void control(String binding, boolean value, float tpf){
        System.out.println("controlling character");
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
                
                System.out.println(forward);
            } else if (binding.equals("Downs")) {
                if (value) {
                    backward = true;
                } else {
                    backward = false;
                }
            } else if (binding.equals("Space")) {
                characterControl.jump();
            }
        
    }
    
    public void stopMovement(){
        forward = false;
        backward = false;
        leftRotate = false;
        rightRotate = false;
        this.walkDirection.set(0, 0, 0);
        this.characterControl.setWalkDirection(this.walkDirection);
    }
    
    
    public void invisible(){
        this.setCullHint(CullHint.Always);
    }
    
    public void visible(){
        this.setCullHint(CullHint.Never);
    }
    
    
    
}
