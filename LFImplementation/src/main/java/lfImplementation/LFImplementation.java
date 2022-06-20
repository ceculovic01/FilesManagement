package lfImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import storageSpec.ExporterManager;
import storageSpec.exceptions.StorageException;
import storageSpec.fileConfig.ConfigSettings;
import storageSpec.fileRepresentation.MyFile;
import storageSpec.StorageExporter;
import storageSpec.users.Privilege;
import storageSpec.users.User;
import storageSpec.users.UsersSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class LFImplementation extends StorageExporter {

    static{
        ExporterManager.registerExporter(new LFImplementation());
    }

    private LFImplementation() {

    }

    // proverava se da li postoji users.json u zadatom folderu, ako postoji izbaci error,
    // ako ne postoji onda pravimo users.json sa userom kog pravim sa zadatim parametrima i dajem mu admina i dodajem u listu usera i setujem currUser i rootPath na zadati path
    @Override
    public void initStorage(String username, String password, String path, String fileName) throws Exception {
        String extension = FilenameUtils.getExtension(fileName);
        if(extension != ""){
            throw new StorageException("fileName is not folder!");
        }
        if(username.isEmpty() || username == null){
            throw new StorageException("Username is missing!");
        }
        if(password.isEmpty() || password == null){
            throw new StorageException("Password is missing!");
        }
        if(path == null){
            throw new StorageException("Path is null!");
        }
        if(fileName == null){
            throw new StorageException("fileName is null!");
        }
        String str = path + "/" + fileName;
        Path p = Paths.get(str);
        Files.createDirectories(p); // pravim folder u koji dodajem users.json
        File f = new File(path + "/" + fileName + "/users.json");
        ObjectMapper objectMapper = new ObjectMapper();
        if(f.exists()){ // ukoliko vec postoji users.json
            throw new StorageException("Storage already exists!");
        }else{ // ukoliko ja pravim users.json, pravim user-a sa prosledjenim parametrima i dajem mu admina i stavljam da je on currentUser
            User user = new User(username, password, Privilege.Admin);
            UsersSettings usersSettings = new UsersSettings();
            usersSettings.addUser(user);
            usersSettings.setCurrentUser(user);
            setUsersSettings(usersSettings);
            objectMapper.writeValue(f, usersSettings); // u users.json upisujem admina
        }
        setRootPath(path + "/" + fileName); // postavljam folder u kom se nalazi users.json na rootPath
        ConfigSettings configSettings = new ConfigSettings();
        configSettings.setCurrentStorageSize(0);
        File configJson = new File(getRootPath() + "/config.json");
        objectMapper.writeValue(configJson, configSettings);
        setConfigSettings(configSettings);
    }

    @Override
    public void connect(String username, String password, String path) throws Exception {
        if(path == null || path.isEmpty()){
            throw new StorageException("Path is missing!");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        File f = new File(path + "/users.json");
        if(!f.exists()) {
            throw new StorageException("users.json does not exists!");
        }

        File f2 = new File(path + "/config.json");
        if(!f2.exists()) {
            throw new StorageException("config.json does not exists!");
        }

        UsersSettings usersSettings = null;
        usersSettings = objectMapper.readValue(f, UsersSettings.class); // kad se neko konektuje ucitavamo sve iz users.json, kao i currentUser-a koji bi trebalo da je null

        ConfigSettings configSettings = objectMapper.readValue(f2, ConfigSettings.class);

        if(usersSettings == null){ // ovo znaci da je users.json prazan i zato ne mozemo da konektujemo nikoga
            throw new StorageException("UsersSettings is null!");
        }

        User user = usersSettings.getUserByCredentials(username, password);
        if(user == null){ // proveravamo da li postoji user sa poslatim kredencijalima
            throw new StorageException("User does not exists!");
        }
        if(usersSettings.getCurrentUser() != null && !user.equals(usersSettings.getCurrentUser())){ // ako currentUser nije null znaci da se neko nije diskonektovao
            throw new StorageException("Another user is already connected!");
        }

        usersSettings.setCurrentUser(user);
        setUsersSettings(usersSettings);
        setConfigSettings(configSettings);
        setRootPath(path);
        objectMapper = new ObjectMapper();
        objectMapper.writeValue(f, usersSettings);
    }

    @Override
    public void disconnect() throws Exception {
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        getUsersSettings().setCurrentUser(null); // kad se neko diskonektuje, stavljamo currentUser-a na null da bi u users.json upisao da je null, jer ce u connect-u to da procita
        ObjectMapper objectMapper = new ObjectMapper();
        File f = new File(getRootPath() + "/users.json");
        File f2 = new File(getRootPath() + "/config.json");
        objectMapper.writeValue(f, getUsersSettings()); // kad se neko diskonektuje upisujemo ga u users.json
        objectMapper.writeValue(f2, getConfigSettings());
        setUsersSettings(null);
        setRootPath(null);
        setConfigSettings(null);
    }

    @Override
    public void createFile(String path, String fileName) throws Exception {
        String initPath = path;

        if(path == null){
            throw new StorageException("Path can't be null!");
        }
        if(fileName == null) {
            throw new StorageException("FileName can't be null!");
        }
        if(fileName.isEmpty()){
            throw new StorageException("FileName is missing!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }
        String defaultPath;
        String extension = FilenameUtils.getExtension(fileName);
        if(!getConfigSettings().isExtensionSupported(extension))
            throw new StorageException(extension.toUpperCase() + " is not supported!");
        String badPath = checkNumberOfFiles(initPath, 1);
        if(!badPath.isEmpty()){ // proveravam broj dozvoljenih fajlova, a ne proveravam velicinu
            throw new StorageException("Maximum number of files on path " + badPath + " is exceeded!");
        }
        if(path.isEmpty()){ // pravimo fajl ili folder na rootPath-u
             defaultPath = getRootPath() + "/" + fileName;
        }else{ // ukoliko je zadat path
            String s = getRootPath() + "/" + path;
            Path p = Paths.get(s);
            Files.createDirectories(p); // prvo pravim folder gde cu da napravim fileName
            defaultPath = getRootPath() + "/" + path + "/" + fileName;
        }
        Path p = Paths.get(defaultPath);
        File f = new File(p.toString());
        if(f.exists()){
            throw new StorageException("File already exists!");
        }
        Files.createFile(p);
    }

    @Override
    public void createDirectory(String path, String fileName) throws Exception {
        if(path == null){
            throw new StorageException("Path can't be null!");
        }
        if(fileName == null) {
            throw new StorageException("FileName can't be null!");
        }
        if(fileName.isEmpty()){
            throw new StorageException("FileName is missing!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }
        String defaultPath;
        if(path.isEmpty()){ // pravimo folder na rootPath-u
            defaultPath = getRootPath() + "/" + fileName;
        }else{ // ukoliko je zadat path
            String s = getRootPath() + "/" + path;
            Path p = Paths.get(s);
            Files.createDirectories(p); // prvo pravim folder gde cu da napravim fileName
            defaultPath = getRootPath() + "/" + path + "/" + fileName;
        }
        Path p = Paths.get(defaultPath);
        Files.createDirectories(p);
    }

    @Override
    public void deleteFile(String path) throws Exception {
        if(path == null){
            throw new StorageException("Path can't be null!");
        }
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        Privilege folderPrivilege = getPrivilegeByPath(path);
        User user = getUsersSettings().getCurrentUser();
        if(folderPrivilege == null && (user.getPrivilege() == Privilege.Viewer || user.getPrivilege() == Privilege.StandardUser)){
            throw new StorageException("No permission for deleting files!");
        }
        if(folderPrivilege == Privilege.Viewer || folderPrivilege == Privilege.StandardUser){
            throw new StorageException("No permission for deleting files!");
        }
        String initPath = path;
        if(path.isEmpty()){
            path = getRootPath();
        }else{
            path = getRootPath() + "/" + path;
        }
        File f = new File(path);
        if(!f.exists()){
            throw new StorageException("Bad path!");
        }
        long size = getFileSize(f);
        getConfigSettings().setCurrentStorageSize(getConfigSettings().getCurrentStorageSize() - size);

        if(f.isDirectory()){
            FileUtils.deleteDirectory(f);
        }else{
            f.delete();
        }

    }

    private boolean isFolder(Path file){
        boolean check = true;
        File f = new File(file.toAbsolutePath().toString());
        if(f.isDirectory())
            check = true;
        else if(f.isFile())
            check = false;
        return check;
    }

    private int getNumberOfFiles(java.io.File file){
        if(!file.isDirectory()){
            return 1;
        }
        int size = 0;
        for (java.io.File f : file.listFiles()) {
            if (f.isFile())
                size += 1;
            else
                size += getNumberOfFiles(f);
        }

        return size;
    }

    private long getFileSize(java.io.File file){
        if(!file.isDirectory()){
            return file.length();
        }
        long length = 0;
        for (java.io.File f : file.listFiles()) {
            if (f.isFile())
                length += f.length();
            else
                length += getFileSize(f);
        }

        return length;
    }

    @Override
    public List<MyFile> viewFiles(String path) throws Exception { // ispisuje fajlove i foldere iz prosledjenog direktorijuma
        String initPath = path;

        if(path == null){
            throw new StorageException("Path can't be null!");
        }
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        String extension = FilenameUtils.getExtension(path);
        if(extension != ""){
            throw new StorageException("Path must be to folder!");
        }
        if(path.isEmpty()){
            path = getRootPath();
        }else{
            path = getRootPath() + "/" + path;
        }
        Path dir = Paths.get(path);

        List<MyFile> files = new ArrayList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path file : stream) {
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

            if(isFolder(file)){ // kad naidjemo na folder ispisujemo sve iz njega i tako dok ne dodjemo do poslednjeg foldera
                MyFile myFile = new MyFile();
                myFile.setName(file.getFileName().toString());
                myFile.setDirectory(true);
                myFile.setDateCreated(new Date(attr.creationTime().toMillis()));
                myFile.setDateModified(new Date(attr.lastModifiedTime().toMillis()));
                myFile.setParent(initPath.equals("")? "/" : file.getParent().getFileName().toString());
                myFile.setPath(initPath + (initPath.equals("") ? "" : "/") + myFile.getName()); // putanja bez rootPath-a
                files.add(myFile);
                List<MyFile> subFiles = viewFiles(initPath + (initPath.equals("") ? "" : "/") + myFile.getName());
                files.addAll(subFiles);
            }else {
                MyFile myFile = new MyFile();
                myFile.setName(file.getFileName().toString());
                myFile.setDirectory(false);
                myFile.setDateCreated(new Date(attr.creationTime().toMillis()));
                myFile.setDateModified(new Date(attr.lastModifiedTime().toMillis()));
                myFile.setParent(initPath.equals("")? "/" : file.getParent().getFileName().toString());
                myFile.setPath(initPath + (initPath.equals("") ? "" : "/") + myFile.getName());

                files.add(myFile);
            }
        }
        return files;
    }

    @Override
    public void moveFile(String currPath, String newPath) throws Exception { // premestanje fajlova iz jednog direktorijuma u drugi
        if(currPath == null){
            throw new StorageException("Current path can't be null!");
        }
        if(newPath == null){
            throw new StorageException("New path can't be null!");
        }
        String initCurrPath = currPath;
        String initNewPath = newPath;
        if(currPath.isEmpty()){
            currPath = getRootPath();
        }else{
            currPath = getRootPath() + "/" + currPath;
        }
        if(newPath.isEmpty()){
            newPath = getRootPath();
        }else{
            newPath = getRootPath() + "/" + newPath;
        }
        File f = new File(currPath);
        int numFiles = getNumberOfFiles(f);
        String badPath = checkNumberOfFiles(initNewPath, numFiles);
        if(!badPath.isEmpty()){ // proveravam broj dozvoljenih fajlova, a ne proveravam velicinu
            throw new StorageException("Maximum number of files on path " + badPath + " is exceeded!");
        }
        f.renameTo(new File(newPath + "/" + f.getName()));
    }

    @Override
    public void copyFile(String fromPath, String toPath) throws Exception { // smestanje fajlova na odredjenu putanju
        if(fromPath == null){
            throw new StorageException("From path can't be null!");
        }
        if(toPath == null){
            throw new StorageException("To path can't be null!");
        }
        String initFromPath = fromPath;
        String initToPath = toPath;
        if(fromPath.isEmpty()){
            fromPath = getRootPath();
        }else{
            fromPath = getRootPath() + "/" + fromPath;
        }
        if(toPath.isEmpty()){
            toPath = getRootPath();
        }else{
            toPath = getRootPath() + "/" + toPath;
        }
        File fromFile = new File(fromPath);
        File toFile = new File(toPath + "/" + fromFile.getName());
        long size = getFileSize(fromFile);
        long currSize = getConfigSettings().getCurrentStorageSize();
        long maxSize = getConfigSettings().getStorageSize();

        if((size + currSize > maxSize) && (maxSize != 0)){
            throw new StorageException("Maximum storage size is surprassed!");
        }

        int numFiles = getNumberOfFiles(fromFile);
        String badPath = checkNumberOfFiles(initToPath, numFiles);
        if(!badPath.isEmpty()){ // proveravam broj dozvoljenih fajlova, a ne proveravam velicinu
            throw new StorageException("Maximum number of files on path " + badPath + " is exceeded!");
        }
        getConfigSettings().setCurrentStorageSize(currSize + size);
        if (!toFile.exists()) { // proveravamo da li vec postoji fajl koji se salje na zadatoj putanji
            if (isFolder(Paths.get(fromFile.getAbsolutePath())))
                FileUtils.copyDirectory(fromFile, toFile);
            else
                Files.copy(fromFile.toPath(), toFile.toPath());
        }
    }

    @Override
    public void saveFile(String path, File file) throws Exception {
        if(path == null){
            throw new StorageException("Path can't be null!");
        }
        if(file == null){
            throw new StorageException("File can't be null!");
        }
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        String initPath = path;
        if(path.isEmpty()){
            path = getRootPath();
        }else{
            path = getRootPath() + "/" + path;
        }
        Privilege folderPrivilege = getPrivilegeByPath(initPath);
        User user = getUsersSettings().getCurrentUser();
        if(folderPrivilege == null && user.getPrivilege() == Privilege.Viewer){
            throw new StorageException("No permission for saving files!");
        }
        if(folderPrivilege == Privilege.Viewer){
            throw new StorageException("No permission for saving files!");
        }
        long size = getFileSize(file);
        long currSize = getConfigSettings().getCurrentStorageSize();
        long maxSize = getConfigSettings().getStorageSize();
        String extension = FilenameUtils.getExtension(file.getName());

        if(!getConfigSettings().isExtensionSupported(extension)){
            throw new StorageException(extension.toUpperCase() + " is not supported!");
        }
        if((size + currSize > maxSize) && (maxSize != 0)){
            throw new StorageException("Maximum storage size is surprassed!");
        }
        int numFiles = getNumberOfFiles(file);
        String badPath = checkNumberOfFiles(initPath, numFiles);
        if(!badPath.isEmpty()){ // proveravam broj dozvoljenih fajlova, a ne proveravam velicinu
            throw new StorageException("Maximum number of files on path " + badPath + " is exceeded!");
        }
        getConfigSettings().setCurrentStorageSize(currSize + size);
        if(!file.exists()){ // ako nemamo fajl ili folder na lokalnom skladistu nego ga sami pravimo
            Path p = Paths.get(path + "/" + file.getName());
            if(FilenameUtils.getExtension(file.getName()).equals(""))
                Files.createDirectories(p);
            else
                Files.createFile(p);
        }else {
            File newFile = new File(path + "/" + file.getName());
            if (!newFile.exists()) {
                if(isFolder(Paths.get(file.getAbsolutePath()))){
                    FileUtils.copyDirectory(file, newFile); // kopira folder koji vec postoji i sve sto ima u njemu
                }else {
                    Files.copy(file.toPath(), newFile.toPath()); // kopira fajl koji vec postoji i njegov sadrzaj
                }
            }
        }
    }

    @Override
    public void downloadFile(String path) throws Exception { // uzimam fajl i smestam u default folder downloads u projektu
        if(getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }
        if(path == null){
            throw new StorageException("Path can't be null!");
        }
        String initPath = path;
        if(path.isEmpty()){
            path = getRootPath();
        }else{
            path = getRootPath() + "/" + path;
        }
        Privilege folderPrivilege = getPrivilegeByPath(initPath);
        User user = getUsersSettings().getCurrentUser();
        if(folderPrivilege == null && user.getPrivilege() == Privilege.Viewer){
            throw new StorageException("No permission for downloading files!");
        }
        if(folderPrivilege == Privilege.Viewer){
            throw new StorageException("No permission for downloading files!");
        }
        Path p = Paths.get(System.getProperty("user.dir") + "/downloads");
        Files.createDirectories(p);
        File file = new File(path);
        File newFile = new File(System.getProperty("user.dir") + "/downloads/" + file.getName());
        if (!newFile.exists())
            if(isFolder(Paths.get(file.getAbsolutePath())))
                FileUtils.copyDirectory(file, newFile); // kopiram folder
            else
                Files.copy(file.toPath(), newFile.toPath()); // kopiram fajl
    }

    private String checkNumberOfFiles(String path, int num) throws Exception{
        List<String> allPaths = getConfigSettings().getAllPaths(path);
        for(String s: allPaths){
            int numFiles = getNumFilesInDirectory(s);
            if(getConfigSettings().isExceeded(s, numFiles + num)){
                return s;
            }
        }
        return "";
    }

    private int getNumFilesInDirectory(String path) throws Exception{
        String initPath = path;

        if(path == null){
            throw new StorageException("Path can't be null!");
        }
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }
        String extension = FilenameUtils.getExtension(path);
        if(extension != ""){
            throw new StorageException("Path must be to folder!");
        }
        if(path.isEmpty()){
            path = getRootPath();
        }else{
            path = getRootPath() + "/" + path;
        }
        Path dir = Paths.get(path);

        List<MyFile> files = new ArrayList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path file : stream) {
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

            if(isFolder(file)){ // kad naidjemo na folder ispisujemo sve iz njega i tako dok ne dodjemo do poslednjeg foldera
                MyFile myFile = new MyFile();
                myFile.setName(file.getFileName().toString());
                myFile.setDirectory(true);
                myFile.setDateCreated(new Date(attr.creationTime().toMillis()));
                myFile.setDateModified(new Date(attr.lastModifiedTime().toMillis()));
                myFile.setParent(file.getParent().getFileName().toString());
                myFile.setPath(initPath + (initPath.equals("") ? "" : "/") + myFile.getName()); // putanja bez rootPath-a
                files.add(myFile);
                List<MyFile> subFiles = viewFiles(initPath + (initPath.equals("") ? "" : "/") + myFile.getName());
                files.addAll(subFiles);
            }else {
                MyFile myFile = new MyFile();
                myFile.setName(file.getFileName().toString());
                myFile.setDirectory(false);
                myFile.setDateCreated(new Date(attr.creationTime().toMillis()));
                myFile.setDateModified(new Date(attr.lastModifiedTime().toMillis()));
                myFile.setParent(file.getParent().getFileName().toString());
                myFile.setPath(initPath + (initPath.equals("") ? "" : "/") + myFile.getName());

                files.add(myFile);
            }
        }
        List<MyFile> myFiles = new ArrayList<>();
        for(MyFile f: files){
            if(!f.isDirectory()){
                myFiles.add(f);
            }
        }
        return myFiles.size();
    }

    private Privilege getPrivilegeByPath(String path){
        return getUsersSettings().getCurrentUser().containsPath(path);
    }
}
