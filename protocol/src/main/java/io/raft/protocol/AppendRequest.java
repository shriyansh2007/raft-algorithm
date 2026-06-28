package io.raft.protocol;

import java.util.Collections;
import java.util.List;
import java.io.Serializable;

import io.raft.protocol.Entry.Entry;

public class AppendRequest implements Serializable {
    private static final long serialVersionUID=1L;

    private final long term;
    private final int leader;
    private final long prevLogIndex;
    private final long prevLogTerm;
    private final List<Entry> entries;
    private final long commitIndex;

    private AppendRequest(Builder b) {
        this.term = b.term;
        this.leader = b.leader;
        this.prevLogIndex = b.prevLogIndex;
        this.prevLogTerm = b.prevLogTerm;
        this.entries = b.entries;
        this.commitIndex = b.commitIndex;

    }

    public long getTerm() {
        return term;

    }

    public int getLeader() {
        return leader;

    }
    public int setLeader(int l){
        
        
        return leader;
    }

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public long getPrevLogTerm() {
        return prevLogTerm;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public boolean isHeartBeat() {
        return entries == null || entries.isEmpty();
    }

    @Override
    public String toString() {
        return "AppendRequest{term=" + term + ", leader=" + leader
                + ", prevLogIndex=" + prevLogIndex + ", prevLogTerm=" + prevLogTerm
                + ", entries=" + (entries == null ? 0 : entries.size())
                + ", commitIndex=" + commitIndex + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long term;
        private int leader;
        private long prevLogIndex;
        private long prevLogTerm;
        private List<Entry> entries = Collections.emptyList();
        private long commitIndex;

        public Builder withTerm(long t) {
            term = t;
            return this;
        }

        public Builder withLeader(int l) {
            leader = l;
            return this;
        }

        public Builder withPrevLogIndex(long i) {
            prevLogIndex = i;
            return this;
        }

        public Builder withPrevLogTerm(long t) {
            prevLogTerm = t;
            return this;
        }

        public Builder withEntries(List<Entry> e) {
            entries = e;
            return this;
        }

        public Builder withCommitIndex(long c) {
            commitIndex = c;
            return this;
        }

        public AppendRequest build() {
            return new AppendRequest(this);
        }
    }

}
