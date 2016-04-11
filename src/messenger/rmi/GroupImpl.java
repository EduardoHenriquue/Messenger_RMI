package messenger.rmi;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by Eduardo on 10/04/2016.
 */
public class GroupImpl {

    private String nameGroup;
    private Map<String, MessengerClient> members;

    public GroupImpl() {
        this.members = new HashMap<String, MessengerClient>();
    }

    public void addMembers(MessengerClient client, String userName){
        this.members.put(userName, client);
    }

    public String listMembers(){
        return this.members.keySet().toString();
    }

    public String getNameGroup() {
        return this.nameGroup;
    }

    public void setNameGroup(String name){
        this.nameGroup = name;
    }
}
