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
import org.pa.dto.ReviewExport;
import org.pa.entity.Book;
import org.pa.entity.Review;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
@Service
public class ReviewsRepositoryImpl implements ReviewsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Review update(Serializable id, Book book, Integer stars, String body) throws Exception {
        Review review = findById(id);
        review.setBody(body);
        review.setBookId(book);
        review.setStars(stars);
        try {
            entityManager.merge(review);
        } catch (Exception e) {

            throw e;
        }
        return review;
    }

    @Override
    @Transactional
    public List<Review> findAll() {

        Query q = entityManager.createQuery("select a from Review a");
        List<Review> list = q.getResultList();
        return list;
    }
    
    @Transactional
     public List<ReviewExport> exportAll() {
        TypedQuery<ReviewExport> q = (TypedQuery<ReviewExport>) entityManager.
                createQuery("select new org.pa.dto.ReviewExport(a.id, a.stars, a.body, a.bookId.id) from Review AS a",ReviewExport.class);
      
        List<ReviewExport> list = q.getResultList();
        return list;
    }
    
    @Transactional
    @Override
    public Review findById(Serializable id) throws javax.persistence.NoResultException {
        return (Review) findOne(id);
    }

    @Transactional
    @Override
    public List<Review> findByBookId(Serializable bookId) {
        Query q = entityManager.createQuery("Select a from Review a  where a.bookId.id = ?1");
        q.setParameter(1, bookId);
        List<Review> list = q.getResultList();
        return list;
    }

    @Override
    @Transactional
    public Object findOne(Serializable id) {
        Query q = entityManager.createQuery("select a from Review a where a.id = ?1");
        q.setParameter(1, id);
        return q.getSingleResult();
    }

    public Iterable save(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional
    public boolean exists(Serializable id) {
        boolean found = false;
        try {
            Review review = this.findById(id);
            found = true;
        } catch (NoResultException nre) {
            found = false;
        }
        return found;
    }

    public Iterable findAll(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Transactional
    public long count() {
        return (Long) entityManager.createQuery("Select count(a.id) from Review a").getSingleResult();
    }

    public void delete(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void deleteAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Transactional
    public void delete(Serializable id) {

        //   System.out.println("serial delete of review");
        Review review = (Review) id;
        if (!entityManager.contains(review)) {
            Query q = entityManager.createQuery("select a from Review a where a.id = ?1");
            q.setParameter(1, review.getId());
            review = (Review) q.getSingleResult();
        }
        entityManager.remove(review);
    }

    @Transactional
    public void delete(Object entity) {
        Review review = (Review) entity;
        if (!entityManager.contains(review)) {
            Query q = entityManager.createQuery("select a from Review a where a.id = ?1");
            q.setParameter(1, review.getId());
            review = (Review) q.getSingleResult();
        }
        entityManager.remove(entity);
    }
    
    @Transactional
    @Override
    public void delete(Integer id) {
        Review review = findById(id);
        if (review != null) {
            delete(review);
        }
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
}
