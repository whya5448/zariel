package org.metalscraps.eso.lang.tool.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.metalscraps.eso.lang.tool.bean.CategoryCSV;
import org.metalscraps.eso.lang.lib.bean.PO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CSVmerge {
    @Getter(AccessLevel.PUBLIC)
    private HashMap<String, CategoryCSV> mergedCSV = new HashMap<>();

    public void MergeCSV (HashSet<CategoryCSV> CategorizedClientCSV, HashMap<String, PO> targetPO, boolean isJapMerge){
        for(CategoryCSV oneCSV : CategorizedClientCSV){
            //System.out.println("oncCSV name ["+oneCSV.getZanataFileName());
            HashMap<String, PO> clientPO = oneCSV.getPODataMap();
            MergePO(clientPO, targetPO, isJapMerge);
            if("book".equals(oneCSV.getType()) || "story".equals(oneCSV.getType())) {
                OverwriteDuplicate(oneCSV);
            }
        }
    }


    private void MergePO(HashMap<String, PO> CategorizedClientPO, HashMap<String, PO> FullPO, boolean isJapPO){
        for(String index : CategorizedClientPO.keySet()){
            PO basePO = CategorizedClientPO.get(index);
            //System.out.println(index + "] po ["+ basePO);

            if(basePO.getSource().equals(basePO.getTarget())){
                basePO.setTarget("");
            }
            PO targetPO = FullPO.get(index);
            if(targetPO == null){
                System.out.println("no index in target:"+index);
            } else {
                if(basePO.getSource().equals(targetPO.getSource())) {
                    basePO.setTarget(targetPO.getTarget());
                    basePO.setFuzzy(targetPO.isFuzzy());
                } else {
                    if(isJapPO) {
                        basePO.setTarget(targetPO.getSource());
                    }
                }
            }
        }
    }

    private void OverwriteDuplicate(CategoryCSV CategorizedCSV) {
        HashMap<String, PO> poMap = CategorizedCSV.getPODataMap();
        HashMap<String, PO> translatedPoMap = new HashMap<>();
        ArrayList<PO> nonTransPoList = new ArrayList<>();

        for(PO po : poMap.values()){
            if(po.getTarget() == null || "".equals(po.getTarget())){
                nonTransPoList.add(po);
            }else {
                translatedPoMap.put(po.getSource(), po);
            }
        }

        System.out.println("non trans size ["+nonTransPoList.size()+"]");
        for(PO po : nonTransPoList){
            PO sameSource = translatedPoMap.get(po.getSource());
            if(sameSource != null){
                po.setTarget(sameSource.getTarget());
            }
        }

    }

}
