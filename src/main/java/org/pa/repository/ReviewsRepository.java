/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.repository;

import java.io.Serializable;
import java.util.List;
import org.pa.dto.ReviewExport;
import org.pa.entity.Book;
import org.pa.entity.Review;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public interface ReviewsRepository extends CrudRepository {

    public List<Review> findAll();
    
    public List<ReviewExport> exportAll();

    public Review update(Serializable id, Book book, Integer stars, String body) throws Exception;

    Review findById(Serializable id) throws javax.persistence.NoResultException;

    List<Review> findByBookId(Serializable bookId);

    public Object findOne(Serializable id);

    public boolean exists(Serializable id);
    
     public Object addNew(Object s)  throws Exception;
     
     public void delete(Integer id);
}
