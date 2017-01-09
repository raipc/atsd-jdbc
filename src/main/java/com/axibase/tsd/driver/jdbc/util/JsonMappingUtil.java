package com.axibase.tsd.driver.jdbc.util;

import java.io.IOException;
import java.io.InputStream;

import com.axibase.tsd.driver.jdbc.content.json.Comments;
import com.axibase.tsd.driver.jdbc.content.json.GeneralError;
import com.axibase.tsd.driver.jdbc.content.json.QueryDescription;
import com.axibase.tsd.driver.jdbc.content.json.Version;
import org.apache.calcite.avatica.com.fasterxml.jackson.core.JsonParser;
import org.apache.calcite.avatica.com.fasterxml.jackson.databind.MappingJsonFactory;
import org.apache.calcite.avatica.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.calcite.avatica.com.fasterxml.jackson.databind.ObjectReader;

public class JsonMappingUtil {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final ObjectReader READER = MAPPER.reader();
	private static final MappingJsonFactory JSON_FACTORY = new MappingJsonFactory(MAPPER);

	public static Comments mapToComments(String json) throws IOException {
		return READER.forType(Comments.class).readValue(json);
	}

	public static QueryDescription[] mapToQueryDescriptionArray(InputStream jsonIs) throws IOException {
		return READER.forType(QueryDescription[].class).readValue(jsonIs);
	}

	public static GeneralError mapToGeneralError(InputStream inputStream) throws IOException {
		return READER.forType(GeneralError.class).readValue(inputStream);
	}

	public static Version mapToVersion(InputStream jsonIs) throws IOException {
		return READER.forType(Version.class).readValue(jsonIs);
	}

	public static JsonParser getParser(String json) throws IOException {
		return JSON_FACTORY.createParser(json);
	}
}
