package org.pa.repository;

import java.io.Serializable;
import java.util.List;
import org.pa.dto.BookCategoryExport;
import org.pa.entity.Book;
import org.pa.entity.BookCategory;
import org.pa.entity.Category;
import org.springframework.stereotype.Repository;

/**
 *
 * @author lorinpa
 */
@Repository
public interface BookCategoriesRepository  {
    
    public Object addNew(Object s)  throws Exception;

    public List<BookCategory> findAll();
    
    public List<BookCategoryExport> exportAll();

    public BookCategory findById(Serializable id) throws javax.persistence.NoResultException;

    public Object findOne(Serializable id);

    public List<Book> findBooksByCategory(Serializable id);

    public boolean exists(Serializable id);

    public BookCategory update(Serializable id, Book book, Category category) throws Exception;

    public long count();
    
    public void delete(Integer id);
}
