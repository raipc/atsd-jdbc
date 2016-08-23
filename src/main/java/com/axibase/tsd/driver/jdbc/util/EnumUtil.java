package com.axibase.tsd.driver.jdbc.util;

import com.axibase.tsd.driver.jdbc.enums.AtsdType;
import com.axibase.tsd.driver.jdbc.enums.DefaultColumns;
import com.axibase.tsd.driver.jdbc.enums.ReservedWordsSQL2003;

import java.util.*;


public class EnumUtil {

	private static final Set<String> reservedWordsSql2003 = createSetFromEnum(ReservedWordsSQL2003.values());
	private static final Map<String, AtsdType> atsdNameTypeMapping = createAtsdNameTypeMapping();
	private static final Map<String, AtsdType> columnPrefixAtsdTypeMapping = createColumnPrefixAtsdTypeMapping();

	private EnumUtil() {}

	private static Map<String, AtsdType> createAtsdNameTypeMapping() {
		Map<String, AtsdType> mapping = new HashMap<>();
		for (AtsdType type : AtsdType.values()) {
			mapping.put(type.originalType, type);
		}
		return Collections.unmodifiableMap(mapping);
	}

	private static Map<String, AtsdType> createColumnPrefixAtsdTypeMapping() {
		Map<String, AtsdType> mapping = new HashMap<>();
		for (DefaultColumns type : DefaultColumns.values()) {
			mapping.put(type.getColumnNamePrefix(), type.getType());
		}
		return Collections.unmodifiableMap(mapping);
	}

	private static <T extends Enum<T>> Set<String> createSetFromEnum(T[] array ) {
		Set<String> set = new HashSet<>(array.length);
		for (T item : array) {
			set.add(item.name());
		}
		return Collections.unmodifiableSet(set);
	}

	private static boolean isTokenInSet(String token, Set<String> set) {
		return set.contains(token.toUpperCase(Locale.US));
	}

	public static boolean isReservedSqlToken(String token) {
		return isTokenInSet(token, reservedWordsSql2003);
	}

	public static AtsdType getAtsdTypeByOriginalName(String name) {
		AtsdType result = atsdNameTypeMapping.get(name);
		if (result == null) {
			result = AtsdType.STRING_DATA_TYPE; // use string type by default
		}
		return result;
	}

	public static AtsdType getAtsdTypeByColumnName(String columnName) {
		int dotIndex = columnName.indexOf('.');
		final String prefix = dotIndex == -1 ? columnName : columnName.substring(0, dotIndex);
		AtsdType type = columnPrefixAtsdTypeMapping.get(prefix);
		if (type == null) {
			type = AtsdType.STRING_DATA_TYPE;
		}
		return type;
	}

}
