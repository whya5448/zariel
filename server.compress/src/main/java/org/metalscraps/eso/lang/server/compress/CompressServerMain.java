package org.metalscraps.eso.lang.server.compress;

import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class CompressServerMain {
    private static final Logger logger = LoggerFactory.getLogger(CompressServerMain.class);
    private final AppWorkConfig appWorkConfig = new AppWorkConfig();
    private final Properties properties = Utils.setConfig(Paths.get("."), Paths.get("./.config"), Map.of());

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

        appWorkConfig.setBaseDirectory2(Paths.get(properties.getProperty("WORK_DIR")));
        appWorkConfig.setPODirectory2(appWorkConfig.getBaseDirectory2().resolve("/PO_"+appWorkConfig.getToday()));
        var lang = appWorkConfig.getBaseDirectory2().resolve("/lang_"+appWorkConfig.getTodayWithYear()+".7z");

        try {
            logger.info("CSV 다운로드");
            Utils.processRun(appWorkConfig.getBaseDirectory2(),"scp "+mainServerCredential+":"+properties.getProperty("MAIN_SERVER_PO_PATH")+appWorkConfig.getToday()+"/*.csv .");
            logger.info("CSV 압축");
            Utils.processRun(appWorkConfig.getBaseDirectory2(),"7za a -mx=7 " + lang + " " + appWorkConfig.getBaseDirectory2() + "/*.csv");
            logger.info("SFX 생성");
            Utils.processRun(appWorkConfig.getBaseDirectory2(),"cat 7zCon.sfx "+lang, ProcessBuilder.Redirect.to(new File(lang+".exe")));
            logger.info("기존 업로드된 SFX 삭제");
            Utils.processRun(appWorkConfig.getBaseDirectory2(),"gsutil rm gs://dcinside-esok-cdn/lang*.exe");
            logger.info("SFX 업로드");
            Utils.processRun(appWorkConfig.getBaseDirectory2(),"gsutil cp "+lang+".exe gs://dcinside-esok-cdn/");
            logger.info("버전 문서 생성");
            Utils.processRun(appWorkConfig.getBaseDirectory2(),"echo "+new Date().getTime()+"/"+appWorkConfig.getTodayWithYear()+"/"+Utils.CRC32(Paths.get(lang+".exe")), ProcessBuilder.Redirect.to(new File("./ver.html")));
            logger.info("버전 문서 업로드");
            Utils.processRun(appWorkConfig.getBaseDirectory2(),"scp ./ver.html "+mainServerCredential+":"+properties.getProperty("MAIN_SERVER_VERSION_DOCUMENT_PATH"));
            logger.info("잔여 파일 삭제");
            deleteTemp();
            System.exit(0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
