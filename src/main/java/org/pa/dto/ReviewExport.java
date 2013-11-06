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
public class ReviewExport implements Serializable {
    
    public Integer id;
    public Integer stars;
    public String body;
    public Integer book_id;

    public ReviewExport() {
    }
    
    

    public ReviewExport(Integer id, Integer stars, String body, Integer book_id) {
        this.id = id;
        this.stars = stars;
        this.body = body;
        this.book_id = book_id;
    }
    
    
    
}
