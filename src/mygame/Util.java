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

/**
 * Based on example by Håkan Jonsson
 * 
 * Still has classes and attributes that are unused. 
 *
 * @author Anton Eliasson, Jonathan Olsson, Olof Enström
 */
public class Util {
    public static final int PORT = 7003;
    public static final String HOSTNAME = "localhost";
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
  
            
            
        }

    }
    @Serializable
    public static class StopGameMessage extends MyAbstractMessage {
        
        public StopGameMessage() {
        }

    }
    
    @Serializable
    public static class ChangeVelocityMessage extends MyAbstractMessage {

        public ChangeVelocityMessage() {
            
        }


}
    @Serializable
    public static class UpdateMessage extends MyAbstractMessage {
        
        public UpdateMessage() {
            
        }


    }
}
