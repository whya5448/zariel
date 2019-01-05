package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class zanataLinkGenerator {
    private static AppWorkConfig appWorkConfig = new AppWorkConfig();
    SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.POPattern);
    HashMap<String, String> urlMap = new HashMap<>();
    private final Scanner sc;

    public zanataLinkGenerator() {
        sc = new Scanner(System.in);
    }

    public void getPO(File file ){
        if(file.isDirectory()){
            File []arrFS=file.listFiles();
            for(int i = 0 ; i<arrFS.length;i++){
                getPO(arrFS[i] );
            }
        }
        else{
            if(file.getPath().contains("ja-JP"));
            addFileURL(file);
        }
    }

    private void addFileURL(File file){
        System.out.println("File name ["+ file.getName()+"] path ["+ file.getPath()+"]");
        String filename = file.getName();
        String projectCategory = null;
        String path = file.getPath();
        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] pathSplit = path.split(pattern);

        if(filename.equals(pathSplit[pathSplit.length-1])){
            projectCategory = pathSplit[pathSplit.length-3];
            if(filename.contains(".po")){
                filename = filename.substring(0, filename.indexOf(".po"));
            }else {
                return;
            }
        }else {
            return;
        }

        String BaseURL = "http://www.dostream.com/zanata/webtrans/translate?";
        String ProjectURL = "project=ESO-" + projectCategory +"&iteration=1.0&localeId=ko&locale=ko-KR&dswid=-5074";
        String fileUrl = null;
        try {
            fileUrl = URLEncoder.encode(filename, "UTF-8");
            fileUrl = fileUrl.replace("+" , "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String DocURL = "#view:doc;doc:"+ fileUrl;
        String indexURL = null;

        HashMap<String, PO> poMap = new HashMap<>();
        poMap.putAll(Utils.sourceToMap(sourceToMapConfig.setFile(file)));
        for(String index : poMap.keySet()){
            String fullURL = BaseURL+ProjectURL+DocURL+";msgcontext:"+index;
            urlMap.put(index, fullURL);
        }
    }

    private String getCommand() {
        System.out.print("input index:");
        String comm = sc.nextLine();
        System.out.println("input is ["+comm+"]");
        return comm;
    }

    public static void main(String args[]){
        zanataLinkGenerator zlg = new zanataLinkGenerator();
        zlg.getPO(FileUtils.getFile("./zanataFile"));
        /*
        File workDir = new File(".");
        workDir.mkdirs();
        appWorkConfig.setBaseDirectory(workDir);
        Utils.downloadPOs(appWorkConfig);
        */

        while(true){
            String index = zlg.getCommand();
            System.out.println("--------------------");
            System.out.println(zlg.urlMap.get(index));
            System.out.println("--------------------");
        }
    }
}
