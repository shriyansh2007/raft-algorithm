package io.raft.server.network;

// import io.raft.network.MessageFramer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.raft.protocol.AppendRequest;
import io.raft.protocol.VoteRequest;
import io.raft.server.state.CandidateState;
import io.raft.server.state.FollowerState;


public class RaftServerListener implements Runnable {

    private final int port;
    private final FollowerState followerState;
    private final CandidateState candidateState;
    private volatile boolean running = true;

    public RaftServerListener(int port, FollowerState fs, CandidateState cs) {
        this.port = port;
        this.followerState = fs;
        this.candidateState = cs;

    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port:" + port);
            while (running) {
                Socket conn = serverSocket.accept();
                new Thread(() -> handle(conn)).start();
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();

            }

        }
    }

    private void handle(Socket conn) {
        try{
            byte[] reqBytes = MessageFramer.readMessage(conn.getInputStream());
            Object request = Serializer.deserialize(reqBytes);
            Object response;
            if (request instanceof VoteRequest) {
                response = followerState.handleVoteResponse((VoteRequest) request);

            } else if (request instanceof AppendRequest) {
                response = followerState.handleAppend((AppendRequest) request);
            } else {
                throw new IllegalArgumentException(
                        "Unknown request type: " + request.getClass());
            }
            byte[] resBytes = Serializer.serialize(response);
            MessageFramer.writeMessage(conn.getOutputStream(), resBytes);
        } catch (Exception e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }
    public int getPort(){
        return port;
    }

    public void stop() {
        running = false;
    }
}
