package org.metalscraps.eso.lang.kr.bean;

import com.sun.istack.internal.Nullable;
import lombok.Data;

/**
 * Created by 안병길 on 2018-01-18.
 * Whya5448@gmail.com
 */

@Data
public class PO implements Comparable {
	public PO(String id, String source, String target) {
		source = source.replaceAll("\"\n\"", "");
		target = target.replaceAll("\"\n\"", "");

		this.id = id;
		this.source = source;
		this.target = target;

		String[] ids = id.split("-");
		id1 = Integer.parseInt(ids[0]);
		id2 = Integer.parseInt(ids[1]);
		id3 = Integer.parseInt(ids[2]);

		if(target.equals("")) this.target = source;
		else if(source.equals("")) this.source = target;
	}

	private String id, source, target;
	private Integer id1, id2, id3;

	public void wrap(@Nullable String prefix, @Nullable String suffix) {
		if (prefix == null) prefix = "";
		if (suffix == null) suffix = "";

		if(!source.equals("")) source = prefix + source + suffix;
		if(!target.equals("")) target = prefix + target + suffix;
	}

	@Override
	public String toString() { return toCSV(true); }
	public String toCSV(boolean t) { return "\""+id+"\",\""+(t?source:"")+"\",\""+target+"\"\n"; }

	@Override
	public int compareTo(Object o) {
		PO x = (PO) o;
		PO t = this;
		if(t.id1.equals(x.id1)) {
			if(t.id2.equals(x.id2)) return t.id3.compareTo(x.id3);
			else return t.id2.compareTo(x.id2);
		} else return t.id1.compareTo(x.id1);

	}
}