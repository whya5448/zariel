package org.metalscraps.eso.lang.kr;

import org.apache.commons.io.FileUtils;
import org.metalscraps.eso.lang.kr.config.AppConfig;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by 안병길 on 2017-12-21.
 * Whya5448@gmail.com
 */
public class MapperMain {

	static HashMap<String, String> map = new HashMap<>();
	static HashMap<Integer, StringBuilder> resList = new HashMap<>();


	private static final String pattern = "msgctxt \"([0-9-]+)\"\\n*?msgid \"{1,2}?\\n?([\\s\\S]*?)\"\\n*?msgstr \"{1,2}?\\n?([\\s\\S]*?)\"\\n{2,}";


	public static void main(String[] args) throws Exception {

		int cores = Runtime.getRuntime().availableProcessors();

		for(int i=0; i<11172; i++) {
			// 한자=>한글
			//map.put(new String(Character.toChars(0x6E00+i)), new String(Character.toChars(0xAC00+i)));

			// 한글=>한자
			map.put(new String(Character.toChars(0xAC00+i)), new String(Character.toChars(0x6E00+i)));
		}

		System.out.println(map);

		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//fc.setCurrentDirectory(FileUtils.getFile(FileUtils.getUserDirectoryPath() + sep + "desktop" + sep + "po"));
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory(); // || FilenameUtils.getExtension(f.getName()).equals("csv");
			}

			@Override
			public String getDescription() { return null; }
		});

		fc.setCurrentDirectory(FileUtils.getFile("C:\\Users\\admin\\Documents\\Elder Scrolls Online\\live\\works\\EsoExtractData v0.31"));
		if (fc.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) System.exit(JFileChooser.CANCEL_OPTION);

		Collection<File> fileList = FileUtils.listFiles(fc.getSelectedFile(), new String[]{"po"}, false);

		for(File ff : fileList) {
			resList.clear();
			System.out.println(ff);
			StringBuilder sb = new StringBuilder(FileUtils.readFileToString(ff, AppConfig.CHARSET));
			int length = sb.length();
			int length_each = length/cores;

			ArrayList<StringBuilder> arrayList = new ArrayList<>();
			for(int i=0; i<cores; i++) {
				StringBuilder sb_each;
				if(i == cores-1) sb_each = new StringBuilder(sb.substring(length_each*i));
				else sb_each = new StringBuilder(sb.substring(length_each*i, length_each*(i+1)));
				arrayList.add(sb_each);
			}

			// 작업물 나눴으므로 초기화.
			sb = new StringBuilder(length);

			int i = 0;
			for(StringBuilder sbb : arrayList) {
				ReplaceMain rm = new ReplaceMain();
				rm.sb = sbb;
				rm.order = ++i;
				new Thread(rm).start();
			}

			while(true) {
				if(resList.size() == cores) break;
				try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
			}

			for(i=1; i<=cores; i++) sb.append(resList.get(i).toString());

			try {
				FileUtils.write(new File(ff.getAbsolutePath()+"2"), sb, StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * Created by 안병길 on 2018-01-13.
	 * Whya5448@gmail.com
	 */
	public static class ReplaceMain implements Runnable {

		StringBuilder sb;
		public int order;

		@Override
		public void run() {

			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				int start = sb.indexOf(key, 0);
				while (start > -1) {
					int end = start + key.length();
					int nextSearchStart = start + value.length();
					sb.replace(start, end, value);
					start = sb.indexOf(key, nextSearchStart);
				}
			}
			resList.put(order, sb);
		}
	}
}
