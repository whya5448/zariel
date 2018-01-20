package org.metalscraps.eso.lang.kr;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.Utils.Utils;
import org.metalscraps.eso.lang.kr.config.AppConfig;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by 안병길 on 2017-12-21.
 * Whya5448@gmail.com
 */
public class MapperMain {

	private HashMap<Integer, StringBuilder> threadResultMap = new HashMap<>();

	public void start() {
		try {
			int cores = Runtime.getRuntime().availableProcessors();

			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setCurrentDirectory(FileUtils.getFile("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31"));
			//fc.setCurrentDirectory(FileUtils.getFile(FileUtils.getUserDirectoryPath() + sep + "desktop" + sep + "po"));
			fc.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}

				@Override
				public String getDescription() {
					return null;
				}
			});

			if (fc.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) System.exit(JFileChooser.CANCEL_OPTION);

			Collection<File> fileList = FileUtils.listFiles(fc.getSelectedFile(), new String[]{"po"}, false);

			for (File ff : fileList) {

				// 파일명
				System.out.println(ff);

				// 스레드 결과값 초기화
				threadResultMap.clear();

				// 파일 불러옴.
				StringBuilder source = new StringBuilder(FileUtils.readFileToString(ff, AppConfig.CHARSET));
				int length = source.length(), length_each = length / cores;

				// 파일별로 매핑하게 바꾼 이상 쓰레드 나눌 필요가 있는지? 테스트 해보기. 언젠간?
				ArrayList<StringBuilder> textList = new ArrayList<>();
				for (int i = 0; i < cores; i++) {
					StringBuilder sb;

					// 마지막 코어 텍스트 끝까지.
					if (i == cores - 1) sb = new StringBuilder(source.substring(length_each * i));

					// 그 외 코어 나눈대로 할당.
					else sb = new StringBuilder(source.substring(length_each * i, length_each * (i + 1)));
					textList.add(sb);
				}

				// 작업물 나눴으므로 초기화.
				source = new StringBuilder(length);

				int i = 0;
				for (StringBuilder sbb : textList) new Thread(new ReplaceMain().setStringBuilder(sbb).setOrder(++i).setMap(threadResultMap)).start();

				while (true) {
					if (threadResultMap.size() == cores) break;
					try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
				}

				for (i = 1; i <= cores; i++) source.append(threadResultMap.get(i));

				try {
					FileUtils.write(new File(ff.getAbsolutePath() + "2"), source, AppConfig.CHARSET);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new MapperMain().start();
	}

	/**
	 * Created by 안병길 on 2018-01-13.
	 * Whya5448@gmail.com
	 */

	@Data
	@Accessors(chain = true)
	public static class ReplaceMain implements Runnable {

		private StringBuilder stringBuilder;
		private int order;
		private Map<Integer, StringBuilder> map;

		@Override
		public void run() { map.put(order, Utils.replaceStringFromMap(stringBuilder, Utils.koToCnMap)); }
	}
}
