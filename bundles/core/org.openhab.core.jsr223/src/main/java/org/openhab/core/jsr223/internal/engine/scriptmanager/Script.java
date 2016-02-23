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
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.openhab.core.jsr223.internal.engine.RuleExecutionRunnable;
import org.openhab.core.jsr223.internal.shared.ChangedEventTrigger;
import org.openhab.core.jsr223.internal.shared.UpdatedEventTrigger;
import org.openhab.core.jsr223.internal.shared.CommandEventTrigger;
import org.openhab.core.jsr223.internal.shared.Event;
import org.openhab.core.jsr223.internal.shared.EventTrigger;
import org.openhab.core.jsr223.internal.shared.Openhab;
import org.openhab.core.jsr223.internal.shared.Rule;
import org.openhab.core.jsr223.internal.shared.RuleSet;
import org.openhab.core.jsr223.internal.shared.ShutdownTrigger;
import org.openhab.core.jsr223.internal.shared.StartupTrigger;
import org.openhab.core.jsr223.internal.shared.TimerTrigger;
import org.openhab.core.jsr223.internal.shared.TriggerType;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.persistence.*;
import org.openhab.core.persistence.extensions.PersistenceExtensions;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.library.tel.types.CallType;
import org.openhab.model.script.actions.BusEvent;
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
public class Script extends ScriptBase {

    static private final Logger logger = LoggerFactory.getLogger(Script.class);
    ArrayList<Rule> rules = new ArrayList<Rule>();

    public Script(ScriptBase scriptbase) throws FileNotFoundException, ScriptException, NoSuchMethodException {
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
