package io.raft.server.state;

import io.raft.protocol.VoteRequest;
import io.raft.protocol.VoteResponse;

public class FollowerState {

    private final serverContext ctx;

    public FollowerState(serverContext ctx) {
        this.ctx = ctx;
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
}