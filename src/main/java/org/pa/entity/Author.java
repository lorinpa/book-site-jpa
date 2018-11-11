/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author
 * mwave
 */
@JsonIgnoreProperties({ "booksCollection", "createdAt","updatedAt" })
@Entity
@Table(name="Authors")
@NamedQueries({
    @NamedQuery(name = "Author.findAll", query = "SELECT a FROM Author a"),
    @NamedQuery(name = "Author.findById", query = "SELECT a FROM Author a WHERE a.id = :id"),
    @NamedQuery(name = "Author.findByFirstName", query = "SELECT a FROM Author a WHERE a.firstName = :firstName"),
    @NamedQuery(name = "Author.findByLastName", query = "SELECT a FROM Author a WHERE a.lastName = :lastName"),
    @NamedQuery(name = "Author.findByCreatedAt", query = "SELECT a FROM Author a WHERE a.createdAt = :createdAt"),
    @NamedQuery(name = "Author.findByUpdatedAt", query = "SELECT a FROM Author a WHERE a.updatedAt = :updatedAt")})
public class Author implements Serializable {
    private static final long serialVersionUID = 1L;
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;

    @Basic(optional = false)
    @Column(name = "FIRST_NAME")
    private String firstName;
   
    @Basic(optional = false)
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "UPDATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "authorId")
    private Collection<Book> booksCollection;

    public Author() {
    }

    public Author(Integer id) {
        this.id = id;
    }

    public Author(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

  
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public Collection<Book> getBooksCollection() {
        return booksCollection;
    }

    public void setBooksCollection(Collection<Book> booksCollection) {
        this.booksCollection = booksCollection;
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
        if (!(object instanceof Author)) {
            return false;
        }
        Author other = (Author) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.pa.entity.Authors[ id=" + id + " ]";
    }
    
}
