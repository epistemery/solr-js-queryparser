package org.epistemery.solr.plugins;

import com.coveo.nashorn_modules.FilesystemFolder;
import com.coveo.nashorn_modules.Require;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.rest.ManagedResource;
import org.apache.solr.rest.ManagedResourceObserver;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class JavaScriptedQParserPlugin extends QParserPlugin implements ResourceLoaderAware, ManagedResourceObserver {
    private static Logger log;

    private String scriptPath;
    private String modulesPath;
    private String entryPoint;
    protected String scriptIdentifier;
    private boolean scriptReload = false;
    private boolean firstCall = true;

    private NashornScriptEngine engine;
    protected Invocable invocable;

    @Override
    public void init(NamedList args) {
        log = Logger.getLogger(JavaScriptedQParserPlugin.class);

        scriptPath = (String)args.get("script");
        modulesPath = (String)args.get("modules");
        entryPoint = (String)args.get("entrypoint");
        if (scriptPath != null) {
            scriptIdentifier = scriptPath;
        } else if (modulesPath != null && entryPoint != null) {
            scriptIdentifier = entryPoint + " @ " + modulesPath;
        } else {
            scriptIdentifier = "<unknown>";
        }

        Object reload = args.getBooleanArg("reload");
        if(reload != null) {
            scriptReload = (boolean)reload;
        }

        loadScript();
        this.firstCall = false;
    }

    @Override
    public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        if (!firstCall && scriptReload) {
            log.info("reloading script '" + scriptIdentifier + "'");
            loadScript();
        }

        return new JavaScriptedQParser(qstr, localParams, params, req, this);
    }

    protected void loadScript() {
        engine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval("global = this;");
            engine.eval("var console = {}");
            engine.eval(
                    "(function() {" +
                            "var Logger = Java.type(\"org.apache.log4j.Logger\");" +
                            "var logger = Logger.getLogger('" + scriptIdentifier + "');" +
                            "console.log = function(msg) { print(msg); };" +
                            "console.info = function(msg) { logger.info(msg); };" +
                            "console.warn = function(msg) { logger.warn(msg); };" +
                            "console.error = function(msg) { logger.error(msg); };" +
                            "})()");

            if (modulesPath != null) {
                File file = new File(modulesPath);
                FilesystemFolder root = FilesystemFolder.create(file, "UTF-8");
                Require.enable(engine, root);
            }
            if (scriptPath != null) {
                engine.eval("load('" + scriptPath + "');");
            } else if (modulesPath != null && entryPoint != null) {
                String evalstring = "var parse = require(\"" + entryPoint + "\");";
                engine.eval(evalstring);
            } else {
                throw new FileNotFoundException(
                        "there's no script source. must define either scriptPath or modules && entrypoint");
            }
            invocable = (Invocable) engine;
        } catch (ScriptException e) {
            log.error("parser script '" + scriptIdentifier + "' evaluation error", e);
        } catch (FileNotFoundException e) {
            log.error("parser script '" + scriptIdentifier + "' not found", e);
        }
    }

    @Override
    public void onManagedResourceInitialized(NamedList<?> args, ManagedResource res) throws SolrException {
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {
    }
}

