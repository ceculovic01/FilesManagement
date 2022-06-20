package test.utils;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    String init = "Usage: init [username] [password] [storagePath]\n" +
            "Initialize storage and admin with given username and password on given path.";
    String addUser = "Usage: addUser [username] [password] [privilege]\n" +
            "Create new user with given username, password and privilege.\n" +
            "Privilege:Administrator, StandardUser, Viewer." ;
    String login = "Usage: login [username] [password] [storagePath]\n" +
            "Connect to storage with specified username and password.";
    String logout = "Usage: logout\n" +
            "Disconnect user from current storage." ;
    String crf = "Usage: crf [path] [fileName]\n" +
            "       crf [path] [[fileName1,fileName2,fileName3...]]\n" +
            "Create file or files on given path.";
    String crdir = "Usage: crdir [path] [fileName]\n" +
            "       crdir [path] [[fileName1,fileName2,fileName3...]]\n" +
            "Create directory or directories on given path." ;
    String del = "Usage: del [path]\n" +
            "       del [[path1,path2,path3...]]\n" +
            "Remove files or directories on given path from storage.";
    String ls = "Usage: ls [option]... [path]...\n" +
            "List information about the files and directories on given path.\n\n" +
            "    -fnd,    --fNamesInDirectory               View file names in directory.\n" +
            "    -dnd,    --dirNamesInDirectory             View directory names in directory.\n" +
            "    -fnsx, --fNamesInStorageByExtension      View file names in storage with certain extension.\n" +
            "    -fndx, --fNamesInDirByExtension          View file names in directory with certain extension.\n" +
            "    -sn,     --fByNames                        View file names in directory sorted by name.\n" +
            "    -sdc,    --fByDateCreated                  View files sorted by date created.\n" +
            "    -sdm,    --fByDateModified                 View files sorted by date modified.\n" +
            "    -fbdc,   --fByDateCreatedBtwDates          View files created between given dates.\n" +
            "    -fbdm,   --fByDateModifiedBtwDates         View files modified between given dates.";

    String download = "Usage: download [path]\n" +
            "Download file or directory on given path from storage.";
    String save = "Usage: save [path] [filePath]\n" +
            "Upload file or directory from file path to given path on storage." ;
    String copy = "Usage: copy [fromPath] [toPath]\n" +
            "       copy [[fromPath1,fromPath2,fromPath3...]] [[toPath1,toPath2,toPath3...]]\n" +
            "Copy files or directories from given path to given path.";
    String move = "Usage: move [fromPath] [toPath]\n" +
            "       move [[fromPath1,fromPath2,fromPath3...]] [[toPath1,toPath2,toPath3...]]\n" +
            "Move files or directories from given path to given path." ;
    String addUserPrivilegeDir = "Usage: addUserPrivilegeDir [username] [path] [privilege]\n" +
            "Add specified privilege for user with given username on given path.\n" +
            "Privilege:Administrator, StandardUser, Viewer.";
    String delUserPrivilegeDir = "Usage: delUserPrivilegeDir [username] [folderPath]\n" +
            "Remove specified privilege for user with given username on given path." ;
    String chngUserPrivilege = "Usage: chngUserPrivilegeDir [username] [privilege]\n" +
            "Change privilege for user with given username to specified privilege.\n" +
            "Privilege:Administrator, StandardUser, Viewer.";
    String setSize = "Usage: setSize [sizeInBytes]\n" +
            "Set maximum storage size in bytes." ;
    String addNSExt = "Usage: addNSExt [extension]\n" +
            "Add not supported extension for storage.\n" +
            "Extension:txt,rtf,docx...";
    String delNSExt = "Usage: delNSExt [extension]\n" +
            "Remove not supported extension for storage.\n" +
            "Extension:txt,rtf,docx..." ;
    String addMaxFilesDir = "Usage: addMaxFilesDir [path] [numberOfFiles]\n" +
            "Set maximum number of files for directory on given path.";
    String delMaxFilesDir = "Usage: delMaxFilesDir [path]\n" +
            "Remove maximum number of files for directory on given path." ;
    String help = "To init storage type: init\n" +
            "To connect type: login\n" +
            "To disconnect type: logout\n" +
            "To create file type: crf\n" +
            "To create file type: crdir\n" +
            "To delete file type: del\n" +
            "To view files type: ls\n" +
            "To download file type: download\n" +
            "To upload file type: save\n" +
            "To copy file type: copy\n" +
            "To move file type: move\n" +
            "To create user type: addUser\n" +
            "To add user privilege for directory type: addUserPrivilegeDir\n" +
            "To remove user privilege for directory type: delUserPrivilegeDir\n" +
            "To change user privilege for directory type: chngUserPrivilegeDir\n" +
            "To set maximum storage size type: setSize\n" +
            "To add not supported extension in storage type: addNSExt\n" +
            "To remove not supported extension in storage type: delNSExt\n" +
            "To add maximum number of files for directory in storage type: addMaxFilesDir\n" +
            "To remove maximum number of files for directory in storage type: delMaxFilesDir\n" +
            "To exit type: exit";

    public Parser(){

    }
    public List<String> parsePattern(String s) throws Exception{
        List<String> fileNames = new ArrayList<>();
        if(s.contains("{") && s.contains("}")){
            try {
                String[] sp = s.split("[{]");
                String prefix = sp[0];
                String len = sp[1].replace("}", "");
                String[] spl = len.split("-");
                String f;
                int i = Integer.parseInt(spl[0]);
                int j = Integer.parseInt(spl[1]);
                for (; i <= j; i++) {
                    f = prefix + i;
                    fileNames.add(f);
                }
                return fileNames;
            }catch (Exception e){
                throw new Exception("Bad pattern format!");
            }
        }else if(s.contains("[") && s.contains("]")){
            try{
                String[] sp = s.replace("[","").replace("]","").split(",");
                for(int i = 0; i < sp.length; i++){
                    fileNames.add(sp[i]);
                }
                return fileNames;
            }catch(Exception e){
                throw new Exception("Bad pattern format!");
            }
        }else{

            fileNames.add(s);
            return fileNames;
        }
    }

    public int parseOption(String option){
        if(option.equals("--fNamesInDirectory") || option.equals("-fnd")){
            return 1;
        }else if(option.equals("--dirNamesInDirectory") || option.equals("-dnd")){
            return 2;
        }else if(option.equals("--fNamesInStorageByExtension") || option.equals("-fnsx")){
            return 3;
        }else if(option.equals("--fNamesInDirByExtension") || option.equals("-fndx")){
            return 4;
        }else if(option.equals("--fByNames") || option.equals("-sn")){
            return 5;
        }else if(option.equals("--fByDateCreated") || option.equals("-sdc")){
            return 6;
        }else if(option.equals("--fByDateModified") || option.equals("-sdm")){
            return 7;
        }else if(option.equals("--fByDateCreatedBtwDates") || option.equals("-fbdc")){
            return 8;
        }else if(option.equals("--fByDateModifiedBtwDates") || option.equals("-fbdm")){
            return 9;
        }

        return 0;
    }

    public boolean isHelpMessage(String command){
        if(command.equals("--help") || command.equals("-h")){
            return true;
        }
        return false;
    }

    public String getHelpMessage(String command){
        if(command.equals("init")){
            return init;
        }
        if(command.equals("addUser")){
            return addUser;
        }
        if(command.equals("login")){
            return login;
        }
        if(command.equals("logout")){
            return logout;
        }
        if(command.equals("crf")){
            return crf;
        }
        if(command.equals("crdir")){
            return crdir;
        }
        if(command.equals("del")){
            return del;
        }
        if(command.equals("ls")){
            return ls;
        }
        if(command.equals("download")){
            return download;
        }
        if(command.equals("save")){
            return save;
        }
        if(command.equals("copy")){
            return copy;
        }
        if(command.equals("move")){
            return move;
        }
        if(command.equals("addUserPrivilegeDir")){
            return addUserPrivilegeDir;
        }
        if(command.equals("delUserPrivilegeDir")){
            return delUserPrivilegeDir;
        }
        if(command.equals("chngUserPrivilege")){
            return chngUserPrivilege;
        }
        if(command.equals("setSize")){
            return setSize;
        }
        if(command.equals("addNSExt")){
            return addNSExt;
        }
        if(command.equals("delNSExt")){
            return delNSExt;
        }
        if(command.equals("addMaxFilesDir")){
            return addMaxFilesDir;
        }
        if(command.equals("delMaxFilesDir")){
            return delMaxFilesDir;
        }
        if(command.equals("help")){
            return help;
        }

        return "";

    }
}
