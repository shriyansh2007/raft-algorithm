package io.raft.protocol.Entry;
import java.io.Serializable;
public abstract class Entry implements Serializable{
    private static final long serialVersionUID=1L;
    
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
