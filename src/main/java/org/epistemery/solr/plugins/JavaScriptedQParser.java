package org.epistemery.solr.plugins;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.SyntaxError;

import javax.script.ScriptException;

public class JavaScriptedQParser extends QParser {
    private static Logger log;
    private JavaScriptedQParserPlugin plugin;
    private String qstr;

    public JavaScriptedQParser (String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req,
                                JavaScriptedQParserPlugin plugin) {
        super(qstr, localParams, params, req);
        this.qstr = qstr;
        this.plugin = plugin;
        log = Logger.getLogger(JavaScriptedQParser.class);
    }

    @Override
    public Query parse() throws SyntaxError {

        try {
            try {
                Query query = (Query)plugin.invocable.invokeFunction("parse", this);

                log.debug("javascript function parsed query to '" + query.toString() + "'");

                return query;
            } catch(ClassCastException e) {
                log.error("parse() did not return instance of org.apache.lucene.search.Query", e);
            }
        } catch (ScriptException e) {
            log.error("parse() function in '" + plugin.scriptIdentifier + "' failed", e);
        } catch (NoSuchMethodException e) {
            log.error("missing parse() function in '" + plugin.scriptIdentifier + "'", e);
        }

        return null;
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
