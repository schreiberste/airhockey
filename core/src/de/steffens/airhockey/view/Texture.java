package de.steffens.airhockey.view;

public class Texture {

	public static Texture defaultFont;
	public static Texture defaultLogo;

	static {
		defaultFont = new Texture();
		defaultFont.setFile("./img/fonts/Font.bmp");
		
		defaultLogo = new Texture();
		defaultLogo.setFile("./img/Logo.bmp");
	}

	private String file;

	public void setFile(String file) {
		this.file = file;
	}

	public String getFile() {
		return file;
	}
}
