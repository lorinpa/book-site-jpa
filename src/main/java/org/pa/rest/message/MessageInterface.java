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
public interface MessageInterface {
    
     
        public void setEntity(String entity);
        public String getEntity();
        public void setData(Serializable target);
        public Serializable getData();
        public String getAction();
        public void setAction(String action);
        public String getStatus();
        public void setStatus(String status);
    
}
