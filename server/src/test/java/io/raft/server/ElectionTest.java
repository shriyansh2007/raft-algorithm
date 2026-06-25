package io.raft.server;
import java.io.File;

import io.raft.protocol.VoteRequest;
import io.raft.protocol.VoteResponse;
import io.raft.server.state.FollowerState;
import io.raft.server.state.serverContext;

public class ElectionTest{
    
    public static void main(String[] args) throws Exception{
        
        File dir= new File("raft-test-election");
        dir.mkdirs();
        serverContext s1= new serverContext(new File(dir,"s1"),1);
        serverContext s2= new serverContext(new File(dir,"s2"),1);
        serverContext s3= new serverContext(new File(dir,"s3"),1);
        FollowerState f2= new FollowerState(s2);
        FollowerState f3= new FollowerState(s3);
        System.out.println("SERVER1 STARTS ELECTION");
        s1.setCurrentTerm(s1.getCurrentTerm()+1);
        s1.setVotedFor(1);
        VoteRequest req= VoteRequest.builder().withTerm(s1.getCurrentTerm()).withCandidate(1).withLogIndex(s1.getLog().lastIndex()).withLogTerm(s1.getLog().lastTerm()).build();
        System.out.println("sending"+req);
        VoteResponse r2= f2.handleVoteResponse(req);
        System.out.println("server 2 says"+r2);
        VoteResponse r3= f3.handleVoteResponse(req);
        System.out.println("Server3 says"+r3);
        int votes=1;
        if(r2.isVoteGranted()){
            votes++;
        }
        if(r3.isVoteGranted()){
            votes++;
        }
        System.out.println("Total votes: " + votes + "/3");
        System.out.println("Result: " + (votes >= 2 ? "LEADER" : "no majority"));
        serverContext s2reload = new serverContext(new File(dir,"s2"), 2);
        System.out.println("After restart, s2 voted for: "+ s2reload.getVotedFor() + " (should be 1)");




        
    }

}