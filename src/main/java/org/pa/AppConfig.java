package org.pa;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.pa.repository.AuthorsRepository;
import org.pa.repository.AuthorsRepositoryImpl;
import org.pa.repository.BookCategoriesRepository;
import org.pa.repository.BookCategoriesRepositoryImpl;
import org.pa.repository.BooksRepository;
import org.pa.repository.BooksRepositoryImpl;
import org.pa.repository.CategoriesRepository;
import org.pa.repository.CategoriesRepositoryImpl;
import org.pa.repository.ReviewsRepository;
import org.pa.repository.ReviewsRepositoryImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class AppConfig {

    // See getDbFileName() 
    private final static String DB_FILE_NAME = getDbFileName();
    // Database URL -- We are using the H2 database in "server" mode.
    // Note! ~// designates the current user home directory. We shoud externalize the directory location.
    private final static String DB_URL = "jdbc:h2:~//development/workspace/java-projects/book-site-jpa/db/"+DB_FILE_NAME+".db;AUTO_SERVER=TRUE";
 
   
   
    
    /** 
     *  This function allows us to control which database instance the application is running against (E.G. test, development, production).. 
     * "dbFileName" is a OS/Shell environment variable. "dbFileName" can be set within the  build/run file script or 
     *  at the command line java -jar ..... -DdbFileame="....../,.,,," or mvn run -DdbFileName=".....
     * The idea is, during automated testing, we don't impact developer created data.
     */
    private final static String getDbFileName() {
        String fileName = "dev";
        if (System.getProperty("dbFileName") != null) {
            fileName = System.getProperty("dbFileName");
        }
        return fileName;
    }

    /*
     *  Note : the setPackagesToScan clause instructs the Dependency Injector which source files
     *  require entity manager dependencies and injection. 
     */
    public @Bean
    EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(pooledDs());
        emf.setPackagesToScan(new String[]{"org.pa.entity", "org.pa.repository", "org.pa.controller", "org.pa.application"});
        emf.setPersistenceProvider(new HibernatePersistenceProvider());
        emf.setPersistenceUnitName("localH2");
        HibernateJpaDialect hibernateJpaDialect = new HibernateJpaDialect();
        emf.setJpaProperties(getJPAProperties());
        emf.setJpaDialect(hibernateJpaDialect);
        emf.afterPropertiesSet();
        return emf.getObject();
    }

    /*
     * Adds a database connection pool. 
     */
    @Bean
    public org.apache.commons.dbcp.BasicDataSource pooledDs() {

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(DB_URL);
        ds.setMaxActive(8);
        ds.setMaxIdle(8);
        ds.setMaxWait(-1);
        ds.setPoolPreparedStatements(true);
        return ds;
    }

    /*
     * Transaction manager. This application uses the Dependency Injector to "inject" 
     *  transaction start, commit, rollback details. 
     * 
     * Note! We are using Hibernate's transaction manager here. 
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory());
        transactionManager.setDataSource(pooledDs());
        transactionManager.setJpaDialect(new HibernateJpaDialect());
        return transactionManager;
    }

    /*
     *  Let's seperate the Hibernate  settings from a potential replacement ORM (or even Hibernate version).
     */
    private Properties getJPAProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        props.setProperty("javax.persistence.jdbc.driver", "org.h2.Driver");
        props.setProperty("javax.persistence.jdbc.url", DB_URL);
        props.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        props.setProperty("hibernate.connection.url", DB_URL);
        props.setProperty("hibernate.connection.release_mode", "on_close");
		return props;
    }

    @Bean
    public BooksRepository bookRepository() {
        return new BooksRepositoryImpl();
    }

    @Bean
    public AuthorsRepository authorRepository() {
        return new AuthorsRepositoryImpl();
    }

    @Bean
    public CategoriesRepository categoryRepository() {
        return new CategoriesRepositoryImpl();
    }

    @Bean
    public BookCategoriesRepository BookCategoryRepository() {
        return new BookCategoriesRepositoryImpl();
    }

    @Bean
    public ReviewsRepository reviewRepository() {
        return new ReviewsRepositoryImpl();
    }
}
