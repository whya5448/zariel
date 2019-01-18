package org.metalscraps.eso.lang.client;

import org.metalscraps.eso.lang.lib.bean.ID;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class ClientMain {
    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);
    private static final Pattern IDPattern = Pattern.compile("([a-zA-Z]?[a-zA-Z\\d-_]+)[_-](\\d)[_-](\\d+)[_-]?");
    private static final Path appPath = Paths.get(System.getenv("localappdata") + "/" + "dcinside_eso_client");
    private static Path configPath = Paths.get(appPath.toString() + "/.config");
    private static final Properties properties = Utils.setConfig(configPath,
            Map.of("ver", "0", "x","0","y","0","width","100","height","24", "opacity", ".5f"));
    private String serverFileName = null;
    private String crc32 = null;

    public static void main(String[] args) throws InterruptedException {
        new ClientMain().run();
        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new Listener());
        Thread.sleep(Long.MAX_VALUE);
    }

    private void run() {

        logger.info("앱 설정 폴더 확인");
        if(!appPath.toFile().exists()) {
            logger.info("폴더 존재하지 않음 생성.");
            if(appPath.toFile().mkdirs()) logger.info(appPath.toString() + "생성 성공");
            else {
                logger.error("설정 폴더 생성 실패. 앱 종료");
                System.exit(AppErrorCode.CANNOT_CREATE_CONFIG_PATH.getErrCode());
            }
        }

        logger.info("설정 파일 확인");

        var configPath = Paths.get(appPath.toString() + "/.config");
        if(!configPath.toFile().exists()) {
            logger.info("설정 존재하지 않음 생성.");
            try {
                if(configPath.toFile().createNewFile()) logger.info(appPath.toString() + "생성 성공");
                else {
                    logger.error("설정 생성 실패. 앱 종료");
                    System.exit(AppErrorCode.CANNOT_CREATE_CONFIG_FILE.getErrCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("설정 생성 실패. 앱 종료");
                System.exit(AppErrorCode.CANNOT_CREATE_CONFIG_FILE.getErrCode());
            }
        }

        logger.info("설정 불러오기");
        long localVer = Long.parseLong(properties.get("ver").toString());

        logger.info("서버 버전 확인 중...");
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
            logger.error("서버 버전 확인 실패");
            System.exit(AppErrorCode.CANNOT_FIND_SERVER_VERSION.getErrCode());
            e.printStackTrace();
        }

        if(response.statusCode() != 200) {
            logger.error("서버 버전 확인 실패");
            System.exit(AppErrorCode.CANNOT_FIND_SERVER_VERSION.getErrCode());
        }
        String[] resData = response.body().trim().split("/");
        Long serverVer = Long.parseLong(resData[0]);
        serverFileName = resData[1];
        crc32 = resData[2];

        logger.info("서버 파일명 : " + serverFileName);
        logger.info("서버 버전 : " + serverVer);
        logger.info("로컬 버전 : " + localVer);

        if(serverVer > localVer) {
            logger.info("업데이트 필요함.");
            if(update()) {
                logger.info("업데이트 성공");
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
            else logger.error("업데이트 실패");
        } else {
            logger.info("최신 버전임");
        }
    }

    private boolean update() {
        Path tool = Paths.get(appPath.toString()+"/EsoExtractData v0.32/EsoExtractData.exe");
        if(!tool.toFile().exists()) {
            logger.info("툴 존재하지 않음.");
            if(downloadTool()) logger.info("툴 전송 성공");
            else {
                logger.info("툴 전송 실패");
                System.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.getErrCode());
            }
        }

        downloadLang();
        try {
            Utils.processRun(appPath.toFile(), tool.toString()+" -p -x kr.csv -o kr.lang");
            Utils.processRun(appPath.toFile(), tool.toString()+" -p -x kr.csv -o kr_beta.lang");
            Utils.processRun(appPath.toFile(), tool.toString()+" -p -x tr.csv -o tr.lang");
        } catch (Exception e) {
            logger.error("LANG 생성 실패");
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
            logger.info("언어 파일 존재함. 다운로드 스킵");
             return;
        }

        logger.info("다운로드 시도");
        try {
            client.send(request, HttpResponse.BodyHandlers.ofFile(langPath));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error("언어 다운로드 실패");
            System.exit(AppErrorCode.CANNOT_DOWNLOAD_LANG.getErrCode());
        }

        if(!crc32.equals(String.valueOf(Utils.CRC32(langPath.toFile())))) {
            logger.error("LANG 파일 CRC 불일치");
            try { Files.deleteIfExists(langPath); } catch (IOException e) { e.printStackTrace(); }
            downloadLang();
        }

        try {
            Utils.processRun(appPath.toFile(), appPath.toFile().toString() + "/lang.exe -y");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            logger.error("언어파일 압축해제 실패");
            System.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.getErrCode());
        }

        langPath.toFile().deleteOnExit();
    }

    private boolean downloadTool() {
        logger.info("다운로드 시도");
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://storage.googleapis.com/dcinside-esok-cdn/EsoExtractData%20v0.32.exe")).build();

        HttpClient client = HttpClient.newHttpClient();

        Path toolPath = Paths.get(appPath.toString() + "/tool.exe");

        try {
            client.send(request, HttpResponse.BodyHandlers.ofFile(toolPath));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error("툴 다운로드 실패");
            System.exit(AppErrorCode.CANNOT_DOWNLOAD_TOOL.getErrCode());
        }

        try {
            Utils.processRun(appPath.toFile(), appPath.toFile().toString() + "/tool.exe -y");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            logger.error("툴 압축해제 실패");
            System.exit(AppErrorCode.CANNOT_DECOMPRESS_TOOL.getErrCode());
        }

        if(toolPath.toFile().delete()) logger.info("임시파일 삭제 성공");
        else logger.warn("임시파일 삭제 실패");

        return Paths.get(toolPath.getParent().toString()+"/EsoExtractData v0.32/EsoExtractData.exe").toFile().exists();
    }


    // 몰아서 작업하는 경우가 있기에 로그 남겨주기
    static class Listener implements FlavorListener {
        private Frame frame;
        private Panel panel;
        private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        private String dupCheck = "Hello World!";

        Listener() {
            // Owner 뺏기
            StringSelection ss = new StringSelection("");
            clipboard.setContents(ss, ss);

            frame = new Frame();
            frame.setAlwaysOnTop(true);
            frame.setUndecorated(true);
            frame.setResizable(false);
            frame.setLayout(new BorderLayout());
            frame.setOpacity(Float.parseFloat(properties.getProperty("opacity")));
            frame.setBounds(
                    Integer.parseInt(properties.getProperty("x")), Integer.parseInt(properties.getProperty("y")),
                    Integer.parseInt(properties.getProperty("width")), Integer.parseInt(properties.getProperty("height"))
            );
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

            panel = new Panel();
            panel.setLayout(new GridLayout());
            TextField textField = new TextField();
            textField.addActionListener(e->{
                String text = textField.getText();
                if(!text.endsWith("_")) text += "_";
                var m = IDPattern.matcher(text);
                ArrayList<ID> arrayList = new ArrayList<>();
                while (m.find()) arrayList.add(new ID(m.group(1), m.group(2), m.group(3)));
                if(arrayList.size() == 1) openZanata(arrayList.get(0));
                else if(arrayList.size() > 0) updatePane(arrayList);
            });
            frame.add(panel, BorderLayout.NORTH);
            frame.add(textField, BorderLayout.SOUTH);
            frame.setVisible(true);
        }

        void updatePane(List list) {
            frame.setVisible(false);
            panel.removeAll();
            for(int i=0; i<list.size(); i++) {
                Button button = new Button();
                button.setLabel(String.valueOf(i));
                button.setActionCommand(list.get(i).toString());
                button.addActionListener(e->openZanata(new ID(e.getActionCommand())));
                panel.add(button);
            }
            frame.setVisible(true);
        }

        @Override
        public void flavorsChanged(FlavorEvent e) {

            try {
                getContent();
            } catch (IOException | UnsupportedFlavorException e1) {
                e1.printStackTrace();
            } catch (IllegalStateException e1) {
                try { getContent(); } catch (Exception ignored) {}
            }

        }

        synchronized void getContent() throws IOException, UnsupportedFlavorException {

            Transferable trans = clipboard.getContents(null);
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {

                String s = (String) trans.getTransferData(DataFlavor.stringFlavor);
                StringSelection ss = new StringSelection(s);
                clipboard.setContents(ss, ss);

                if(!s.equals(dupCheck)) dupCheck = s;
                else return;

                var m = IDPattern.matcher(s);
                ArrayList<ID> arrayList = new ArrayList<>();
                while (m.find()) {
                    var id = new ID(m.group(1), m.group(2), m.group(3));
                    try { logger.info(id.toString() + "\t\t" + getURL(id)); }
                    catch (ID.NotFileNameHead ignored) { continue; }
                    catch (Exception e) { e.printStackTrace(); }
                    arrayList.add(id);

                }

                if(arrayList.size() == 1) openZanata(arrayList.get(0));
                else if(arrayList.size() > 0) updatePane(arrayList);
            }
        }

        private void openZanata(ID id) {
            try {
                Desktop.getDesktop().browse(new URI(getURL(id)));
            }
            catch (ID.NotFileNameHead ignored) {}
            catch (Exception e) {
                logger.error(e.getMessage()+"/"+id.toString());
                e.printStackTrace();
            }
        }

        private String getURL(ID id) throws Exception {
            String projectName = Utils.getProjectNameByDocument(id);
            String latestVersion = Utils.getLatestVersion(projectName);
            return AppConfig.ZANATA_DOMAIN+"webtrans/translate?dswid=-3784&iteration="+latestVersion+"&project="+projectName+"&locale=ko-KR&localeId=ko#view:doc;doc:"+id.getHead()+";msgcontext:"+id.getBody()+"-"+id.getTail();
        }
    }
}
