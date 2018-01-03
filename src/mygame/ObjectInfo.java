/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import java.lang.annotation.Annotation;

/**
 *
 * @author olofe
 */
public class ObjectInfo{
    public int id;
    public Vector3f position;
    public Vector3f viewDirection;
    public Vector3f walkDirection;
    public Vector3f velocity;
    public ObjectInfo(int id, Vector3f position, Vector3f walkDirection, 
            Vector3f viewDirection) {
        this.id=id;
        this.viewDirection=viewDirection;
        this.walkDirection=walkDirection;
        this.position=position;
    }
    public ObjectInfo() {
        
    }


}
