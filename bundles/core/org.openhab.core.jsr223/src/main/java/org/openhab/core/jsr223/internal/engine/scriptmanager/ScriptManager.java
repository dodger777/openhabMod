/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.jsr223.internal.engine.scriptmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openhab.config.core.ConfigDispatcher;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.jsr223.internal.engine.RuleTriggerManager;
import org.openhab.core.jsr223.internal.shared.Event;
import org.openhab.core.jsr223.internal.shared.EventTrigger;
import org.openhab.core.jsr223.internal.shared.Rule;
import org.openhab.core.jsr223.internal.shared.UtilityScript;
import org.openhab.core.jsr223.internal.shared.StartupTrigger;
import org.openhab.core.jsr223.internal.shared.TriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main component of script engine. It checks for available Script engines, loads scripts from the scripts directory and
 * listens for script changes (which lead to script reloading)
 *
 * @author Simon Merschjohann
 * @since 1.7.0
 */
public class ScriptManager {

    static private final Logger logger = LoggerFactory.getLogger(ScriptManager.class);

    public HashMap<String, ScriptRule> scripts = new HashMap<String, ScriptRule>();
    public HashMap<Rule, ScriptRule> ruleMap = new HashMap<Rule, ScriptRule>();
    public HashMap<String, ScriptUtility> utilityScripts = new HashMap<String, ScriptUtility>();
    public HashMap<UtilityScript, ScriptUtility> utilityScriptMap = new HashMap<UtilityScript, ScriptUtility>();

    private ItemRegistry itemRegistry;

    private RuleTriggerManager triggerManager;

    private Thread scriptUpdateWatcher;

    private static ScriptManager instance;

    public ScriptManager(RuleTriggerManager triggerManager, ItemRegistry itemRegistry) {
        this.triggerManager = triggerManager;
        instance = this;
        logger.info("Available engines:");
        for (ScriptEngineFactory f : new ScriptEngineManager().getEngineFactories()) {
            logger.info(f.getEngineName());
        }

        this.setItemRegistry(itemRegistry);

        File folder = getFolder("scripts");

        if (folder.exists() && folder.isDirectory()) {
            loadScripts(folder);

            scriptUpdateWatcher = new Thread(new ScriptUpdateWatcher(this, folder));
            scriptUpdateWatcher.start();
        } else {
            logger.warn("Script directory: jsr_scripts missing, no scripts will be added!");
        }
    }

    public void loadScripts(File folder) {
        for (File file : folder.listFiles()) {
            loadScript(file);
        }
    }

    private ScriptBase loadScript(File file) {
        ScriptBase script = null;
        try {
            //Filtering Directories and not usable Files
            if (!file.isFile() || file.getName().startsWith(".") || getFileExtension(file) == null) {
                return null;
            }
            script = returnNewScript(file);
            if (!script.isLoaded()) {
                return null;
            } else {
                logger.info("Engine found for File: {}", file.getName());
                insertOrModifyScript(script, file, true);
            }

        } catch (NoSuchMethodException e) {
            logger.error("Script file misses mandotary function: getRules()", e);
        } catch (FileNotFoundException e) {
            logger.error("script file not found", e);
        } catch (ScriptException e) {
            logger.error("script exception", e);
        } catch (Exception e) {
            logger.error("unknown exception", e);
        }

        return script;
    }

    private void insertOrModifyScript(ScriptBase script, File file, boolean isInsert) {
        if (script.getScriptType() == ScriptBase.TypeScript.RULE) {
            if (isInsert) {
                scripts.put(file.getName(), (ScriptRule) script);
            } else {
                scripts.replace(file.getName(), (ScriptRule) script);
            }

            List<Rule> newRules = ((ScriptRule) script).getRules();
            for (Rule rule : newRules) {
                ruleMap.put(rule, (ScriptRule) script);
            }
            // add all rules to the needed triggers
            triggerManager.addRuleModel(newRules);
        } else if (script.getScriptType() == ScriptBase.TypeScript.UTILITY_SCRIPT) {
            if (isInsert) {
                utilityScripts.put(file.getName(), (ScriptUtility) script);
            } else {
                utilityScripts.replace(file.getName(), (ScriptUtility) script);
            }

            List<UtilityScript> newUtilityScripts = ((ScriptUtility) script).getScripts();
            for (UtilityScript utilityScript : newUtilityScripts) {
                utilityScriptMap.put(utilityScript, (ScriptUtility) script);

            }
        }
    }

    private ScriptBase returnNewScript(File file) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        ScriptBase scriptBase = new ScriptBase(this, file);
        return scriptBase.returnScript();
    }

    public static ScriptManager getInstance() {
        return instance;
    }

    public Collection<Rule> getAllRules() {
        return ruleMap.keySet();
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public synchronized void executeRules(Rule[] rules, org.openhab.core.jsr223.internal.shared.Event event) {
        for (Rule rule : rules) {
            ruleMap.get(rule).executeRule(rule, event);
        }
    }

    public synchronized void executeRules(Iterable<Rule> rules, org.openhab.core.jsr223.internal.shared.Event event) {
        for (Rule rule : rules) {
            ruleMap.get(rule).executeRule(rule, event);
        }
    }

    /**
     * returns the {@link File} object for a given foldername
     *
     * @param foldername the foldername to get the {@link File} for
     * @return the corresponding {@link File}
     */
    private File getFolder(String foldername) {
        File folder = new File(ConfigDispatcher.getConfigFolder() + File.separator + foldername);
        return folder;
    }

    public ScriptRule getScript(Rule rule) {
        return ruleMap.get(rule);
    }

    private String getFileExtension(File file) {
        String extension = null;
        if (file.getName().contains(".")) {
            String name = file.getName();
            extension = name.substring(name.lastIndexOf('.') + 1, name.length());
        }
        return extension;
    }

    public void scriptsChanged(List<File> addedScripts, List<File> removedScripts, List<File> modifiedScripts) {
        for (File scriptFile : removedScripts) {
            removeScript(scriptFile.getName());
        }

        for (File scriptFile : addedScripts) {
            ScriptBase script = loadScript(scriptFile);
            runStartupRules(script);
        }

        for (File scriptFile : modifiedScripts) {
            modifyScript(scriptFile);
        }
    }

    private void runStartupRules(ScriptBase scriptBase) {
        if (scriptBase != null && scriptBase.getScriptType() == ScriptBase.TypeScript.RULE) {
            ScriptRule script = (ScriptRule) scriptBase;
            ArrayList<Rule> toTrigger = new ArrayList<Rule>();
            for (Rule rule : script.getRules()) {
                for (EventTrigger trigger : rule.getEventTrigger()) {
                    if (trigger instanceof StartupTrigger) {
                        toTrigger.add(rule);
                        break;
                    }
                }
            }
            if (toTrigger.size() > 0) {
                executeRules(toTrigger, new Event(TriggerType.STARTUP, null, null, null, null));
            }
        }
    }

    private void removeScript(String scriptName) {
        if (scripts.containsKey(scriptName)) {
            ScriptRule script = scripts.remove(scriptName);
            removeRuleTriggers(script);
        }
        if (utilityScripts.containsKey(scriptName)) {
            ScriptUtility script = utilityScripts.remove(scriptName);
            removeUtilityScriptsMap(script);
        }
    }

    private void removeRuleTriggers(ScriptRule script) {
        List<Rule> allRules = script.getRules();

        triggerManager.removeRuleModel(allRules);
        for (Rule rule : allRules) {
            ruleMap.remove(rule);
        }
    }

    private void removeUtilityScriptsMap(ScriptUtility script) {
        for (UtilityScript oneScript : script.getScripts()) {
            utilityScriptMap.remove(oneScript);
        }
    }

    private void modifyScript(File scriptFile) {
        try {
            String scriptName = scriptFile.getName();
            Object oldGlobal = null;
            if (scripts.containsKey(scriptName)) {
                removeRuleTriggers(scripts.get(scriptName));
            }
            if (utilityScripts.containsKey(scriptName)) {
                removeUtilityScriptsMap(utilityScripts.get(scriptName));
            }
            ScriptBase modifiedScript = returnNewScript(scriptFile);
            insertOrModifyScript(modifiedScript, scriptFile, false);
            runStartupRules(modifiedScript);
        } catch (Exception e) {
            logger.error("unknown exception in modifyScript", e);
        }
    }

    public UtilityScript getUtilityScript(String scriptName) {
        String pattern = "(\\w+)";
        Pattern r = Pattern.compile(pattern);
        
        for (UtilityScript key : utilityScriptMap.keySet()) {
            Matcher m = r.matcher(key.toString());
            if ((m.find() && (!m.group(1).isEmpty()) && m.group(1).equals(scriptName))) {
                return key;
            }
        }
        return null;
    }

}
