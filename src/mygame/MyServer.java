/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import mygame.Util.ChangeVelocityMessage;
import mygame.Util.DisconnectMessage;
import mygame.Util.HonkMessage;
import mygame.Util.InOutVehicleMessage;
import mygame.Util.MyAbstractMessage;
import mygame.Util.StartGameMessage;
import mygame.Util.TimeUpdateMessage;
import mygame.Util.UpdateMessage;

/**
 *
 * @author Olof E, Jonathan O, Anton E
 * 
 * This class originated from our Server class of Lab 3. The Server class from Lab 3 originated from an example 
 * made by Håkan J. This mean that the original code was written by Håkan, but this class has been modified A LOT.
 */
public class MyServer extends SimpleApplication {
    private Server server;
    private final int port;
    private Game game;
    private int objectCounter = 0;
    private MessageQueue messageQueue;
    private boolean running = false;
    
    public static void main(String[] args) {
        Util.initialiseSerializables();
        new MyServer(Util.PORT).start(JmeContext.Type.Headless);


    }

    public MyServer(int port) {
        this.port = port;
        messageQueue = new MessageQueue();

    }
    protected void initGame(int numberOfConnections) {
       
    }
    // sets up the server and the necessary threads and listeners
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {

       

        try {
            System.out.println("Using port " + port);
            server = Network.createServer(port);
            server.start(); 
        } catch (IOException ex) {
            ex.printStackTrace();
            destroy();
            this.stop();
        }
        System.out.println("Server started");
        game = new Game(false);
        stateManager.attach(game);
        
        
        new Thread(new NetWrite()).start();
        server.addMessageListener(new ServerListener(), ChangeVelocityMessage.class, StartGameMessage.class, DisconnectMessage.class);
      
    }
    int framecounter = 0;
    
    /**
     * Sends out updates to clients, containing positions, rotations, and other things as well
     * 
     */
    @Override
    public void simpleUpdate(float tpf) {
        if (game.serverTimeSinceLightMessage>5) {
            messageQueue.enqueue(new TimeUpdateMessage(game.a, game.b, game.c, game.d, game.DayOrNight));
            game.serverTimeSinceLightMessage = 0;
        }
        framecounter++;
        if (framecounter>1) {
            framecounter=0;
            int[] ids = new int[game.entities.size()];
            Vector3f[] viewDirections = new Vector3f[game.entities.size()];
            Vector3f[] walkDirections = new Vector3f[game.entities.size()];
            Vector3f[] positions = new Vector3f[game.entities.size()];
            Quaternion viewDir2;
            float[] a = new float[game.entities.size()];
            float[] b = new float[game.entities.size()];
            float[] c = new float[game.entities.size()];
            float[] d = new float[game.entities.size()];
            boolean[] visible = new boolean[game.entities.size()];
            
            
            for (int i=0; i<game.entities.size(); i++) {
                GameObject c1 = game.entities.get(i);
                ids[i]=c1.getID();
                positions[i]= c1.getLocalTranslation();

                viewDir2 = c1.getLocalRotation();
                a[i]=viewDir2.getX();
                b[i]=viewDir2.getY();
                c[i]=viewDir2.getZ();
                d[i]=viewDir2.getW();
                
                visible[i] = c1.visible;
            }

            UpdateMessage msg = new UpdateMessage(ids, positions, a, b, c, d, visible);
            
            messageQueue.enqueue(msg);

        }
    }
    //destroy the server
    @Override
    public void destroy() {
        System.out.println("Server going down");
        server.close();
        super.destroy();
        System.out.println("Server down");
    }
    /**
     * Listens to all packets sent by clients. 
     * Just like the client's listener, its some complicated and incomprehensible code
     * Basically, your keypress from the client is sent here and dealt with. 
     * 
     * If a client disconnects and sends a disconnectmessage to the server, it is forwarded to other clients 
     * If a new client connects, send them initial information
     * 
     * 
     */
    private class ServerListener implements MessageListener<HostedConnection> {
        @Override
        public void messageReceived(final HostedConnection source, final Message m) {
            if (m instanceof ChangeVelocityMessage) {
                
                Future result = MyServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        int id = ((ChangeVelocityMessage) m).id;
                        for (int i=0; i<game.entities.size(); i++) {
                            if (id==game.entities.get(i).getID()) {
                                GameObject c1 = game.entities.get(i);
                                c1.control(((ChangeVelocityMessage) m).name, ((ChangeVelocityMessage) m).isPressed, ((ChangeVelocityMessage) m).tpf);
                                if (((ChangeVelocityMessage) m).name.equals("EnterExit")) {
                                    boolean before = ((Character) c1).isInVehicle;
                                    game.characterAction(((ChangeVelocityMessage) m).name, ((ChangeVelocityMessage) m).isPressed,
                                            ((ChangeVelocityMessage) m).tpf, ((Character) c1));
                                    boolean after = ((Character) c1).isInVehicle;
                                    if (before!=after) {
                                        int thisid = 0;
                                        if (after) {
                                            Spatial parent = c1.getParent();
                                            thisid = ((GameObject) parent).getID();
                                        }
                                        else {
                                            thisid = c1.getID();
                                        }
                                        System.out.println("ID IS " + thisid);
                                        InOutVehicleMessage msg = new InOutVehicleMessage(thisid);
                                        msg.destinationID = source.getId();
                                        messageQueue.enqueue(msg);
                                    }
                                    
                                }
                                if (((ChangeVelocityMessage) m).name.equals("Space") && c1 instanceof Car && ((ChangeVelocityMessage) m).isPressed) {
                                    System.out.println("HONK MESSAGE INC");
                                    messageQueue.enqueue(new HonkMessage(((ChangeVelocityMessage) m).id));
                                }
                            }
                        }
                        return true;
                    }
                });
            }
            else if (m instanceof StartGameMessage) {
                Future result = MyServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        int id = game.serverMakeCharacter();
                        StartGameMessage newMessage = new StartGameMessage(id, game.a, game.b, game.c, game.d, game.DayOrNight);
                        newMessage.destinationID = source.getId();
                        messageQueue.enqueue(newMessage);
       
                        return true;
                    }
                });
            }
            else if (m instanceof DisconnectMessage) {
                Future result = MyServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                       System.out.println("Player disconnected");
                       if (game.getCharacterById(((DisconnectMessage) m).id).isInVehicle) {
                           Spatial c = game.getCharacterById(((DisconnectMessage) m).id).getParent();
                           ((Car) c).occupied=false;
                           ((Car) c).stopMovement();
                       }
                       game.getCharacterById(((DisconnectMessage) m).id).getParent().detachChild(game.getCharacterById(((DisconnectMessage) m).id));
                       game.entities.remove((game.getEntityById(((DisconnectMessage) m).id)));
                       game.characters.remove((game.getCharacterById(((DisconnectMessage) m).id)));
                       messageQueue.enqueue(new DisconnectMessage(((DisconnectMessage) m).id));

                       return true;
                    }
                });
            }
           
        }
    }

    //pushes out all packets to clients, some are broadcasted, some are not
    // uses synchronized messageQueue
    private class NetWrite implements Runnable {
        public void run() {
            
            while(true) {
                try {
                    Thread.sleep(10); // ... sleep ...
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                while (!messageQueue.isEmpty()) {
                    try {
                        MyAbstractMessage m = messageQueue.pop();
                        m.setReliable(false); // UDP
                        if (m.destinationID!=-1) {
                            HostedConnection conn
                            = MyServer.this.server
                                    .getConnection(m.destinationID);
                            conn.send(m);
                        }
                        else {
                            server.broadcast(m);
                        }
                    }
                    catch (Exception e) {
                        //
                        System.out.println("net write exception");
                        e.printStackTrace();
                    }
                }
                
            }
        }
    }
}
