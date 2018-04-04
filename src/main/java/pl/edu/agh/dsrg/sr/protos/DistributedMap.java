package pl.edu.agh.dsrg.sr.protos;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;
import pl.edu.agh.dsrg.sr.protos.DistributedMapProtos.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistributedMap extends ReceiverAdapter implements SimpleStringMap{

    private Map<String, String> state = new ConcurrentHashMap<String, String>();
    private JChannel channel;

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized(state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        ConcurrentHashMap<String, String> table;
        table = (ConcurrentHashMap<String, String>) Util.objectFromStream(new DataInputStream(input));
        synchronized(state) {
            state.clear();
            state.putAll(table);
        }
    }

    @Override
    public void receive(Message msg) {
        byte[] stream = msg.getBuffer();
        try {
            MapOperation mapOperation = MapOperation.parseFrom(stream);
            if(mapOperation.getType().equals(MapOperation.Type.PUT)) {
                synchronized (state) {
                    state.put(mapOperation.getKey(), mapOperation.getValue());
                }
            } else if(mapOperation.getType().equals(MapOperation.Type.REMOVE)) {
                synchronized (state) {
                    state.remove(mapOperation.getKey());
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println(view.toString());
        if(view instanceof MergeView) {
            ViewHandler viewHandler = new ViewHandler(channel, (MergeView) view);
            viewHandler.start();
        }
    }

    void start(String channelName) throws Exception {
        System.setProperty("java.net.preferIPv4Stack","true");
        channel = new JChannel(false);
        ProtocolStack stack=new ProtocolStack();
        channel.setProtocolStack(stack);
        stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName("230.0.0.100")))
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL()
                        .setValue("timeout", 12000)
                        .setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER());

        stack.init();

        channel.setReceiver(this);
        channel.connect(channelName);
        channel.getState(null, 0);
    }

    public boolean containsKey(String key) {
        synchronized (state) {
            return state.containsKey(key);
        }
    }

    public String get(String key) {
        synchronized (state) {
            return state.get(key);
        }
    }

    private void sendMessage(MapOperation.Type type, String key, String value) throws Exception{
        MapOperation mapOperation;
        mapOperation = MapOperation.newBuilder()
                .setType(type)
                .setKey(key)
                .setValue(value)
                .build();
        byte[] bytes = mapOperation.toByteArray();
        Message msg = new Message(null, null, bytes);
        channel.send(msg);
    }

    public String put(String key, String value) {
        String returnValue;
        synchronized (state) {
            returnValue = state.put(key,value);
        }
        try {
            sendMessage(MapOperation.Type.PUT, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public String remove(String key) {
        String returnValue;
        synchronized (state) {
            returnValue = state.remove(key);
        }
        try {
            sendMessage(MapOperation.Type.REMOVE, key, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }
}