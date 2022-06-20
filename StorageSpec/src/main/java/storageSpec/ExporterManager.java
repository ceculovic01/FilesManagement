package storageSpec;

import storageSpec.users.UsersSettings;

public class ExporterManager {
    private static StorageExporter storageExporter;

    public ExporterManager() {

    }

    public static void registerExporter(StorageExporter stExporter) {
        storageExporter = stExporter;
    }

    public static StorageExporter getExporter() {
        return storageExporter;
    }
}
