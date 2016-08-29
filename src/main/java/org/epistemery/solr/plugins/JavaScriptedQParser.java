package org.epistemery.solr.plugins;

import jdk.nashorn.api.scripting.JSObject;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;

public class JavaScriptedQParser extends QParser {
    private static Logger log;
    private JavaScriptedQParserPlugin plugin;
    private String qstr;
    private JSObject parser;

    public JavaScriptedQParser (String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req,
                                JavaScriptedQParserPlugin plugin) {
        super(qstr, localParams, params, req);
        this.qstr = qstr;
        this.plugin = plugin;

        if(plugin.Parser != null) {
            this.parser = (JSObject) plugin.Parser.newObject(this);
        }

        log = Logger.getLogger(JavaScriptedQParser.class);
    }

    @Override
    public Query parse() throws SyntaxError {
        try {
            if(this.parser == null) {
                throw new SyntaxError("could not instantiate Parser class");
            }

            JSObject parse = (JSObject)parser.getMember("parse");
            try {
                Query query = (Query)parse.call(this.parser);
                log.debug("javascript function parsed query to '" + query.toString() + "'");

                return query;
            } catch(ClassCastException e) {
                throw new SyntaxError("Parser.parse() did not return instance of org.apache.lucene.search.Query", e);
            }
        } catch(ClassCastException e) {
            throw new SyntaxError("Parser class doesn't have method parse()", e);
        }
    }

    public String getQstr() {
        return qstr;
    }

    public QParserPlugin getPlugin() {
        return plugin;
    }

    public void setParams(SolrParams params) {
        this.params = params;
    }
}
