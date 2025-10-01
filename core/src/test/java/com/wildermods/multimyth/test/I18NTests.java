package com.wildermods.multimyth.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import com.wildermods.multimyth.I18N;

class I18NTest {

	private I18N i18n;

	@BeforeEach
	void setUp() throws IOException {
		try(InputStream in = getClass().getClassLoader().getResourceAsStream("i18n/en_US.lang")) {
			Properties translations = new Properties();
			if(in == null) {
				throw new IOException("Could not find resource: i18n/en_US.lang");
			}
			translations.load(in);
			i18n = new I18N(Locale.US, translations);
		}
	}

	@Test
	void format_NullText_ReturnsEmpty() {
		assertEquals("", i18n.format("", "test"));
	}

	@Test
	void format_EmptyText_ReturnsEmpty() {
		assertEquals("", i18n.format("", "test"));
	}

	@Test
	void format_NoPlaceholders_ReturnsOriginalText() {
		String text = "Hello World";
		assertEquals(text, i18n.format(text, "ignored"));
	}

	@Test
	void format_SinglePlaceholder_ReplacesCorrectly() {
		String result = i18n.format("Hello {0}", "World");
		assertEquals("Hello World", result);
	}

	@Test
	void format_MultiplePlaceholders_ReplacesCorrectly() {
		String result = i18n.format("{0} + {1} = {2}", 2, 3, 5);
		assertEquals("2 + 3 = 5", result);
	}

	@Test
	void format_SamePlaceholderMultipleTimes_ReplacesAllOccurrences() {
		String result = i18n.format("Warning: {0}. {1} is less than {0}", "minValue", "currentValue");
		assertEquals("Warning: minValue. currentValue is less than minValue", result);
	}

	@Test
	void format_NotEnoughObjects_LeavesUnfilledPlaceholders() {
		String result = i18n.format("Hello {0}, you have {1} messages", "Alice");
		assertEquals("Hello Alice, you have {1} messages", result);
	}

	@Test
	void format_NoObjects_LeavesAllPlaceholders() {
		String result = i18n.format("Hello {0}, you have {1} messages");
		assertEquals("Hello {0}, you have {1} messages", result);
	}

	@Test
	void format_NullObjectsArray_TreatsAsEmpty() {
		String result = i18n.format("Hello {0}", (Object[]) null);
		assertEquals("Hello {0}", result);
	}

	@Test
	void format_IndexOutOfBounds_LeavesPlaceholder() {
		String result = i18n.format("Values: {0}, {1}, {5}", "A", "B");
		assertEquals("Values: A, B, {5}", result);
	}

	@Test
	void format_NegativeIndex_LeavesPlaceholder() {
		String result = i18n.format("Invalid index: {-1}", "test");
		assertEquals("Invalid index: {-1}", result);
	}

	@Test
	void format_NonNumericPlaceholder_LeavesPlaceholder() {
		String result = i18n.format("Hello {name}, age {age}", "Alice");
		assertEquals("Hello {name}, age {age}", result);
	}

	@Test
	void format_MixedValidAndInvalidPlaceholders_HandlesCorrectly() {
		String result = i18n.format("Valid: {0}, Invalid: {abc}, Valid: {1}", "first", "second");
		assertEquals("Valid: first, Invalid: {abc}, Valid: second", result);
	}

	@Test
	void format_PlaceholderAtStartAndEnd_ReplacesCorrectly() {
		String result = i18n.format("{0} middle {1}", "start", "end");
		assertEquals("start middle end", result);
	}

	@Test
	void format_ConsecutivePlaceholders_ReplacesCorrectly() {
		String result = i18n.format("{0}{1}{2}", "a", "b", "c");
		assertEquals("abc", result);
	}

	@Test
	void format_EmptyPlaceholder_LeavesPlaceholder() {
		String result = i18n.format("Empty: {}", "test");
		assertEquals("Empty: {}", result);
	}

	@Test
	void format_UnclosedPlaceholder_LeavesAsText() {
		String result = i18n.format("Unclosed: {0", "test");
		assertEquals("Unclosed: {0", result);
	}

	@Test
	void format_OnlyOpeningBrace_LeavesAsText() {
		String result = i18n.format("Just {", "test");
		assertEquals("Just {", result);
	}

	@Test
	void format_OnlyClosingBrace_LeavesAsText() {
		String result = i18n.format("Just }", "test");
		assertEquals("Just }", result);
	}

	@Test
	void format_ComplexRealWorldExample_HandlesCorrectly() {
		String template = "User {0} has {1} items. Price: ${2}. Status: {3}. Contact: {4}";
		String result = i18n.format(template, "John", 5, 99.99, "active", "john@example.com");
		assertEquals("User John has 5 items. Price: $99.99. Status: active. Contact: john@example.com", result);
	}

	@Test
	void format_WithNullObjects_ReplacesWithNull() {
		String result = i18n.format("Value: {0}", new Object[] { null });
		assertEquals("Value: null", result);
	}

	@Test
	void format_WithDifferentObjectTypes_HandlesCorrectly() {
		String result = i18n.format("Int: {0}, Double: {1}, Boolean: {2}, String: {3}",  42, 3.14, true, "text");
		assertEquals("Int: 42, Double: 3.14, Boolean: true, String: text", result);
	}

	@Test
	void format_LargeIndexWithEnoughObjects_ReplacesCorrectly() {
		String result = i18n.format("{9}", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
		assertEquals("j", result);
	}

	@Test
	void format_PlaceholderWithSpaces_LeavesPlaceholder() {
		String result = i18n.format("Invalid: { 0 }", "test");
		assertEquals("Invalid: { 0 }", result);
	}

	@Test
	void format_ZeroIndexWithNoObjects_LeavesPlaceholder() {
		String result = i18n.format("Value: {0}");
		assertEquals("Value: {0}", result);
	}
}
