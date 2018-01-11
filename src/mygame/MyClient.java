/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.network.Client;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.scene.Spatial;
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
import mygame.Util.StopGameMessage;
import mygame.Util.TimeUpdateMessage;
import mygame.Util.UpdateMessage;


/**
 *
 * @author Olof E, Jonathan O, Anton E
 * 
 * This class originated from our Client class of Lab 3. The Client class from Lab 3 originated from an example 
 * made by Håkan J. This mean that the original code was written by Håkan, but this class has been modified A LOT.
 */
public class MyClient extends SimpleApplication implements ActionListener, AnalogListener{
    

    private Client serverConnection;
    private final String hostname; 
    private final int port; 

    private MessageQueue messageQueue = new MessageQueue(); 
    private boolean running = false;
    private boolean fullyInitialized = false;
    private GameObject currentObject;
    private int myId;
    
    Game game;
    
    public static void main(String[] args) {
        Util.initialiseSerializables();
        MyClient app = new MyClient(Util.HOSTNAME, Util.PORT);
        app.start();

    }

    public MyClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.game = new Game(true);
        stateManager.attach(game);
    }
    private void initKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("EnterExit", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "EnterExit");
        
    }
    
    
    
    /**
     * set up the client and start the necessary threads, initialize keys etc
     */
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        initKeys();
        System.out.println("Initializing");
        setDisplayStatView(false);
        setDisplayFps(false);

        try {
            System.out.println("Opening server connection");
            
            serverConnection = Network.connectToServer(hostname, port);
            
            System.out.println("Server is starting networking");
            System.out.println("Building scene graph");
            System.out.println("Adding network listener");

            serverConnection
                    .addMessageListener(new ClientNetworkMessageListener(),
                            StartGameMessage.class,
                            StopGameMessage.class,
                            ChangeVelocityMessage.class,
                            UpdateMessage.class,
                            InOutVehicleMessage.class,
                            DisconnectMessage.class,
                            HonkMessage.class,
                            TimeUpdateMessage.class);

 
            
            setPauseOnLostFocus(false);
            getFlyByCamera().setEnabled(false);

            serverConnection.start();
            new Thread(new NetWrite()).start();
            
            messageQueue.enqueue(new StartGameMessage());
            System.out.println("Client communication back to server started");
        } catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }

    }



  
    @Override
    public void simpleUpdate(float tpf) {

    }
    // on key press, send that key press to the server. 
    // "ChangeVelocityMessage" is an inaccurate name
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!fullyInitialized) {
            return;
        }
        int id = currentObject.getID();
        if (currentObject instanceof Car && name.equals("EnterExit")) {
            id = myId;
        }
        messageQueue.enqueue(new ChangeVelocityMessage(id, name, isPressed, tpf));
        
        
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
    }
    private void setup(int id) {
        this.myId=id;
        this.currentObject = game.getCharacterById(id);
        System.out.println("here");
    }
    //listens to packets send from the server.
    // complicated stuff is set up from some of the messages
    // basically it converges to server values, and if there are new clients that connected/disconnected, it adapts to that
    
    private class ClientNetworkMessageListener
            implements MessageListener<Client> {

        @Override
        public void messageReceived(Client source, final Message m) {
            if (m instanceof StartGameMessage) {
                 Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        game.clientMakeCharacter(((StartGameMessage) m).id);
                        setup(((StartGameMessage) m).id);
                        for (int i=0; i<game.cars.size(); i++) {
                            game.getPhysicsSpace().remove(game.cars.get(i).carControl);

                        }
                        System.out.println(game.getPhysicsSpace().getVehicleList());
                        game.a = ((StartGameMessage) m).a;
                        game.b = ((StartGameMessage) m).b;
                        game.c = ((StartGameMessage) m).c;
                        game.d = ((StartGameMessage) m).d;
                        game.DayOrNight = ((StartGameMessage) m).dayOrNight;
                        AudioNode noise = ((AudioNode) game.sapp.getRootNode().getChild("background_noise"));
                        noise.play();
                        fullyInitialized=true;
                        return true;
                    }
                });
               
                
            }
            else if (m instanceof TimeUpdateMessage) {
                Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        
                        game.a=((TimeUpdateMessage) m).a;
                        game.b=((TimeUpdateMessage) m).b;
                        game.c=((TimeUpdateMessage) m).c;
                        game.d=((TimeUpdateMessage) m).d;
                        game.DayOrNight=((TimeUpdateMessage) m).dayOrNight;
                     return true;
                    }
                });
            }
            else if (m instanceof StopGameMessage) {
                 Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                     return true;
                    }
                });                
                           
            }else if (m instanceof InOutVehicleMessage) {
                 Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println("here1");
                        Character c1 = game.getCharacterById(myId);
                        int id = ((InOutVehicleMessage) m).id;
                        for (int i=0; i<game.entities.size(); i++) {
                            if (id==game.entities.get(i).getID()) {
                                System.out.println("here2");

                                game.changeControlClient(c1, game.entities.get(i));
                                currentObject = game.entities.get(i);
                            }
                        }
                        
                     return true;
                    }
                });                
                           
            }else if (m instanceof UpdateMessage) {
                Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        if (!fullyInitialized) {
                           return true;

                        }
                        ArrayList<Integer> notFoundIds = new ArrayList<Integer>();
                        boolean found;
                        for (int i=0; i<((UpdateMessage) m).ids.length; i++) {
                            found=false;
                            for (int j=0; j<game.entities.size(); j++) {
                                if (game.entities.get(j).getID()==((UpdateMessage) m).ids[i]) {
                                    found=true;
                                }
                            }
                            if (!found) {
                                notFoundIds.add(((UpdateMessage) m).ids[i]);
                                
                            }
                        }
                        for (int i=0; i<notFoundIds.size(); i++) {
                            Character newchar = game.spawnEntity(notFoundIds.get(i));
                        }
                        for (int i=0; i<((UpdateMessage) m).ids.length; i++) {
                                    GameObject c1 = game.getEntityById(((UpdateMessage) m).ids[i]);
                                    c1.setLocalTranslation(((UpdateMessage) m).positions[i]);
                                    float a = ((UpdateMessage) m).a[i];
                                    float b = ((UpdateMessage) m).b[i];
                                    float c = ((UpdateMessage) m).c[i];
                                    float d = ((UpdateMessage) m).d[i];
                                    c1.setLocalRotation(new Quaternion(a,b,c,d));
                                    
                                    if (((UpdateMessage) m).visible[i]) {
                                        c1.visible();
                                    }
                                    else {
                                        c1.invisible();
                                    }
                        }
                        
                        return true;
                    }
                });

                
            }
            else if (m instanceof DisconnectMessage) {
                Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        if (((DisconnectMessage) m).id==myId) {
                            System.out.println("Disconnecting...");
                            serverConnection.close();
                        }
                        game.getCharacterById(((DisconnectMessage) m).id).getParent().detachChild(game.getCharacterById(((DisconnectMessage) m).id));
                        game.entities.remove((game.getEntityById(((DisconnectMessage) m).id)));
                        game.characters.remove((game.getCharacterById(((DisconnectMessage) m).id)));
                        
                     return true;
                    }
                }); 
            }else if (m instanceof HonkMessage) {
                Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        GameObject car = game.getEntityById(((HonkMessage) m).id);
                        Spatial audio = ((Car) car).getChild("Audio");
                        ((AudioNode) audio).play();
                        System.out.println("honk!");
                        
                     return true;
                    }
                }); 
            }
            else {
                // must be a programming error(!)
                throw new RuntimeException("Unknown message.");
            }
        }
    }
    //if you close down the client, tell the server and then destroy it
    @Override
    public void destroy() {
        serverConnection.send(new DisconnectMessage(myId)); 
        super.destroy();
    }
    //thread that pushes out the packages to the server, uses the synchronized messageQueue.
    private class NetWrite implements Runnable {
        public void run() {
            while(true) {
                try {
                    Thread.sleep(10); // ... sleep ...
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
       
                while (!messageQueue.isEmpty()) {
                    MyAbstractMessage m = messageQueue.pop();
                    m.setReliable(false); //UDP
                    serverConnection.send(m);
                }
                
            }
        }
    }
}
