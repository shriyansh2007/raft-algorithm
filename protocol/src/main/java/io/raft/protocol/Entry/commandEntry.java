package io.raft.protocol.Entry;
public class commandEntry extends Entry{
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