/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Quaternion;
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
    public static final int PORT = 7002;
    public static final String HOSTNAME = "localhost";
    // register all message types there are
    public static void initialiseSerializables() {
        Serializer.registerClass(StartGameMessage.class);
            Serializer.registerClass(StopGameMessage.class);
        Serializer.registerClass(ChangeVelocityMessage.class);
        Serializer.registerClass(UpdateMessage.class);
        Serializer.registerClass(InOutVehicleMessage.class);
        Serializer.registerClass(DisconnectMessage.class);

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
        int id;
        
        public StartGameMessage() {
;       
        }
        public StartGameMessage(int id) {
            this.id=id;
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
        Vector3f[] positions;
        float[] a;
        float[] b;
        float[] c;
        float[] d;
        boolean[] visible;
        
        
        
        public UpdateMessage(int[] ids, Vector3f[] positions, 
                float[] a, float[] b, float[] c, float[] d, boolean[] visible) {
            this.ids=ids;

            this.positions=positions;
            this.visible = visible;
            
            this.a=a;
            this.b=b;
            this.c=c;
            this.d=d;

        }

        public UpdateMessage() {
            
        }
    }
    @Serializable
    public static class InOutVehicleMessage extends MyAbstractMessage {
        public int id;
        public InOutVehicleMessage() {
        }
        public InOutVehicleMessage(int id) {
            this.id=id;
        }

    }
    @Serializable
    public static class DisconnectMessage extends MyAbstractMessage {
        public int id;
        public DisconnectMessage() {
        }
        public DisconnectMessage(int id) {
            this.id=id;
        }

    }
}