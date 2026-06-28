package io.raft.server.network;
public class PeerAddress{
    public final int id;
    public final String host;
    public final int port;
    public PeerAddress(int id, String host, int port){
        this.id=id;
        this.host=host;
        this.port=port;

    }
    public RaftPeerClient client(){
        return new RaftPeerClient(host,port);
    }
}