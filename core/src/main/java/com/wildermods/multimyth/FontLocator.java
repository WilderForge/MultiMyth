package com.wildermods.multimyth;

import java.net.URI;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.util.autodetect.FontFileFinder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontLocator {

	public static void getFonts(I18N localization) {
		FontFileFinder fileFinder = new FontFileFinder();
		TTFParser parser = new TTFParser();
		
		fileFinder.find().parallelStream().filter((font) -> {
			String path = font.getPath();
			return path.contains("CJK");
		}).forEach((font) -> {
			System.out.println(font);
		});;
	}
	
	
	public static BitmapFont getFont(I18N localization) {
		Properties properties = localization.translations();
		String must = properties.getProperty("special.font.contain.must");
		String mustNot = properties.getProperty("special.font.contain.must.not");
		String prefer = properties.getProperty("special.font.prefer");
		
		if(prefer == null) {
			if(must != null || mustNot != null) {
				throw new IllegalArgumentException("`must` or `must.not` has been set, but there is no `prefer`!");
			}
			System.out.println("No font constraints found, fallback to libgdx defaults (" + localization.toString() + ")");
			return null;
		}
		
		FontParser fontParser = new FontParser(must, mustNot, prefer);
		FontFileFinder fileFinder = new FontFileFinder();
		Set<URI> matching = fileFinder.find().stream().filter((font) -> {
			String fontName = Path.of(font).getFileName().toString();
			return fontParser.test(fontName);
		}).collect(Collectors.toSet());
		
		if (matching.isEmpty()) {
			System.out.println("Could not locate suitable font using constraints, fallback to libgdx defaults");
			return null;
		}
		
		int highestScore = -1;
		URI highest = null;
		for(URI font : matching) {
			int score = fontParser.score(Path.of(font).getFileName().toString());
			if(score > highestScore) {
				highestScore = score;
				highest = font;
			}
		}
		
		if(highestScore == -1 || highest == null) {
			throw new AssertionError();
		}
		
		int size = 12;
		try {
			size = Integer.parseInt(properties.getProperty("special.font.size", "12"));
		} catch(NumberFormatException e) {
			//swallow
		}
		
		BitmapFont font =  createBitmapFont(highest, size);
		if(font == null) {
			System.out.println("Could not create bitmap font!");
		}
		else {
			System.out.println(localization.locale() + " - Using font: " + font);
		}
		return font;
	}
	
	public static BitmapFont createBitmapFont(URI fontFile, int fontSize) {
		try {
			FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute(Path.of(fontFile).toAbsolutePath().toString()));
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.incremental = true; //needed for japanese text and such
			parameter.size = fontSize;
			
			BitmapFont font = generator.generateFont(parameter);
			return font;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	//TODO: Unit tests?
	private static class FontParser implements Predicate<String> {
	
		private final String must;
		private final String mustNot;
		private final String prefer;
	
		public FontParser(String must, String mustNot, String prefer) {
			this.must = must != null ? must.toLowerCase() : "";
			this.mustNot = mustNot != null ? mustNot.toLowerCase() : "";
			this.prefer = prefer != null ? prefer.toLowerCase() : "";
		}
	
		/** Check if a font satisfies all [must] conditions and none of the [must.not] conditions */
		@Override
		public boolean test(String fontName) {
			String lowerFont = fontName.toLowerCase();
	
			// Check must (OR inside brackets)
			if (!must.isEmpty() && !checkGroups(lowerFont, must, false)) {
				return false; // rejected because it does not satisfy a required group
			}
			
			// Check must-not first (OR inside brackets)
			if (!mustNot.isEmpty() && !checkGroups(lowerFont, mustNot, true)) {
				return false; // excluded because it contains a forbidden group
			}
	
			return true;
		}
	
		/** Returns a score based on [prefer] rules */
		public int score(String fontName) {
			String lowerFont = fontName.toLowerCase();
			return scoreGroups(lowerFont, prefer);
		}
	
		/** Helper: checks bracket groups */
		private boolean checkGroups(String text, String rules, boolean isMustNot) {
			Pattern pattern = Pattern.compile("\\[(.*?)]");
			Matcher matcher = pattern.matcher(rules);
	
			while (matcher.find()) {
				String group = matcher.group(1); // inside brackets
				String[] options = group.split("\\|");
				boolean matched = false;
	
				for (String option : options) {
					if (text.contains(option)) {
						matched = true;
						break;
					}
				}
	
				if (isMustNot && matched) return false; // forbidden group matched
				if (!isMustNot && !matched) return false; // required group missing
			}
	
			return true;
		}
	
		/** Helper: calculate score for [prefer] groups */
		private int scoreGroups(String text, String rules) {
			int score = 0;
			if (rules == null || rules.isEmpty()) return 0;
	
			Pattern pattern = Pattern.compile("\\[(.*?)]");
			Matcher matcher = pattern.matcher(rules);
	
			while (matcher.find()) {
				String group = matcher.group(1);
				String[] options = group.split("\\|");
	
				for (String option : options) {
					if (text.contains(option)) {
						score++;
						break; // only count 1 per group
					}
				}
			}
	
			return score;
		}
	}
	
}
