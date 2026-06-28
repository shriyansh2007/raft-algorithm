package io.raft.protocol.Entry;
import java.io.Serializable;
public class commandEntry extends Entry implements Serializable{
    private static final long serialVersionUID=1L;
    
    private byte[] command;
    private long session;
    private long sequence;
    public byte[] getCommand(){
        return command;
    }
    public void setCommand(byte[] command){
        this.command=command;
    }
    public long getSession(){
        return session;
    }
    public void setSession(long session){
        this.session= session;
    }
    public long getSequence(){
        return sequence;
    }
    public void setSequence(long sequence){
        this.sequence= sequence;
    }
}