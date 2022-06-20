package storageSpec.users;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User {
    private String username;
    private String password;
    private Privilege privilege;
    private Map<String, Privilege> folderPrivileges;

    public User(String username, String password, Privilege privilege) {
        this.username = username;
        this.password = password;
        this.privilege = privilege;
        folderPrivileges = new HashMap<>();
    }

    public User(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }

    public Map<String, Privilege> getFolderPrivileges() {
        return folderPrivileges;
    }

    public void setFolderPrivileges(Map<String, Privilege> folderPrivileges) {
        this.folderPrivileges = folderPrivileges;
    }

    public void addFolderPrivilege(String folderName, Privilege privilege){
        folderPrivileges.put(folderName, privilege);
    }

    public void removeFolderPrivilege(String folderName){
        folderPrivileges.remove(folderName);
    }

    public Privilege containsPath(String path){
        for(Map.Entry<String, Privilege> entry: folderPrivileges.entrySet()){
            if(path.contains(entry.getKey())){
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) && password.equals(user.password);
    }

}

