package io.raft.server.state;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import io.raft.server.network.*;


import io.raft.protocol.VoteRequest;
import io.raft.protocol.VoteResponse;
import io.raft.server.raftServer;
public class CandidateState{
    private final serverContext ctx;
    private final List<PeerAddress> peers;

    public CandidateState(serverContext ctx, List<PeerAddress> peers){
        this.peers=peers;
        this.ctx=ctx;
    }
    public void startElection() throws Exception{
        ctx.setCurrentTerm(ctx.getCurrentTerm()+1);
        ctx.setVotedFor(ctx.getMyId());
        VoteRequest req = VoteRequest.builder().withTerm(ctx.getCurrentTerm()).withCandidate(ctx.getMyId()).withLogIndex(ctx.getLog().lastIndex()).withLogTerm(ctx.getLog().lastTerm()).build();
        int majority= (peers.size()+1)/2+1;
        AtomicInteger votes= new AtomicInteger(1);
        System.out.println("Starting election for term "+ ctx.getCurrentTerm() + ", need " + majority + " votes");
        for(PeerAddress peer: peers){
            new Thread(()->{
                try{
                    VoteResponse res= sendVoteRequest(peer,req);
                    if(res.getTerm()>ctx.getCurrentTerm()){
                        ctx.setCurrentTerm(res.getTerm());
                        ctx.setState(raftServer.State.FOLLOWER);
                        return;
                    }
                    if(res.isVoteGranted()){
                        int total= votes.incrementAndGet();
                        System.out.println("Got vote from " + peer + " (" + total + "/" + majority + ")");
                        if(total==majority){
                            ctx.setState(raftServer.State.LEADER);
                            System.out.println("Won election! Becoming LEADER for term "+ ctx.getCurrentTerm());
                            return;

                        }

                    }
                }catch (Exception e) {
                    
                    System.out.println("No response from peer " + peer.id
                        + ": " + e.getMessage());
                }

            }).start();
            

            
            

        }
        

    }
    private VoteResponse sendVoteRequest(PeerAddress peer, VoteRequest req) {
    
        return VoteResponse.builder()
        .withTerm(req.getTerm())
        .withVoteGranted(true) 
        .build();
    }

}