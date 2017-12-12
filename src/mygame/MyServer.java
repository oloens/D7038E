/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import mygame.Util.ChangeVelocityMessage;
import mygame.Util.MyAbstractMessage;

/**
 *
 * @author olofe
 */
public class MyServer extends SimpleApplication {
    private Server server;
    private final int port;
    
    private MessageQueue messageQueue = new MessageQueue();
    private boolean running = false;
    
    public static void main(String[] args) {
        Util.initialiseSerializables();
        new MyServer(Util.PORT).start(JmeContext.Type.Headless);


    }

    public MyServer(int port) {
        this.port = port;
        running=false;

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
        new Thread(new NetWrite()).start();
        server.addMessageListener(new ServerListener(), ChangeVelocityMessage.class);
        // add a listener that reacts on incoming network packets
      
    }

    @Override
    public void simpleUpdate(float tpf) {
        
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
        public void messageReceived(HostedConnection source, final Message m) {
            if (m instanceof ChangeVelocityMessage) {
                
                Future result = MyServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        System.out.println("new message");
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
                        //lol
                    }
                }
                
            }
        }
    }
}
