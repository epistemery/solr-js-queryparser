package org.epistemery.solr.plugins;

import org.apache.solr.SolrTestCaseJ4;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSimpleQParserPlugin extends SolrTestCaseJ4 {
    @BeforeClass
    public static void beforeClass() throws Exception {
        initCore("solr/conf/solrconfig.xml", "solr/conf/schema.xml", "src/test/resources/tmp/solr", "core-1");
        index();
    }

    @Test
    public void testFindTwo() throws Exception {
        assertJQ(req("defType", "javascript", "q", "abc"), "/response/numFound==2");
    }

    @Test
    public void testFindOne() throws Exception {
        assertJQ(req("defType", "javascript", "q", "def"), "/response/numFound==1");
    }

    @Test
    public void testFindNull() throws Exception {
        assertJQ(req("defType", "javascript", "q", "ijk"), "/response/numFound==0");
    }

    public static void index() throws Exception {
        assertU(adoc("id", "42", "text", "abc", "number", "123", "bool", "true"));
        assertU(adoc("id", "43", "text", "abc def", "number", "456", "bool", "true"));
        assertU(adoc("id", "44", "text", "ghi", "number", "789", "bool", "false"));
        assertU(commit());
    }
}

