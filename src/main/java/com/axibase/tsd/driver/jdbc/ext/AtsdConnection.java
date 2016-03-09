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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.calcite.avatica.AvaticaConnection;
import org.apache.calcite.avatica.AvaticaFactory;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.UnregisteredDriver;

import com.axibase.tsd.driver.jdbc.logging.LoggingFacade;

public class AtsdConnection extends AvaticaConnection {
	@SuppressWarnings("unused")
	private static final LoggingFacade logger = LoggingFacade.getLogger(AtsdConnection.class);
	protected static final Trojan TROJAN = createTrojan();
	protected final Properties info;
	
	protected AtsdConnection(UnregisteredDriver driver, AvaticaFactory factory, String url, Properties info) {
		super(driver, factory, url, info);
		this.info = info;
	}

	public Properties getInfo() {
		return info;
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return true;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return super.getMetaData();
	}
	
	protected Meta getMeta(){
		return TROJAN.getMeta(this);
	}

	@Override
	public void close() throws SQLException {
		super.close();
		AtsdMeta meta = (AtsdMeta) getMeta();
		meta.close();
	}
	

}