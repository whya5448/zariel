package org.metalscraps.eso.lang.lib.config;

import lombok.*;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */

@Data
public class AppWorkConfig {

	public AppWorkConfig() {
		this.today = dateTime.format(DateTimeFormatter.ofPattern("MMdd"));
		this.todayWithYear = dateTime.format(DateTimeFormatter.ofPattern("yyMMdd"));
	}

	private Path baseDirectory;
	private Path PODirectory;
	private Path ZanataCategoryConfigDirectory;

	@Getter(AccessLevel.PUBLIC)
	private final String today, todayWithYear;

	@Getter(AccessLevel.PUBLIC)
	private LocalDateTime dateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

	public Path getBaseDirectoryToPath() {
		return baseDirectory;
	}

	public void setBaseDirectoryToPath(Path baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public Path getPODirectoryToPath() {
		return PODirectory;
	}

	public void setPODirectoryToPath(Path PODirectory) {
		this.PODirectory = PODirectory;
	}

	public Path getZanataCategoryConfigDirectoryToPath() {
		return ZanataCategoryConfigDirectory;
	}

	public void setZanataCategoryConfigDirectoryToPath(Path zanataCategoryConfigDirectory) {
		ZanataCategoryConfigDirectory = zanataCategoryConfigDirectory;
	}

	@Deprecated public File getBaseDirectory() {
		return baseDirectory.toFile();
	}

	@Deprecated public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory.toPath();
	}

	@Deprecated public File getPODirectory() {
		return PODirectory.toFile();
	}

	@Deprecated public void setPODirectory(File PODirectory) {
		this.PODirectory = PODirectory.toPath();
	}

	@Deprecated public File getZanataCategoryConfigDirectory() {
		return ZanataCategoryConfigDirectory.toFile();
	}

	@Deprecated public void setZanataCategoryConfigDirectory(File zanataCategoryConfigDirectory) {
		ZanataCategoryConfigDirectory = zanataCategoryConfigDirectory.toPath();
	}

}
