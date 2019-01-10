package cse.uta.edu.Utils;

public class Util {
    public static String formStageKey(String stageName) {
        return stageName.trim()
                .replace(" ", "_")
                .replace(":", "_")
                .toLowerCase();
    }

    public static String getIpsoOutputPath(String rootPath, String stageName) {
        return (rootPath + formStageKey(stageName));
    }
}
