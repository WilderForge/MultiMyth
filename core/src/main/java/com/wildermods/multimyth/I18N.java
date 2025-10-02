package com.wildermods.multimyth;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public record I18N(Locale locale, Properties translations, Data data) {
	
	public static I18N INSTANCE;
	static {
		try {
			INSTANCE = new I18N();
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize I18N", e);
		}
	}
	
	public I18N() throws IOException {
		this(Locale.getDefault());
	}
	
	public I18N(Locale locale) throws IOException {
		this(locale, loadTranslations(locale), new Data());
	}
	
	public I18N(Locale locale, Properties translations) {
		this(locale, translations, new Data());
	}
	
	public String itranslate(String key, Object... objects) {
		return iformat(
			translations.getProperty(key, "<" + locale + ":" + key + ">"),
			objects
		);
	}
	
	public String iformat(String text, Object... objects) {
		if(text == null) {
			return "";
		}
		if(objects == null || objects.length == 0) {
			return text;
		}
		
		StringBuilder ret = new StringBuilder();
		int i = 0;
		int length = text.length();
		
		while(i < length) {
			char currentChar =  text.charAt(i);
			
			if(currentChar == '{' && i + 2 < length) {
				int endIndex = text.indexOf('}', i + 1);
				if(endIndex != -1) {
					String content = text.substring(i + 1, endIndex);
					
					try {
						int index = Integer.parseInt(content);
						if(index >= 0 && index < objects.length) {
							ret.append(objects[index]);
							i = endIndex + 1;
							continue;
						}
					}
					catch(NumberFormatException e) {
						//swallow, leave placeholder unchanged
					}
				}
			}
			ret.append(currentChar);
			i++;
		}
		return ret.toString();
	}
	
	public BitmapFont igetFont() {
		if(data.font == null) {
			data.font = FontLocator.getFont(this);
		}
		return data.font;
	}
	
	public void ireload() throws IOException {
		Properties newTranslations = loadTranslations(this.locale);
		this.translations.clear();
		this.translations.putAll(newTranslations);
	}
	
	public static String format(String text, Object... objects) {
		return INSTANCE.iformat(text, objects);
	}
	
	public static String translate(String text, Object... objects) {
		return INSTANCE.itranslate(text, objects);
	}
	
	public static BitmapFont getFont() {
		return INSTANCE.igetFont();
	}
	
	//this code is ass
	public static Stream<Locale> supportedLocales() {
		FileHandle external = Gdx.files.external("i18n/");
		FileHandle local = Gdx.files.local("i18n/");
		FileHandle internal = Gdx.files.internal("i18n/");

		return Stream.concat(
				// Handle external and local directories normally
				Stream.of(external, local)
					.filter(FileHandle::exists)
					.filter(FileHandle::isDirectory)
					.flatMap(dir -> {
						FileHandle[] files = dir.list();
						return Arrays.stream(files);
					}
				),
					
				//special handling for 'internal' directories
				discoverInternalFiles(internal)
			)
			.filter(file -> "lang".equals(file.extension()))
			.map(file -> parseLocaleFromFilename(file))
			.filter(Objects::nonNull);
	   }
	
	private static Locale parseLocaleFromFilename(FileHandle file) {
		String[] loc = StringUtils.split(file.nameWithoutExtension(), '_');
		if (loc.length == 2) {
			Locale.Builder b = new Locale.Builder();
			b.setLanguage(loc[0]);
			b.setRegion(loc[1]);
			return b.build();
		}
		return null;
	}
	
	private static Stream<FileHandle> discoverInternalFiles(FileHandle internalDir) {
		try {
			InputStream stream = I18N.class.getClassLoader().getResourceAsStream(internalDir.path());
			if(stream == null) {
				return Stream.empty();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			return reader.lines().map(file -> Gdx.files.internal(internalDir.path() + "/" + file)).filter(FileHandle::exists);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void reload() throws IOException {
		INSTANCE.ireload();
	}
	
	public static Properties parseLangFile(FileHandle langFile) throws IOException {
		if(langFile.exists()) {
			String languageFile = langFile.readString(StandardCharsets.UTF_8.toString());
			Properties ret = new Properties();
			ret.load(new StringReader(languageFile));
			return ret;
		}
		else {
			throw new FileNotFoundException(langFile.path().toString());
		}
	}
	
	private static Properties loadTranslations(Locale locale) throws IOException {
		String fileName = locale.toString() + ".lang";
		
		// 1. First priority: Filesystem (external storage)
		FileHandle externalFile = Gdx.files.external("i18n/" + fileName);
		if (externalFile.exists()) {
			return parseLangFile(externalFile);
		}
		
		// 2. Second priority: Local filesystem (same directory as jar)
		FileHandle localFile = Gdx.files.local("i18n/" + fileName);
		if (localFile.exists()) {
			return parseLangFile(localFile);
		}
		
		// 3. Third priority: Internal (classpath) - built-in resources
		FileHandle internalFile = Gdx.files.internal("i18n/" + fileName);
		if (internalFile.exists()) {
			return parseLangFile(internalFile);
		}
		
		return new Properties();
	}
	
	private static class Data {
		public BitmapFont font;
	}
	
}
