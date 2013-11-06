/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.rest.message;

import java.io.Serializable;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public class SuccessMessage extends Message {

    public SuccessMessage() {
        this.status = MessageDefinitions.SUCCESS;
    }

   
 public SuccessMessage(String action, String entity, Serializable data) {
        this.status = MessageDefinitions.SUCCESS;
        this.action = action;
        this.entity = entity;
        this.data = data;
    }
   
    
    
    
}
