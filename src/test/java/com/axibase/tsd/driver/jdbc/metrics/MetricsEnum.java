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
package com.axibase.tsd.driver.jdbc.metrics;

public enum MetricsEnum {
	AWS_TABLE("aws_ec2.cpuutilization.average"),
	HADOOP_TABLE("hadoop.datanode.ps_eden_space.collectionusagethresholdsupported"),
	JMX_TABLE("jmx.metricsmap.http.thread_pool_used_percent"),
	NO_TABLE("no2"),
	OEM_TABLE("oem.host.total_disk_usage.total_disk_space_available_across_all_local_filesystems_in_mb"),
	SCOM_TABLE("scom.sqlserver.mssql.rtc:broker_dbm_transport.message_fragment_receives_sec"),
	VMWARE_TABLE("vmware.datastore.write")
	;

	private final String metric;

	private MetricsEnum(String metric) {
		this.metric = metric;
	}

	public String get() {
		return this.metric;
	}
}