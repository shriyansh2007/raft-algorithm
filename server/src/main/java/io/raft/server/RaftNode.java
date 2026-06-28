package io.raft.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.raft.server.network.PeerAddress;
import io.raft.server.network.RaftServerListener;
import io.raft.server.state.CandidateState;
import io.raft.server.state.FollowerState;
import io.raft.server.state.LeaderState;
import io.raft.server.state.serverContext;

public class RaftNode {

    private final serverContext ctx;
    private final FollowerState followerState;
    private final CandidateState candidateState;
    private final LeaderState leaderState;
    private final RaftServerListener listener;

    public RaftNode(int myId, int port, File dataDir, List<PeerAddress> peers) throws Exception {
        this.ctx = new serverContext(dataDir, myId);
        this.followerState = new FollowerState(ctx);
        this.candidateState = new CandidateState(ctx, peers);
        this.leaderState = new LeaderState(ctx, peers);
        this.listener = new RaftServerListener(port, followerState, candidateState);
    }

    public void start() {
        Thread listenerThread = new Thread(listener);
        listenerThread.setDaemon(true);
        listenerThread.start();
        ctx.setState(raftServer.State.FOLLOWER);
        followerState.start();
        new Thread(this::stateLoop).start();
        System.out.println("RaftNode " + ctx.getMyId() + " started on port " + listener.getPort());
    }

    private void stateLoop() {
        raftServer.State last = null;

        while (true) {
            raftServer.State curr = ctx.getState();

            if (curr != last) {

                // Stop the previous state's background tasks
                if (last == raftServer.State.FOLLOWER) {
                    followerState.stop();
                } else if (last == raftServer.State.LEADER) {
                    leaderState.stop();
                }

                System.out.println(curr);

                // Start the new state
                switch (curr) {
                    case FOLLOWER:
                        followerState.start();
                        break;

                    case CANDIDATE:
                        try {
                            candidateState.startElection();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case LEADER:
                        leaderState.start();
                        break;
                }

                last = curr;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int myId = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        File dataDir = new File(args[2]);
        List<PeerAddress> peers = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            String[] parts = args[i].split(":");
            peers.add(new PeerAddress(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2])));
        }
        new RaftNode(myId, port, dataDir, peers).start();
        Thread.currentThread().join();

    }

}
