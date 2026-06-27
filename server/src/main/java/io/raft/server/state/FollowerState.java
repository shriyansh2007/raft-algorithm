package io.raft.server.state;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.raft.protocol.AppendRequest;
import io.raft.protocol.AppendResponse;
import io.raft.protocol.Entry.Entry;
import io.raft.protocol.VoteRequest;
import io.raft.protocol.VoteResponse;
import io.raft.server.raftServer;


public class FollowerState {

    private final serverContext ctx;
    private final ScheduledExecutorService schedular= Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> electionTimer;
    private static final long ELECTION_TIMEOUT_MS=300;
    private static final long ELECTION_JITTER_MS=150;
    public void start(){
        resetElectionTimer();
    }
    public void stop(){
        if(electionTimer!=null){
            electionTimer.cancel(false);
        }
        schedular.shutdown();
    }
    private void resetElectionTimer() {
        if (electionTimer != null) electionTimer.cancel(false);

        
        long delay = ELECTION_TIMEOUT_MS+ (long)(Math.random() * ELECTION_JITTER_MS);

        electionTimer = schedular.schedule(
        this::startElection,
        delay,
        TimeUnit.MILLISECONDS
        );
    }
    private void startElection() {
        System.out.println("Server " + ctx.getMyId()
        + ": election timer fired — no leader heard. "
        + "Transitioning to CANDIDATE.");
        ctx.setState(raftServer.State.CANDIDATE);
    }

    public FollowerState(serverContext ctx) {
        this.ctx = ctx;
    }
    public serverContext getContext(){
        return ctx;
    }

    public VoteResponse handleVoteResponse(VoteRequest req) throws Exception {

        if (req.getTerm() < ctx.getCurrentTerm()) {
            return VoteResponse.builder()
                    .withTerm(ctx.getCurrentTerm())
                    .withVoteGranted(false)
                    .build();
        }

        if (req.getTerm() > ctx.getCurrentTerm()) {
            ctx.setCurrentTerm(req.getTerm());
            ctx.setVotedFor(-1);
        }

        int votedFor = ctx.getVotedFor();

        if (votedFor != -1 && votedFor != req.getCandidate()) {
            return VoteResponse.builder()
                    .withTerm(ctx.getCurrentTerm())
                    .withVoteGranted(false)
                    .build();
        }

        if (!isCandidateLogUpToDate(
                req.getLogIndex(),
                req.getLogTerm())) {

            return VoteResponse.builder()
                    .withTerm(ctx.getCurrentTerm())
                    .withVoteGranted(false)
                    .build();
        }

        ctx.setVotedFor(req.getCandidate());

        return VoteResponse.builder()
                .withTerm(ctx.getCurrentTerm())
                .withVoteGranted(true)
                .build();
    }

    private boolean isCandidateLogUpToDate(long theirIndex,
            long theirTerm) {

        long myLastTerm = ctx.getLog().lastTerm();
        long myLastIndex = ctx.getLog().lastIndex();

        if (theirTerm > myLastTerm) {
            return true;
        }

        if (theirTerm < myLastTerm) {
            return false;
        }

        return theirIndex >= myLastIndex;
    }

    public AppendResponse handleAppend(AppendRequest req) throws Exception {
        if (req.getTerm() < ctx.getCurrentTerm()) {
            return AppendResponse.builder().withTerm(ctx.getCurrentTerm()).withLogIndex(ctx.getLog().lastIndex()).withSuccess(false).build();

        }
        if (req.getTerm() > ctx.getCurrentTerm()) {
            ctx.setCurrentTerm(req.getTerm());
            ctx.setVotedFor(-1);
        }
        ctx.setLeader(req.getLeader());
        resetElectionTimer();
        if (req.getPrevLogIndex() > 0) {
            Entry prev = ctx.getLog().get(req.getPrevLogIndex());
            if (prev == null || prev.getTerm() != req.getPrevLogTerm()) {
                return AppendResponse.builder().withTerm(ctx.getCurrentTerm()).withSuccess(false).withLogIndex(ctx.getLog().lastIndex()).build();
            }

        }
        if (req.getEntries() != null && !req.getEntries().isEmpty()) {
            for (Entry entry : req.getEntries()) {
                Entry existing = ctx.getLog().get(entry.getIndex());
                if (existing != null && existing.getTerm() != entry.getTerm()) {

                    ctx.getLog().truncate(entry.getIndex());
                }
                if (ctx.getLog().get(entry.getIndex()) == null) {
                    ctx.getLog().append(entry);
                }
            }

        }
        if (req.getCommitIndex() > ctx.getCommitIndex()) {
            long newCommit = Math.min(req.getCommitIndex(), ctx.getLog().lastIndex());
            ctx.setCommitIndex(newCommit);
            applyCommitted();

        }
        return AppendResponse.builder().withTerm(ctx.getCurrentTerm()).withSuccess(true).withLogIndex(ctx.getLog().lastIndex()).build();

    }

    private void applyCommitted() {

        while (ctx.getLastApplied() < ctx.getCommitIndex()) {
            long idx = ctx.getLastApplied() + 1;
            Entry e = ctx.getLog().get(idx);
            if (e != null) {
                System.out.println("Applying entry at index " + idx);
                ctx.setLastApplied(idx);
            }
        }
    }

    
}
