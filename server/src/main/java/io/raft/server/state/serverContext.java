package io.raft.server.state;
import java.io.File;
import java.io.IOException;

import io.raft.server.raftServer;
import io.raft.server.storage.Log;
import io.raft.server.storage.system.metaStore;


public class serverContext{
    private final metaStore metaStore;
    private final Log log;
    public serverContext(File dataDir) throws IOException{
        this.metaStore= new metaStore(dataDir);
        this.log=new Log();
        this.currentTerm= metaStore.loadTerm();
        this.votedFor= (int) metaStore.loadVote();




    }
    private long currentTerm=0;
    private int votedFor=-1;
    private long commitIndex=0;
    private long lastApplied=0;
    private int leader=-1;
    private raftServer.State state= raftServer.State.INACTIVE;
    public long getCurrentTerm(){
        return currentTerm;
    }
    public void setCurrentTerm(long term) throws IOException{
        this.currentTerm= term;
        metaStore.storeTerm(term);
    }

    public int getVotedFor(){
        return votedFor;
    }
    public void setVotedFor(int id) throws IOException{
        this.votedFor= id;
        metaStore.storeVote(id);
    }
    public Log getLog(){
        return log;
    }
    public raftServer.State getState() { return state; }
    public void setState(raftServer.State s) { this.state = s; }   
}