package org.metalscraps.eso.lang.kr;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by 안병길 on 2018-01-24.
 * Whya5448@gmail.com
 */

public class AppWorkConfig {

	AppWorkConfig() {
		LocalDate today = LocalDate.now();
		this.today = today.format(DateTimeFormatter.ofPattern("MMdd"));
		this.todayWithYear = today.format(DateTimeFormatter.ofPattern("yyMMdd"));
	}

	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC)
	private File baseDirectory, PODirectory;

	@Getter(AccessLevel.PUBLIC)
	private final String today, todayWithYear;
}
