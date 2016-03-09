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
package com.axibase.tsd.driver.jdbc.content.json;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "forecastEnabled",
    "hbaseServers",
    "remoteHbase",
    "productVersion",
    "dataVersioningEnabled"
})
public class License {

    @JsonProperty("forecastEnabled")
    private boolean forecastEnabled;
    @JsonProperty("hbaseServers")
    private int hbaseServers;
    @JsonProperty("remoteHbase")
    private boolean remoteHbase;
    @JsonProperty("productVersion")
    private String productVersion;
    @JsonProperty("dataVersioningEnabled")
    private boolean dataVersioningEnabled;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public License() {
    }

    /**
     * 
     * @param dataVersioningEnabled
     * @param hbaseServers
     * @param forecastEnabled
     * @param remoteHbase
     * @param productVersion
     */
    public License(boolean forecastEnabled, int hbaseServers, boolean remoteHbase, String productVersion, boolean dataVersioningEnabled) {
        this.forecastEnabled = forecastEnabled;
        this.hbaseServers = hbaseServers;
        this.remoteHbase = remoteHbase;
        this.productVersion = productVersion;
        this.dataVersioningEnabled = dataVersioningEnabled;
    }

    /**
     * 
     * @return
     *     The forecastEnabled
     */
    @JsonProperty("forecastEnabled")
    public boolean isForecastEnabled() {
        return forecastEnabled;
    }

    /**
     * 
     * @param forecastEnabled
     *     The forecastEnabled
     */
    @JsonProperty("forecastEnabled")
    public void setForecastEnabled(boolean forecastEnabled) {
        this.forecastEnabled = forecastEnabled;
    }

    public License withForecastEnabled(boolean forecastEnabled) {
        this.forecastEnabled = forecastEnabled;
        return this;
    }

    /**
     * 
     * @return
     *     The hbaseServers
     */
    @JsonProperty("hbaseServers")
    public int getHbaseServers() {
        return hbaseServers;
    }

    /**
     * 
     * @param hbaseServers
     *     The hbaseServers
     */
    @JsonProperty("hbaseServers")
    public void setHbaseServers(int hbaseServers) {
        this.hbaseServers = hbaseServers;
    }

    public License withHbaseServers(int hbaseServers) {
        this.hbaseServers = hbaseServers;
        return this;
    }

    /**
     * 
     * @return
     *     The remoteHbase
     */
    @JsonProperty("remoteHbase")
    public boolean isRemoteHbase() {
        return remoteHbase;
    }

    /**
     * 
     * @param remoteHbase
     *     The remoteHbase
     */
    @JsonProperty("remoteHbase")
    public void setRemoteHbase(boolean remoteHbase) {
        this.remoteHbase = remoteHbase;
    }

    public License withRemoteHbase(boolean remoteHbase) {
        this.remoteHbase = remoteHbase;
        return this;
    }

    /**
     * 
     * @return
     *     The productVersion
     */
    @JsonProperty("productVersion")
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * 
     * @param productVersion
     *     The productVersion
     */
    @JsonProperty("productVersion")
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public License withProductVersion(String productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    /**
     * 
     * @return
     *     The dataVersioningEnabled
     */
    @JsonProperty("dataVersioningEnabled")
    public boolean isDataVersioningEnabled() {
        return dataVersioningEnabled;
    }

    /**
     * 
     * @param dataVersioningEnabled
     *     The dataVersioningEnabled
     */
    @JsonProperty("dataVersioningEnabled")
    public void setDataVersioningEnabled(boolean dataVersioningEnabled) {
        this.dataVersioningEnabled = dataVersioningEnabled;
    }

    public License withDataVersioningEnabled(boolean dataVersioningEnabled) {
        this.dataVersioningEnabled = dataVersioningEnabled;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public License withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dataVersioningEnabled ? 1231 : 1237);
		result = prime * result + (forecastEnabled ? 1231 : 1237);
		result = prime * result + hbaseServers;
		result = prime * result + ((productVersion == null) ? 0 : productVersion.hashCode());
		result = prime * result + (remoteHbase ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		License other = (License) obj;
		if (dataVersioningEnabled != other.dataVersioningEnabled)
			return false;
		if (forecastEnabled != other.forecastEnabled)
			return false;
		if (hbaseServers != other.hbaseServers)
			return false;
		if (productVersion == null) {
			if (other.productVersion != null)
				return false;
		} else if (!productVersion.equals(other.productVersion))
			return false;
		if (remoteHbase != other.remoteHbase)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "License [forecastEnabled=" + forecastEnabled + ", hbaseServers=" + hbaseServers + ", remoteHbase="
				+ remoteHbase + ", productVersion=" + productVersion + ", dataVersioningEnabled="
				+ dataVersioningEnabled + "]";
	}

}
