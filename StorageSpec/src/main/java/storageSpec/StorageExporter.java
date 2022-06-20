package storageSpec;

import org.apache.commons.io.FilenameUtils;
import storageSpec.exceptions.StorageException;
import storageSpec.fileConfig.ConfigSettings;
import storageSpec.fileRepresentation.MyFile;
import storageSpec.users.Privilege;
import storageSpec.users.User;
import storageSpec.users.UsersSettings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a Storage exporter.
 * @author Igor Todorovic
 * @author Milos Ceculovic
 * @version 1.0
 */

public abstract class StorageExporter {
    private String rootPath = null;
    private UsersSettings usersSettings;
    private ConfigSettings configSettings;
    private String defaultStorageName = "defaultStorage";

    /**
     * Creates a Storage exporter.
     * */
    public StorageExporter() {

    }

    /**
     * Initialise storage with the specified username and password on given path with given name.
     * @param username User's username.
     * @param password User's password.
     * @param path Path where the storage will be initialised.
     * @param fileName Name for storage root directory.
     * @throws Exception If a Storage or some other exception occurred.
     */

    public abstract void initStorage(String username, String password, String path, String fileName) throws Exception;

    /**
     * Initialise storage with the specified username and password on given path with default storage name.
     * @param username User's username.
     * @param password User's password.
     * @param path Path where the storage will be initialised.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void initStorage(String username, String password, String path) throws Exception{
        initStorage(username, password, path, defaultStorageName);
    }


    /**
     * Connects user with the specified username and pasword to storage on given path.
     * @param username User's username.
     * @param password User's password.
     * @param path  Path on where existing storage is.
     * @throws Exception  If a Storage or some other exception occurred.
     */
    public abstract void connect(String username, String password, String path) throws Exception;

    /**
     * Disconnects user from connected storage.
     * @throws Exception
     */
    public abstract void disconnect() throws Exception;

    /**
     * Create file on specified path with given name.
     * @param path Path where new file is created.
     * @param fileName Name of created file.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract void createFile(String path, String fileName) throws Exception;

    /**
     * Create file in storage root directory with given name.
     * @param fileName Name of created file.
     * @throws Exception
     */
    public void createFile(String fileName) throws Exception{
        createFile("", fileName);
    }

    /**
     * Create files on specified path with given name of file.
     * @param path Path where files are created.
     * @param fileNames Names of created files.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void createFiles(String path, List<String> fileNames) throws  Exception{
        if(fileNames == null){
            throw new StorageException("File names cannot be null!");
        }
        if(fileNames.isEmpty()){
            throw new StorageException("File names cannot be empty!");
        }
        for(String f: fileNames){
            createFile(path, f);
        }
        return;
    }

    /**
     * Create files on specified path with given name of file.
     * @param path Path where files are created.
     * @param fileNames Names of created files.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void createFiles(String path, String[] fileNames) throws  Exception{
        if(fileNames == null){
            throw new StorageException("File names cannot be null!");
        }
        if(fileNames.length == 0){
            throw new StorageException("File names cannot be empty!");
        }
        for(String f: fileNames){
            createFile(path, f);
        }
        return;
    }

    /**
     * Create directory on specified path with given name.
     * @param path Path where new directory is created.
     * @param fileName Name of created directory.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract void createDirectory(String path, String fileName) throws Exception;

    /**
     * Create directories on specified path with given names.
     * @param path Path where new directories are created.
     * @param fileNames Names of created directories.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void createDirectories(String path, List<String> fileNames) throws Exception {
        if (fileNames == null) {
            throw new StorageException("File names cannot be null!");
        }
        if (fileNames.isEmpty()) {
            throw new StorageException("File names cannot be empty!");
        }
        for (String f : fileNames) {
            createDirectory(path, f);
        }
        return;
    }

    /**
     * Upload specified file on given path in storage.
     * @param path Path where file is uploaded.
     * @param file File which will be uploaded.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract void saveFile(String path, File file) throws Exception;  //Admin, Administrator, StandardUser

    /**
     * Upload file which has specified file path on given path in storage.
     * @param path Path where file is uploaded.
     * @param filePath File path on which file exists.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void saveFile(String path, String filePath) throws Exception{
        File file = new File(filePath);
        saveFile(path, file);
    }

    /**
     * Upload file in storage root directory.
     * @param file File which will be uploaded.
     * @throws Exception If a Storage or some other exception occurred.
     */

    public void saveFile(File file) throws Exception{
        saveFile("", file);
    }

    /**
     * Upload file which has specified file path in storage root directory.
     * @param filePath File path on which file exists.
     * @throws Exception If a Storage or some other exception occurred.
     */

    public void saveFile(String filePath) throws Exception{
        saveFile("", new File(filePath));
    }

    /**
     * Upload specified file on given path in storage.
     * @param path Path where files are uploaded.
     * @param files Files which will be uploaded.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void saveFiles(String path, List<File> files) throws Exception{
        if(files == null)
            throw new StorageException("Files cannot be null!");
        if(files.isEmpty())
            throw new StorageException("Files cannot be empty!");
        for(File f: files){
            saveFile(path, f);
        }
    }

    /**
     * Delete file on specified path in storage.
     * @param path Path to file in storage.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract void deleteFile(String path) throws Exception; //Admin, Administrator

    /**
     * Delete files on specified paths in storage.
     * @param paths Paths to files in storage.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void deleteFiles(List<String> paths) throws Exception{
        if(paths == null)
            throw new StorageException("Files cannot be null!");
        if(paths.isEmpty())
            throw new StorageException("Files cannot be empty!");
        for(String f: paths){
            deleteFile(f);
        }
    }
    /**
     * Delete files on specified paths in storage.
     * @param paths Paths to files in storage.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void deleteFiles(String[] paths) throws Exception{
        if(paths == null)
            throw new StorageException("Files cannot be null!");
        if(paths.length == 0)
            throw new StorageException("Files cannot be empty!");
        for(String f: paths){
            deleteFile(f);
        }
    }

    /**
     * View all files in directory and his subdirectories on given path.
     * @param path Path to directory in storage.
     * @return Object MyFile with data of files in it.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract List<MyFile> viewFiles(String path) throws Exception;  //Admin, Administrator, StandardUser, Viewer


    /**
     * View all file names in directory with given path.
     * @param path Path to directory.
     * @return List of Strings which contain names of files.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getAllFileNamesInDirectory(String path) throws Exception{
        List<MyFile> myFiles = viewFiles(path);
        String[] folders = path.split("/");
        String folderName = path.equals("")? "/" : folders[folders.length-1];
        System.out.println("MYFILES SIZE: " + myFiles.size());
        List<String> names = new ArrayList<>();
        for(MyFile f: myFiles){
            System.out.println("FILE NAME: " + f.getName());
            System.out.println("FILE PARENT: " + f.getParent());
            if(!f.isDirectory() && f.getParent().equals(folderName))
                names.add(f.getName());
        }
        System.out.println("NAMES SIZE:" + names.size());
        return names;
    }

    /**
     * View all directory names in directory with given path.
     * @param path  Path to directory.
     * @return List of Strings which contain names of directories.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getAllDirectoryNamesInDirectory(String path) throws Exception{
        List<MyFile> myFiles = viewFiles(path);
        String[] folders = path.split("/");
        String folderName = path.equals("")? "/" : folders[folders.length-1];

        List<String> names = new ArrayList<>();
        for(MyFile f: myFiles){
            if(f.isDirectory() && f.getParent().equals(folderName))
                names.add(f.getName());
        }
        return names;
    }

    /**
     * View all files in storage with specified extension.
     * @param extension Extension name.
     * @return List of Strings which contain name of all files with given extension.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getAllFilesInStorageByExtension(String extension) throws Exception{
        List<MyFile> myFiles = viewFiles("");
        List<String> fileNames = new ArrayList<>();

        for(MyFile f: myFiles){
            if(FilenameUtils.getExtension(f.getName()).toLowerCase().equals(extension.toLowerCase())){
                fileNames.add(f.getName());
            }
        }

        return fileNames;
    }

    /**
     * View all files in directory with given path with specified extension.
     * @param path Path to directory.
     * @param extension Extension name.
     * @return List of String which contain file names with given extension.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getAllFilesInDirectoriesAndSubdirectoriesByExtension(String path, String extension) throws Exception{
        List<MyFile> myFiles = viewFiles(path);
        List<String> fileNames = new ArrayList<>();

        for(MyFile f: myFiles){
            if(FilenameUtils.getExtension(f.getName()).toLowerCase().equals(extension.toLowerCase())){
                fileNames.add(f.getName());
            }
        }

        return fileNames;
    }

    /**
     * View all files in directory with given path sorted by name.
     * @param path Path to directory.
     * @param isAscending Ascending or descending option for sorting.
     * @return List of Strings which contain sorted file names.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getAllFilesByName(String path, boolean isAscending) throws Exception{
        List<MyFile> myFiles = viewFiles(path);
        List<String> files = new ArrayList<>();

        for(MyFile f: myFiles){
            files.add(f.getName());
        }
        if(isAscending)
            Collections.sort(files);
        else
            Collections.sort(files, Collections.reverseOrder());
        return files;
    }

    /**
     * View all files in directory with given path sorted by date created.
     * @param path Path to directory
     * @param isAscending Ascending or descending option for sorting.
     * @return List of Strings which contain sorted files by date created.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getAllFilesByDateCreated(String path, boolean isAscending) throws Exception{
        List<MyFile> myFiles = viewFiles(path);
        List<String> files = new ArrayList<>();

        Collections.sort(myFiles, new Comparator<MyFile>() {
            @Override
            public int compare(MyFile o1, MyFile o2) {
                if(isAscending)
                    return o1.getDateCreated().compareTo(o2.getDateCreated());
                return o2.getDateCreated().compareTo(o1.getDateCreated());
            }
        });


        for(MyFile f: myFiles){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            files.add(f.getName() + " " + sdf.format(f.getDateCreated()));
        }

        return files;
    }

    /**
     * View all files in directory with given path sorted by date modified.
     * @param path Path to directory.
     * @param isAscending Ascending or descending option for sorting.
     * @return List of Strings which contain sorted files by date modified.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getAllFilesByDateModified(String path, boolean isAscending) throws Exception{
        List<MyFile> myFiles = viewFiles(path);
        List<String> files = new ArrayList<>();

        Collections.sort(myFiles, new Comparator<MyFile>() {
            @Override
            public int compare(MyFile o1, MyFile o2) {
                if(isAscending)
                    return o1.getDateModified().compareTo(o2.getDateModified());
                return o2.getDateModified().compareTo(o1.getDateModified());
            }
        });


        for(MyFile f: myFiles){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            files.add(f.getName() + " " + sdf.format(f.getDateModified()));
        }

        return files;
    }

    /**
     * View all files in directory with given path where created date is between two given dates.
     * @param path Path to directory.
     * @param startDate Start date.
     * @param endDate End date.
     * @return List of Strings which contain files where date created is between startDate and endDate.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getFilesByDateCreatedBetweenDates(String path, String startDate, String endDate) throws  Exception {
        List<MyFile> myFiles = viewFiles(path);
        List<String> files = new ArrayList<>();


       Date sDate = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
       Date eDate = new SimpleDateFormat("dd/MM/yyyy").parse(endDate);

        myFiles = myFiles
                .stream()
                .filter(f -> f.getDateCreated().after(sDate) && f.getDateCreated().before(eDate))
                .collect(Collectors.toList());

        for(MyFile f: myFiles){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            files.add(f.getName() + " " + sdf.format(f.getDateCreated()));
        }

        return files;
    }
    /**
     * View all files in directory with given path where modified date is between two given dates.
     * @param path Path to directory.
     * @param startDate Start date.
     * @param endDate End date.
     * @return List of Strings which contain files where date modified is between startDate and endDate.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public List<String> getFilesByDateModifiedBetweenDates(String path, String startDate, String endDate) throws  Exception {
        List<MyFile> myFiles = viewFiles(path);
        List<String> files = new ArrayList<>();


        Date sDate = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
        Date eDate = new SimpleDateFormat("dd/MM/yyyy").parse(endDate);

        myFiles = myFiles
                .stream()
                .filter(f -> f.getDateModified().after(sDate) && f.getDateModified().before(eDate))
                .collect(Collectors.toList());

        for(MyFile f: myFiles){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            files.add(f.getName() + " " + sdf.format(f.getDateModified()));
        }

        return files;
    }
    /**
     * Copies file from one location to another location.
     * @param fromPath Path from which the file is copied.
     * @param toPath Path to which the file is copied.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract void copyFile(String fromPath, String toPath) throws Exception;

    /**
     * Copies multiple files from one location to another location.
     * @param fromPaths Paths from which the files are copied.
     * @param toPaths Paths to which the files are copied.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void copyFiles(List<String> fromPaths, List<String> toPaths) throws Exception{
        if(fromPaths == null){
            throw new StorageException("From paths cannot be null!");
        }
        if(toPaths == null){
            throw new StorageException("From paths cannot be null!");
        }
        if(fromPaths.isEmpty()){
            throw new StorageException("From paths cannot be empty!");
        }
        if(toPaths.isEmpty()){
            throw new StorageException("From paths cannot be empty!");
        }
        for(String f: fromPaths){

            for(String t: toPaths){
                copyFile(f, t);
            }
        }

        return;
    }

    /**
     * Downloads file from given path.
     * @param path Path where file is located.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract void downloadFile(String path) throws Exception; //Admin, Administrator, StandardUser,

    /**
     * Moves file from one location to another location.
     * @param fromPath Path from which the folder will be moved.
     * @param toPath Path to which the folder will be moved.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public abstract void moveFile(String fromPath, String toPath) throws Exception;

    /**
     * Moves files from one location to another location.
     * @param fromPaths Paths from which the folders will be moved.
     * @param toPaths Paths to which the folders will be moved.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void moveFiles(List<String> fromPaths, List<String> toPaths) throws Exception{
        if(fromPaths == null){
            throw new StorageException("From paths cannot be null!");
        }
        if(toPaths == null){
            throw new StorageException("From paths cannot be null!");
        }
        if(fromPaths.isEmpty()){
            throw new StorageException("From paths cannot be empty!");
        }
        if(toPaths.isEmpty()){
            throw new StorageException("From paths cannot be empty!");
        }
        for(String f: fromPaths){

            for(String t: toPaths){
                moveFile(f, t);
            }
        }

        return;
    }
    /**
     * Creates new user.
     * @param username Username that new user will get.
     * @param password Password that new user will get.
     * @param privilege Privilege that new user will get.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void createUser(String username, String password, Privilege privilege) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected");
        }
        if(username == null){
            throw new StorageException("Username cannot be null!");
        }
        if(password == null){
            throw new StorageException("Password cannot be null!");
        }
        if(privilege == null){
            throw new StorageException("Privilege cannot be null!");
        }
        User user = getUsersSettings().getCurrentUser();
        if(user.getPrivilege() != Privilege.Admin){
            throw new StorageException("You don't have privilege for this action!");
        }
        if(privilege == Privilege.Admin){
            throw new StorageException("You can't add another admin!");
        }
        getUsersSettings().addUser(new User(username, password, privilege));
    }
    /**
     * Adds users privilege for certain folder.
     * @param username Username of user whose privilege is being added.
     * @param path Path where folder is located.
     * @param privilege Privilege that the user will get.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void addUserPrivilegeForFolder(String username, String path, Privilege privilege) throws  Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(username == null){
            throw new StorageException("Username cannot be null!");
        }
        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(privilege == null){
            throw new StorageException("Privilege cannot be null!");
        }

        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }
        User user = getUsersSettings().getUserByUsername(username);
        if(user == null){
            throw new StorageException("User with given username does not exist!");
        }

        viewFiles(path);

        user.addFolderPrivilege(path, privilege);

    }
    /**
     * Removes users privilege for certain folder.
     * @param folderName Name of the folder.
     * @param username Username of user whose privilege is being removed.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void removeUserPrivilegeForFolder(String username, String folderName) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }
        User user = getUsersSettings().getUserByUsername(username);
        if(user == null){
            throw new StorageException("User with given username does not exist!");
        }



        user.removeFolderPrivilege(folderName);

    }
    /**
     * Changes users privilege.
     * @param username Username of the user whose privilege is being changed.
     * @param privilege New privilege of the user.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void changeUserPrivilege(String username, Privilege privilege) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }

        User user = getUsersSettings().getUserByUsername(username);
        if(user == null){
            throw new StorageException("User with given username does not exist!");
        }

        user.setPrivilege(privilege);
    }
    /**
     * Sets storage size.
     * @param size Size of storage.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void setStorageSize(long size) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }
        if(size == 0){
            throw new StorageException("Cannot set storage size to 0.");
        }

        if(!getConfigSettings().setStrgSize(size)){
            throw new StorageException("Current size of storage is bigger than given size!");
        }
    }
    /**
     * Adding extensions that are not supported.
     * @param extension Extension that will not be supported.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void addNotSupportedExtension(String extension) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(extension == null){
            throw new StorageException("Extension cannot be null!!");

        }
        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }
        getConfigSettings().addNotSupportedExtension(extension);
    }
    /**
     * Removing extensions that are not supported.
     * @param extension Extension that will not be supported.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void removeNotSupportedExtension(String extension) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(extension == null){
            throw new StorageException("Extension cannot be null!!");

        }
        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }
        getConfigSettings().removeNotSupportedExtension(extension);
    }
    /**
     * Adding maximum number of files in a directory.
     * @param path Path to directory.
     * @param number Number of maximum files.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void addMaxNumberFilesInDirectory(String path, int number) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }
        viewFiles(path);

        getConfigSettings().addMaxNumberForDirectory(number, path);

    }
    /**
     * Removing maximum number of files in a directory.
     * @param path Path to directory.
     * @throws Exception If a Storage or some other exception occurred.
     */
    public void removeMaxNumberFilesInDirectory(String path) throws Exception{
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        if(getUsersSettings().getCurrentUser().getPrivilege() != Privilege.Admin){
            throw new StorageException("Only admin can do this operation!");
        }
        viewFiles(path);

        getConfigSettings().removeMaxNumberForDirectory( path);

    }


    /**
     * Gets users settings.
     * @return users settings.
     */
    public UsersSettings getUsersSettings() {
        return usersSettings;
    }

    /**
     * Sets users settings.
     * @param usersSettings
     */
    public void setUsersSettings(UsersSettings usersSettings) {
        this.usersSettings = usersSettings;
    }

    /**
     * Sets rootpath.
     * @param rootPath
     */
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Gets root path.
     * @return root path.
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Gets configuration settings.
     * @return configuration settings.
     */
    public ConfigSettings getConfigSettings() {
        return configSettings;
    }

    /**
     * Sets configuration settings.
     * @param configSettings
     */
    public void setConfigSettings(ConfigSettings configSettings) {
        this.configSettings = configSettings;
    }
}


