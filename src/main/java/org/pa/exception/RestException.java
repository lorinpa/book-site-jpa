/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.exception;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public class RestException extends Exception {
    
    Map container;
    
    private final static String EXCEPTION = "EXCEPTION";

    public RestException() {
    }

    public RestException(String message) {
        super(message);
        container = new HashMap();
        container.put(EXCEPTION, message);
    }
    
    public Map exceptionMap() {
        return this.container;
    }
    
    public void putTarget(String key, Object obj) {
       container.put(key, obj);
    }
}
