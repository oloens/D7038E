/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;


/**
 * Based on example by Håkan Jonsson
 * 
 * Still has classes and attributes that are unused. 
 *
 * @author Anton Eliasson, Jonathan Olsson, Olof Enström
 */
public class Util {
    public static final int PORT = 7003;
    public static final String HOSTNAME = "127.0.0.1";
    // register all message types there are
    public static void initialiseSerializables() {
        Serializer.registerClass(StartGameMessage.class);
        Serializer.registerClass(StopGameMessage.class);
        Serializer.registerClass(ChangeVelocityMessage.class);
        Serializer.registerClass(UpdateMessage.class);

    }



    // an intermediate class that contains some common declarations
    abstract public static class MyAbstractMessage extends AbstractMessage {

        protected int destinationID = -1;

        public MyAbstractMessage() {
            
        }

        public void setDestination(int id) {
            this.destinationID=id;
        }

    }

    


    @Serializable
    public static class StartGameMessage extends MyAbstractMessage {
        
        
        public StartGameMessage() {
;       
        }

    }
    @Serializable
    public static class StopGameMessage extends MyAbstractMessage {
        
        public StopGameMessage() {
        }

    }
    
    @Serializable
    public static class ChangeVelocityMessage extends MyAbstractMessage {
        int id;
        String name;
        boolean isPressed;
        float tpf;
        public ChangeVelocityMessage(int id, String name, boolean isPressed, float tpf) {
            this.id=id;
            this.name=name;
            this.isPressed=isPressed;
            this.tpf=tpf;
        }
        
        public ChangeVelocityMessage() {
            
        }


}
    @Serializable
    public static class UpdateMessage extends MyAbstractMessage {
        int[] ids;
        Vector3f[] viewDirections;
        Vector3f[] walkDirections;
        Vector3f[] positions;
        
        
        public UpdateMessage(int[] ids, Vector3f[] viewDirections, Vector3f[] walkDirections,
                Vector3f[] positions) {
            this.ids=ids;
            this.viewDirections=viewDirections;
            this.walkDirections=walkDirections;
            this.positions=positions;
        }

        public UpdateMessage() {
            
        }
    }
}
