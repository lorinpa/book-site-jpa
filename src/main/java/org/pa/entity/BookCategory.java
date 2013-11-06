/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author
 * mwave
 */
@Entity
@Table(name = "BOOK_CATEGORIES")
@NamedQueries({
    @NamedQuery(name = "BookCategory.findAll", query = "SELECT b FROM BookCategory b"),
    @NamedQuery(name = "BookCategory.findById", query = "SELECT b FROM BookCategory b WHERE b.id = :id"),
    @NamedQuery(name = "BookCategory.findByCreatedAt", query = "SELECT b FROM BookCategory b WHERE b.createdAt = :createdAt"),
    @NamedQuery(name = "BookCategory.findByUpdatedAt", query = "SELECT b FROM BookCategory b WHERE b.updatedAt = :updatedAt")})
public class BookCategory implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Column(name = "CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "UPDATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Category categoryId;
    @JoinColumn(name = "BOOK_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Book bookId;

    public BookCategory() {
    }

    public BookCategory(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    public Book getBookId() {
        return bookId;
    }

    public void setBookId(Book bookId) {
        this.bookId = bookId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookCategory)) {
            return false;
        }
        BookCategory other = (BookCategory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.pa.entity.BookCategory[ id=" + id + " ]";
    }
    
}
