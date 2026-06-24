package io.raft.server.storage.system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class metaStore {

    private final Path file;
    private long term = 0;
    private int vote = -1;

    public metaStore(File directory) throws IOException {

    Files.createDirectories(directory.toPath());

    this.file = directory.toPath().resolve("metaStore");

    if (Files.exists(file)) {
        load();
    }
}

    public void storeTerm(long term) throws IOException {
        this.term = term;
        persist();
    }

    public void storeVote(int candidateID) throws IOException {
        this.vote = candidateID;
        persist();

    }

    public long loadTerm() {
        return term;
    }

    public long loadVote() {
        return vote;

    }

    private void persist() throws IOException {
        String json = "{\"term\":" + term + ",\"vote\":" + vote + "}";
        Files.write(file, json.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.SYNC); // SYNC = fsync before returning
    }

    private void load() throws IOException {
        String json = new String(Files.readAllBytes(file));
        // simple parse (no library needed for two fields)
        term = Long.parseLong(
                json.replaceAll(".*\"term\":(\\d+).*", "$1")
        );

        vote = Integer.parseInt(
                json.replaceAll(".*\"vote\":(-?\\d+).*", "$1")
        );
    }
}
