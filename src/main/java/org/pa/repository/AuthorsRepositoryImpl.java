package org.pa.repository;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.pa.entity.Author;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *  Service component. Moves information in and out of the application's database.
 *  Note. Entity and transaction managers are injected. Transaction operations (e.g commit/rollback) are also injected. 
 * @author lorinpa
 */
@Service
public class AuthorsRepositoryImpl implements AuthorsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Author update(Serializable id, String first_name, String last_name) throws Exception {
        Author author = findById(id);
        author.setFirstName(first_name);
        author.setLastName(last_name);
        entityManager.merge(author);
        return author;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findAll() {
        Query q = entityManager.createQuery("select a from Author a");
        List<Author> list = q.getResultList();
        return list;
    }

    @Transactional(readOnly = true)
    public Author findById(Serializable id) throws javax.persistence.NoResultException {
        Query q = entityManager.createQuery("select a from Author a where a.id = ?1");
        q.setParameter(1, id);
        return (Author) q.getSingleResult();
    }

 
    public Object findOne(Serializable id) {
        Query q = entityManager.createQuery("select a from Author a where a.id = ?1");
        q.setParameter(1, id);
        return q.getSingleResult();
    }

  
    @Transactional(readOnly = true)
    public boolean exists(Serializable id) {
        boolean found = false;
        try {
            Author author = this.findById(id);
            found = true;
        } catch (NoResultException nre) {
            found = false;
        }
        return found;
    }
   
    public Iterable findAll(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return (Long) entityManager.createQuery("Select count(a.id) from Author a").getSingleResult();
    }

    @Transactional
    public void delete(Integer author_id) {
        Author author = this.findById(author_id);
        if (author != null) {
            delete(author);
        }
    }

    @Override
    @Transactional
    public void delete(Serializable id) {
        Author author = (Author) id;
        if (!entityManager.contains(author)) {
            Query q = entityManager.createQuery("select a from Author a where a.id = ?1");
            q.setParameter(1, author.getId());
            author = (Author) q.getSingleResult();
        }
        entityManager.remove(author);
    }

   
    @Transactional
    public void delete(Object t) {
        Author author = (Author) t;
        if (!entityManager.contains(author)) {
            Query q = entityManager.createQuery("select a from Author a where a.id = ?1");
            q.setParameter(1, author.getId());
            author = (Author) q.getSingleResult();
        }
        entityManager.remove(author);
    }

   
    public void delete(Iterable itrbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    public void deleteAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public Iterable save(Iterable arg0) {
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
}
