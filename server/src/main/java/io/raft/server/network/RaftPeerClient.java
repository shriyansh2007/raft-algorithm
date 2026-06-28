package io.raft.server.network;
import java.net.Socket;

import io.raft.protocol.AppendRequest;
import io.raft.protocol.AppendResponse;
import io.raft.protocol.VoteRequest;
import io.raft.protocol.VoteResponse;
public class RaftPeerClient{
    private final String host;
    private final int port;
    public RaftPeerClient(String host, int port){
        this.host=host;
        this.port=port;

    }
    public Object send(Object request) throws Exception {
        try(Socket socket = new Socket(host,port)){
            socket.setSoTimeout(3000);
            byte[] reqBytes= Serializer.serialize(request);
            MessageFramer.writeMessage(socket.getOutputStream(),reqBytes);
            byte[] resBytes= MessageFramer.readMessage(socket.getInputStream());
            return Serializer.deserialize(resBytes);
            
        }

    }
    public VoteResponse sendVote(VoteRequest req) throws Exception {
        return (VoteResponse) send(req);
    }

    public AppendResponse sendAppend(AppendRequest req) throws Exception {
        return (AppendResponse) send(req);
    }
    }