/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.jsr223.internal;

import org.openhab.core.jsr223.internal.engine.RuleTriggerManager;
import org.openhab.core.jsr223.internal.shared.Rule;
import org.openhab.core.scriptengine.action.ActionDoc;
import org.openhab.core.scriptengine.action.ParamDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * This class provides static methods that can be used in automation rules
 * for interacting with the JSR223 Engine 
 * 
 * @author Joel Perron
 * @since 1.8.2
 *
 */public class Jsr223Action {

	private static final Logger logger = LoggerFactory.getLogger(Jsr223Action.class);

	static String test;
        static private RuleTriggerManager triggerManager;


	/**
	 * ask the engine to reload the rule triggers
	 * 
	 * @param rulesConfigs the rule configuration
	 * 
	 * @return <code>true</code>, if reloading the trigger is successfull 
	 * <code>false</code> in all other cases.
	 */
	@ActionDoc(text="Ask the Jsr223 Engine to reload a script Triggers")
	static public boolean reloadTriggers(
		@ParamDoc(name="rulesConfigs") Rule rule) {
		
		logger.debug("Rules ask for triggers reset!! ".concat(rule.toString()));
                triggerManager.updateTrigger(rule);
		return false;
	}
        
        
        static public void setTriggerManager(RuleTriggerManager tM) {
            triggerManager = tM;
        }
}
