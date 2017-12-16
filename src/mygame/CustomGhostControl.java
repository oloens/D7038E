/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;

/**
 *
 * @author Anton
 */
public class CustomGhostControl extends RigidBodyControl implements PhysicsCollisionListener {
    
    public CustomGhostControl(BulletAppState bulletAppState){
        super(0f);
        //super(new SphereCollisionShape(0.7f));
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        //bulletAppState.getPhysicsSpace().add(this);
    }
    
    public void collision(PhysicsCollisionEvent event){
        System.out.println(event.getNodeA() + " node A");
        System.out.println(event.getNodeB() + " node B");
    }
    
}
