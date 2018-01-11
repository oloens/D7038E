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
    public static final int PORT = 7003;
    public static final String HOSTNAME = "130.240.55.48";
    // register all message types there are
    public static void initialiseSerializables() {
        Serializer.registerClass(StartGameMessage.class);
        Serializer.registerClass(StopGameMessage.class);
        Serializer.registerClass(ChangeVelocityMessage.class);
        Serializer.registerClass(UpdateMessage.class);
        Serializer.registerClass(InOutVehicleMessage.class);
        Serializer.registerClass(DisconnectMessage.class);
        Serializer.registerClass(HonkMessage.class);
        Serializer.registerClass(TimeUpdateMessage.class);
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

    
    // sent to new clients when they connect
    // send out initial daytime info, and most importantly, sends out the ID that the new clients avatar is going to use!
    @Serializable
    public static class StartGameMessage extends MyAbstractMessage {
        int id;
        float a,b,c,d;
        boolean dayOrNight;
        
        public StartGameMessage() {
;       
        }
        public StartGameMessage(int id, float a, float b, float c, float d, boolean dayOrNight) {
            this.id=id;
            this.a=a;
            this.b=b;
            this.c=c;
            this.d=d;
            this.dayOrNight=dayOrNight;
        }

    }
    /**
     * daytime updates
     */
    @Serializable
    public static class TimeUpdateMessage extends MyAbstractMessage {
        float a,b,c,d;
        boolean dayOrNight;
        
        public TimeUpdateMessage() {
        }
        public TimeUpdateMessage(float a, float b, float c, float d, boolean dayOrNight) {
            this.a=a;
            this.b=b;
            this.c=c;
            this.d=d;
            this.dayOrNight=dayOrNight;
            
        }

    }@Serializable
    public static class StopGameMessage extends MyAbstractMessage {
        
        public StopGameMessage() {
        }

    }
    /**
     * sent by clients to represent the key they pressed, client tpf is not used despite of what it looks like here
     */
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
    /**
     * all the entity IDs, positions, and rotations are sent out to clients using this message
     */
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
    // sent to clients to let them know if a character entered or exited a vehicle
    @Serializable
    public static class InOutVehicleMessage extends MyAbstractMessage {
        public int id;
        public InOutVehicleMessage() {
        }
        public InOutVehicleMessage(int id) {
            this.id=id;
        }

    }
    //sent to server to annouce that you disconnected, forwarded to clients to let them know what character ID disconnected
    @Serializable
    public static class DisconnectMessage extends MyAbstractMessage {
        public int id;
        public DisconnectMessage() {
        }
        public DisconnectMessage(int id) {
            this.id=id;
        }

    }
    //let the clients know what car just honked so they can play the sound
    @Serializable
    public static class HonkMessage extends MyAbstractMessage {
        public int id;
        public HonkMessage() {
        }
        public HonkMessage(int id) {
            this.id=id;
        }

    }
}
