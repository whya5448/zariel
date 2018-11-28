package org.metalscraps.eso.client;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ClientMain {

    private final Path appPath = Paths.get(System.getenv("appdata") + "/" + "dcinside_eso_client");
    private static final Logger LOG = Logger.getGlobal();
    private long servVer = 0;
    private String servFileName = null;
    private String crc32 = null;

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

        String[] resData = response.body().trim().split("/");
        servVer = Long.parseLong(resData[0]);
        servFileName = resData[1];
        crc32 = resData[2];

        LOG.info("서버 파일명 : " + servFileName);
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

        downloadLang();
        try {
            processRun(tool.toString()+" -p -x kr"+servFileName+".csv -o kr.lang");
            processRun(tool.toString()+" -p -x tr"+servFileName+".csv -o tr.lang");
        } catch (Exception e) {
            LOG.severe("LANG 생성 실패");
            e.printStackTrace();
            System.exit(AppErrorCode.CANNOT_CREATE_LANG_USING_TOOL.getErrCode());
        }

        return false;
    }

    private void downloadLang() {

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://storage.googleapis.com/dcinside-esok-cdn/lang_"+servFileName+".7z.exe")).build();
        HttpClient client = HttpClient.newHttpClient();
        Path langPath = Paths.get(appPath.toString() + "/lang.exe");

        if(langPath.toFile().exists() && CRC32(langPath.toFile()).equals(crc32)) {
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

        if(!crc32.equals(CRC32(langPath.toFile()))) {
            LOG.severe("LANG 파일 CRC 불일치");
            langPath.toFile().delete();
            downloadLang();
        }

        try {
            processRun(appPath.toFile().toString() + "/lang.exe -y");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            LOG.severe("언어파일 압축해제 실패");
            System.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.getErrCode());
        }

        if(langPath.toFile().delete()) LOG.info("임시파일 삭제 성공");
        else LOG.warning("임시파일 삭제 실패");

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
            processRun(appPath.toFile().toString() + "/tool.exe -y");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            LOG.severe("툴 압축해제 실패");
            System.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.getErrCode());
        }

        if(toolPath.toFile().delete()) LOG.info("임시파일 삭제 성공");
        else LOG.warning("임시파일 삭제 실패");

        return Paths.get(toolPath.getParent().toString()+"/EsoExtractData v0.32/EsoExtractData.exe").toFile().exists();
    }


    private void processRun(String command) throws IOException, InterruptedException { processRun(command, ProcessBuilder.Redirect.INHERIT); }

    private void processRun(String command, ProcessBuilder.Redirect redirect) throws IOException, InterruptedException { processRun(command, redirect, appPath.toFile()); }

    private void processRun(String command, ProcessBuilder.Redirect redirect, File directory) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder()
                .directory(directory)
                .command((command).split("\\s+"))
                .redirectError(redirect)
                .redirectOutput(redirect);
        pb.start().waitFor();
    }


    private String CRC32(File f) {
        Checksum crc = new CRC32();
        if(!f.exists() || f.length() <= 0) return null;

        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(f))) {
            byte[] buffer = new byte[32768];
            int length;

            while ((length = in.read(buffer)) >= 0) crc.update(buffer, 0, length);
        } catch (IOException e) { e.printStackTrace(); }
        return String.valueOf(crc.getValue());
    }


}
