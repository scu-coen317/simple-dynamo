package edu.scu.coen317.common.util;

import edu.scu.coen317.common.Configuration;
import edu.scu.coen317.common.model.Node;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class NodeLocator {

    // return replication nodes except for itself
    public static Set<Node> getNodes(ConcurrentSkipListSet<Node> members, String key, String selfHash) {
        Set<Node> nodes = new HashSet<>();
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Node tmp = new Node("", 0, HashFunctions.md5Hash(key), Integer.valueOf(pid));
        Node start = members.ceiling(tmp);
        if (start == null) {
            start = members.first();
        }
        Set<Node> tailSet = members.tailSet(start, true);

        // get N replicas from members list
        if (tailSet.size() >= Configuration.N) {
            int cnt = 0;
            for (Node n : tailSet) {
                if (cnt++ == Configuration.N) {
                    break;
                }
                nodes.add(n.clone());
            }
        } else {
            nodes.addAll(tailSet);
            int size = Configuration.N - nodes.size();
            for (Node n : members) {
                if (size-- == 0) {
                    break;
                }
                nodes.add(n.clone());
            }
        }

        // remove node itself from list
        nodes.removeIf(n -> n.getHash().equals(selfHash));

        return nodes;
    }
}
