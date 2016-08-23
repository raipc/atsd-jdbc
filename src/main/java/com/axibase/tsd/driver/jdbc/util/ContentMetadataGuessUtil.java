package com.axibase.tsd.driver.jdbc.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ContentMetadataGuessUtil {
	private static final String SELECT = "SELECT ";
	private static final String FROM = " FROM ";
	private static final Pattern SINGLE_QUOTED_TABLE_NAME_PATTERN = Pattern.compile("'([^\']*?)[^\\\\]'");
	private static final Pattern DOUBLE_QUOTED_TABLE_NAME_PATTERN = Pattern.compile("\"([^\"]*?)[^\\\\]\"");
	private static final Pattern TABLE_NAME_PATTERN_WITHOUT_QUOTES = Pattern.compile("(\\S*?)\\s");

	private ContentMetadataGuessUtil() {

	}

	public static Pair<String, String[]> findTableNameAndFields(String query) {
		if (hasWildcard(query)) {
			throw new IllegalArgumentException("Cannot process generate default schema for query with wildcards");
		}
		int endOfSelect = StringUtils.indexOfIgnoreCase(query, SELECT);
		if (endOfSelect == -1) {
			throw new IllegalArgumentException("Query should contain SELECT statement");
		}
		int startOfFrom = StringUtils.indexOfIgnoreCase(query, FROM, endOfSelect);
		if (startOfFrom == -1) {
			throw new IllegalArgumentException("Query should contain datasource");
		}
		String fieldNamesGroup = query.substring(endOfSelect + SELECT.length(), startOfFrom).trim();
		String[] fieldNames = fieldNamesGroup.split("\\s*,\\s*");

		final int endOfFrom = startOfFrom + FROM.length();
		final String tableName = findTableName(query, endOfFrom);

		return new ImmutablePair<>(tableName, fieldNames);

	}

	private static String findTableName(String query, int startIndex){
		int index = startIndex;
		char processedChar = query.charAt(index);
		while (isSpaceCharacter(processedChar)) {
			++index;
			processedChar = query.charAt(index);
		}
		switch (processedChar) {
			case '\'' :
				return getUnquotedTableName(SINGLE_QUOTED_TABLE_NAME_PATTERN, query, index);
			case '"' :
				return getUnquotedTableName(DOUBLE_QUOTED_TABLE_NAME_PATTERN, query, index);
			default :
				return getUnquotedTableName(TABLE_NAME_PATTERN_WITHOUT_QUOTES, query, index);
		}
	}

	private static String getUnquotedTableName(Pattern pattern, String query, int startIndex) {
		String result = null;
		Matcher matcher = pattern.matcher(query);
		if (matcher.find(startIndex)) {
			result = matcher.group(1);
		}
		return result;
	}

	private static boolean isSpaceCharacter(char c) {
		return c == ' ' || c == '\t';
	}

	private static boolean hasWildcard(String query) {
		return query.indexOf('*') != -1;
	}
}
