package org.metalscraps.eso.lang.kr.Utils;

import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class PoConverter {
    private AppWorkConfig appWorkConfig;

    public void setAppWorkConfig(AppWorkConfig appWorkConfig) {
        this.appWorkConfig = appWorkConfig;
    }

    public void translateGoogle() {

        //File file = new File("C:\\Users\\user\\Documents\\Elder Scrolls Online\\EsoKR\\PO_0203/achievement.po");
        Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);
        ArrayList<PO> LtransList = new ArrayList<>();
        for (File file : fileList) {
            ArrayList<PO> fileItems = new ArrayList<>(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)).values());
            System.out.println("target : " + file);

            int requestCount = 0;

            ArrayList<PO> skippedItem = new ArrayList<>();
            ArrayList<Thread> workerList = new ArrayList<>();

            GoogleTranslate worker = new GoogleTranslate();
            for (PO oneItem : fileItems) {
                if (oneItem.getSource().equals(oneItem.getTarget())) {
                    oneItem.modifyDoubleQuart();
                    worker.addJob(oneItem);
                    Thread transWork = new Thread(worker);
                    transWork.start();
                    workerList.add(transWork);
                    requestCount++;
                } else {
                    skippedItem.add(oneItem);
                }


                if (requestCount > 0) {
                    System.out.println("wait for Google translate....");
                    for (Thread t : workerList) {
                        try {
                            t.join();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    requestCount = 0;
                }

                for (Thread t : workerList) {
                    try {
                        t.join();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            //LtransList.addAll(skippedItem);
            LtransList.addAll(worker.getResult());

            String outputName = LtransList.get(1).getFileName() + "_conv.po";
            this.makePOFile(outputName, LtransList);
            LtransList.clear();
        }
    }

    public void filterNewPO() {
        Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);
        ArrayList<PO> LtransList = new ArrayList<>();

        for (File file : fileList) {
            ArrayList<PO> fileItems = new ArrayList<>(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)).values());
            System.out.println("target : " + file);
            for (PO oneItem : fileItems) {
                if (oneItem.getSource().equals(oneItem.getTarget())) {
                    oneItem.setTarget("");
                    LtransList.add(oneItem);
                }
            }
            System.out.println("target size: " + LtransList.size());

            String outputName = file.getName() + "_conv.po";
            this.makePOFile(outputName, LtransList);
            LtransList.clear();
        }
    }

    public void setFuzzyNbyG() {
        Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);
        ArrayList<PO> LtransList = new ArrayList<>();

        for (File file : fileList) {
            ArrayList<PO> fileItems = new ArrayList<>(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)).values());
            System.out.println("target : " + file);
            for (PO oneItem : fileItems) {
                oneItem.setFuzzy(true);
                oneItem.setTarget(GoogleTranslate.ReplaceSpecialChar(oneItem.getTarget())+"-G-");
                LtransList.add(oneItem);

            }
            System.out.println("target size: " + LtransList.size());

            String outputName = file.getName() + "_trans.po";
            this.makePOFile(outputName, LtransList);
            LtransList.clear();
        }
    }



    private void makePOFile(String filename ,ArrayList<PO> poList) {

        StringBuilder sb = new StringBuilder();
        System.out.println("po file making... file : "+appWorkConfig.getPODirectory()+"\\"+filename);
        File file = new File( appWorkConfig.getPODirectory()+"\\"+filename);
        for(PO p : poList) {
            if(p.isFuzzy()){
                sb.append("#, fuzzy\n");
            }
            sb.append("msgctxt \"").append(p.getId()).append("\"\nmsgid \"").append(p.getSource()).append("\"\nmsgstr \"").append(p.getTarget()).append("\"\n\n");
        }

        try {
            FileUtils.writeStringToFile(file, sb.toString(), AppConfig.CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
