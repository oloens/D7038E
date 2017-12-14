/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.scene.Node;

/**
 *
 * @author Anton
 */
public abstract class GameObject extends Node {
    
    public GameObject(String name){
        super(name);
    }
    
    public abstract void control(String binding, boolean value, float tpf);
    
}
