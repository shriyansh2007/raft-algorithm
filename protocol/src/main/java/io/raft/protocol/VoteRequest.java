package io.raft.protocol;
import java.io.Serializable;

public class VoteRequest implements Serializable {

    private final long term;
    private final int candidate;
    private final long logIndex;
    private final long logTerm;
    private static final long serialVersionUID=1L;

    private VoteRequest(Builder b) {
        this.term = b.term;
        this.candidate = b.candidate;
        this.logIndex = b.logIndex;
        this.logTerm = b.logTerm;
    }

    public long getTerm() {
        return term;
    }

    public int getCandidate() {
        return candidate;
    }

    public long getLogIndex() {
        return logIndex;
    }

    public long getLogTerm() {
        return logTerm;

    }

    @Override
    public String toString() {
        return "VoteRequest{term=" + term
                + ", candidate=" + candidate
                + ", logIndex=" + logIndex
                + ", logTerm=" + logTerm
                + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long term;
        private int candidate;
        private long logIndex;
        private long logTerm;

        public Builder withTerm(long t) {
            term = t;
            return this;
        }

        public Builder withCandidate(int c) {
            candidate = c;
            return this;

        }

        public Builder withLogIndex(long Li) {
            logIndex = Li;
            return this;
        }

        public Builder withLogTerm(long Lt) {
            logTerm = Lt;
            return this;
        }

        public VoteRequest build() {
            return new VoteRequest(this);
        }
    }

}
