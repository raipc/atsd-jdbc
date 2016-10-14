package com.axibase.tsd.driver.jdbc.content.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
		"error"
})
public class GeneralError {
	@JsonProperty("error")
	private String error;

	@JsonProperty("error")
	public String getError() {
		return error;
	}

	@JsonProperty("error")
	public void setError(String error) {
		this.error = error;
	}

	public static String errorFromInputStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
		final String json = outputStream.toString(StandardCharsets.UTF_8.name());
		final GeneralError errorObject = new ObjectMapper().readValue(json, GeneralError.class);
		return errorObject.getError();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (error != null) {
			result = prime + error.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof GeneralError)) {
			return false;
		}
		final GeneralError other = (GeneralError) o;
		if (this.error == null) {
			return other.error == null;
		}
		return this.error.equals(other.error);
	}

	@Override
	public String toString() {
		return "GeneralError [error=" + error + "]";
	}
}
