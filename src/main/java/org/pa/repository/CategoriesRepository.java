/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.repository;

import java.io.Serializable;
import java.util.List;
import org.pa.entity.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */

@Repository
public interface CategoriesRepository extends CrudRepository {
    
    public Category update(Serializable id, String title) throws Exception;
    
    public List<Category> findAll();
    
    public Category findById(Serializable id) throws javax.persistence.NoResultException;
  
    public boolean exists(Serializable id);
    
     public void delete(Category category) throws Exception;
     
     public void delete (Integer id) throws Exception;
     
     public Object addNew(Object s)  throws Exception ;
     
}
