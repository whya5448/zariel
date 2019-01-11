package org.metalscraps.eso.lang.client;

import org.metalscraps.eso.lang.lib.util.Utils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class ClientMain {

    private final Path appPath = Paths.get(System.getenv("localappdata") + "/" + "dcinside_eso_client");
    private static final Logger LOG = Logger.getGlobal();
    private String serverFileName = null;
    private String crc32 = null;
    private Properties properties;


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
        properties = Utils.setConfig(configPath, Map.of("ver", "0"));
        long localVer = Long.parseLong(properties.get("ver").toString());

        LOG.info("서버 버전 확인 중...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://eso.metalscraps.org/ver.html"))
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

        if(response.statusCode() != 200) {
            LOG.severe("서버 버전 확인 실패");
            System.exit(AppErrorCode.CANNOT_FIND_SERVER_VERSION.getErrCode());
        }
        String[] resData = response.body().trim().split("/");
        Long serverVer = Long.parseLong(resData[0]);
        serverFileName = resData[1];
        crc32 = resData[2];

        LOG.info("서버 파일명 : " + serverFileName);
        LOG.info("서버 버전 : " + serverVer);
        LOG.info("로컬 버전 : " + localVer);

        if(serverVer > localVer) {
            LOG.info("업데이트 필요함.");
            if(update()) {
                LOG.info("업데이트 성공");
                properties.setProperty("ver", String.valueOf(serverVer));
                Utils.storeConfig(configPath, properties);
                var f = new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath()+"/Elder Scrolls Online/live/AddOns/gamedata/lang");
                f.mkdirs();
                try {
                    Files.find(appPath, 1, (path, attr) -> attr.isRegularFile()).forEach(x->{
                        try {
                            if(x.getFileName().toString().endsWith(".csv"))  Files.delete(x);
                            else if(x.getFileName().toString().endsWith(".lang"))  {
                                Files.deleteIfExists(Paths.get(f.getAbsolutePath()+File.separator+x.getFileName()));
                                Files.move(x, Paths.get(f.getAbsolutePath()+File.separator+x.getFileName()));
                            }
                        } catch (IOException e) { e.printStackTrace(); }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
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

        downloadLang();
        try {
            Utils.processRun(appPath.toFile(), tool.toString()+" -p -x kr.csv -o kr.lang");
            Utils.processRun(appPath.toFile(), tool.toString()+" -p -x tr.csv -o tr.lang");
        } catch (Exception e) {
            LOG.severe("LANG 생성 실패");
            e.printStackTrace();
            System.exit(AppErrorCode.CANNOT_CREATE_LANG_USING_TOOL.getErrCode());
        }

        return true;
    }

    private void downloadLang() {

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://storage.googleapis.com/dcinside-esok-cdn/lang_"+ serverFileName +".7z.exe")).build();
        HttpClient client = HttpClient.newHttpClient();
        Path langPath = Paths.get(appPath.toString() + "/lang.exe");

        if(langPath.toFile().exists() && String.valueOf(Utils.CRC32(langPath.toFile())).equals(crc32)) {
            LOG.info("언어 파일 존재함. 다운로드 스킵");
             return;
        }

        LOG.info("다운로드 시도");
        try {
            client.send(request, HttpResponse.BodyHandlers.ofFile(langPath));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LOG.severe("언어 다운로드 실패");
            System.exit(AppErrorCode.CANNOT_DOWNLOAD_LANG.getErrCode());
        }

        if(!crc32.equals(String.valueOf(Utils.CRC32(langPath.toFile())))) {
            LOG.severe("LANG 파일 CRC 불일치");
            try { Files.deleteIfExists(langPath); } catch (IOException e) { e.printStackTrace(); }
            downloadLang();
        }

        try {
            Utils.processRun(appPath.toFile(), appPath.toFile().toString() + "/lang.exe -y");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            LOG.severe("언어파일 압축해제 실패");
            System.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.getErrCode());
        }

        langPath.toFile().deleteOnExit();
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

        try {
            Utils.processRun(appPath.toFile(), appPath.toFile().toString() + "/tool.exe -y");
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
