
package org.travice.database;

import org.travice.model.Device;
import org.travice.model.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupTree {

    private static class TreeNode {

        private Group group;
        private Device device;
        private Collection<TreeNode> children = new HashSet<>();

        TreeNode(Group group) {
            this.group = group;
        }

        TreeNode(Device device) {
            this.device = device;
        }

        @Override
        public int hashCode() {
            if (group != null) {
                return (int) group.getId();
            } else {
                return (int) device.getId();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TreeNode)) {
                return false;
            }
            TreeNode other = (TreeNode) obj;
            if (other == this) {
                return true;
            }
            if (group != null && other.group != null) {
                return group.getId() == other.group.getId();
            } else if (device != null && other.device != null) {
                return device.getId() == other.device.getId();
            }
            return false;
        }

        public Group getGroup() {
            return group;
        }

        public Device getDevice() {
            return device;
        }

        public void setParent(TreeNode parent) {
            if (parent != null) {
                parent.children.add(this);
            }
        }

        public Collection<TreeNode> getChildren() {
            return children;
        }

    }

    private final Map<Long, TreeNode> groupMap = new HashMap<>();

    public GroupTree(Collection<Group> groups, Collection<Device> devices) {

        for (Group group : groups) {
            groupMap.put(group.getId(), new TreeNode(group));
        }

        for (TreeNode node : groupMap.values()) {
            if (node.getGroup().getGroupId() != 0) {
                node.setParent(groupMap.get(node.getGroup().getGroupId()));
            }
        }

        Map<Long, TreeNode> deviceMap = new HashMap<>();

        for (Device device : devices) {
            deviceMap.put(device.getId(), new TreeNode(device));
        }

        for (TreeNode node : deviceMap.values()) {
            if (node.getDevice().getGroupId() != 0) {
                node.setParent(groupMap.get(node.getDevice().getGroupId()));
            }
        }

    }

    public Collection<Group> getGroups(long groupId) {
        Set<TreeNode> results = new HashSet<>();
        getNodes(results, groupMap.get(groupId));
        Collection<Group> groups = new ArrayList<>();
        for (TreeNode node : results) {
            if (node.getGroup() != null) {
                groups.add(node.getGroup());
            }
        }
        return groups;
    }

    public Collection<Device> getDevices(long groupId) {
        Set<TreeNode> results = new HashSet<>();
        getNodes(results, groupMap.get(groupId));
        Collection<Device> devices = new ArrayList<>();
        for (TreeNode node : results) {
            if (node.getDevice() != null) {
                devices.add(node.getDevice());
            }
        }
        return devices;
    }

    private void getNodes(Set<TreeNode> results, TreeNode node) {
        if (node != null) {
            for (TreeNode child : node.getChildren()) {
                results.add(child);
                getNodes(results, child);
            }
        }
    }

}
