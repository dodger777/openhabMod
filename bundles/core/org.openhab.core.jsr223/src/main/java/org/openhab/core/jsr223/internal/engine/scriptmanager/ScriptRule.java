/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.jsr223.internal.engine.scriptmanager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.openhab.core.jsr223.internal.engine.RuleExecutionRunnable;
import org.openhab.core.jsr223.internal.shared.Event;
import org.openhab.core.jsr223.internal.shared.Rule;
import org.openhab.core.jsr223.internal.shared.RuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Script holds information about a script-file. Furthermore it feeds information and objects to the Jsr223
 * Script-Engine to allow interoperability with openHAB.
 *
 * @author Simon Merschjohann
 * @author Helmut Lehmeyer
 * @since 1.7.0
 */
public class ScriptRule extends ScriptBase {

    static private final Logger logger = LoggerFactory.getLogger(ScriptRule.class);
    ArrayList<Rule> rules = new ArrayList<Rule>();

    public ScriptRule(ScriptBase scriptbase) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        super(scriptbase);
        if(this.isLoaded()) {
            ScriptEngine engine = super.getEngine();
            engine.eval(new FileReader(super.getFile()));
            Invocable inv = (Invocable) engine;
            RuleSet ruleSet = (RuleSet) inv.invokeFunction("getRules");
            rules.addAll(ruleSet.getRules());
        }
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public void executeRule(Rule rule, Event event) {
        Thread t = new Thread(new RuleExecutionRunnable(rule, event));
        t.start();
    }
    
    @Override
    public TypeScript getScriptType() {
        return ScriptBase.TypeScript.RULE;
    }
}
