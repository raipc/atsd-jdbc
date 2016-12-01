package com.axibase.tsd.driver.jdbc.ext;

import java.util.Properties;

import com.axibase.tsd.driver.jdbc.enums.AtsdDriverConnectionProperties;

import static com.axibase.tsd.driver.jdbc.enums.AtsdDriverConnectionProperties.*;

public class AtsdConnectionInfo {
	private final Properties info;

	public AtsdConnectionInfo(Properties info) {
		this.info = info;
	}

	public String host() {
		return (String) info.get("host");
	}

	public String url() {
		return (String) info.get("url");
	}

	public String user() {
		return propertyOrEmpty("user");
	}

	public String password() {
		return propertyOrEmpty("password");
	}

	public boolean trustCertificate() {
		final AtsdDriverConnectionProperties property = trustServerCertificate;
		final String result = info.getProperty(property.camelName());
		return result == null ? (Boolean) property.defaultValue() : Boolean.parseBoolean(result);
	}

	public int connectTimeout() {
		final AtsdDriverConnectionProperties property = connectTimeout;
		final String result = info.getProperty(property.camelName());
		return result == null ? (Integer) property.defaultValue() : Integer.parseInt(result);
	}

	public int readTimeout() {
		final AtsdDriverConnectionProperties property = readTimeout;
		final String result = info.getProperty(property.camelName());
		return result == null ? (Integer) property.defaultValue() : Integer.parseInt(result);
	}

	public String strategy() {
		final AtsdDriverConnectionProperties property = strategy;
		final String result = info.getProperty(property.camelName());
		return result == null ? (String) property.defaultValue() : result;
	}

	private String propertyOrEmpty(String key) {
		final String result = (String) info.get(key);
		return result == null ? "" : result;
	}
}
