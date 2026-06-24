package io.raft.server.storage;
import java.util.ArrayList;
import java.util.List;

import io.raft.server.storage.Entry.Entry;
public class Log{
    private final List<Entry> entries= new ArrayList<>();
    public synchronized long append(Entry entry){
        long index= entries.size()+1;
        entry.setIndex(index);
        entries.add(entry);
        return index;

    }
    public synchronized Entry get(long index){
        return entries.get((int)(index - 1));

    }
    public synchronized long lastIndex(){
        return entries.size();
    }
    public synchronized long lastTerm() {
        if (entries.isEmpty()) return 0;
        return entries.get(entries.size()-1).getTerm();
    }
    public synchronized void truncate(long fromIndex) {
        while (entries.size() >= fromIndex) {
        entries.remove(entries.size()-1);
        }
    }
}