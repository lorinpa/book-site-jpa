/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.repository;

import java.io.Serializable;
import java.util.List;
import org.pa.dto.BookExport;
import org.pa.entity.Author;
import org.pa.entity.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author
 * mwave
 */
@Repository
public interface BooksRepository   {

   
    public Book update(Integer id, String title, Author author);
    
    public Book update(Serializable id, String title, Author author);
    
     public List<Book> findAll();
     
     public List<BookExport> exportAll();
     
     public Book findById(Serializable id) throws javax.persistence.NoResultException;
     public Object findOne(Serializable id) ;
     public Object addNew(Object s)  throws Exception;
     public void delete(Serializable id);
     public void delete (Integer id);
     public void delete(Book book);
     public long count();
}
