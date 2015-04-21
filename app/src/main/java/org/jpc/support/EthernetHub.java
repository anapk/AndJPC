/*
 JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine
 Release Version 2.4

 A project from the Physics Dept, The University of Oxford

 Copyright (C) 2007-2010 The University of Oxford

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 2 as published by
 the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 
 Details (including contact information) can be found at: 

 jpc.sourceforge.net
 or the developer website
 sourceforge.net/projects/jpc/

 Conceived and Developed by:
 Rhys Newman, Ian Preston, Chris Dennis

 End of licence header
 */
package org.jpc.support;

import android.os.AsyncTask;
import android.util.Log;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * @author Ian Preston
 */
public class EthernetHub extends EthernetOutput {
    private static final Logger LOGGING = Logger.getLogger(ArrayBackedSeekableIODevice.class.getName());

    RemoteEndpoint.Basic remoteEndpoint;
    Session session;
    Semaphore sem = new Semaphore(1);
    private int port;
    private String serverHost;
    private Queue<byte[]> inQueue = new ConcurrentLinkedQueue<byte[]>();

    public EthernetHub(final String host, final int port) {
        System.out.println((char) 27 + "[31mINITIALIZE ETHERNETHUB!" + (char) 27 + "[0m");
        serverHost = host;
        this.port = port;
        System.out.println("Connecting to remote EthernetHub at: " + host + ":" + port);

        try {
            sem.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        ClientManager client = ClientManager.createClient();

        ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {
            @Override
            public boolean onDisconnect(CloseReason r) {
                System.out.println("Disconnect!");
                return true;
            }

            @Override
            public boolean onConnectFailure(Exception exception) {
                System.out.println("Connection failure, waiting...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                return true;
            }
        };

        client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    session = container.connectToServer(new WSClient(), URI.create("ws://" + host + ":" + port));
                    remoteEndpoint = session.getBasicRemote();
                } catch (DeploymentException ex) {
                    LOGGING.log(Level.WARNING, ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    ex.printStackTrace();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return null;
            }
        }.execute();

        try {
            sem.acquire(); // wait until we are connected and sem is released
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public byte[] getPacket() {
        byte[] packet = inQueue.poll();
        return packet;
    }

    public void sendPacket(byte[] data, int offset, int length) {
        byte[] p = new byte[length];
        System.arraycopy(data, offset, p, 0, length);
        if (session == null || !session.isOpen()) {
            System.out.println("Dropping packet, socket not open!");
            return;
        }
        try {
            remoteEndpoint.sendBinary(ByteBuffer.wrap(p));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        System.out.println("Sent packet: " + length);
    }

    class WSClient extends Endpoint {

        public void onOpen(Session session, EndpointConfig EndpointConfig) {
            session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
                @Override
                public void onMessage(ByteBuffer data) {
                    byte[] arr = data.array();
                    inQueue.add(arr);
                    //System.out.println("EthernetHub: Got packet: " + arr.length);
                }
            });
            sem.release();
        }
    }
}
