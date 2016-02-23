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
package org.openhab.core.jsr223.internal.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Joel Perron
 */
public class UtilitySet {
	private List<UtilityScript> utilityScripts;

	public UtilitySet() {
		this.utilityScripts = new ArrayList<UtilityScript>();
	}

	public UtilitySet(UtilityScript... rules) {
		this.utilityScripts = Arrays.asList(rules);
	}

	public void addRule(UtilityScript rule) {
		this.utilityScripts.add(rule);
	}

	public void removeRule(UtilityScript rule) {
		this.utilityScripts.remove(rule);
	}

	public List<UtilityScript> getUtilityScripts() {
		return this.utilityScripts;
	}
}
