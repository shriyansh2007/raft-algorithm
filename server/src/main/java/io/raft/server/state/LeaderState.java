package io.raft.server.state;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.raft.protocol.AppendRequest;
import io.raft.protocol.AppendResponse;
import io.raft.protocol.Entry.Entry;
import io.raft.server.raftServer;
public class LeaderState{
    private final serverContext ctx;
    private final ScheduledExecutorService schedular = Executors.newSingleThreadScheduledExecutor();
    private  ScheduledFuture<?> heartbeatTimer;

    private final long HEARTBEAT_INTERVAL_MS=150;
    private final Map<Integer, Long> nextIndex= new HashMap<>();
    private final Map<Integer, Long> matchIndex= new HashMap<>();
    public LeaderState(serverContext ctx){
        this.ctx=ctx;

    }
    public void start(List<Integer> peerIds) {
        System.out.println("Became LEADER for term " + ctx.getCurrentTerm());

        
        for (int peerId : peerIds) {
        nextIndex.put(peerId, ctx.getLog().lastIndex() + 1);
        matchIndex.put(peerId, 0L);
        }

       
        sendHeartbeats(peerIds);
        heartbeatTimer = schedular.scheduleAtFixedRate(
        () -> sendHeartbeats(peerIds),
        HEARTBEAT_INTERVAL_MS,
        HEARTBEAT_INTERVAL_MS,
        TimeUnit.MILLISECONDS
        );
    }
    public void stop() {
        if (heartbeatTimer != null) heartbeatTimer.cancel(false);
        schedular.shutdown();
    }
    private void sendHeartbeats(List<Integer> peerIds) {
        for (int peerId : peerIds) {
            long prevIndex = nextIndex.get(peerId) - 1;
            long prevTerm  = prevIndex > 0
                ? ctx.getLog().get(prevIndex).getTerm() : 0;

            AppendRequest req = AppendRequest.builder()
                .withTerm(ctx.getCurrentTerm())
                .withLeader(ctx.getMyId())
                .withPrevLogIndex(prevIndex)
                .withPrevLogTerm(prevTerm)
                .withEntries(Collections.emptyList()) 
                .withCommitIndex(ctx.getCommitIndex())
                .build();

        
            System.out.println("Heartbeat → peer " + peerId + ": " + req);
            try {
                AppendResponse res = simulateSend(peerId, req);
                handleResponse(peerId, req, res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void handleResponse(int peerId, AppendRequest req,AppendResponse res) throws IOException{
        if (res.getTerm() > ctx.getCurrentTerm()) {
        
        ctx.setCurrentTerm(res.getTerm());
        ctx.setState(raftServer.State.FOLLOWER);
        stop();
        return;
        }
        if (res.getSuccess()) {
        matchIndex.put(peerId, res.getLogIndex());
        nextIndex.put(peerId, res.getLogIndex() + 1);
        tryAdvanceCommitIndex();
        } else {
        
        nextIndex.put(peerId, Math.max(1, nextIndex.get(peerId) - 1));
        }
    }
    private void tryAdvanceCommitIndex() {
    
        List<Long> indices = new ArrayList<>(matchIndex.values());
        indices.add(ctx.getLog().lastIndex()); 
        Collections.sort(indices);
        long quorumIndex = indices.get(indices.size() / 2);

        if (quorumIndex > ctx.getCommitIndex()) {
        Entry e = ctx.getLog().get(quorumIndex);
        if (e != null && e.getTerm() == ctx.getCurrentTerm()) {
            ctx.setCommitIndex(quorumIndex);
            System.out.println("Committed up to index " + quorumIndex);
        }
        }
    }
    private AppendResponse simulateSend(int peerId, AppendRequest req) throws IOException {
        return AppendResponse.builder()
        .withTerm(req.getTerm())
        .withSuccess(true)
        .withLogIndex(req.getPrevLogIndex())
        .build();
    }



}