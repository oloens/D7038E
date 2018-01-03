/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import mygame.Util.ChangeVelocityMessage;
import mygame.Util.MyAbstractMessage;
import mygame.Util.StartGameMessage;
import mygame.Util.StopGameMessage;
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
        this.game = new Game();
        stateManager.attach(game);
    }
    private void initKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("EnterExit", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
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
                            UpdateMessage.class);

 
            
            setPauseOnLostFocus(false);
            // disable the flycam which also removes the key mappings
            getFlyByCamera().setEnabled(false);

            serverConnection.start();
            new Thread(new NetWrite()).start();
            
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
        game.characters.get(0).rotate(new Quaternion(2f, 0f, 0f, 0f));
    }
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        int id = game.characters.get(0).getID();
        messageQueue.enqueue(new ChangeVelocityMessage(id, name, isPressed, tpf));
        
        //game.onAction(name, isPressed, tpf);
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
                           
            }else if (m instanceof UpdateMessage) {
                 Future result = MyClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        boolean found;
                        for (int i=0; i<((UpdateMessage) m).ids.length; i++) {
                            found=false;
                            int id = ((UpdateMessage) m).ids[i];
                            
                            for (int j=0; j<game.characters.size(); j++) {
                                if (id==game.characters.get(i).getID()) {
                                    found=true;
                                    Character c1 = game.characters.get(i);
                                   // c1.characterControl.
                                    c1.characterControl.warp(((UpdateMessage) m).positions[i]);
                                    //c1.characterControl.setViewDirection(((UpdateMessage) m).viewDirections[i]);
                                    Quaternion t1 = c1.getLocalRotation();
                                    //t1.lookAt(((UpdateMessage) m).viewDirections[i], Vector3f.UNIT_Y);
                                    //c1.setLocalRotation(t1);
                                    //c1.characterControl.update(0.01f);
                                    System.out.println("here");
                                    //c1.characterControl.setWalkDirection(((UpdateMessage) m).walkDirections[i]);
                                   // Vector3f viewDir = ((UpdateMessage) m).viewDirections[i];
                                    
                                    //cam.setRotation(new Quaternion(viewDir.x, viewDir.y, viewDir.z, 0.0f));

                                }
                            }
                            if (!found) {
                                Character newchar = game.spawnEntity("Character");
                                newchar.setID(id);
                                newchar.characterControl.warp(((UpdateMessage) m).positions[i]);
                                newchar.characterControl.setViewDirection(((UpdateMessage) m).viewDirections[i]);
                                //game.characters.get(0).characterControl.setUseViewDirection(true);
                               // game.characters.get(0).characterControl.update(0.001f);
                            }
                        }
                        
                        return true;
                    }
                });      
                
                
            } else {
                // must be a programming error(!)
                throw new RuntimeException("Unknown message.");
            }
        }
    }

    // takes down all communication channels gracefully, when called
    @Override
    public void destroy() {
        serverConnection.close();
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
