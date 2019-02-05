package cse.uta.edu.IPSO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class AppStageTag {
    private static AppStageTag ourInstance = new AppStageTag();
    /* Tags for each stage*/
    private HashMap<String, IPSOStageTypes> tags = new HashMap<>();
    /* Cache for stages only exists in parallel processing environment (contribute to only Wo) */
    private ArrayList<String> unicoreStages = new ArrayList<>();

    public static AppStageTag getInstance() {
        return ourInstance;
    }

    private AppStageTag() {}

    public void put(HashMap<Long, IPSOStage> stages) {
        if(IPSOExprConfig.getInstance().MP() == 1 && unicoreStages.isEmpty())
            stages.forEach((k,v) -> unicoreStages.add(v.getStageKey()));

        if(IPSOExprConfig.getInstance().MP() != 1
                && IPSOExprConfig.getInstance().MP() != IPSOExprConfig.getInstance().NP()
                && tags.isEmpty()) {
            stages.forEach((k, v) -> tags.put(v.getStageKey(), v.stageType()));
        }

    }

    public IPSOStageTypes get(String stageKey) {
        return tags.get(stageKey);
    }

    public boolean isReady() {
        return (!tags.isEmpty() && !unicoreStages.isEmpty());
    }

    public boolean isWoStage(String stage) {
        return (tags.containsKey(stage) && !unicoreStages.contains(stage));
    }
}
