package org.metalscraps.eso.lang.server;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.Operation;
import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.lib.bean.ToCSVConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
    private final AppWorkConfig appWorkConfig = new AppWorkConfig();
    private final Properties properties = Utils.setConfig(Paths.get("."), Paths.get(".config"), Map.of());

    private void run() {

        logger.info(appWorkConfig.getDateTime().format(DateTimeFormatter.ofPattern("yy-MM-dd hh:mm:ss"))+" / 작업 시작");
        appWorkConfig.setBaseDirectory(new File(properties.getProperty("WORK_DIR")));
        appWorkConfig.setPODirectory(new File(appWorkConfig.getBaseDirectory()+"/PO_"+appWorkConfig.getToday()));

        // 이전 데이터 삭제
        logger.info("이전 데이터 삭제");
        deletePO();
        logger.info("PO 다운로드");
        Utils.downloadPOs(appWorkConfig);
        logger.info("다운로드 된 PO 파일 문자셋 변경");
        Utils.convertKO_PO_to_CN(appWorkConfig);
        logger.info("CSV 생성");
        makeCSV();

        logger.info("인스턴스 시작");
        var res = startCompressServer();
        logger.info(Objects.requireNonNull(res).getStatus());
    }

    public static void main(String[] args) {
        new ServerMain().run();
    }

    private void deletePO() {

        Predicate<Path> p = x -> x.toString().startsWith("./PO_") && !x.toString().startsWith("./PO_"+appWorkConfig.getToday());
        try {
            // 디렉토리 사용중 오류, 파일 먼저 지우고 디렉토리 지우기
            Files.walk(Paths.get(".")).filter(p.and(Files::isRegularFile)).forEach(path -> { try { Files.delete(path); } catch (IOException e) { e.printStackTrace(); } });
            Files.walk(Paths.get(".")).filter(p.and(Files::isDirectory)).forEach(path -> { try { Files.delete(path); } catch (IOException e) { e.printStackTrace(); } });
        } catch (IOException e) {
            logger.error(e.getMessage()+" 이전 파일 삭제 실패");
            e.printStackTrace();
        }

    }

    private void makeCSV() {

        File lang = new File(appWorkConfig.getPODirectory()+"/lang_"+appWorkConfig.getTodayWithYear()+".7z");

        var listFiles = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po2"}, false);

        try {
            if(!lang.exists() && lang.length() <= 0) {
                var list = Utils.getMergedPO(listFiles);
                var config = new ToCSVConfig().setWriteSource(false);

                Utils.makeCSVwithLog(new File(appWorkConfig.getPODirectory() + "/kr.csv"), config, list);
                Utils.makeCSVwithLog(new File(appWorkConfig.getPODirectory() + "/kr_beta.csv"), config.setBeta(true), list);
                Utils.makeCSVwithLog(new File(appWorkConfig.getPODirectory() + "/tr.csv"), config.setWriteFileName(true).setBeta(false), list);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Operation startCompressServer() {

        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(Path.of(properties.getProperty("GCP_PERM_JSON")).toFile()));
            if (credential.createScopedRequired()) credential = credential.createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

            Compute computeService = new Compute.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName("Google-ComputeSample/0.1")
                    .build();

            Compute.Instances.Start request = computeService.instances().start(properties.getProperty("GCP_PROJECT_NAME"), properties.getProperty("GCP_PROJECT_ZONE"), properties.getProperty("GCP_COMPRESS_SERVER_INSTANCE_NAME"));
            return request.execute();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
