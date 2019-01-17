package org.metalscraps.eso.lang.server.compress;

import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class CompressServerMain {
    private static final Logger logger = LoggerFactory.getLogger(CompressServerMain.class);
    private final AppWorkConfig appWorkConfig = new AppWorkConfig();
    private final Properties properties = Utils.setConfig(Path.of("./.config"), Map.of());

    private void deleteTemp() {

        try {
            Files.walk(Paths.get("."))
                    .filter(x -> Files.isRegularFile(x)
                        && (
                            x.toString().endsWith(".csv")
                            || x.toString().endsWith(".7z")
                            || x.toString().endsWith(".7z.exe")
                            || x.toString().endsWith(".html")
                        )
                    ).forEach(path -> { try { Files.delete(path); } catch (IOException e) { e.printStackTrace(); } });
        } catch (IOException e) {
            logger.error(e.getMessage()+" 이전 파일 삭제 실패");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) { new CompressServerMain().run(); }
    private void run() {

        logger.info(appWorkConfig.getDateTime().format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss"))+" / 작업 시작");

        String mainServerAccount = properties.getProperty("MAIN_SERVER_ACCOUNT");
        String mainServer = properties.getProperty("MAIN_SERVER");
        String mainServerCredential = mainServerAccount+"@"+mainServer;

        File workDir = new File(".");
        appWorkConfig.setBaseDirectory(workDir);
        appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));
        File lang = new File(appWorkConfig.getBaseDirectory()+"/lang_"+appWorkConfig.getTodayWithYear()+".7z");

        try {
            logger.info("CSV 다운로드");
            Utils.processRun(appWorkConfig.getBaseDirectory(),"scp "+mainServerCredential+":"+properties.getProperty("MAIN_SERVER_PO_PATH")+appWorkConfig.getToday()+"/*.csv .");
            logger.info("CSV 압축");
            Utils.processRun(appWorkConfig.getBaseDirectory(),"7za a -mx=7 " + lang.getAbsolutePath() + " " + appWorkConfig.getBaseDirectory() + "/*.csv");
            logger.info("SFX 생성");
            Utils.processRun(appWorkConfig.getBaseDirectory(),"cat 7zCon.sfx "+lang.getAbsolutePath(), ProcessBuilder.Redirect.to(new File(lang.getAbsolutePath()+".exe")));
            logger.info("기존 업로드된 SFX 삭제");
            Utils.processRun(appWorkConfig.getBaseDirectory(),"gsutil rm gs://dcinside-esok-cdn/lang*.exe");
            logger.info("SFX 업로드");
            Utils.processRun(appWorkConfig.getBaseDirectory(),"gsutil cp "+lang.getAbsolutePath()+".exe gs://dcinside-esok-cdn/");
            logger.info("버전 문서 생성");
            Utils.processRun(appWorkConfig.getBaseDirectory(),"echo "+new Date().getTime()+"/"+appWorkConfig.getTodayWithYear()+"/"+Utils.CRC32(new File(lang.getAbsolutePath()+".exe")), ProcessBuilder.Redirect.to(new File("./ver.html")));
            logger.info("버전 문서 업로드");
            Utils.processRun(appWorkConfig.getBaseDirectory(),"scp ./ver.html "+mainServerCredential+":"+properties.getProperty("MAIN_SERVER_VERSION_DOCUMENT_PATH"));
            logger.info("잔여 파일 삭제");
            deleteTemp();
            System.exit(0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
