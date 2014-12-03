package com.scienvo.sample.st.data;

public class SongTasteItem {
	public static final String mpsXml = "http://www.songtaste.com/api/android/songurl.php?songid=%s";
	public String ID;
	public String Name;
	public String UserId;
	public String Singer;

	public String getMp3Xml() {
		return String.format(mpsXml, ID);
	}
}
