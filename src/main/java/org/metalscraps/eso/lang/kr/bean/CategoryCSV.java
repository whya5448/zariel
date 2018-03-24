package org.metalscraps.eso.lang.kr.bean;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.util.*;

@Data
public class CategoryCSV {

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	private String Category;

	@Setter(AccessLevel.PUBLIC) @Getter(AccessLevel.PUBLIC)
	ArrayList<String> PoIndexList, CSVList = null;

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

}