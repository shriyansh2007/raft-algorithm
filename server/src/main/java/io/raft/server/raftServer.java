package io.raft.server;
public class raftServer{
    public enum State{
        INACTIVE,
        FOLLOWER,
        CANDIDATE,
        LEADER
    }
}