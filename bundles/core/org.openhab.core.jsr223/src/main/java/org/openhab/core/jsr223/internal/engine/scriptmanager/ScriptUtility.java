/*
 * Copyright (c) 2016 openHAB.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    openHAB.org - initial API and implementation and/or initial documentation
 */
package org.openhab.core.jsr223.internal.engine.scriptmanager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openhab.core.jsr223.internal.shared.UtilityScript;
import java.util.ArrayList;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import org.openhab.core.jsr223.internal.shared.UtilitySet;
import java.util.List;

/**
 *
 * @author Joel Perron
 */
public class ScriptUtility extends ScriptBase {

    private ArrayList<UtilityScript> scripts = new ArrayList<UtilityScript>();

    static private final Logger logger = LoggerFactory.getLogger(ScriptRule.class);

    public ScriptUtility(ScriptBase scriptbase) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        super(scriptbase);
        if(this.isLoaded()) {
            ScriptEngine engine = super.getEngine();
            engine.eval(new FileReader(super.getFile()));
            Invocable inv = (Invocable) engine;
            UtilitySet utilitySet = (UtilitySet) inv.invokeFunction("getUtilityScripts");
            scripts.addAll(utilitySet.getUtilityScripts());
        }
    }

    @Override
    public TypeScript getScriptType() {
        return ScriptBase.TypeScript.UTILITY_SCRIPT;
    }

    public List<UtilityScript> getScripts() {
        return scripts;
    }
}
