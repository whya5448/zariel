package org.metalscraps.eso.lang.kr.Utils;

import lombok.AccessLevel;
import lombok.Getter;
import org.metalscraps.eso.lang.kr.bean.CategoryCSV;
import org.metalscraps.eso.lang.kr.bean.PO;
import org.metalscraps.eso.lang.kr.config.FileNames;

import java.util.HashMap;
import java.util.HashSet;

public class CSVmerge {
    private HashMap<String, CategoryCSV> esoClientCSV = new HashMap<>();
    private HashMap<String, CategoryCSV> zanataCSV = new HashMap<>();
    @Getter(AccessLevel.PUBLIC)
    private HashMap<String, CategoryCSV> mergedCSV = new HashMap<>();

    public void GenMergedCSV (HashSet<CategoryCSV> clientCSV, HashSet<CategoryCSV> zanataCSV){
        setCategoryNameMap(clientCSV, zanataCSV);
        genMergedPO(false);
    }

    private void setCategoryNameMap(HashSet<CategoryCSV> clientCSV, HashSet<CategoryCSV> zanataCSV){
        for(CategoryCSV oneCSV : clientCSV){
            this.esoClientCSV.put(oneCSV.getZanataFileName(), oneCSV);
        }

        for(CategoryCSV oneCSV : zanataCSV){
            this.zanataCSV.put(oneCSV.getZanataFileName(), oneCSV);
        }
    }

    private void genMergedPO(boolean getOnlyChanged){
        for(String filename : this.esoClientCSV.keySet()){
            CategoryCSV merged = MergeCategory(this.esoClientCSV.get(filename), this.zanataCSV.get(filename), getOnlyChanged);
            this.mergedCSV.put(filename, merged);
        }
        for(CategoryCSV oneCSV : this.mergedCSV.values()){
            HashMap<String, PO> poMap = oneCSV.getPODataMap();
            for(PO po : poMap.values()){
                po.setFileName(FileNames.fromString(oneCSV.getZanataFileName()));
                if(po.getSource().equals(po.getTarget())){
                    po.setTarget("");
                }
            }
        }
    }



    private CategoryCSV MergeCategory(CategoryCSV origin, CategoryCSV zanata, boolean getOnlyChanged){
        CategoryCSV merged = new CategoryCSV();
        if(zanata == null){
            merged = origin;
            return merged;
        }
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
            if(zanataPO == null){
                mergedSource = originPO.getSource();
                mergedTarget = originPO.getTarget();
            } else if (originPO.getSource().equals( zanataPO.getSource() )){
                if(getOnlyChanged){
                    continue;
                }
                mergedSource = originPO.getSource();
                mergedTarget = zanataPO.getTarget();
            } else {
                mergedSource = originPO.getSource();
                mergedTarget = originPO.getTarget();
            }

            PO mergedPO = new PO(index, mergedSource, mergedTarget);
            mergedPO.setFileName(originPO.getFileName());
            merged.putPoData(index, mergedPO);
        }

        return merged;
    }


}
