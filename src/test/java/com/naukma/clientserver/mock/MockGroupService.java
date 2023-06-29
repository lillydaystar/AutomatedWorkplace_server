package com.naukma.clientserver.mock;

import com.naukma.clientserver.exception.group.GroupAlreadyExistsException;
import com.naukma.clientserver.exception.group.GroupNotFoundException;
import com.naukma.clientserver.model.Group;
import com.naukma.clientserver.service.GroupService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockGroupService extends GroupService {
    private Map<Integer, Group> mockGroups;
    private int counter = 0;

    public MockGroupService() {
        super(null);
        mockGroups = new HashMap<>();
    }

    @Override
    public List<Group> getAllGroups() {
        return new ArrayList<>(mockGroups.values());
    }

    @Override
    public Group getGroupById(int id) {
        return mockGroups.get(id);
    }

    @Override
    public void createGroup(Group group) throws GroupAlreadyExistsException {
        if(groupExists(group.getName()))
            throw new GroupAlreadyExistsException("Group with this name already exists");
        group.setId(counter);
        mockGroups.put(counter++, group);
    }

    @Override
    public Group getGroupByName(String groupName) throws GroupNotFoundException {
        for (int i=0; i<counter; i++) {
            Group group = mockGroups.get(i);
            if(group != null)
                if (group.getName().equals(groupName))
                    return group;
        }
        throw new GroupNotFoundException("Not found");
    }

    @Override
    public void updateGroup(Group group) throws GroupNotFoundException {
        int key = group.getId();
        if (key == -1 || !mockGroups.containsKey(key))
            throw new GroupNotFoundException("Not found");

        Group group1 = mockGroups.get(key);
        group1.setName(group.getName());
        group1.setDescription(group.getDescription());
    }

    @Override
    public void deleteGroup(int id) throws GroupNotFoundException {
        if (mockGroups.containsKey(id))
            mockGroups.remove(id);
        else throw new GroupNotFoundException("Not found");
    }

    public void setMockGroups(Map<Integer, Group> mockGroups) {
        this.mockGroups = mockGroups;
    }

    private boolean groupExists(String name) {
        for (int i=0; i<counter; i++) {
            Group group = mockGroups.get(i);
            if(group != null)
                if (group.getName().equals(name))
                    return true;
        }
        return false;
    }

}