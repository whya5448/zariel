package org.metalscraps.eso.lang.kr.Utils;

import lombok.AccessLevel;
import lombok.Getter;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.PO;

import java.util.HashMap;
import java.util.HashSet;

public class CSVmerge {
    @Getter(AccessLevel.PUBLIC)
    private HashMap<String, CategoryCSV> mergedCSV = new HashMap<>();

    public void MergeCSV (HashSet<CategoryCSV> CategorizedClientCSV, HashMap<String, PO> targetPO, boolean isJapMerge){
        for(CategoryCSV oneCSV : CategorizedClientCSV){
            HashMap<String, PO> clientPO = oneCSV.getPODataMap();
            MergePO(clientPO, targetPO, isJapMerge);
        }
    }

    private void MergePO(HashMap<String, PO> CategorizedClientPO, HashMap<String, PO> FullPO, boolean isJapPO){
        for(String index : CategorizedClientPO.keySet()){
            PO basePO = CategorizedClientPO.get(index);
            if(basePO.getSource().equals(basePO.getTarget())){
                basePO.setTarget("");
            }
            PO targetPO = FullPO.get(index);
            if(targetPO == null){
                continue;
            } else {
                if(basePO.getSource().equals(targetPO.getSource())){
                    if(isJapPO){
                        basePO.setTarget(targetPO.getSource());
                    }else {
                        basePO.setTarget(targetPO.getTarget());
                        basePO.setFuzzy(targetPO.isFuzzy());
                    }
                }else {
                    continue;
                }
            }
        }
    }


}
