package org.metalscraps.eso.lang.tool.bean;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.metalscraps.eso.lang.lib.bean.PO;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class CategoryCSV {

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private String zanataFileName;
	private String type;
	private int linkCount;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	ArrayList<String> PoIndexList = new ArrayList<>();

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	HashMap<String, PO> PODataMap = new HashMap<>();

	public void addPoIndex(String Index){
		PoIndexList.add(Index);
	}

	public void putPoData(String index, PO po){
		PODataMap.put(index, po);
	}
}