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
import io.raft.server.network.PeerAddress;
import io.raft.server.raftServer;

public class LeaderState {

    private final serverContext ctx;
    private ScheduledExecutorService schedular;
    private ScheduledFuture<?> heartbeatTimer;
    private final List<PeerAddress> peers;

    private final long HEARTBEAT_INTERVAL_MS = 150;
    private final Map<Integer, Long> nextIndex = new HashMap<>();
    private final Map<Integer, Long> matchIndex = new HashMap<>();

    public LeaderState(serverContext ctx, List<PeerAddress> peers) {
        this.peers = peers;
        this.ctx = ctx;

    }

    public synchronized void start() {

        // Don't start twice
        if (schedular != null && !schedular.isShutdown()) {
            return;
        }

        System.out.println("Became LEADER for term " + ctx.getCurrentTerm());

        schedular = Executors.newSingleThreadScheduledExecutor();

        nextIndex.clear();
        matchIndex.clear();

        for (PeerAddress peer : peers) {
            nextIndex.put(peer.id, ctx.getLog().lastIndex() + 1);
            matchIndex.put(peer.id, 0L);
        }

        // Send an immediate heartbeat
        sendHeartbeats(peers);

        heartbeatTimer = schedular.scheduleAtFixedRate(
                ()->sendHeartbeats(peers),
                HEARTBEAT_INTERVAL_MS,
                HEARTBEAT_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    public synchronized void stop() {

        if (heartbeatTimer != null) {
            heartbeatTimer.cancel(true);
            heartbeatTimer = null;
        }

        if (schedular != null) {
            schedular.shutdownNow();
            schedular = null;
        }
    }

    private void sendHeartbeats(List<PeerAddress> peers) {
        for (PeerAddress peer : peers) {
            long prevIndex = nextIndex.get(peer.id) - 1;
            long prevTerm = prevIndex > 0
                    ? ctx.getLog().get(prevIndex).getTerm() : 0;
            List<Entry> entries = buildEntriesToSend(peer.id);

            AppendRequest req = AppendRequest.builder()
                    .withTerm(ctx.getCurrentTerm())
                    .withLeader(ctx.getMyId())
                    .withPrevLogIndex(prevIndex)
                    .withPrevLogTerm(prevTerm)
                    .withEntries(entries)
                    .withCommitIndex(ctx.getCommitIndex())
                    .build();

            System.out.println("Heartbeat → peer " + peer.id + ": " + req);
            new Thread(() -> {
                try {
                    AppendResponse res = peer.client().sendAppend(req);
                    handleResponse(peer.id, req, res);
                } catch (Exception e) {

                    e.printStackTrace();
                }

            }).start();

        }

    }

    private List<Entry> buildEntriesToSend(int peerId) {
        long next = nextIndex.get(peerId);
        List<Entry> entries = new ArrayList<>();
        for (long i = next; i <= ctx.getLog().lastIndex() && i < next + 10; i++) {
            Entry e = ctx.getLog().get(i);
            if (e != null) {
                entries.add(e);
            }

        }
        return entries;
    }

    private void handleResponse(int peerId, AppendRequest req, AppendResponse res) throws IOException {
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
