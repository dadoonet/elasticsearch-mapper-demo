/*
 * Licensed to David Pilato under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. David Pilato licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.pilato.elasticsearch.plugin.demo;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.core.StringFieldMapper;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.stringField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parsePathType;

/**
 * <pre>
 *      field1 : "..."
 * </pre>
 */
public class DemoMapper implements Mapper {

    public static final String CONTENT_TYPE = "demo";
    public static final String FIELD_DEMO = "demo";

    public static class Defaults {
        public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
    }

    public static class Builder extends Mapper.Builder<Builder, DemoMapper> {

        private ContentPath.Type pathType = Defaults.PATH_TYPE;

        private StringFieldMapper.Builder contentBuilder;
        private StringFieldMapper.Builder demoBuilder = stringField(FIELD_DEMO);

        public Builder(String name) {
            super(name);
            this.builder = this;
            this.contentBuilder = stringField(name);
        }

        public Builder pathType(ContentPath.Type pathType) {
            this.pathType = pathType;
            return this;
        }

        public Builder content(StringFieldMapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder demo(StringFieldMapper.Builder demo) {
            this.demoBuilder = demo;
            return this;
        }

        @Override
        public DemoMapper build(BuilderContext context) {
            ContentPath.Type origPathType = context.path().pathType();
            context.path().pathType(pathType);

            // create the content mapper under the actual name
            StringFieldMapper contentMapper = contentBuilder.build(context);

            // create the Uppercase under the name
            context.path().add(name);
            StringFieldMapper demoMapper = demoBuilder.build(context);
            context.path().remove();

            context.path().pathType(origPathType);

            return new DemoMapper(name, contentMapper, demoMapper);
        }
    }

    /**
     * <pre>
     *  field1 : { type : "demo" }
     * </pre>
     * Or:
     * <pre>
     *  field1 : {
     *      type : "demo",
     *      fields : {
     *          demo : {type : "string"}
     *      }
     * }
     * </pre>
     */
    public static class TypeParser implements Mapper.TypeParser {

        @SuppressWarnings({"unchecked"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
            DemoMapper.Builder builder = new DemoMapper.Builder(name);

            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                // TODO Check the path usage
                if (fieldName.equals("path")) {
                    builder.pathType(parsePathType(name, fieldNode.toString()));
                } else if (fieldName.equals("fields")) {
                    Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                    for (Map.Entry<String, Object> entry1 : fieldsNode.entrySet()) {
                        String propName = entry1.getKey();
                        Object propNode = entry1.getValue();

                        if (name.equals(propName)) {
                            // that is the content
                            builder.content((StringFieldMapper.Builder) parserContext.typeParser("string").parse(name, (Map<String, Object>) propNode, parserContext));
                            // that is the demo field we will manage
                        } else if (FIELD_DEMO.equals(propName)) {
                            builder.demo((StringFieldMapper.Builder) parserContext.typeParser("string").parse("demo", (Map<String, Object>) propNode, parserContext));
                        }
                    }
                }
            }

            return builder;
        }
    }

    private final String name;

    private final StringFieldMapper contentMapper;
    private final StringFieldMapper demoMapper;

    public DemoMapper(String name, StringFieldMapper contentMapper, StringFieldMapper demoMapper) {
        this.name = name;
        this.contentMapper = contentMapper;
        this.demoMapper = demoMapper;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        String content = null;

        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
        }

        String parsedContent;
        if (content != null) {
            context.externalValue(content);
            contentMapper.parse(context);

            // TODO : Do what you need here. We uppercase the content in a new field called field.demo
            parsedContent = content.toUpperCase();
            context.externalValue(parsedContent);
            demoMapper.parse(context);
        }
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
        // ignore this for now
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        contentMapper.traverse(fieldMapperListener);
        demoMapper.traverse(fieldMapperListener);
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
    }

    @Override
    public void close() {
        contentMapper.close();
        demoMapper.close();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        builder.field("type", CONTENT_TYPE);

        builder.startObject("fields");
        contentMapper.toXContent(builder, params);

        // We add our field demo here
        demoMapper.toXContent(builder, params);
        builder.endObject();

        builder.endObject();
        return builder;
    }
}
