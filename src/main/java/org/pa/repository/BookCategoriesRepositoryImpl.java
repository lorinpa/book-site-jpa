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
import org.pa.dto.BookCategoryExport;
import org.pa.entity.Book;
import org.pa.entity.BookCategory;
import org.pa.entity.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
@Service
public class BookCategoriesRepositoryImpl implements BookCategoriesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    //@Override
    @Transactional
    public Iterable findAll(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional
    public List<BookCategory> findAll() {
        Query q = entityManager.createQuery("select a from BookCategory a");
        List<BookCategory> list = null;
        try {
            list = q.getResultList();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
        }
        return list;

    }
    
    @Transactional
    public List<BookCategoryExport> exportAll() {
         TypedQuery<BookCategoryExport> q = (TypedQuery<BookCategoryExport>) entityManager.
                createQuery("select new org.pa.dto.BookCategoryExport(a.id, a.bookId.id, a.categoryId.id) from BookCategory AS a",BookCategoryExport.class);
      
        List<BookCategoryExport> list = q.getResultList();
        return list;
    }

    @Transactional
    public BookCategory findById(Serializable id) throws javax.persistence.NoResultException {
        return (BookCategory) findOne(id);
    }

    @Override
    @Transactional
    public Object findOne(Serializable id) {
        Query q = entityManager.createQuery("select a from BookCategory a where a.id = ?1");
        q.setParameter(1, id);
        return q.getSingleResult();
    }

    /*
     * This method reports all the books that have been cateogorized as a specific
     * category. Example report all comedy books.
     */
    @Transactional
    public List<Book> findBooksByCategory(Serializable id) {

        Query q = entityManager.createQuery("Select b from Book b where b.id in (Select  a.bookId.id from BookCategory a where a.categoryId.id = ?1)");
        q.setParameter(1, id);
        List<Book> list = q.getResultList();
        return list;
    }

    @Override
    @Transactional
    public boolean exists(Serializable id) {
        boolean found = false;
        try {
            BookCategory category = this.findById(id);
            found = true;
        } catch (NoResultException nre) {
            found = false;
        }
        return found;
    }

    @Override
    @Transactional
    public long count() {
        return (Long) entityManager.createQuery("Select count(a.id) from BookCategory a").getSingleResult();

    }

    //@Override
    public Iterable save(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Transactional
    public BookCategory update(Serializable id, Book book, Category category) throws Exception {

        BookCategory bookCategory = findById(id);
        bookCategory.setBookId(book);
        bookCategory.setCategoryId(category);

        try {
            entityManager.merge(bookCategory);

        } catch (Exception e) {
        }
        return bookCategory;
    }

    public void delete(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void deleteAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional
    public Object addNew(Object s) throws Exception {
        entityManager.persist(s);
        return s;
    }

    public Object save(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Transactional
    public void delete(Serializable id) {
        BookCategory bookCat = (BookCategory) id;
        if (entityManager.contains(bookCat)) {
              entityManager.remove(bookCat);
        } else {
            Query q = entityManager.createQuery("select a from BookCategory a where a.id = ?1");
            q.setParameter(1, bookCat.getId());
            bookCat = (BookCategory) q.getSingleResult();
            entityManager.remove(bookCat);
        }
    }

    @Transactional
    public void delete(Object entity) {
          BookCategory bookCat = (BookCategory) entity;
        if (entityManager.contains(bookCat)) {
              entityManager.remove(bookCat);
        } else {
             Query q = entityManager.createQuery("select a from BookCategory a where a.id = ?1");
            q.setParameter(1, bookCat.getId());
            bookCat = (BookCategory) q.getSingleResult();
            entityManager.remove(bookCat);
        }
    }
    
    @Transactional
    public void delete(Integer book_id) {
        BookCategory bookCat = this.findById(book_id);
        if (bookCat != null) {
            delete(bookCat);
        }
    }
    
}
