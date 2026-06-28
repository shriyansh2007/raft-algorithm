package io.raft.server.network;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class MessageFramer{
    public static void writeMessage(OutputStream out, byte[] data) throws IOException{
        DataOutputStream dos= new DataOutputStream(out);
        dos.writeInt(data.length);
        dos.write(data);
        dos.flush();


    }
    public static byte[] readMessage(InputStream in) throws IOException{
        DataInputStream dis= new DataInputStream(in);
        int length=dis.readInt();
        byte[] data= new byte[length];
        dis.readFully(data);
        return data;

    }
}