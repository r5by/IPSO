package cse.uta.edu.Utils;

import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.components.threeD.Scene;

public class Util {
    public static String formStageKey(String stageName) {
        return stageName.trim()
                .replace(" ", "_")
                .replace(":", "_")
                .toLowerCase();
    }

    public static String ipsoScalingFactorOutputPath(String rootPath, String stageName) {
        return (rootPath + formStageKey(stageName));
    }

    public static String ipsoSpeedupOutputPath(String rootPath, String appName) {
        return (rootPath + appName);
    }

    //----------------------------
    //  Tablesaw Plotting
    //----------------------------
    public static Layout standardLayout(String title, String xCol, String yCol, String zCol, boolean showLegend) {
        return Layout.builder().title(title).height(800).width(1000).showLegend(showLegend).scene(Scene.sceneBuilder().xAxis(Axis.builder().title(xCol).build()).yAxis(Axis.builder().title(yCol).build()).zAxis(Axis.builder().title(zCol).build()).build()).build();
    }
}
