package io.raft.server.state;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.raft.protocol.VoteRequest;
import io.raft.protocol.VoteResponse;
import io.raft.server.raftServer;
public class CandidateState{
    private final serverContext ctx;
    public CandidateState(serverContext ctx){
        this.ctx=ctx;
    }
    public void startElection(List<Integer> peerIds) throws Exception{
        ctx.setCurrentTerm(ctx.getCurrentTerm()+1);
        ctx.setVotedFor(ctx.getMyId());
        VoteRequest req = VoteRequest.builder().withTerm(ctx.getCurrentTerm()).withCandidate(ctx.getMyId()).withLogIndex(ctx.getLog().lastIndex()).withLogTerm(ctx.getLog().lastTerm()).build();
        int majority= (peerIds.size()+1)/2+1;
        AtomicInteger votes= new AtomicInteger(1);
        System.out.println("Starting election for term "+ ctx.getCurrentTerm() + ", need " + majority + " votes");
        for(int peerId: peerIds){
            VoteResponse res= sendVoteRequest(peerId,req);
            if(res.getTerm()>ctx.getCurrentTerm()){
                ctx.setCurrentTerm(res.getTerm());
                ctx.setState(raftServer.State.FOLLOWER);
                return;
            }
            if(res.isVoteGranted()){
                int total= votes.incrementAndGet();
                System.out.println("Got vote from " + peerId+ " (" + total + "/" + majority + ")");
                if(total>=majority){
                    ctx.setState(raftServer.State.LEADER);
                    System.out.println("Won election! Becoming LEADER for term "+ ctx.getCurrentTerm());
                    return;

                }

            }

        }
        

    }
    private VoteResponse sendVoteRequest(int peerId, VoteRequest req) {
    
        return VoteResponse.builder()
        .withTerm(req.getTerm())
        .withVoteGranted(true) 
        .build();
    }

}