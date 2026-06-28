package io.raft.protocol;
import java.io.Serializable;
public class VoteResponse implements Serializable{
    private final long term;
    private final boolean voteGranted;
    private static final long serialVersionUID=1L;
    private VoteResponse(Builder b){
        this.term= b.term;
        this.voteGranted= b.voteGranted;
    }
    public long getTerm(){
        return term;
    }
    public boolean isVoteGranted(){
        return voteGranted;
    }
    @Override
    public String toString(){
        return "VoteResponse{term=" + term
      + ", voteGranted=" + voteGranted + "}";
    }
    public static Builder builder(){
        return new Builder();
    }
    public static class Builder{
        private  long term;
        private boolean voteGranted;
        public Builder withTerm(long t){
            term=t;
            return this;
        }
        public Builder withVoteGranted(boolean v){
            voteGranted=v;
            return this;
        }
        public VoteResponse build(){
            return new VoteResponse(this);
        }
    }



}