package org.metalscraps.eso.lang.server;

import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.bean.ToCSVConfig;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger("org.metalscraps.eso.lang.server.ServerMain");
    private final AppWorkConfig appWorkConfig = new AppWorkConfig();
    private final File VERSION_DOCUMENT = new File("/usr/share/nginx/html/ver.html");

    private void run(String[] args) {

        boolean useCache = false;
        for(var s : args) if(s.equals("-cache")) useCache = true;

        File workDir = new File(".");
        workDir.mkdirs();
        appWorkConfig.setBaseDirectory(workDir);
        appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));

        // 테스트용 고정값 쓰기
        if(!useCache) Utils.downloadPOs(appWorkConfig);
        else appWorkConfig.setPODirectory(new File((appWorkConfig.getBaseDirectory()+"/PO_CACHE")));

        Utils.convertCN_PO_to_KO(appWorkConfig);
        makeAndUpload();
    }

    public static void main(String[] args) {
        new ServerMain().run(args);
    }

    private void makeAndUpload() {

        ArrayList<PO> sourceList = new ArrayList<>();
        File lang = new File(appWorkConfig.getPODirectory()+"/lang_"+appWorkConfig.getTodayWithYear()+".7z");

        Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po2"}, false);
        try {
            if(!lang.exists() && lang.length() <= 0) {
                for (File file : fileList) {
                    sourceList.addAll(Utils.sourceToMap(new SourceToMapConfig().setFile(file).setPattern(AppConfig.POPattern)).values());
                    logger.info(file.toString());
                }

                ToCSVConfig csvConfig = new ToCSVConfig().setWriteSource(false);
                sourceList.sort(null);

                Utils.makeFile(new File(appWorkConfig.getPODirectory() + "/kr.csv"), csvConfig, sourceList);
                Utils.makeFile(new File(appWorkConfig.getPODirectory() + "/tr.csv"), csvConfig.setWriteFileName(true), sourceList);

                try {
                    Files.find(Paths.get("."), 1, (filePath, fileAttr) -> fileAttr.isDirectory()
                            && filePath.getFileName().toString().startsWith("PO_")
                            && !filePath.getFileName().toString().equalsIgnoreCase("PO_"+appWorkConfig.getToday())
                    ) .forEach(x->x.toFile().delete());
                } catch (Exception e) { e.printStackTrace(); }
                Utils.processRun(appWorkConfig.getBaseDirectory(),"7za a -mx=7 " + lang.getAbsolutePath() + " " + appWorkConfig.getPODirectory() + "/*.csv");
                Utils.processRun(appWorkConfig.getBaseDirectory(),"cat 7zCon.sfx "+lang.getAbsolutePath(), ProcessBuilder.Redirect.to(new File(lang.getAbsolutePath()+".exe")));
                Utils.processRun(appWorkConfig.getBaseDirectory(),"gsutil cp "+lang.getAbsolutePath()+".exe gs://dcinside-esok-cdn/");
                Utils.processRun(appWorkConfig.getBaseDirectory(),"echo "+new Date().getTime()+"/"+appWorkConfig.getTodayWithYear()+"/"+Utils.CRC32(new File(lang.getAbsolutePath()+".exe")), ProcessBuilder.Redirect.to(VERSION_DOCUMENT));
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

}
