package com.axibase.tsd.driver.jdbc.util;

import java.io.IOException;
import java.io.InputStream;

import com.axibase.tsd.driver.jdbc.content.json.Comments;
import com.axibase.tsd.driver.jdbc.content.json.GeneralError;
import com.axibase.tsd.driver.jdbc.content.json.QueryDescription;
import com.axibase.tsd.driver.jdbc.content.json.Version;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class JsonMappingUtil {
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final ObjectReader reader = mapper.reader();
	private static final MappingJsonFactory factory = new MappingJsonFactory(mapper);

	public static Comments mapToComments(String json) throws IOException {
		return reader.forType(Comments.class).readValue(json);
	}

	public static QueryDescription[] mapToQueryDescriptionArray(InputStream jsonIs) throws IOException {
		return reader.forType(QueryDescription[].class).readValue(jsonIs);
	}

	public static GeneralError mapToGeneralError(InputStream inputStream) throws IOException {
		return reader.forType(GeneralError.class).readValue(inputStream);
	}

	public static Version mapToVersion(InputStream jsonIs) throws IOException {
		return reader.forType(Version.class).readValue(jsonIs);
	}

	public static JsonParser getParser(String json) throws IOException {
		return factory.createParser(json);
	}
}
