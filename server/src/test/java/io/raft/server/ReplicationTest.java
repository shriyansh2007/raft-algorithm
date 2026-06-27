package io.raft.server;
import java.io.File;
import java.util.Collections;

import io.raft.protocol.AppendRequest;
import io.raft.protocol.AppendResponse;
import io.raft.protocol.Entry.commandEntry;
import io.raft.server.state.FollowerState;
import io.raft.server.state.serverContext;

public class ReplicationTest{
    public static void main(String [] args) throws Exception{
        File dir= new File("raft-replication-test");
        dir.mkdirs();
        serverContext leader= new serverContext(new File(dir,"L"),1);
        serverContext followerA= new serverContext(new File(dir,"A"),2);
        serverContext followerB= new serverContext(new File(dir,"B"),3);
        leader.setCurrentTerm(1);
        leader.setState(raftServer.State.LEADER);
        FollowerState fA= new FollowerState(followerA);
        FollowerState fB= new FollowerState(followerB);
        System.out.println("Test 1");
        AppendRequest heartbeat= new AppendRequest.Builder().withTerm(1).withLeader(1).withPrevLogIndex(0).withPrevLogTerm(0).withEntries(Collections.emptyList()).withCommitIndex(0).build();
        AppendResponse rA= fA.handleAppend(heartbeat);
        AppendResponse rB= fB.handleAppend(heartbeat);
        System.out.println("follower a"+rA);
        System.out.println("followerB"+rB);
        assert rA.getSuccess() && rB.getSuccess() : "Heartbeat should succedd";
        System.out.println("Test-2");
        commandEntry cmd = new commandEntry();
        cmd.setIndex(1); cmd.setTerm(1);
        cmd.setCommand("SET x=1".getBytes());

        AppendRequest replication = AppendRequest.builder()
        .withTerm(1).withLeader(1)
        .withPrevLogIndex(0).withPrevLogTerm(0)
        .withEntries(Collections.singletonList(cmd))
        .withCommitIndex(0)
        .build();

        AppendResponse rA2 = fA.handleAppend(replication);
        AppendResponse rB2 = fB.handleAppend(replication);
        System.out.println("Follower A after replication: " + rA2);
        System.out.println("Follower B after replication: " + rB2);
        assert rA2.getSuccess() && rA2.getLogIndex() == 1;
        assert fA.getContext().getLog().lastIndex() == 1;
        System.out.println("Test-3");
        AppendRequest commit = AppendRequest.builder().withTerm(1).withLeader(1).withPrevLogIndex(1).withPrevLogTerm(1).withEntries(Collections.emptyList()).withCommitIndex(1) .build();

        fA.handleAppend(commit);
        fB.handleAppend(commit);
        System.out.println("Follower A commitIndex: "
        + followerA.getCommitIndex()); 
        System.out.println("Follower B commitIndex: "
        + followerB.getCommitIndex());


        
    }
}
