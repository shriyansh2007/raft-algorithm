package io.raft.protocol;

public class AppendResponse {

    private final long term;
    private final boolean success;
    private final long logIndex;

    private AppendResponse(Builder b) {
        this.term = b.term;
        this.success = b.success;
        this.logIndex = b.logIndex;

    }

    public long getTerm() {
        return term;
    }

    public boolean getSuccess() {
        return success;
    }

    public long getLogIndex() {
        return logIndex;
    }

    @Override
    public String toString() {
        return "AppendResponse{term=" + term
                + ", success=" + success
                + ", logIndex=" + logIndex + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long term;
        private boolean success;
        private long logIndex;

        public Builder withTerm(long t) {
            term = t;
            return this;
        }

        public Builder withSuccess(boolean s) {
            success = s;
            return this;
        }

        public Builder withLogIndex(long i) {
            logIndex = i;
            return this;
        }

        public AppendResponse build() {
            return new AppendResponse(this);
        }
    }
}
