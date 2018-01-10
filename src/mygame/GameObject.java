/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;

/**
 *
 * @author Anton
 */
public abstract class GameObject extends Node {
    public GameObject() {
        
    }
    public Vector3f camPosition;
    private int id;
    GameObject currentObject;
    CameraNode camNode;
    Car collidedCar;
    Camera camera;
    GhostControl ghostControl;
    boolean visible = true;
    
    public GameObject(String name, Vector3f camPosition){
        super(name);
        this.camPosition = camPosition;
        
    }
    public void setID(int id) {
        this.id=id;
    }
    public int getID() {
        return this.id;
    }
    public abstract void control(String binding, boolean value, float tpf);
    
    
    // stanna objektet (används särkilt när man bytar kamera till ett annat objekt för att förhindra att nuvarande objektet fortsätte röra sig ( finns säkert en annan lösning)) 
    public abstract void stopMovement();
    
    public void invisible(){
        this.setCullHint(CullHint.Always);
        visible = false;
    }
    
    public void visible(){
        this.setCullHint(CullHint.Never);
        visible = true;

    }
    
}
