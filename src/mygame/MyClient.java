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
 * @author olofe
 */
public class MyClient extends SimpleApplication implements ActionListener, AnalogListener{
    

    // the connection back to the server
    private Client serverConnection;
    // the scene contains just a rotating box
    private final String hostname; // where the server can be found
    private final int port; // the port att the server that we use

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
        //running=false;
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
    
    
    
    
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        initKeys();
        System.out.println("Initializing");
        //cam.setLocation(new Vector3f(-84f, 0.0f, 720f));
        //cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));
        setDisplayStatView(false);
        setDisplayFps(false);

        try {
            System.out.println("Opening server connection");
            serverConnection = Network.connectToServer(hostname, port);
            System.out.println("Server is starting networking");
            System.out.println("Building scene graph");

            // TODO build scene graph


            System.out.println("Adding network listener");
            // this make the client react on messages when they arrive by
            // calling messageReceived in ClientNetworkMessageListener
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
            // disable the flycam which also removes the key mappings
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
        //messageQueue.enqueue(new ChangeVelocityMessage());
        //System.out.println("x = " + game.characters.get(0).characterControl.getViewDirection().x);
        //System.out.println(" y = " + game.characters.get(0).characterControl.getViewDirection().y);
        //System.out.println("z = " + game.characters.get(0).characterControl.getViewDirection().z);
        //game.characters.get(0).setLocalTranslation(-50f, 10f, 50f);
    }
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
        
        //game.onAction(name, isPressed, tpf);
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private void setup(int id) {
        this.myId=id;
        this.currentObject = game.getCharacterById(id);
        System.out.println("here");
    }

    // This class is a packet handler
    private class ClientNetworkMessageListener
            implements MessageListener<Client> {

        // this method is called whenever network packets arrive
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

    // takes down all communication channels gracefully, when called
    @Override
    public void destroy() {
        serverConnection.send(new DisconnectMessage(myId));
        //serverConnection.close();
        super.destroy();
    }
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
