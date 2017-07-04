/*
* Copyright 2016 Axibase Corporation or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License").
* You may not use this file except in compliance with the License.
* A copy of the License is located at
*
* https://www.axibase.com/atsd/axibase-apache-2.0.pdf
*
* or in the "license" file accompanying this file. This file is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/
package com.axibase.tsd.driver.jdbc.content;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.axibase.tsd.driver.jdbc.enums.MetadataFormat;
import com.axibase.tsd.driver.jdbc.ext.AtsdConnectionInfo;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static com.axibase.tsd.driver.jdbc.DriverConstants.*;

@Data
public class ContentDescription {
	private static final LoggingFacade logger = LoggingFacade.getLogger(ContentDescription.class);

	private String host;
	private String query;
	private String login;
	private String password;
	private long contentLength;
	private String[] headers;
	private String jsonScheme;
	private final String metadataFormat;
	private long maxRowsCount;
	private final String queryId;
	@Getter(AccessLevel.NONE)
	private final AtsdConnectionInfo atsdConnectionInfo;

	public ContentDescription(String host, AtsdConnectionInfo atsdConnectionInfo) {
		this(host, atsdConnectionInfo, "", 0, "");
	}

	public ContentDescription(AtsdConnectionInfo atsdConnectionInfo, String query, StatementContext context) {
		this(atsdConnectionInfo.host() , atsdConnectionInfo, query, context.getVersion(), context.getQueryId());
	}

	private ContentDescription(String host, AtsdConnectionInfo atsdConnectionInfo, String query, int atsdVersion, String queryId) {
		this.host = host;
		this.query = query;
		this.login = atsdConnectionInfo.user();
		this.password = atsdConnectionInfo.password();
		this.metadataFormat = MetadataFormat.EMBED.name();
		this.atsdConnectionInfo = atsdConnectionInfo;
		this.queryId = queryId;
	}

	public String getEncodedQuery() {
		try {
			return URLEncoder.encode(query, DEFAULT_CHARSET.name());
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			return query;
		}
	}

	public String getPostParams() {
		if (StringUtils.isEmpty(query)) {
			return "";
		}
		return QUERY_ID_PARAM_NAME + '=' + queryId + '&' +
				Q_PARAM_NAME + '=' + getEncodedQuery() + '&' +
				FORMAT_PARAM_NAME + '=' + FORMAT_PARAM_VALUE + '&' +
				METADATA_FORMAT_PARAM_NAME + '=' + metadataFormat + '&' +
				LIMIT_PARAM_NAME + '=' + maxRowsCount;
	}

	public String getCancelQueryUrl() {
		return host + CANCEL_METHOD + '?' + QUERY_ID_PARAM_NAME + '=' + queryId;
	}

	public Map<String, String> getQueryParamsAsMap() {
		if (StringUtils.isEmpty(query)) {
			return Collections.emptyMap();
		}
		Map<String, String> map = new HashMap<>();
		map.put(Q_PARAM_NAME, query);
		map.put(FORMAT_PARAM_NAME, FORMAT_PARAM_VALUE);
		map.put(METADATA_FORMAT_PARAM_NAME, metadataFormat);
		map.put(LIMIT_PARAM_NAME, Long.toString(maxRowsCount));
		return map;
	}

	public boolean isSsl() {
		return StringUtils.startsWithIgnoreCase(host, "https://");
	}

	public boolean isTrusted() {
		return atsdConnectionInfo.trustCertificate();
	}

	public int getConnectTimeout() {
		return atsdConnectionInfo.connectTimeout();
	}

	public int getReadTimeout() {
		return atsdConnectionInfo.readTimeout();
	}

	public String getStrategyName() {
		return atsdConnectionInfo.strategy();
	}

	public String getQueryId() {
		return queryId;
	}

}
