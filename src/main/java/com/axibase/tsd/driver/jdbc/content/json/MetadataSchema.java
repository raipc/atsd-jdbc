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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "@context",
    "dc:created",
    "dc:publisher",
    "dc:title",
    "rdfs:comment",
    "@type",
    "url",
    "tableSchema",
    "dialect"
})
public class MetadataSchema {

    @JsonProperty("@context")
    private List<String> Context = new ArrayList<String>();
    @JsonProperty("dc:created")
    private DcCreated dcCreated;
    @JsonProperty("dc:publisher")
    private DcPublisher dcPublisher;
    @JsonProperty("dc:title")
    private String dcTitle;
    @JsonProperty("rdfs:comment")
    private String rdfsComment;
    @JsonProperty("@type")
    private String Type;
    @JsonProperty("url")
    private String url;
    @JsonProperty("tableSchema")
    private TableSchema tableSchema;
    @JsonProperty("dialect")
    private Dialect dialect;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The Context
     */
    @JsonProperty("@context")
    public List<String> getContext() {
        return Context;
    }

    /**
     * 
     * @param Context
     *     The @context
     */
    @JsonProperty("@context")
    public void setContext(List<String> Context) {
        this.Context = Context;
    }

    public MetadataSchema withContext(List<String> Context) {
        this.Context = Context;
        return this;
    }

    /**
     * 
     * @return
     *     The dcCreated
     */
    @JsonProperty("dc:created")
    public DcCreated getDcCreated() {
        return dcCreated;
    }

    /**
     * 
     * @param dcCreated
     *     The dc:created
     */
    @JsonProperty("dc:created")
    public void setDcCreated(DcCreated dcCreated) {
        this.dcCreated = dcCreated;
    }

    public MetadataSchema withDcCreated(DcCreated dcCreated) {
        this.dcCreated = dcCreated;
        return this;
    }

    /**
     * 
     * @return
     *     The dcPublisher
     */
    @JsonProperty("dc:publisher")
    public DcPublisher getDcPublisher() {
        return dcPublisher;
    }

    /**
     * 
     * @param dcPublisher
     *     The dc:publisher
     */
    @JsonProperty("dc:publisher")
    public void setDcPublisher(DcPublisher dcPublisher) {
        this.dcPublisher = dcPublisher;
    }

    public MetadataSchema withDcPublisher(DcPublisher dcPublisher) {
        this.dcPublisher = dcPublisher;
        return this;
    }

    /**
     * 
     * @return
     *     The dcTitle
     */
    @JsonProperty("dc:title")
    public String getDcTitle() {
        return dcTitle;
    }

    /**
     * 
     * @param dcTitle
     *     The dc:title
     */
    @JsonProperty("dc:title")
    public void setDcTitle(String dcTitle) {
        this.dcTitle = dcTitle;
    }

    public MetadataSchema withDcTitle(String dcTitle) {
        this.dcTitle = dcTitle;
        return this;
    }

    /**
     * 
     * @return
     *     The rdfsComment
     */
    @JsonProperty("rdfs:comment")
    public String getRdfsComment() {
        return rdfsComment;
    }

    /**
     * 
     * @param rdfsComment
     *     The rdfs:comment
     */
    @JsonProperty("rdfs:comment")
    public void setRdfsComment(String rdfsComment) {
        this.rdfsComment = rdfsComment;
    }

    public MetadataSchema withRdfsComment(String rdfsComment) {
        this.rdfsComment = rdfsComment;
        return this;
    }

    /**
     * 
     * @return
     *     The Type
     */
    @JsonProperty("@type")
    public String getType() {
        return Type;
    }

    /**
     * 
     * @param Type
     *     The @type
     */
    @JsonProperty("@type")
    public void setType(String Type) {
        this.Type = Type;
    }

    public MetadataSchema withType(String Type) {
        this.Type = Type;
        return this;
    }

    /**
     * 
     * @return
     *     The url
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @param url
     *     The url
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    public MetadataSchema withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 
     * @return
     *     The tableSchema
     */
    @JsonProperty("tableSchema")
    public TableSchema getTableSchema() {
        return tableSchema;
    }

    /**
     * 
     * @param tableSchema
     *     The tableSchema
     */
    @JsonProperty("tableSchema")
    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public MetadataSchema withTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
        return this;
    }

    /**
     * 
     * @return
     *     The dialect
     */
    @JsonProperty("dialect")
    public Dialect getDialect() {
        return dialect;
    }

    /**
     * 
     * @param dialect
     *     The dialect
     */
    @JsonProperty("dialect")
    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public MetadataSchema withDialect(Dialect dialect) {
        this.dialect = dialect;
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

    public MetadataSchema withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
