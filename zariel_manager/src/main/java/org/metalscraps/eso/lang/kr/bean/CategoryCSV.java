package org.metalscraps.eso.lang.kr.bean;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Data
public class CategoryCSV {


	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private String zanataFileName;
	private String type;


	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	ArrayList<String> PoIndexList, CSVList = null;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	ArrayList<PO> PODataList = null;


	public void addPoIndex(String Index){
		if(this.PoIndexList == null){
			this.PoIndexList = new ArrayList<>();
		}
		PoIndexList.add(Index);
	}

	public void addCSV(String oneCSVItem){
		if(this.PoIndexList == null){
			this.PoIndexList = new ArrayList<>();
		}
		PoIndexList.add(oneCSVItem);
	}

	public void addPoData(PO po){
		if(this.PODataList == null){
			this.PODataList = new ArrayList<>();
		}
		PODataList.add(po);
	}
}