package storageSpec.fileConfig;


import java.util.*;

public class ConfigSettings {

   private long storageSize;
   private long currentStorageSize;
   private Set<String> fileExtNotSupported;
   private Map<String, Integer> maxFilesInDirectory;

   public ConfigSettings(){
       fileExtNotSupported = new HashSet<>();
       maxFilesInDirectory = new HashMap<>();
       maxFilesInDirectory = new HashMap<>();
   }

   public void addNotSupportedExtension(String extension){
       fileExtNotSupported.add(extension.toLowerCase());
   }

   public void removeNotSupportedExtension(String extension) { fileExtNotSupported.remove(extension); }

   public boolean addMaxNumberForDirectory(int max, String path){
       maxFilesInDirectory.put(path, max);
       return true;
   }

   public void removeMaxNumberForDirectory(String path){
       maxFilesInDirectory.remove(path);
   }

    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }

    public long getStorageSize() {
        return storageSize;
    }

    public Set<String> getFileExtNotSupported() {
        return fileExtNotSupported;
    }

    public void setFileExtNotSupported(Set<String> fileExtNotSupported) {
        this.fileExtNotSupported = fileExtNotSupported;
    }

    public boolean isExtensionSupported(String extension){
       return !fileExtNotSupported.contains(extension);
    }

    public Map<String, Integer> getMaxFilesInDirectory() {
        return maxFilesInDirectory;
    }

    public void setMaxFilesInDirectory(Map<String, Integer> maxFilesInDirectory) {
        this.maxFilesInDirectory = maxFilesInDirectory;
    }

    public long getCurrentStorageSize() {
        return currentStorageSize;
    }

    public void setCurrentStorageSize(long currentStorageSize) {
       this.currentStorageSize = currentStorageSize;
    }

    public boolean setStrgSize(long storageSize){
       if(currentStorageSize > storageSize)
           return false;
       setStorageSize(storageSize);
       return true;
    }

    public List<String> getAllPaths(String path){
       List<String> paths = new ArrayList<>();
       for(Map.Entry<String, Integer> entry: maxFilesInDirectory.entrySet()){
           if(path.contains(entry.getKey())){
               paths.add(entry.getKey());
           }
       }
       return paths;
    }

    public boolean isExceeded(String path, int num){
       int maxNum = maxFilesInDirectory.get(path);
       if(num > maxNum){
           return true;
       }
       return false;
    }

}
