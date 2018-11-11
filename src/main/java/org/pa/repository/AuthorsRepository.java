package org.pa.repository;

import java.io.Serializable;
import java.util.List;
import org.pa.entity.Author;
import org.springframework.stereotype.Repository;

/**
 *
 * @author
 * mwave
 */
@Repository  
public interface AuthorsRepository  {

    public Object addNew(Object s) throws Exception;

    public Author update(Serializable id, String first_name, String last_name) throws Exception;

    public List<Author> findAll();

    public Author findById(Serializable id) throws javax.persistence.NoResultException;

    public void delete(Serializable id);

    public void delete(Integer author_id);

    public long count();
}
