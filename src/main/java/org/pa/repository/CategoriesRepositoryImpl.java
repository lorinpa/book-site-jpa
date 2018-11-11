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
import org.pa.entity.Author;
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
public class CategoriesRepositoryImpl implements CategoriesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Category update(Serializable id, String title) throws Exception {
        Category category = findById(id);
        category.setTitle(title);
        entityManager.merge(category);
        return category;
    }

    @Override
    @Transactional
    public List<Category> findAll() {

        Query q = entityManager.createQuery("select a from Category a");
        List<Category> list = q.getResultList();
        return list;
    }

    @Transactional
    public Category findById(Serializable id) throws javax.persistence.NoResultException {
        Query q = entityManager.createQuery("select a from Category a where a.id = ?1");
        q.setParameter(1, id);
        return (Category) q.getSingleResult();
    }

    @Transactional
    public Object findOne(Serializable id) {
        Query q = entityManager.createQuery("select a from Category a where a.id = ?1");
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
            Category category = this.findById(id);
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
        return (Long) entityManager.createQuery("Select count(a.id) from Category a").getSingleResult();

    }

    @Transactional
    public void delete(Category category) throws Exception {
        if (!entityManager.contains(category)) {
            Query q = entityManager.createQuery("select a from Category a where a.id = ?1");
            q.setParameter(1, category.getId());
            category = (Category) q.getSingleResult();
        }
        entityManager.remove(category);
    }
    
     @Transactional
    public void delete(Integer id) throws Exception {
         Category category = this.findById(id);
        if (category != null) {
            delete(category);
        }
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
        entityManager.remove(id);
    }

    @Transactional
    public void delete(Object entity) {
        entityManager.remove(entity);
    }
}
