/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.repository;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.pa.dto.BookExport;
import org.pa.entity.Author;
import org.pa.entity.Book;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author  lorinpa
 */
@Service
public class BooksRepositoryImpl implements BooksRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public Book update(Integer id, String title, Author author) {

        Book book = findById(id);
        book.setTitle(title);
        book.setAuthorId(author);
    //    System.out.println("Book update with Integer signature called");
        entityManager.merge(book);

    //    entityManager.flush();
    //    entityManager.refresh(book);

        return book;
    }

    @Transactional
    @Override
    public Book update(Serializable id, String title, Author author) {

        Book book = findById(id);
        book.setTitle(title);
        book.setAuthorId(author);
        entityManager.merge(book);
        return book;
    }

  

    @Transactional
    public Book update(Book book) throws Exception {
        entityManager.merge(book);
        return book;
    }

    @Transactional
    public List<BookExport> exportAll() {
        TypedQuery<BookExport> q = (TypedQuery<BookExport>) entityManager.
                createQuery("select new org.pa.dto.BookExport(a.id, a.title, a.authorId.id) from Book AS a",BookExport.class);
      
        List<BookExport> list = q.getResultList();
        return list;
        
    }
    
    @Override
    @Transactional
    public List<Book> findAll() {

        Query q = entityManager.createQuery("select a from Book a");
        List<Book> list = q.getResultList();
        return list;

    }

    // @Cacheable(value="books", key="#id")
    @Transactional
    public Book findById(Serializable id) throws javax.persistence.NoResultException {
        return (Book) findOne(id);
    }

    @Override
    @Transactional
    public Object findOne(Serializable id) {
        Query q = entityManager.createQuery("select a from Book a where a.id = ?1");
        q.setParameter(1, id);
        return q.getSingleResult();
    }

    @Override
    public Iterable save(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional
    public boolean exists(Serializable id) {
        boolean found = false;
        try {
            Book book = this.findById(id);
            found = true;
        } catch (NoResultException nre) {
            found = false;
        }
        return found;
    }

    @Override
    @Transactional
    public Iterable findAll(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional
    public long count() {
        return (Long) entityManager.createQuery("Select count(a.id) from Book a").getSingleResult();

    }

    
    @Transactional
    public void delete(Integer book_id) {
        Book book = this.findById(book_id);
        if (book != null) {
            delete(book);
        }
    }

    @Transactional
    @Override
    public void delete(Book book) {
        if (!entityManager.contains(book)) {
            Query q = entityManager.createQuery("select a from Book a where a.id = ?1");
            q.setParameter(1, book.getId());
            book = (Book) q.getSingleResult();
        }
        entityManager.remove(book);
    }

    @Transactional
    @Override
    public void delete(Serializable id) {
        Book book = (Book) id;
        if (!entityManager.contains(book)) {
            Query q = entityManager.createQuery("select a from Book a where a.id = ?1");
            q.setParameter(1, book.getId());
            book = (Book) q.getSingleResult();
        }
        entityManager.remove(book);
    }

    @Override
    public void delete(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional
    public Object addNew(Object s) throws Exception {
        entityManager.persist(s);
        return s;
    }

    @Override
    public Object save(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(Object entity) {
        entityManager.remove(entity);
    }
}
