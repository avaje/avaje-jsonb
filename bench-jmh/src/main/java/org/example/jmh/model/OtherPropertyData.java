package org.example.jmh.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class OtherPropertyData {
	@JsonField
	private String prop1;
	@JsonField private String prop2;
	@JsonField private String prop3;
	@JsonField private String prop4;
	@JsonField private String prop5;
	@JsonField private String prop6;
	@JsonField private String prop7;
	@JsonField private String prop8;
	@JsonField private String prop9;
	@JsonField private String prop10;

	public OtherPropertyData() {
	}

	public OtherPropertyData(String prop1, String prop2, String property3, String property4, String property5, String property6, String property7, String property8, String property9, String property10) {
		this.prop1 = prop1;
		this.prop2 = prop2;
		this.prop3 = property3;
		this.prop4 = property4;
		this.prop5 = property5;
		this.prop6 = property6;
		this.prop7 = property7;
		this.prop8 = property8;
		this.prop9 = property9;
		this.prop10 = property10;
	}

	public String getProp1() {
		return prop1;
	}

	public void setProp1(String prop1) {
		this.prop1 = prop1;
	}

	public String getProp2() {
		return prop2;
	}

	public void setProp2(String prop2) {
		this.prop2 = prop2;
	}

	public String getProp3() {
		return prop3;
	}

	public void setProp3(String prop3) {
		this.prop3 = prop3;
	}

	public String getProp4() {
		return prop4;
	}

	public void setProp4(String prop4) {
		this.prop4 = prop4;
	}

	public String getProp5() {
		return prop5;
	}

	public void setProp5(String prop5) {
		this.prop5 = prop5;
	}

	public String getProp6() {
		return prop6;
	}

	public void setProp6(String prop6) {
		this.prop6 = prop6;
	}

	public String getProp7() {
		return prop7;
	}

	public void setProp7(String prop7) {
		this.prop7 = prop7;
	}

	public String getProp8() {
		return prop8;
	}

	public void setProp8(String prop8) {
		this.prop8 = prop8;
	}

	public String getProp9() {
		return prop9;
	}

	public void setProp9(String prop9) {
		this.prop9 = prop9;
	}

	public String getProp10() {
		return prop10;
	}

	public void setProp10(String prop10) {
		this.prop10 = prop10;
	}
}
