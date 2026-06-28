# Raft Algorithm

A Java implementation of the **Raft consensus algorithm** — a fault-tolerant protocol for managing a replicated log across a distributed cluster of servers.

---

## What is Raft?

Raft is a consensus algorithm designed to be easy to understand. It ensures that a cluster of servers agrees on a sequence of values (log entries) even in the presence of failures. As long as a majority of nodes are available, the cluster continues to make progress.

Core concepts:
- **Leader Election** — one node is elected leader per term; all client requests go through it
- **Log Replication** — the leader replicates log entries to followers; an entry is committed once a majority acknowledges it
- **Safety** — committed entries are never lost, even across leader failures

---

## Project Structure

This is a multi-module Maven project:

```
raft-algorithm/
├── protocol/       # Shared message types and RPC definitions
├── server/         # Raft node implementation (leader, follower, candidate logic)
├── client/         # Client to send commands to the cluster
└── pom.xml         # Parent POM (groupId: io.raft)
```

| Module     | Responsibility                                                    |
|------------|-------------------------------------------------------------------|
| `protocol` | Defines the message contracts used between nodes and clients      |
| `server`   | Core Raft logic — leader election, log replication, state machine |
| `client`   | Sends commands to the Raft cluster and receives responses         |

---

## Prerequisites

- Java 8+
- Maven 3.x

---

## Build

Clone the repository and build all modules:

```bash
git clone https://github.com/shriyansh2007/raft-algorithm.git
cd raft-algorithm
mvn clean install
```

---

## Running

### Start a Server Node

```bash
cd server
mvn exec:java -Dexec.mainClass="<ServerMainClass>" -Dexec.args="<node-id> <port> <peer-list>"
```

Repeat for each node in your cluster (a minimum of 3 nodes is recommended for fault tolerance).

### Run the Client

```bash
cd client
mvn exec:java -Dexec.mainClass="<ClientMainClass>" -Dexec.args="<command>"
```

> Replace `<ServerMainClass>` and `<ClientMainClass>` with the actual fully qualified class names from the `server` and `client` modules.

---

## How It Works

1. On startup, all nodes begin as **followers**.
2. If a follower receives no heartbeat within the election timeout, it becomes a **candidate** and requests votes.
3. A candidate that receives votes from a majority becomes the **leader**.
4. The leader accepts commands from clients, appends them to its log, and replicates them to followers.
5. Once a majority of nodes have written an entry, it is **committed** and applied to the state machine.
6. If the leader fails, a new election is triggered automatically.

---

## Fault Tolerance

| Cluster Size | Tolerated Failures |
|--------------|--------------------|
| 3 nodes      | 1 node             |
| 5 nodes      | 2 nodes            |
| 7 nodes      | 3 nodes            |

---

## References

- [Raft: In Search of an Understandable Consensus Algorithm](https://raft.github.io/raft.pdf) — Diego Ongaro & John Ousterhout
- [Raft visualization](https://raft.github.io/) — interactive demo

---

## License

This project is open source. See the repository for details.