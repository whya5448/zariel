package org.metalscraps.eso.lang.tool;

import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public class zanataLinkGenerator {
    private static AppWorkConfig appWorkConfig = new AppWorkConfig();
    SourceToMapConfig sourceToMapConfig = new SourceToMapConfig().setPattern(AppConfig.INSTANCE.getPOPattern());
    HashMap<String, String> urlMap = new HashMap<>();
    HashMap<String, StringBuilder> fileMap = new HashMap<>();
    private final Scanner sc;

    public zanataLinkGenerator() {
        sc = new Scanner(System.in);
    }

    public void getPO(File file ){
        if(file.isDirectory()){
            File[] arrFS = file.listFiles();
            for (File arrF : arrFS) getPO(arrF);
        }
        else {
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
            projectCategory = pathSplit[pathSplit.length-2];
            if(filename.contains(".pot")){
                filename = filename.substring(0, filename.indexOf(".pot"));
            }else {
                return;
            }
        }else {
            return;
        }

        String BaseURL = "http://www.dostream.com/zanata/webtrans/translate?";
        String ProjectURL = "project=ESO-" + projectCategory +"&iteration=1.0&localeId=ko&locale=ko-KR";
        String fileUrl = null;
        fileUrl = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        fileUrl = fileUrl.replace("+" , "%20");
        String DocURL = "#view:doc;doc:"+ fileUrl;
        String indexURL = null;

        HashMap<String, PO> poMap = new HashMap<>(Utils.sourceToMap(sourceToMapConfig.setFile(file)));
        for(String index : poMap.keySet()){
            PO po = poMap.get(index);
            StringBuilder sb = new StringBuilder();
            String fullURL = BaseURL+ProjectURL+DocURL+";msgcontext:"+index;
            sb.append(filename).append("        ").append(po.getId1()).append("_").append(po.getId2()).append("_").append(po.getId3()).append("      ").append(po.getSource()).append("      ").append(fullURL).append("     \n");
            StringBuilder mapsb = fileMap.get(projectCategory);
            if(mapsb == null) {
                fileMap.put(projectCategory, sb);
            } else {
                mapsb.append(sb);
            }
        }
    }

    private String getCommand() {
        System.out.print("input index:");
        String comm = sc.nextLine();
        System.out.println("input is ["+comm+"]");
        return comm;
    }

    public static void main(String[] args){
        zanataLinkGenerator zlg = new zanataLinkGenerator();
        zlg.getPO(Paths.get("./zanataFile").toFile());
        /*
        File workDir = new File(".");
        workDir.mkdirs();
        appWorkConfig.setBaseDir(workDir);
        Utils.downloadPOs(appWorkConfig);
        */

        for(String filename : zlg.fileMap.keySet()) {
            StringBuilder sb = zlg.fileMap.get(filename);
            try {
                Files.writeString(Paths.get(filename+".txt"), sb.toString(), AppConfig.INSTANCE.getCHARSET());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
        while(true){
            String index = zlg.getCommand();
            System.out.println("--------------------");
            System.out.println(zlg.urlMap.get(index));
            System.out.println("--------------------");
        }
        */
    }
}
