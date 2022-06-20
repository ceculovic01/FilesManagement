package storageSpec.users;

import java.util.ArrayList;
import java.util.List;

public class UsersSettings {

    private List<User> userList;
    private User currentUser = null;

    public UsersSettings() {
        userList = new ArrayList<>();
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getUserByCredentials(String username, String password){
        for(User u: userList){
            if(u.getUsername().equals(username) && u.getPassword().equals(password))
                return  u;
        }
        return null;
    }

    public User getUserByUsername(String username){
        for(User u: userList){
            if(u.getUsername().equals(username))
                return  u;
        }
        return null;
    }

    public void addUser(User user){
        for(User u: userList){
            if(u.getUsername().equals(user.getUsername())){
                return;
            }
        }
        userList.add(user);
    }
}
