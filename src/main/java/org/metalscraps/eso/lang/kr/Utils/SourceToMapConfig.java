package org.metalscraps.eso.lang.kr.Utils;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by 안병길 on 2018-01-20.
 * Whya5448@gmail.com
 */

@Data
@Accessors(chain = true)
public class SourceToMapConfig {

	private File file = null;
	private int keyGroup = 1;
	private boolean
			processText = true,
			processItemName = true,
			addFileNameToTitle = false,
			toLowerCase = false;
	private Pattern pattern = null;
}
