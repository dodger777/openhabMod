/*
 * Copyright (c) 2016 openHAB.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.core.jsr223.internal.shared;

import java.util.Map;

/**
 * UtilityScript-Interface: Those script will no be executed by trigger, but they can be invoke in rules
 * 
 * @author Joel Perron
 */
public interface UtilityScript {
    
    public void setGlobal(Object global);
    
    public Object getGlobal();
}
