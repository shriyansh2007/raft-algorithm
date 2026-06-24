package io.raft.server.storage.Entry;
public abstract class Entry{
    private long Index;
    private long Term;
    public long getIndex(){
        return Index;
    }
    public void setIndex(long Index){
        this.Index=Index;
    }
    public long getTerm(){
        return Term;
    }
    public void setTerm(long term){
        this.Term=term;
    }

}
