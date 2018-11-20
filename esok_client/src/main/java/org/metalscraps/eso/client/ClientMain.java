package org.metalscraps.eso.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

public class ClientMain {

    private final Path appPath = Paths.get(System.getenv("appdata") + "/" + "dcinside_eso_client");
    private static final Logger LOG = Logger.getGlobal();

    public static void main(String[] args) {
        new ClientMain().run();
        System.exit(0);
    }

    private void run() {

        LOG.info("앱 설정 폴더 확인");
        if(!appPath.toFile().exists()) {
            LOG.info("폴더 존재하지 않음 생성.");
            if(appPath.toFile().mkdirs()) LOG.fine(appPath.toString() + "생성 성공");
            else {
                LOG.severe("설정 폴더 생성 실패. 앱 종료");
                System.exit(AppErrorCode.CANNOT_CREATE_CONFIG_PATH.getErrCode());
            }
        }

        LOG.info("설정 파일 확인");

        var configPath = Paths.get(appPath.toString() + "/.config");
        if(!configPath.toFile().exists()) {
            LOG.info("설정 존재하지 않음 생성.");
            try {
                if(configPath.toFile().createNewFile()) LOG.fine(appPath.toString() + "생성 성공");
                else {
                    LOG.severe("설정 생성 실패. 앱 종료");
                    System.exit(AppErrorCode.CANNOT_CREATE_CONFIG_FILE.getErrCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOG.severe("설정 생성 실패. 앱 종료");
                System.exit(AppErrorCode.CANNOT_CREATE_CONFIG_FILE.getErrCode());
            }
        }

        LOG.info("설정 불러오기");
        Properties p = new Properties();
        try(var fis = new FileInputStream(configPath.toFile());var fos = new FileOutputStream(configPath.toFile())) {
            p.load(fis);

            if(configPath.toFile().length() == 0) {
                LOG.info("설정 데이터 없음. 초기화");
                p.setProperty("ver", "0");
                p.store(fos, "init");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        long localVer = Long.parseLong(p.get("ver").toString());

        LOG.info("서버 버전 확인 중...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://eso.metalscraps.org/test.html"))
                .GET() //used by default if we don't specify
                .build();

        //sending request and receiving response via HttpClient
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            LOG.severe("서버 버전 확인 실패");
            System.exit(AppErrorCode.CANNOT_FIND_SERVER_VERSION.getErrCode());
            e.printStackTrace();
        }

        long servVer = Long.parseLong(response.body().trim());

        LOG.info("서버 버전 : " + servVer);
        LOG.info("로컬 버전 : " + localVer);

        if(servVer > localVer) {
            LOG.info("업데이트 필요함.");
            if(update()) LOG.info("업데이트 성공");
            else LOG.severe("업데이트 실패");
        } else {
            LOG.info("최신 버전임");
            System.exit(0);
        }
    }

    private boolean update() {
        Path tool = Paths.get(appPath.toString()+"/EsoExtractData v0.32/EsoExtractData.exe");
        if(!tool.toFile().exists()) {
            LOG.info("툴 존재하지 않음.");
            if(downloadTool()) LOG.info("툴 전송 성공");
            else {
                LOG.info("툴 전송 실패");
                System.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.getErrCode());
            }
        }

        return false;
    }

    private boolean downloadTool() {
        LOG.info("다운로드 시도");
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://storage.googleapis.com/dcinside-esok-cdn/EsoExtractData%20v0.32.exe")).build();

        HttpClient client = HttpClient.newHttpClient();

        Path toolPath = Paths.get(appPath.toString() + "/tool.exe");

        try {
            client.send(request, HttpResponse.BodyHandlers.ofFile(toolPath));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LOG.severe("툴 다운로드 실패");
            System.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.getErrCode());
        }

        ProcessBuilder pb = new ProcessBuilder()
                .directory(toolPath.toFile().getParentFile())
                .command((toolPath.toFile().getParentFile().toString() + "/tool.exe -y").split("\\s+"))
                .inheritIO();
        try {
            pb.start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            LOG.severe("툴 압축해제 실패");
            System.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.getErrCode());
        }

        if(toolPath.toFile().delete()) LOG.info("임시파일 삭제 성공");
        else LOG.warning("임시파일 삭제 실패");

        return Paths.get(toolPath.getParent().toString()+"/EsoExtractData v0.32/EsoExtractData.exe").toFile().exists();
    }
}
