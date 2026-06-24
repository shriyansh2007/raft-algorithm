package io.raft.server.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class serverContextTest {

    @Test
    void metaStoreSurvivesRestart() throws IOException {

        serverContext ctx = new serverContext(new File("raft-test"));

        ctx.setCurrentTerm(3);
        ctx.setVotedFor(2);

        serverContext ctx2 = new serverContext(new File("raft-test"));

        assertEquals(3, ctx2.getCurrentTerm());
        assertEquals(2, ctx2.getVotedFor());

        System.out.println("MetaStore survives restart: PASS");
    }
}