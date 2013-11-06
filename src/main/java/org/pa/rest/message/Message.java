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
public class Message implements MessageInterface {
    
    protected String entity;
    protected Serializable data;
    protected String action;
    protected String status;

    @Override
    public void setEntity(String entnty) {
      this.entity = entnty;
    }

    @Override
    public String getEntity() {
        return this.entity;
    }

    @Override
    public void setData(Serializable target) {
      this.data = target;
    }

    @Override
    public Serializable getData() {
       return this.data;
    }

    @Override
    public String getAction() {
        return this.action;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String getStatus() {
       return this.status;
    }

    @Override
    public void setStatus(String status) {
       this.status = status;
    }
    
}
