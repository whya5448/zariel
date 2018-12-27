package org.metalscraps.eso.lang.kr;

import java.io.IOException;
import java.nio.file.*;

/**
 * Created by 안병길 on 2018-01-26.
 * Whya5448@gmail.com
 */
class TTCTableWatcher {

	public static void main(String[] args) {

		final String base = "C:/Users/admin/Documents/Elder Scrolls Online/live/AddOns/TamrielTradeCentre/";
		WatchEvent.Kind<Path> ENTRY_DELETE = StandardWatchEventKinds.ENTRY_DELETE;
		Path backupTable = Paths.get(base+"Table_KR.lua");

		try {

			WatchService watcher = FileSystems.getDefault().newWatchService();
			Paths.get(base).register(watcher, ENTRY_DELETE);

			while (true) {
				WatchKey key;
				try {
					// wait for a key to be available
					key = watcher.take();
				} catch (InterruptedException ex) { return; }

				for (WatchEvent<?> event : key.pollEvents()) {
					// get file name
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path path = ev.context();

					if (event.kind() == ENTRY_DELETE && path.toString().equals("ItemLookUpTable_KR.lua"))
						Files.copy(backupTable, path);
				}

				// IMPORTANT: The key must be reset after processed
				if (!key.reset()) break;
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
}
