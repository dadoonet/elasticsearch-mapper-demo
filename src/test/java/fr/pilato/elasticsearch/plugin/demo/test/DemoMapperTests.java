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

package fr.pilato.elasticsearch.plugin.demo.test;

import fr.pilato.elasticsearch.plugin.demo.DemoMapper;
import org.apache.lucene.document.Document;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 */
@Test
public class DemoMapperTests {

    private DocumentMapperParser mapperParser;

    @BeforeClass
    public void setupMapperParser() {
        mapperParser = new DocumentMapperParser(new Index("itest"), new AnalysisService(new Index("itest")));
        mapperParser.putTypeParser(DemoMapper.CONTENT_TYPE, new DemoMapper.TypeParser());
    }

    @Test
    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("/fr/pilato/elasticsearch/plugin/demo/test/test-mapping.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);
        String text = "Hello World";

        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("lowercase", text).endObject().bytes();

        Document doc = docMapper.parse(json).rootDoc();

        assertThat(docMapper.mappers().smartName("lowercase.demo"), notNullValue());
        assertThat(doc.get(docMapper.mappers().smartName("demo").mapper().names().indexName()), equalTo("HELLO WORLD"));
        assertThat(doc.get(docMapper.mappers().smartName("lowercase").mapper().names().indexName()), containsString("Hello World"));

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = mapperParser.parse(builtMapping);

        json = jsonBuilder().startObject().field("_id", 1).field("lowercase", text).endObject().bytes();

        doc = docMapper.parse(json).rootDoc();

        assertThat(docMapper.mappers().smartName("lowercase.demo"), notNullValue());
        assertThat(doc.get(docMapper.mappers().smartName("lowercase.demo").mapper().names().indexName()), equalTo("HELLO WORLD"));
        assertThat(doc.get(docMapper.mappers().smartName("lowercase").mapper().names().indexName()), containsString("Hello World"));
    }
}
