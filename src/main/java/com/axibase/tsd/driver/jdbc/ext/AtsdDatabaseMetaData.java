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
package com.axibase.tsd.driver.jdbc.ext;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaDatabaseMetaData;
import org.apache.calcite.avatica.ConnectionConfig;
import org.apache.commons.lang3.StringUtils;

import static com.axibase.tsd.driver.jdbc.DriverConstants.*;
import com.axibase.tsd.driver.jdbc.content.ContentDescription;
import com.axibase.tsd.driver.jdbc.content.json.Version;
import com.axibase.tsd.driver.jdbc.enums.LexerTokens;
import com.axibase.tsd.driver.jdbc.enums.NumericFunctions;
import com.axibase.tsd.driver.jdbc.enums.ReservedWordsSQL2003;
import com.axibase.tsd.driver.jdbc.enums.TimeDateEnums;
import com.axibase.tsd.driver.jdbc.intf.IContentProtocol;
import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;
import com.axibase.tsd.driver.jdbc.protocol.ProtocolFactory;
import com.axibase.tsd.driver.jdbc.protocol.SdkProtocolImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AtsdDatabaseMetaData extends AvaticaDatabaseMetaData {
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtsdDatabaseMetaData.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final ReservedWordsSQL2003[] FILTERED_KEYWORDS = ReservedWordsSQL2003.values();
	private String revision = "Unknown Revision";
	private String edition = "Unknown Edition";

	protected AtsdDatabaseMetaData(AvaticaConnection connection) {
		super(connection);
	}

	public void init(AvaticaConnection connection) {
		final ConnectionConfig config = connection.config();
		assert config != null;
		final Properties info = ((AtsdConnection) connection).getInfo();
		String urlSuffix = config.url();
		final String host = urlSuffix.substring(0,
				urlSuffix.indexOf("/", urlSuffix.indexOf(PROTOCOL_SEPARATOR) + PROTOCOL_SEPARATOR.length()))
				+ VERSION_ENDPOINT;
		String user = info != null ? (String) info.get("user") : "";
		String pass = info != null ? (String) info.get("password") : "";
		final String[] parts = urlSuffix.split(PARAM_SEPARATOR);
		String[] params = new String[parts.length - 1];
		if (parts.length > 1) {
			System.arraycopy(parts, 1, params, 0, parts.length - 1);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("[init] host: " + parts[0]);
			logger.trace("[init] params: " + params.length);
		}
		initVersions(host, user, pass, params);
	}

	private void initVersions(final String host, String user, String pass, String[] params) {
		final ContentDescription cd = new ContentDescription(host, "", user, pass, params);
		final IContentProtocol protocol = ProtocolFactory.create(SdkProtocolImpl.class, cd);
		try {
			final InputStream is = protocol.readInfo();
			final Version version = mapper.readValue(is, Version.class);
			if (logger.isTraceEnabled())
				logger.trace("[initVersions] " + version.toString());
			edition = version.getLicense().getProductVersion();
			revision = version.getBuildInfo().getRevisionNumber();
			if (logger.isDebugEnabled()) {
				logger.debug("[initVersions] edition: " + edition);
				logger.debug("[initVersions] revision: " + revision);
			}
		} catch (final AtsdException | GeneralSecurityException | IOException e) {
			if (logger.isDebugEnabled())
				logger.debug(e.getMessage());
		}
	}

	@Override
	public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
			throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("[getTables]");
		return super.getTables(catalog, schemaPattern, tableNamePattern, types);
	}

	@Override
	public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("[getSchemas]");
		return super.getSchemas(catalog, schemaPattern);
	}

	@Override
	public ResultSet getSchemas() throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("[getSchemas]");
		return super.getSchemas();
	}

	@Override
	public ResultSet getCatalogs() throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("[getCatalogs]");
		return super.getCatalogs();
	}

	@Override
	public ResultSet getTableTypes() throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("[getTableTypes]");
		return super.getTableTypes();
	}

	@Override
	public ResultSet getTypeInfo() throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("[getTypeInfo]");
		return super.getTypeInfo();
	}

	@Override
	public String getDatabaseProductName() throws SQLException {
		return super.getDatabaseProductName();
	}

	@Override
	public String getDatabaseProductVersion() throws SQLException {
		if (logger.isDebugEnabled())
			logger.debug("[getDatabaseProductVersion]");
		return String.format("%s, %s, %s: %s", super.getDatabaseProductVersion(), edition, REVISION_LINE, revision);
	}

	@Override
	public int getDatabaseMajorVersion() throws SQLException {
		try {
			return Integer.parseInt(revision);
		} catch (NumberFormatException e) {
			if (logger.isDebugEnabled())
				logger.debug(e.getMessage());
		}
		return 1;
	}

	@Override
	public String getDriverName() throws SQLException {
		return super.getDriverName();
	}

	@Override
	public String getDriverVersion() throws SQLException {
		return super.getDriverVersion();
	}

	@Override
	public int getDriverMajorVersion() {
		return super.getDriverMajorVersion();
	}

	@Override
	public int getDriverMinorVersion() {
		return super.getDriverMinorVersion();
	}

	@Override
	public String getSQLKeywords() throws SQLException {
		LexerTokens[] values = LexerTokens.values();
		List<String> keywords = new ArrayList<>();
		for (LexerTokens value : values) {
			String name = value.name();
			if (!filtered(name))
				keywords.add(name);
		}
		return StringUtils.join(keywords, ',');
	}

	@Override
	public String getNumericFunctions() throws SQLException {
		NumericFunctions[] values = NumericFunctions.values();
		List<String> keywords = new ArrayList<>();
		for (NumericFunctions value : values) {
			keywords.add(value.name());
		}
		return StringUtils.join(keywords, ',');
	}

	@Override
	public String getStringFunctions() throws SQLException {
		return super.getStringFunctions();
	}

	@Override
	public String getSystemFunctions() throws SQLException {
		return super.getSystemFunctions();
	}

	@Override
	public String getTimeDateFunctions() throws SQLException {
		TimeDateEnums[] values = TimeDateEnums.values();
		List<String> keywords = new ArrayList<>();
		for (TimeDateEnums value : values) {
			keywords.add(value.name());
		}
		return StringUtils.join(keywords, ',');
	}

	@Override
	public boolean allProceduresAreCallable() throws SQLException {
		return false;
	}

	@Override
	public boolean allTablesAreSelectable() throws SQLException {
		return false;
	}

	@Override
	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		return false;
	}

	@Override
	public boolean locatorsUpdateCopy() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsConvert() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
		return super.supportsDataManipulationTransactionsOnly();
	}

	@Override
	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsExpressionsInOrderBy() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsMinimumSQLGrammar() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsCoreSQLGrammar() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsExtendedSQLGrammar() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsANSI92FullSQL() throws SQLException {
		return false;
	}

	@Override
	public boolean isCatalogAtStart() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsCorrelatedSubqueries() throws SQLException {
		return false;
	}

	@Override
	public boolean storesLowerCaseIdentifiers() throws SQLException {
		return true;
	}

	@Override
	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		return true;
	}

	@Override
	public boolean storesUpperCaseIdentifiers() throws SQLException {
		return false;
	}

	@Override
	public int getDefaultTransactionIsolation() throws SQLException {
		return Connection.TRANSACTION_NONE;
	}

	@Override
	public boolean supportsSubqueriesInComparisons() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSubqueriesInExists() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSubqueriesInIns() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSchemasInDataManipulation() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsTableCorrelationNames() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsTransactions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsUnion() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsUnionAll() throws SQLException {
		return false;
	}

	@Override
	public boolean usesLocalFiles() throws SQLException {
		return false;
	}

	@Override
	public boolean usesLocalFilePerTable() throws SQLException {
		return false;
	}

	private boolean filtered(String name) {
		for (ReservedWordsSQL2003 filter : FILTERED_KEYWORDS) {
			if (!name.equals(filter.name()))
				continue;
			return true;
		}
		return false;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

}
