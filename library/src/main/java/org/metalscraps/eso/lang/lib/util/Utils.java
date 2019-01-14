package org.metalscraps.eso.lang.lib.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.metalscraps.eso.lang.lib.bean.PO;
import org.metalscraps.eso.lang.lib.bean.ToCSVConfig;
import org.metalscraps.eso.lang.lib.config.AppConfig;
import org.metalscraps.eso.lang.lib.config.AppWorkConfig;
import org.metalscraps.eso.lang.lib.config.SourceToMapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored", "unused"})
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static String serverVersion = null;
    public static String getLatestVersion(String projectName) {
        if(serverVersion != null) return serverVersion;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://www.dostream.com/zanata/rest/projects/p/"+projectName))
                .header("Accept","application/json")
                .build();

        JsonNode jsonNode = getBodyFromHTTPsRequest(request);
        String[] serverVer = null;
        for (JsonNode node : jsonNode.get("iterations")) {
            if(node.get("status").toString().equalsIgnoreCase("\"ACTIVE\"") ) { // && temp > version
                String[] tempVer = node.get("id").asText().split("\\.");
                if(serverVer == null) {
                    serverVer = tempVer;
                    continue;
                }
                for(int i=0; i<3; i++) {
                    if(Integer.parseInt(tempVer[i]) > Integer.parseInt(serverVer[i])) {
                        serverVer = tempVer;
                        break;
                    }
                }
            }
        }
        if(serverVer == null) serverVer = new String[] {"0.0.0"};
        return serverVersion = String.join(".", serverVer);
    }

    public static JsonNode getBodyFromHTTPsRequest(HttpRequest request){

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;

        try {
            jsonNode = objectMapper.readTree(Objects.requireNonNull(response).body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonNode;
    }

    public static ArrayList<String> getFileNames(String projectName){
        ArrayList<String> filenames = new ArrayList<>();
        final String url = AppConfig.ZANATA_DOMAIN+"rest/projects/p/"+ projectName +"/iterations/i/" +Utils.getLatestVersion(projectName)+"/r";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept","application/json")
                .build();

        JsonNode jsonNode = getBodyFromHTTPsRequest(request);

        for (Iterator<JsonNode> it = jsonNode.elements(); it.hasNext(); ) {
            JsonNode node = it.next();
            String Trim= node.get("name").toString().replaceAll("^\"|\"$", "");
            filenames.add(Trim);
        }

        return filenames;
    }

    public static void downloadPOs(AppWorkConfig appWorkConfig){
        LocalTime timeTaken = LocalTime.now();
        downloadPO(appWorkConfig, "ESO-item");
        downloadPO(appWorkConfig, "ESO-skill");
        downloadPO(appWorkConfig, "ESO-system");
        downloadPO(appWorkConfig, "ESO-book");
        downloadPO(appWorkConfig, "ESO-story");
        logger.info("총 " + timeTaken.until(LocalTime.now(), ChronoUnit.SECONDS) + "초");
    }

    public static void downloadPO(AppWorkConfig appWorkConfig, String projectName) {

        final String url = AppConfig.ZANATA_DOMAIN+"rest/file/translation/"+projectName+"/"+Utils.getLatestVersion(projectName)+"/ko/po?docId=";
        final File baseDirectory = appWorkConfig.getBaseDirectory();
        final File PODirectory = new File(baseDirectory.getAbsolutePath() + "/PO_" + appWorkConfig.getToday());
        appWorkConfig.setPODirectory(PODirectory);

        File fPO = null;
        try {

            ArrayList<String > fileNames = getFileNames(projectName);

            for (String fileName : fileNames) {

                // 우리가 사용하는 데이터 아님.
                if (fileName.equals("00_EsoUI_Client") || fileName.equals("00_EsoUI_Pregame")) continue;

                LocalTime ltStart = LocalTime.now();
                String fileURL = url+fileName;
                fileURL =  fileURL.replace(" ", "%20");
                logger.trace("download zanata file  ["+fileName + "] to local ["+PODirectory.getAbsolutePath()+"/"+fileName+".po] ");

                fPO = new File(PODirectory.getAbsolutePath() + "/" + fileName + ".po");
                if (!fPO.exists() || fPO.length() <= 0) FileUtils.writeStringToFile(fPO, IOUtils.toString(new URL(fileURL), AppConfig.CHARSET), AppConfig.CHARSET);
                fPO = null;

                LocalTime ltEnd = LocalTime.now();
                logger.trace(" " + ltStart.until(ltEnd, ChronoUnit.SECONDS) + "초");
            }

        } catch (IOException e) {
            if(e.getMessage().contains("Premature EOF")) {
                logger.warn("EOF 재시도");
                if(fPO.exists()) fPO.delete();
                try { Thread.sleep(1800000); } catch (InterruptedException e1) {  e1.printStackTrace(); }
                Utils.downloadPO(appWorkConfig, projectName);
            }
            else e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static long CRC32(File f) {
        Checksum crc = new CRC32();
        if(!f.exists() || f.length() <= 0) return -1;

        try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(f))) {
            byte[] buffer = new byte[32768];
            int length;

            while ((length = in.read(buffer)) >= 0) crc.update(buffer, 0, length);
        } catch (IOException e) { e.printStackTrace(); }
        return crc.getValue();
    }


    public static void processRun(File baseDirectory, String command) throws IOException, InterruptedException { processRun(baseDirectory, command, ProcessBuilder.Redirect.INHERIT); }

    public static void processRun(File baseDirectory, String command, ProcessBuilder.Redirect redirect) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder()
                .directory(baseDirectory)
                .command((command).split("\\s+"))
                .redirectError(redirect)
                .redirectOutput(redirect);
        pb.start().waitFor();
    }

    public static void makeCSV(File file, ToCSVConfig toCSVConfig, ArrayList<PO> poList) {
        StringBuilder sb = new StringBuilder("\"Location\",\"Source\",\"Target\"\n");
        for (PO p : poList) sb.append(p.toCSV(toCSVConfig));
        try { FileUtils.writeStringToFile(file, sb.toString(), AppConfig.CHARSET); } catch (IOException e) { e.printStackTrace(); }
    }

    /*
        @link makeCSV ㅎㅅㅎ
     */
    @Deprecated
    public static void makeFile(File file, ToCSVConfig toCSVConfig, ArrayList<PO> poList) { makeCSV(file, toCSVConfig, poList); }

    /*
        @link convertCN_PO_to_KO 쓰셈
     */
    @Deprecated
    public static void Mapping(AppWorkConfig appWorkConfig) { convertKO_PO_to_CN(appWorkConfig); }

    public static void convertKO_PO_to_CN(AppWorkConfig appWorkConfig) {

        Collection<File> fileList = FileUtils.listFiles(appWorkConfig.getPODirectory(), new String[]{"po"}, false);

        try {
            for (File file : fileList) {
                File po2 = new File(file.getAbsolutePath() + "2");
                if(!po2.exists() || po2.length() <= 0) FileUtils.write(po2, Utils.KOToCN(FileUtils.readFileToString(file, AppConfig.CHARSET)), AppConfig.CHARSET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    @link convertKO_PO_to_CN 쓰셈 이름 잘못지음ㅎㅎ
    */
    @Deprecated
    public static void convertCN_PO_to_KO(AppWorkConfig appWorkConfig) { convertKO_PO_to_CN(appWorkConfig); }

    public static String KOToCN(String string) {
        char[] c = string.toCharArray();
        for(int i=0; i < c.length; i++) if (c[i] >= 0xAC00 && c[i] <= 0xEA00) c[i] -= 0x3E00;
        return new String(c);
    }

    public static String CNtoKO(String string) {
        char[] c = string.toCharArray();
        for(int i=0; i < c.length; i++) if (c[i] >= 0x6E00 && c[i] <= 0xAC00) c[i] += 0x3E00;
        return new String(c);
    }


    public static void replaceStringFromMap(StringBuilder stringBuilder, Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object rawValue = entry.getValue();
            String value = rawValue instanceof PO ? ((PO) rawValue).getTarget() : rawValue instanceof String ? (String) rawValue : key;

            int start = stringBuilder.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                stringBuilder.replace(start, end, value);
                start = stringBuilder.indexOf(key, nextSearchStart);
            }
        }
    }

    public static HashMap<String, PO> sourceToMap(SourceToMapConfig config) {

        HashMap<String, PO> poMap = new HashMap<>();
        String fileName = FilenameUtils.getBaseName(config.getFile().getName());
        String source = sourceToMapParser(config);

        Matcher m = config.getPattern().matcher(source);
        boolean isPOPattern = config.getPattern() == (AppConfig.POPattern);
        while (m.find()) {
            PO po = new PO(m.group(2), m.group(6), m.group(7)).wrap(config.getPrefix(), config.getSuffix(), config.getPoWrapType());
            //po.setFileName(FileNames.fromString(fileName));
            po.setStringFileName(fileName);
            if(isPOPattern && m.group(1) != null && m.group(1).equals("#, fuzzy")) po.setFuzzy(true);
            poMap.put(m.group(config.getKeyGroup()), po);
        }

        return poMap;
    }

    private static String sourceToMapParser(SourceToMapConfig config) {

        String source = null;
        try {
            source = FileUtils.readFileToString(config.getFile(), AppConfig.CHARSET);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (config.isToLowerCase()) source = Objects.requireNonNull(source).toLowerCase();

        if (config.isProcessText()) {
            if (config.isProcessItemName()) source = Objects.requireNonNull(source).replaceAll("\\^[\\w]+", ""); // 아이템 명 뒤의 기호 수정
            source = Objects.requireNonNull(source).replaceAll("msgid \"\\\\+\"\n", "msgid \"\"\n") // "//" 이런식으로 되어있는 문장 수정. Extactor 에서 에러남.
                    .replaceAll("msgstr \"\\\\+\"\n", "msgstr \"\"\n") // "//" 이런식으로 되어있는 문장 수정. Extactor 에서 에러남.
                    .replaceAll("\\\\\"", "\"\"") // \" 로 되어있는 쌍따옴표 이스케이프 변환 "" 더블-더블 쿼테이션으로 이스케이프 시켜야함.
                    .replaceAll("\\\\\\\\", "\\\\"); // 백슬래쉬 두번 나오는거 ex) ESOUI\\ABC\\DEF 하나로 고침.

            // 주석삭제 안씀
            // if (config.isRemoveComment()) source = source.replaceAll(AppConfig.englishTitlePattern, "$1");
        }
        return source;

    }


    private void processRun(String command, File directory) throws IOException, InterruptedException { processRun(command, ProcessBuilder.Redirect.INHERIT, directory); }

    private void processRun(String command, @SuppressWarnings("SameParameterValue") ProcessBuilder.Redirect redirect, File directory) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder()
                .directory(directory)
                .command((command).split("\\s+"))
                .redirectError(redirect)
                .redirectOutput(redirect);
        pb.start().waitFor();
    }

    public static Properties setConfig(Path configPath, Map<String, String> config) {
        Properties properties = new Properties();
        try {
            if (Files.exists(configPath) && Files.size(configPath) > 0) try(var fis = new FileInputStream(configPath.toFile())) { properties.load(fis); } catch (Exception e) { e.printStackTrace(); }
            else try(var fos = new FileOutputStream(configPath.toFile())) {
                logger.info("설정 데이터 없음. 초기화");
                for(var entry : config.entrySet()) properties.setProperty(entry.getKey(), entry.getValue());
                properties.store(fos, "init");
            } catch (Exception e) { e.printStackTrace(); }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }


    public static void storeConfig(Path configPath, Properties properties) {
        try(var fos = new FileOutputStream(configPath.toFile())) {
            properties.store(fos, String.valueOf(new Date().getTime()));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
