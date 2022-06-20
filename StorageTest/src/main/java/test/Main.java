package test;

import storageSpec.ExporterManager;
import storageSpec.StorageExporter;
import storageSpec.fileRepresentation.MyFile;
import storageSpec.users.Privilege;
import test.utils.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String initPath;
        if(args.length < 1){
            System.out.println("Enter init path");
            initPath = scanner.next();
        }else
            initPath = args[0];

        try {
            Class.forName("lfImplementation.LFImplementation");
        } catch (ClassNotFoundException e2) {
            try{
                Class.forName("gdImplementation.GDImplementation");
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }

        StorageExporter storageExporter = ExporterManager.getExporter();


        try {
            System.out.print("Enter username:");
            String username2 = scanner.next();
            System.out.print("Enter password:");
            String password2 = scanner.next();
            System.out.print("Enter storage file name:");
            String fileName2 = scanner.next();
            if(initPath.equals("/")){
                initPath = "";
            }
            storageExporter.initStorage(username2, password2, initPath, fileName2);
            System.out.println("For help type <help>");

        }catch(Exception e){
            System.out.println(e);
        }

        Parser parser = new Parser();

        while(true) {

            try {
                String input = scanner.nextLine();
                if(input.equals(""))
                    continue;
                String[] split = input.split(" ");
                String username = "";
                String password = "";
                String path = "";
                String fileName = "";

                switch(split[0]){
                    case "init":
                        if(split.length == 5){
                            username = split[1];
                            password = split[2];
                            path = split[3];
                            if(path.equals("/")){
                                path = "";
                            }
                            fileName = split[4];
                            storageExporter.initStorage(username, password, path, fileName);
                            System.out.println("Storage initialized");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters");
                            }
                        }
                        else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "help":
                        System.out.println(parser.getHelpMessage(split[0]));
                        break;
                    case "addUser":
                        if(split.length == 4){
                            username = split[1];
                            password = split[2];
                            Privilege priv = null;
                            if (split[3].equalsIgnoreCase("administrator"))
                                priv = Privilege.Administrator;
                            if (split[3].equalsIgnoreCase("standarduser"))
                                priv = Privilege.StandardUser;
                            if (split[3].equalsIgnoreCase("viewer"))
                                priv = Privilege.Viewer;
                            if(priv == null){
                                System.out.println("Invalid parameters!");
                                break;
                            }
                            storageExporter.createUser(username, password, priv);
                            System.out.println("User created!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "login":
                        if(split.length == 4){
                            username = split[1];
                            password = split[2];
                            path = split[3];
                            storageExporter.connect(username, password, path);
                            System.out.println("Successful login!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "logout":
                        if(split.length == 1){
                            storageExporter.disconnect();
                            System.out.println("Successful logout!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters");
                            }
                        }else{
                            System.out.println("Invalid parameters!");

                        }
                        break;
                    case "crf":
                           if(split.length == 3){
                            List<String> fileNames = parser.parsePattern(split[2]);
                            path = split[1];
                            if(path.equals("/")){
                                path = "";
                            }
                            storageExporter.createFiles(path, fileNames);
                            String message = fileNames.size() > 1 ? "Files successfully created!" : "File successfully created!";
                            System.out.println(message);
                        }else if(split.length == 2){
                               if(parser.isHelpMessage(split[1])){
                                   System.out.println(parser.getHelpMessage(split[0]));
                               }else{
                                   System.out.println("Invalid parameters");
                               }
                           }else {
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "crdir":
                        if(split.length == 3){
                            List<String> fileNames = parser.parsePattern(split[2]);
                            path = split[1];
                            if(path.equals("/")){
                                path = "";
                            }
                            storageExporter.createDirectories(path, fileNames);
                            String message = fileNames.size() > 1 ? "Files successfully created!" : "File successfully created!";
                            System.out.println(message);
                        }else if(split.length == 2) {
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "del":
                        if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                List<String> paths = parser.parsePattern(split[1]);
                                storageExporter.deleteFiles(paths);
                                System.out.println("File deleted successfully!");
                            }
                        }else {
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "ls":
                        if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                path = split[1];
                                if(path.equals("/")){
                                    path = "";
                                }
                                List<MyFile> files = storageExporter.viewFiles(path);
                                for(MyFile f: files){
                                    System.out.println("File name: " + f.getName());
                                }
                            }
                        }else if(split.length == 3){
                                int option = parser.parseOption(split[1]);
                                List<String> result = new ArrayList<>();
                                String pth = split[2];
                                if(pth.equals("/"))
                                    pth = "";
                                switch (option){
                                    case 1:
                                            result = storageExporter.getAllFileNamesInDirectory(pth);
                                            break;
                                    case 2:
                                            result = storageExporter.getAllDirectoryNamesInDirectory(pth);
                                            break;
                                    case 3:
                                            result = storageExporter.getAllFilesInStorageByExtension(pth);
                                            break;
                                    case 5:
                                            result = storageExporter.getAllFilesByName(pth, true);
                                            break;
                                    case 6:
                                            result = storageExporter.getAllFilesByDateCreated(pth, true);
                                            break;
                                    case 7:
                                            result = storageExporter.getAllFilesByDateModified(pth, true);
                                            break;
                                    default:
                                            System.out.println("Invalid parameters!");
                                }
                                if(!result.isEmpty()){
                                    for(String s: result){
                                        System.out.println(s);
                                    }
                                }
                        }else if(split.length == 4){
                            int option = parser.parseOption(split[1]);
                            List<String> result = new ArrayList<>();
                            String pth = split[2];
                            if(pth.equals("/"))
                                pth = "";
                            String sort = "";
                            boolean isAscending = false;
                            switch (option){
                                case 4:
                                    result = storageExporter.getAllFilesInDirectoriesAndSubdirectoriesByExtension(pth, split[3]);
                                    break;
                                case 5:
                                    sort = split[3];
                                    if(sort.equalsIgnoreCase("asc")){
                                        isAscending = true;
                                    }else if(sort.equalsIgnoreCase("desc")){
                                        isAscending = false;
                                    }else{
                                        System.out.println("Invalid parameters!");
                                        break;
                                    }
                                    result = storageExporter.getAllFilesByName(pth, isAscending);
                                    break;
                                case 6:
                                    sort = split[3];
                                    if(sort.equalsIgnoreCase("asc")){
                                        isAscending = true;
                                    }else if(sort.equalsIgnoreCase("desc")){
                                        isAscending = false;
                                    }else{
                                        System.out.println("Invalid parameters!");
                                        break;
                                    }
                                    result = storageExporter.getAllFilesByDateCreated(pth, isAscending);
                                    break;
                                case 7:
                                    sort = split[3];
                                    if(sort.equalsIgnoreCase("asc")){
                                        isAscending = true;
                                    }else if(sort.equalsIgnoreCase("desc")){
                                        isAscending = false;
                                    }else{
                                        System.out.println("Invalid parameters!");
                                        break;
                                    }
                                    result = storageExporter.getAllFilesByDateModified(pth, isAscending);
                                    break;
                                default:
                                    System.out.println("Invalid parameters!");
                            }
                            if(!result.isEmpty()){
                                for(String s: result){
                                    System.out.println(s);
                                }
                            }
                        }else if(split.length == 5){
                            int option = parser.parseOption(split[1]);
                            List<String> result = new ArrayList<>();
                            String pth = split[2];
                            if(pth.equals("/"))
                                pth = "";

                            switch (option){
                                case 8:
                                    result = storageExporter.getFilesByDateCreatedBetweenDates(pth, split[3], split[4]);
                                    break;
                                case 9:
                                    result = storageExporter.getFilesByDateModifiedBetweenDates(pth, split[4], split[4]);
                                    break;
                                default:
                                    System.out.println("Invalid parameters!");
                            }
                            if(!result.isEmpty()){
                                for(String s: result){
                                    System.out.println(s);
                                }
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "download":
                         if(split.length == 2){
                             if(parser.isHelpMessage(split[1])){
                                 System.out.println(parser.getHelpMessage(split[0]));
                             }else{
                                 String pth = split[1];
                                 if(pth.equals("/"))
                                     pth = "";
                                 storageExporter.downloadFile(pth);
                                 System.out.println("File successfully downloaded!");
                             }
                        }else {
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "save":
                        if(split.length == 3){
                            String pth = split[1];
                            if(pth.equals("/"))
                                pth = "";
                            storageExporter.saveFile(pth, split[2]);
                            System.out.println("File successfully uploaded!");
                        }else if(split.length == 2) {
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters!");
                            }
                        }else {
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "copy":
                            if(split.length == 3){
                               List<String> fromPaths =  parser.parsePattern(split[1]);
                               List<String> toPaths = parser.parsePattern(split[2]);
                               int index = fromPaths.indexOf("/");
                               if(index != -1)
                                   fromPaths.set(index, "");
                               index = toPaths.indexOf("/");
                               if(index != -1)
                                   toPaths.set(index, "");
                               storageExporter.copyFiles(fromPaths, toPaths);
                                System.out.println("Files successfully copied!");
                            }else if(split.length == 2){
                                if(parser.isHelpMessage(split[1])){
                                    System.out.println(parser.getHelpMessage(split[0]));
                                }else{
                                    System.out.println("Invalid parameters!");
                                }
                            }else{
                                System.out.println("Invalid parameters!");
                            }
                        break;
                    case "move":
                        if (split.length == 3) {
                            List<String> fromPaths = parser.parsePattern(split[1]);
                            List<String> toPaths = parser.parsePattern(split[2]);
                            int index = fromPaths.indexOf("/");
                            if(index != -1)
                                fromPaths.set(index, "");
                            index = toPaths.indexOf("/");
                            if(index != -1)
                                toPaths.set(index, "");
                            storageExporter.moveFiles(fromPaths, toPaths);
                            System.out.println("Files successfully moved!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters!");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "addUserPrivilegeDir":
                        if(split.length == 4){
                            username = split[1];
                            path = split[2];
                            Privilege priv = null;
                            if (split[3].equalsIgnoreCase("administrator"))
                                priv = Privilege.Administrator;
                            if (split[3].equalsIgnoreCase("standarduser"))
                                priv = Privilege.StandardUser;
                            if (split[3].equalsIgnoreCase("viewer"))
                                priv = Privilege.Viewer;
                            if(priv == null){
                                System.out.println("Invalid parameters!");
                                break;
                            }
                            storageExporter.addUserPrivilegeForFolder(username, path, priv);
                            System.out.println("Added user privilege for directory!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters!");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "delUserPrivilegeDir":
                        if(split.length == 3){
                            username = split[1];
                            String folderName = split[2];
                            storageExporter.removeUserPrivilegeForFolder(username,folderName);
                            System.out.println("Deleted user privilege for directory!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters!");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "chngUserPrivilege":
                        if(split.length == 3){
                            username = split[1];
                            Privilege priv = null;
                            if (split[2].equalsIgnoreCase("administrator"))
                                priv = Privilege.Administrator;
                            if (split[2].equalsIgnoreCase("standarduser"))
                                priv = Privilege.StandardUser;
                            if (split[2].equalsIgnoreCase("viewer"))
                                priv = Privilege.Viewer;
                            if(priv == null){
                                System.out.println("Invalid parameters!");
                                break;
                            }
                            storageExporter.changeUserPrivilege(username,priv);
                            System.out.println("Changed user privilege!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters!");
                            }
                        }else {
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "setSize":
                        if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                long size = Long.parseLong(split[1]);
                                storageExporter.setStorageSize(size);
                                System.out.println("Storage size set!");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "addNSExt":
                        if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                storageExporter.addNotSupportedExtension(split[1]);
                                System.out.println("Added not supported extension!");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "delNSExt":
                        if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                storageExporter.removeNotSupportedExtension(split[1]);
                                System.out.println("Deleted not supported extension!");
                            }

                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "addMaxFilesDir":
                        if(split.length == 3){
                            storageExporter.addMaxNumberFilesInDirectory(split[1], Integer.parseInt(split[2]));
                            System.out.println("Added max files for directory!");
                        }else if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                System.out.println("Invalid parameters!");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "delMaxFilesDir":
                        if(split.length == 2){
                            if(parser.isHelpMessage(split[1])){
                                System.out.println(parser.getHelpMessage(split[0]));
                            }else{
                                storageExporter.removeMaxNumberFilesInDirectory(split[1]);
                                System.out.println("Removed max files for directory!");
                            }
                        }else{
                            System.out.println("Invalid parameters!");
                        }
                        break;
                    case "exit":
                        return;
                    default:
                        System.out.println("Unknown command!");
                }
            }catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

    }


}
