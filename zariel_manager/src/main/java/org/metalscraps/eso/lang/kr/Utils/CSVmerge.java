package org.metalscraps.eso.lang.kr.Utils;

import lombok.AccessLevel;
import lombok.Getter;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.PO;

import java.util.HashMap;
import java.util.HashSet;

public class CSVmerge {
    private HashMap<String, CategoryCSV> esoClientCSV = new HashMap<>();
    private HashMap<String, CategoryCSV> zanataCSV = new HashMap<>();
    private HashMap<String, CategoryCSV> mergedCSV = new HashMap<>();

    public void MergeCSV (HashSet<CategoryCSV> clientCSV, HashSet<CategoryCSV> zanataCSV){
        setCategoryNameMap(clientCSV, zanataCSV);
        genMergedPO();
    }

    private void setCategoryNameMap(HashSet<CategoryCSV> clientCSV, HashSet<CategoryCSV> zanataCSV){
        for(CategoryCSV oneCSV : clientCSV){
            this.esoClientCSV.put(oneCSV.getZanataFileName(), oneCSV);
        }

        for(CategoryCSV oneCSV : zanataCSV){
            this.zanataCSV.put(oneCSV.getZanataFileName(), oneCSV);
        }
    }

    private void genMergedPO(){
        for(String filename : this.esoClientCSV.keySet()){
            CategoryCSV merged = MergeCategory(this.esoClientCSV.get(filename), this.zanataCSV.get(filename));
            this.mergedCSV.put(filename, merged);
        }
    }

    private CategoryCSV MergeCategory(CategoryCSV origin, CategoryCSV zanata){
        CategoryCSV merged = new CategoryCSV();
        HashMap<String, PO> originPOMap = origin.getPODataMap();
        HashMap<String, PO> zanataPOMap = zanata.getPODataMap();

        merged.setZanataFileName( origin.getZanataFileName());
        merged.setLinkCount( origin.getLinkCount());
        merged.setType( origin.getType());
        merged.setPoIndexList( origin.getPoIndexList());

        for(String index : originPOMap.keySet()){
            PO originPO = originPOMap.get(index);
            PO zanataPO = zanataPOMap.get(index);

            String mergedSource = "";
            String mergedTarget = "";
            if(originPO.getSource().equals( zanataPO.getSource() )){
                mergedSource = originPO.getSource();
                mergedTarget = zanataPO.getTarget();
            } else {
                mergedSource = originPO.getSource();
                mergedTarget = originPO.getTarget();
            }

            PO mergedPO = new PO(index, mergedSource, mergedTarget);
            merged.putPoData(index, mergedPO);
        }

        return merged;
    }


}
