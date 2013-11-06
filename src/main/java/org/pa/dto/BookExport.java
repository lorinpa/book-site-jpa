/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.dto;

import java.io.Serializable;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public class BookExport implements Serializable {
    
    public String title;
    public Integer id;
    public Integer author_id;

    public BookExport() {
    }
    
    

    public BookExport( Integer id,String title, Integer author_id) {
        this.title = title;
        this.id = id;
        this.author_id = author_id;
    }
    
    
}
