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
package com.axibase.tsd.driver.jdbc.intf;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.axibase.tsd.driver.jdbc.content.StatementContext;
import com.axibase.tsd.driver.jdbc.ext.AtsdException;
import org.apache.calcite.avatica.ColumnMetaData;

public interface IStoreStrategy extends AutoCloseable {

	String[] openToRead(List<ColumnMetaData> metadataList) throws IOException;

	List<Object[]> fetch(long from, int size) throws IOException, AtsdException;

	void store(InputStream is) throws IOException;

	StatementContext getContext();
}