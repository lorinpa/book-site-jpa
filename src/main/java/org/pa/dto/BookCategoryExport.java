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
public class BookCategoryExport implements Serializable {
    
    public Integer id;
    public Integer book_id;
    public Integer category_id;

    public BookCategoryExport() {
    }

    
    
    public BookCategoryExport(Integer id, Integer book_id, Integer category_id) {
        this.id = id;
        this.book_id = book_id;
        this.category_id = category_id;
    }
    
        
    
}
