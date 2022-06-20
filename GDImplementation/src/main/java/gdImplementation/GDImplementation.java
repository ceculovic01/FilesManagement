package gdImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import storageSpec.exceptions.StorageException;
import storageSpec.ExporterManager;
import storageSpec.fileConfig.ConfigSettings;
import storageSpec.fileRepresentation.MyFile;
import storageSpec.StorageExporter;
import storageSpec.users.Privilege;
import storageSpec.users.User;
import storageSpec.users.UsersSettings;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

public class GDImplementation extends StorageExporter {
    private static final String APPLICATION_NAME = "SK PROJEKAT";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";


    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";



    private static NetHttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            ExporterManager.registerExporter(new GDImplementation());
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public GDImplementation(){
        super();
    }



    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GDImplementation.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));



        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static Drive getDriveService() throws IOException {
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }



    private List<File> getFileByName(String name, Drive service) throws Exception{

        String query = "visibility = 'limited' and trashed = false and name = '" + name + "'";
        List<File> result = new ArrayList<>();
        Drive.Files.List request = service.files().list().setFields("files(id,name,parents,mimeType,size)").setQ(query);

        do {
            try {
                FileList files = request.execute();

                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }
    private File getFileById(String fileId, Drive service){


        File file = null;
        try {
            file = service.files().get(fileId).setFields("id,name,parents,mimeType,size").execute();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private List<File> checkPath(String path, Drive service) throws  Exception{
        String[] paths = path.split("/");
        List<File> files = getFileByName(paths[paths.length-1], service);

        List<File> result = new ArrayList<>();
        for(File file : files){

            String parentId = file.getParents() != null? file.getParents().get(0) : null;
            int count = paths.length-2;
            while(parentId != null && count >= 0){
                File parent = getFileById(parentId, service);
                if(parent.getName().equals(paths[count])){
                    count--;
                    parentId = parent.getParents() != null ? parent.getParents().get(0) : null;
                }else{
                    parentId = null;
                }
            }
            if(count == -1){
                result.add(file);
            }

        }
        return result;
    }

    @Override
    public void initStorage(String username , String password, String path, String fileName) throws Exception{


            if(username == null){
                throw new StorageException("Username cannot be null!");

            }
            if(password == null){
                throw new StorageException("Password cannot be null.");
            }
            if(path == null){
                throw new StorageException("Path cannot be null.");
            }
            if(fileName == null){
                throw new StorageException("FileName cannot be null.");
            }
            if(username.isEmpty()){
                throw new StorageException("Username cannot be empty");
            }
            if(password.isEmpty()){
                throw new StorageException("Password cannot be empty");
            }


            Drive service = getDriveService();

            String symbol = "/";
            if(path.isEmpty()){
                symbol = "";
            }

            List<File> folder = checkPath(path + symbol + fileName, service);
            if(!folder.isEmpty()){
                List<File> usersFile = checkPath(path + symbol + fileName + "/" + "users.json", service);
                if(!usersFile.isEmpty()){
                    throw new StorageException("Storage already exists!");
                }
            }


            List<File> files = checkPath(path, service);

            if(files.isEmpty() && !path.isEmpty()){
                throw new StorageException("Path does not exist!");
            }

            File destFolder = null;


            if(path.isEmpty()){
                File fMetaData = new File();
                fMetaData.setName(fileName);
                fMetaData.setMimeType("application/vnd.google-apps.folder");
                destFolder = service.files().create(fMetaData).setFields("id,name").execute();

            }else {
                for (File f : files) {
                    File fMetaData = new File();
                    fMetaData.setName(fileName);
                    fMetaData.setParents(Collections.singletonList(f.getId()));
                    fMetaData.setMimeType("application/vnd.google-apps.folder");
                    destFolder = service.files().create(fMetaData).setFields("id,name").execute();
                }
            }

            UsersSettings usersSettings = new UsersSettings();
            User admin = new User(username, password, Privilege.Admin);
            usersSettings.addUser(admin);
            usersSettings.setCurrentUser(admin);

            ObjectMapper objectMapper = new ObjectMapper();
            java.io.File jsonFile = new java.io.File("users.json");
            objectMapper.writeValue(jsonFile,usersSettings);
            File fileMetaData = new File();
            fileMetaData.setName("users.json");
            fileMetaData.setParents(Collections.singletonList(destFolder.getId()));
            FileContent fileContent = new FileContent("text/plain", jsonFile);
            service.files().create(fileMetaData, fileContent).setFields("id").execute();
            jsonFile.delete();

            ConfigSettings configSettings = new ConfigSettings();
            configSettings.setCurrentStorageSize(0);

            java.io.File configFile = new java.io.File("config.json");
            objectMapper.writeValue(configFile,configSettings);
            File configMetaData = new File();
            configMetaData.setName("config.json");
            configMetaData.setParents(Collections.singletonList(destFolder.getId()));
            FileContent configContent = new FileContent("text/plain", configFile);
            service.files().create(configMetaData, configContent).setFields("id").execute();
            configFile.delete();

            setRootPath(path + (path.isEmpty()? "" : "/") + fileName);
            setUsersSettings(usersSettings);
            setConfigSettings(configSettings);


        return;
    }

    @Override
    public void connect(String username, String password, String path) throws Exception {

        if(username == null){
            throw new StorageException("Username cannot be null!");
        }
        if(password == null){
            throw new StorageException("Password cannot be null!");
        }
        if(path == null){
            throw new StorageException("Path cannot be null!");
        }

        Drive service = getDriveService();
        List<File> usersSettings = checkPath(path + "/users.json", service);
        List<File> configSettings = checkPath(path + "/config.json", service);
        if(usersSettings.isEmpty()){
            throw new StorageException("Storage does not exist on specified path!");
        }

        File jsonFile = usersSettings.get(0);
        File configFile = configSettings.get(0);

        InputStream in =  service.files().get(jsonFile.getId()).executeMedia().getContent();
        String content = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        ObjectMapper objectMapper = new ObjectMapper();

        setUsersSettings(objectMapper.readValue(content, UsersSettings.class));

        in = service.files().get(configFile.getId()).executeMedia().getContent();
        content = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        setConfigSettings(objectMapper.readValue(content, ConfigSettings.class));

        User user = getUsersSettings().getUserByCredentials(username,password);
        if(user == null){
            throw new StorageException("Incorrect username or password!");
        }
        if(getUsersSettings().getCurrentUser() != null && !user.equals(getUsersSettings().getCurrentUser())){
            throw new StorageException("Another user is already connected!");
        }
        getUsersSettings().setCurrentUser(user);
        setRootPath(path);

        updateUsersSettings(service);


    }

    @Override
    public void disconnect() throws Exception {
        if(getUsersSettings() == null || getRootPath() == null){
            throw new StorageException("Not connected!");
        }

        Drive service = getDriveService();

        getUsersSettings().setCurrentUser(null);

        updateUsersSettings(service);
        updateConfigSettings(service);

        setRootPath(null);
        setUsersSettings(null);
        setConfigSettings(null);

        return;

    }
    private void createFile(String name, boolean isDirectory, String parentId, Drive service) throws IOException, GeneralSecurityException{

        File fileMetadata = new File();
        fileMetadata.setName(name);
        if(isDirectory)
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
        else
            fileMetadata.setMimeType("text/plain");
        if(parentId != null)
            fileMetadata.setParents(Collections.singletonList(parentId));

        service.files().create(fileMetadata)
                .setFields("id")
                .execute();
    }

    @Override
    public void createFile(String path, String fileName) throws StorageException,Exception{

        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(fileName == null){
            throw new StorageException("FileName cannot be null!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }
        if(fileName.isEmpty()){
            throw new StorageException("File not named!");
        }

        String initPath = path;

        String symbol = "/";
        if(path.isEmpty()){
             symbol = "";
        }
        path = getRootPath() + symbol + path;


        Drive service = getDriveService();

        List<File> files = checkPath(path, service);

        if(files.isEmpty()){
            throw new StorageException("Bad path input!");
        }
        String extension = FilenameUtils.getExtension(fileName);

        if(!getConfigSettings().isExtensionSupported(extension)){
            throw new StorageException(extension.toUpperCase() + " is not supported!");
        }

        String badPath = checkNumberOfFiles(initPath,1, service);
        if(!badPath.isEmpty()){
            throw new StorageException("Maximum number of files on path " + badPath + " is exceeded!" );
        }

        for(File f: files){
            createFile(fileName, false, f.getId(), service);
        }



        return;

    }

    @Override
    public void createDirectory(String path, String fileName) throws Exception {
        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(fileName == null){
            throw new StorageException("FileName cannot be null!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }
        if(fileName.isEmpty()){
            throw new StorageException("File not named!");
        }

        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;


        Drive service = getDriveService();

        List<File> files = checkPath(path, service);

        if(files.isEmpty()){
            throw new StorageException("Bad path input!");
        }

        for(File f: files){
            createFile(fileName, true, f.getId(), service);
        }



        return;
    }

    @Override
    public void saveFile(String path, java.io.File file) throws Exception{

        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(file == null){
            throw new StorageException("File cannot be null!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }


        Privilege folderPrivilege = getPrivelegeByPath(path);

        if(folderPrivilege == null && getUsersSettings().getCurrentUser().getPrivilege() == Privilege.Viewer){
            throw new StorageException("You don't have privilege for this action!");
        }
        if(folderPrivilege == Privilege.Viewer){
            throw new StorageException("You don't have privelege for this action!");
        }

        String initPath = path;

        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;


        Drive service = getDriveService();


        List<File> files = checkPath(path, service);

        if(files.isEmpty()){
            throw new StorageException("Bad path!");
        }

        for(File f: files){

            File fileMetadata = new File();
            fileMetadata.setName(file.getName());
            fileMetadata.setParents(Collections.singletonList(f.getId()));

            String contentType = FilenameUtils.getExtension(file.getName());
            FileContent mediaContent = new FileContent(getFileContentType(contentType), file);

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
            String badPath = checkNumberOfFiles(initPath, numFiles, service);
            if(!badPath.isEmpty()){
                throw new StorageException("Maximum number of files on path " + badPath + " is surprassed!" );
            }
            getConfigSettings().setCurrentStorageSize(currSize + size);

            service.files().create(fileMetadata, mediaContent).setFields("id").execute();
        }



        return;
    }
    @Override
    public void deleteFile(String path) throws Exception{
        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }


        Privilege folderPrivilege = getPrivelegeByPath(path);

        if( folderPrivilege == null && !(getUsersSettings().getCurrentUser().getPrivilege() == Privilege.Admin ||  getUsersSettings().getCurrentUser().getPrivilege() == Privilege.Administrator)){
            throw new StorageException("You don't have privilege for this action!");
        }
        if(folderPrivilege == Privilege.StandardUser || folderPrivilege == Privilege.Viewer){
            throw new StorageException("You don't have privilege for this action!");
        }

        String initPath = path;

        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;


        Drive service = getDriveService();
        List<File> files = checkPath(path, service);

        if(files.isEmpty()){
            throw new StorageException("Bad path!");
        }

        for(File f: files){

            long size = getDriveFileSize(initPath, service);
            getConfigSettings().setCurrentStorageSize(getConfigSettings().getCurrentStorageSize() - size);


            service.files().delete(f.getId()).execute();

        }

        return;

    }

    private File getParentByParentsList(List<String> parents, Drive service){
            if(parents == null){
                return null;
            }
            if(parents.isEmpty()){
                return  null;
            }
            try{
                for(String p: parents){
                    return getFileById(p, service);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
    }

    private List<MyFile> viewFilesFromDirectory(String path, Drive service) throws Exception{

        String initPath = path;

        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;


        List<File> directories = checkPath(path, service);

        if(directories.isEmpty()){
            throw new StorageException("Bad path!");
        }

        List<File> result = new ArrayList<File>();
        String query =  "'" + directories.get(0).getId() + "'"+ " in parents and "+"visibility = 'limited' and trashed = false";
        Drive.Files.List request = service.files().list().setFields("files(id,name,parents,mimeType,createdTime, modifiedTime,size)").setQ(query);

        do {
            try {
                FileList fileList = request.execute();

                result.addAll(fileList.getFiles());
                request.setPageToken(fileList.getNextPageToken());
            } catch (IOException e) {
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);


        List<MyFile> myFiles = new ArrayList<>();

        for(File f: result){
            MyFile newFile = new MyFile();
            newFile.setName(f.getName());

            newFile.setDirectory(f.getMimeType().equals("application/vnd.google-apps.folder") ? true : false);
            newFile.setDateCreated(new Date(f.getCreatedTime().getValue()));
            newFile.setDateModified(new Date(f.getModifiedTime().getValue()));
            File parent = getParentByParentsList(f.getParents(), service);
            newFile.setParent(getRootPath().contains(parent.getName())? "/" : parent.getName());
            newFile.setPath( initPath + (initPath.isEmpty()? "" : "/") + f.getName());
            myFiles.add(newFile);

        }
        for(File f: result){
            if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                String newPath = initPath+(initPath.isEmpty()? "" : "/") + f.getName();
                List<MyFile> subFiles = viewFilesFromDirectory(newPath, service);
                myFiles.addAll(subFiles);
            }
        }



        return myFiles;
    }

    @Override
    public List<MyFile> viewFiles(String path) throws Exception{
        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }

        String initPath = path;

        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;

        Drive service = getDriveService();

        List<File> directories = checkPath(path, service);

        if(directories.isEmpty()){
            throw new StorageException("Bad path!");
        }

        for(File f: directories){
            if(!f.getMimeType().equals("application/vnd.google-apps.folder")){
                throw new StorageException("Path must be to folder!");
            }
        }

        List<File> result = new ArrayList<File>();
        String query =  "'" + directories.get(0).getId() + "'"+ " in parents and "+"visibility = 'limited' and trashed = false";
        Drive.Files.List request = service.files().list().setFields("files(id,name,parents,mimeType,createdTime, modifiedTime,size)").setQ(query);

        do {
            try {
                FileList fileList = request.execute();

                result.addAll(fileList.getFiles());
                request.setPageToken(fileList.getNextPageToken());
            } catch (IOException e) {
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);


        List<MyFile> myFiles = new ArrayList<>();

        for(File f: result){
            MyFile newFile = new MyFile();
            newFile.setName(f.getName());

            newFile.setDirectory(f.getMimeType().equals("application/vnd.google-apps.folder") ? true : false);
            newFile.setDateCreated(new Date(f.getCreatedTime().getValue()));
            newFile.setDateModified(new Date(f.getModifiedTime().getValue()));
            File parent = getParentByParentsList(f.getParents(), service);
            newFile.setParent(getRootPath().contains(parent.getName())? "/" : parent.getName());
            newFile.setPath(initPath + (initPath.isEmpty()? "" : "/") + f.getName());
            myFiles.add(newFile);

        }
        for(File f: result){
            if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                List<MyFile> subFiles = viewFilesFromDirectory(initPath+(initPath.isEmpty()? "" : "/") + f.getName(), service);
                myFiles.addAll(subFiles);
            }
        }



        return myFiles;
    }

    private int getNumFilesInDirectory(String path, Drive service) throws Exception{
        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }

        String initPath = path;

        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;


        List<File> directories = checkPath(path, service);

        if(directories.isEmpty()){
            throw new StorageException("Bad path!");
        }

        for(File f: directories){
            if(!f.getMimeType().equals("application/vnd.google-apps.folder")){
                throw new StorageException("Path must be to folder!");
            }
        }

        List<File> result = new ArrayList<File>();
        String query =  "'" + directories.get(0).getId() + "'"+ " in parents and "+"visibility = 'limited' and trashed = false";
        Drive.Files.List request = service.files().list().setFields("files(id,name,parents,mimeType,createdTime, modifiedTime,size)").setQ(query);

        do {
            try {
                FileList fileList = request.execute();

                result.addAll(fileList.getFiles());
                request.setPageToken(fileList.getNextPageToken());
            } catch (IOException e) {
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);


        List<MyFile> myFiles = new ArrayList<>();

        for(File f: result){
            MyFile newFile = new MyFile();
            newFile.setName(f.getName());

            newFile.setDirectory(f.getMimeType().equals("application/vnd.google-apps.folder") ? true : false);
            newFile.setDateCreated(new Date(f.getCreatedTime().getValue()));
            newFile.setDateModified(new Date(f.getModifiedTime().getValue()));
            File parent = getParentByParentsList(f.getParents(), service);
            newFile.setParent(parent == null? null : parent.getName());
            newFile.setPath(initPath + (initPath.isEmpty()? "" : "/") + f.getName());
            myFiles.add(newFile);

        }
        for(File f: result){
            if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                List<MyFile> subFiles = viewFilesFromDirectory(initPath+(initPath.isEmpty()? "" : "/") + f.getName(), service);
                myFiles.addAll(subFiles);
            }
        }

        List<MyFile> newFiles = new ArrayList<>();
        for(MyFile f: myFiles){
            if(!f.isDirectory())
                newFiles.add(f);
        }


        return newFiles.size();
    }

    private void downloadFilesToFolder(File folder, String path, Drive service) throws Exception{


        List<File> result = new ArrayList<File>();
        String query =  "'" + folder.getId() + "'"+ " in parents and "+"visibility = 'limited' and trashed = false";
        Drive.Files.List request = service.files().list().setFields("files(id,name,parents,mimeType,size)").setQ(query);

        do {
            try {
                FileList files = request.execute();

                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        String folderPath = path+"\\"+folder.getName();
        java.io.File file = new java.io.File(folderPath);

        boolean dirCreated = file.mkdir();
        if(!dirCreated){
            return;
        }

        for(File f: result){
            if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                downloadFilesToFolder(f, folderPath, service );
            }else{
                java.io.File newFile = new java.io.File(folderPath + "\\" +f.getName());
                newFile.createNewFile();
                FileOutputStream oFile = new FileOutputStream(newFile, false);
                try{
                    service.files().get(f.getId()).executeMediaAndDownloadTo(oFile);
                }catch (HttpResponseException e){
                    if(!(e.getStatusCode() == 416)){
                        throw e;
                    }
                }
                oFile.close();
            }
        }

      return;
    }

    @Override
    public void downloadFile(String path) throws  Exception{
        if(path == null){
            throw new StorageException("Path cannot be null!");
        }
        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }

        Privilege folderPrivilege = getPrivelegeByPath(path);

        if( folderPrivilege == null && (getUsersSettings().getCurrentUser().getPrivilege() == Privilege.Viewer)){
            throw new StorageException("You don't have privilege for this action!");
        }
        if(folderPrivilege == Privilege.Viewer){
            throw new StorageException("You don't have privilege for this action!");
        }

        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;


        Drive service = getDriveService();

        List<File> files = checkPath(path, service);
        if(files.isEmpty()){
            throw new StorageException("Bad path!");
        }
        Path p = Paths.get(System.getProperty("user.dir") + "/downloads");

        Files.createDirectories(p);

        for(File f: files){
            if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                downloadFilesToFolder(f, p.normalize().toString(), service);
            }else{
                java.io.File newFile = new java.io.File(p.normalize().toString() + "\\" +f.getName());
                newFile.createNewFile();
                FileOutputStream oFile = new FileOutputStream(newFile, false);
                try{
                    service.files().get(f.getId()).executeMediaAndDownloadTo(oFile);
                }catch (HttpResponseException e){
                    if(!(e.getStatusCode() == 416)){
                        throw e;
                    }
                }
                oFile.close();
            }
        }


    }

    private long getDriveFileSize(String path, Drive service) throws  Exception{


        String symbol = "/";
        if(path.isEmpty()){
            symbol = "";
        }
        path = getRootPath() + symbol + path;

        List<File> files = checkPath(path, service);
        if(files.isEmpty()){
            throw new StorageException("Bad path!");
        }
        Path p = Paths.get(System.getProperty("user.dir") + "/utilFolder");

        Files.createDirectories(p);

        for(File f: files){
            if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                downloadFilesToFolder(f, p.normalize().toString(), service);
            }else{
                java.io.File newFile = new java.io.File(p.normalize().toString() + "\\" +f.getName());
                newFile.createNewFile();
                FileOutputStream oFile = new FileOutputStream(newFile, false);
                try{
                    service.files().get(f.getId()).executeMediaAndDownloadTo(oFile);
                }catch (HttpResponseException e){
                    if(!(e.getStatusCode() == 416)){
                        throw e;
                    }
                }
                oFile.close();
            }
        }
        java.io.File newFile = new java.io.File(p.normalize().toString() + "\\" + files.get(0).getName());
        long size = getFileSize(newFile);
        if(newFile.isDirectory()){
            FileUtils.deleteDirectory(newFile);
        }else{
            newFile.delete();
        }
        FileUtils.deleteDirectory(new java.io.File(System.getProperty("user.dir") + "/utilFolder"));
        return size;

    }

    @Override
    public void moveFile(String fromPath, String toPath) throws Exception {
        if(fromPath == null){
            throw new StorageException("Path to file cannot be null!");
        }
        if(toPath == null){
            throw new StorageException("Path where to move file cannot be null!");
        }

        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }

        String initFromPath = fromPath;
        String initToPath = toPath;

        String symbol1 = "/";
        if(fromPath.isEmpty()){
            symbol1 = "";
        }
        fromPath = getRootPath() + symbol1 + fromPath;

        String symbol2 = "/";
        if(toPath.isEmpty()){
            symbol2 = "";
        }
        toPath = getRootPath() + symbol2 + toPath;


        Drive service = getDriveService();

        List<File> filesToMove = checkPath(fromPath, service);
        List<File> toFolders = checkPath(toPath, service);

        if (toFolders.isEmpty()) {
            throw new StorageException("Specified path to directory does not exist!");
        }
        if (filesToMove.isEmpty()) {
            throw new StorageException("Specified path to file does not exist!");
        }


        StringBuilder previousParents = new StringBuilder();
        for(File f: filesToMove){
            for (String parent : f.getParents()) {
                previousParents.append(parent);
                previousParents.append(',');
            }
            for(File folder: toFolders){
                int numFiles;
                if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                    numFiles = getNumFilesInDirectory(initFromPath, service);

                }else{
                    numFiles = 1;

                }
                String badPath = checkNumberOfFiles(initToPath, numFiles, service);
                if(!badPath.isEmpty()){
                    throw new StorageException("Maximum number of files on path " + badPath + " is surprassed!" );
                }

                f = service.files().update(f.getId(), null)
                        .setAddParents(folder.getId())
                        .setRemoveParents(previousParents.toString())
                        .setFields("id, parents")
                        .execute();
            }
        }

        return;
    }

    private void copyFilesFromFolder(File oldFolder, File newFolder, File destFolder, Drive service){
        try{
            newFolder = service.files().create(newFolder).setFields("id").execute();
            List<File> result = new ArrayList<File>();
            String query =  "'" + oldFolder.getId() + "'"+ " in parents and "+"visibility = 'limited' and trashed = false";
            Drive.Files.List request = service.files().list().setFields("files(id,name,parents,mimeType, size)").setQ(query);

            do {
                try {
                    FileList files = request.execute();

                    result.addAll(files.getFiles());
                    request.setPageToken(files.getNextPageToken());
                } catch (IOException e) {
                    request.setPageToken(null);
                }
            } while (request.getPageToken() != null &&
                    request.getPageToken().length() > 0);

            for(File f: result){
                File file = new File();
                file.setName(f.getName());
                file.setParents(Collections.singletonList(newFolder.getId()));
                if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                    file.setMimeType("application/vnd.google-apps.folder");
                    copyFilesFromFolder(f,file,newFolder, service);

                }else{
                    service.files().copy(f.getId(), file).execute();

                }

            }



        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void copyFile(String fromPath, String toPath) throws Exception{
        if(fromPath == null){
            throw new StorageException("Path to file cannot be null!");
        }
        if(toPath == null){
            throw new StorageException("Path where to move file cannot be null!");
        }

        if(getRootPath() == null || getUsersSettings() == null){
            throw new StorageException("Not connected!");
        }

        String initFromPath = fromPath;
        String initToPath =toPath;

        String symbol1 = "/";
        if(fromPath.isEmpty()){
            symbol1 = "";
        }
        fromPath = getRootPath() + symbol1 + fromPath;

        String symbol2 = "/";
        if(toPath.isEmpty()){
            symbol2 = "";
        }
        toPath = getRootPath() + symbol2 + toPath;



        Drive service = getDriveService();

        List<File> files = checkPath(fromPath, service);


        if(files.isEmpty()){
            throw new StorageException("Specified file does not exist!");
        }
        List<File> destinationFolder = checkPath(toPath, service);


        if(destinationFolder.isEmpty()){
            throw new StorageException("Specified directory does not exist!");
        }
        Iterator<File>  i = destinationFolder.iterator();
        while(i.hasNext()){
            File f = i.next();
            if(!f.getMimeType().equals("application/vnd.google-apps.folder")){
                i.remove();
            }
        }
        if(destinationFolder.isEmpty()){
            throw new StorageException("Specified directory does not exist!");
        }

        for(File f: files){
            for(File destFolder: destinationFolder){

                long size = getDriveFileSize(initFromPath, service);
                long currSize = getConfigSettings().getCurrentStorageSize();
                long maxSize = getConfigSettings().getStorageSize();
                if((size + currSize > maxSize) && (maxSize != 0)){
                    throw new StorageException("Maximum storage size is surprassed!");
                }
                int numFiles;
                if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                    numFiles = getNumFilesInDirectory(initFromPath, service);

                }else{
                    numFiles = 1;

                }
                String badPath = checkNumberOfFiles(initToPath, numFiles,service);
                if(!badPath.isEmpty()){
                    throw new StorageException("Maximum number of files on path " + badPath + " is surprassed!" );
                }
                getConfigSettings().setCurrentStorageSize(currSize + size);


                File file = new File();
                file.setName(f.getName());
                file.setParents(Collections.singletonList(destFolder.getId()));
                if(f.getMimeType().equals("application/vnd.google-apps.folder")){
                    file.setMimeType("application/vnd.google-apps.folder");
                    copyFilesFromFolder(f,file,destFolder, service);
                }else{
                    service.files().copy(f.getId(), file).execute();

                }
            }
        }
    }

    private void updateUsersSettings(Drive service) throws  Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = checkPath(getRootPath() + "/users.json", service).get(0);

        File fileMetaData = new File();
        fileMetaData.setName("users.json");
        fileMetaData.setMimeType(jsonFile.getMimeType());
        java.io.File updatedJsonFile = new java.io.File("users.json");
        objectMapper.writeValue(updatedJsonFile, getUsersSettings());
        FileContent fileContent = new FileContent("text/plain", updatedJsonFile);
        service.files().update(jsonFile.getId(), fileMetaData, fileContent).execute();
        updatedJsonFile.delete();
    }

    private void updateConfigSettings(Drive service) throws  Exception{

        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = checkPath(getRootPath() + "/config.json", service).get(0);

        File fileMetaData = new File();
        fileMetaData.setName("config.json");
        fileMetaData.setMimeType(jsonFile.getMimeType());
        java.io.File updatedJsonFile = new java.io.File("config.json");
        objectMapper.writeValue(updatedJsonFile, getConfigSettings());
        FileContent fileContent = new FileContent("text/plain", updatedJsonFile);
        service.files().update(jsonFile.getId(), fileMetaData, fileContent).execute();
        updatedJsonFile.delete();
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

    private String checkNumberOfFiles(String path, int numb, Drive service) throws Exception{
        List<String> allPaths = getConfigSettings().getAllPaths(path);

        for(String p: allPaths){
            int numFiles = getNumFilesInDirectory(p, service);
            if(getConfigSettings().isExceeded(p,numFiles+numb))
                return p;
        }
        return "";
    }

    private Privilege getPrivelegeByPath(String path){
        return getUsersSettings().getCurrentUser().containsPath(path);
    }

    private String getFileContentType(String extension){

        extension = extension.toLowerCase();

        //DOCS
        extension = extension.toLowerCase();

        if(extension.equals("htm") || extension.equals("html")){
            return "text/html";
        }
        if(extension.equals("txt") || extension.equals("tmpl")){
            return "text/plain";
        }
        if(extension.equals("rtf")){
            return "application/rtf";
        }
        if(extension.equals("xml")){
            return "text/xml";
        }
        if(extension.equals("odt")){
            return "application/vnd.oasis.opendocument.text";
        }
        if(extension.equals("pdf")){
            return "application/pdf";
        }
        if(extension.equals("doc") || extension.equals("docx") || extension.equals("docm") || extension.equals("dotx")){
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        //SPREADSHEETS

        if(extension.equals("xls") || extension.equals("xlsx") || extension.equals("xlsx") || extension.equals("xml")){
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        if(extension.equals("ods")){
            return "application/x-vnd.oasis.opendocument.spreadsheet";
        }
        if(extension.equals("csv")){
            return "text/csv";
        }
        //ZIP

        if(extension.equals("zip")){
            return "application/zip";
        }
        if(extension.equals("rar")){
            return "application/rar";
        }
        if(extension.equals("tar")){
            return "application/tar";
        }
        if(extension.equals("arj")){
            return "application/arj";
        }

        //IMAGES

        if(extension.equals("jpeg") || extension.equals("jpg")){
            return "image/jpeg";
        }
        if(extension.equals("png")){
            return "image/png";
        }
        if(extension.equals("svg")){
            return "image/svg+xml";
        }
        if(extension.equals("gif")){
            return "image/gif";
        }
        if(extension.equals("bmp")){
            return "image/bmp";
        }

        //Presentations

        if(extension.equals("pptx") || extension.equals("ppt") ||  extension.equals("pptm")){
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }
        if(extension.equals("odp")){
            return "application/vnd.oasis.opendocument.presentation";
        }

        //Scripts

        if(extension.equals("json")){
            return "application/vnd.google-apps.script+json";
        }
        if(extension.equals("js")){
            return "text/js";
        }
        return "application/vnd.google-apps.folder";

    }


}