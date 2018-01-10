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
import mygame.Util.UpdateMessage;


/**
 *
 * @author olofe
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
        //running=false;
        messageQueue = new MessageQueue();

    }
    protected void initGame(int numberOfConnections) {
       
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        // In a game server, the server builds and maintains a perfect 
        // copy of the game and makes use of that copy to make descisions 
       

        try {
            System.out.println("Using port " + port);
            // create the server by opening a port
            server = Network.createServer(port);
            server.start(); // start the server, so it starts using the port
        } catch (IOException ex) {
            ex.printStackTrace();
            destroy();
            this.stop();
        }
        System.out.println("Server started");
        // create a separat thread for sending "heartbeats" every now and then
        game = new Game(false);
        stateManager.attach(game);
        /*for (int i=0; i<game.characters.size(); i++) {
            objectCounter++;
            Character a = game.characters.get(i);
            a.setID(objectCounter);
        }
        for (int i=0; i<game.cars.size(); i++) {
            objectCounter++;
            Car a = game.cars.get(i);
            a.setID(objectCounter);
        }*/
        
        new Thread(new NetWrite()).start();
        server.addMessageListener(new ServerListener(), ChangeVelocityMessage.class, StartGameMessage.class, DisconnectMessage.class);
        // add a listener that reacts on incoming network packets
      
    }
    int framecounter = 0;
    @Override
    public void simpleUpdate(float tpf) {
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
                //walkDirections[i] = c1.characterControl.getWalkDirection();
                //viewDirections[i] = c1.viewDirection;
                viewDir2 = c1.getLocalRotation();
                //System.out.println(viewDir2);
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

    @Override
    public void destroy() {
        System.out.println("Server going down");
        server.close();
        super.destroy();
        System.out.println("Server down");
    }

    // this class provides a handler for incoming network packets
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
                        StartGameMessage newMessage = new StartGameMessage(id);
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

    
    /**
     * Sends out a heart beat to all clients every TIME_SLEEPING seconds, after
     * first having waited INITIAL_WAIT seconds. .
     */
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
