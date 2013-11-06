package org.pa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.pa.entity.BookCategory;
import org.pa.entity.Category;
import org.pa.exception.MessageDetailDefinitions;
import org.pa.repository.CategoriesRepository;
import org.pa.validation.CategoryValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author
 * lorinpa
 public-action.org
 */
@Controller
public class CategoryController {

    private final static String ADD_URL = "category/addCategory";
    private final static String MOD_URL = "category/editCategory";
    private final static String DEL_URL = "category/deleteCategory";
    private final static String LIST_URL = "category/categoryHome";
    private final static String REDIRECT_HOME_URL = "redirect:categoryHome.htm";
   // private final static String JSON_EXPORT = "/category/export";
    
    private CategoriesRepository categoryRepo;

    @Inject
    @Qualifier("categoryRepository")
    public void setCategoryRepo(CategoriesRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.GET)
    public ModelAndView displayDeleteCateogory(@RequestParam("id") Integer id) throws Exception {
        Category category = categoryRepo.findById(id);
        ModelAndView mav = new ModelAndView(DEL_URL);
        mav.addObject("category", category);
        return mav;
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.POST)
    public String submitDeleteForm(@ModelAttribute Category category, BindingResult result, SessionStatus status) {
        try {
            categoryRepo.delete(category);
            return REDIRECT_HOME_URL;
        } catch (PersistenceException pe) {
            ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DELETE_CATEGORY_EXCEPTION);
            result.addError(oe);
            return DEL_URL;
        } catch (Exception ex) {
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
            ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DELETE_CATEGORY_EXCEPTION);
            result.addError(oe);
            return DEL_URL;
        }
    }

    @RequestMapping(value = MOD_URL, method = RequestMethod.GET)
    public ModelAndView displayEditCateogory(@RequestParam("id") Integer id) throws Exception {
        Category category = categoryRepo.findById(id);
        ModelAndView mav = new ModelAndView(MOD_URL);
        mav.addObject("category", category);
        return mav;
    }

    @RequestMapping(value = MOD_URL, method = RequestMethod.POST)
    public String submitEditForm(@ModelAttribute Category category, BindingResult result, SessionStatus status) {
        new CategoryValidator().validate(category, result);
        if (result.hasErrors()) {
            return MOD_URL;
        }
        try {
            categoryRepo.update(category.getId(), category.getTitle());
            return REDIRECT_HOME_URL;
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DUPLICATE_CATEGORY_EXCEPTION);
                result.addError(oe);
            }
            return MOD_URL;
        }  catch(DataIntegrityViolationException de) {
              ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DUPLICATE_CATEGORY_EXCEPTION);
               result.addError(oe);
               return MOD_URL;
        } catch (Exception ex) {
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
            return MOD_URL;
        }
    }

    @RequestMapping(value = ADD_URL, method = RequestMethod.GET)
    public ModelAndView displayAddCategory() throws Exception {
        Category category = new Category();
        ModelAndView mav = new ModelAndView(ADD_URL);
        mav.addObject("category", category);
        return mav;
    }

    @RequestMapping(value = ADD_URL, method = RequestMethod.POST)
    public String submitAddForm(@ModelAttribute Category category, BindingResult result, SessionStatus status) {
        new CategoryValidator().validate(category, result);
        if (result.hasErrors()) {
            return ADD_URL;
        }
        try {
            category.setBookCategoriesCollection(new ArrayList<BookCategory>());
            categoryRepo.addNew(category);
            return REDIRECT_HOME_URL;
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DUPLICATE_CATEGORY_EXCEPTION);
                result.addError(oe);
            }
            return ADD_URL;
        } catch (Exception ex) {
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
            return ADD_URL;
        }
    }

    @RequestMapping(value = LIST_URL, method = RequestMethod.GET)
    public ModelAndView list() throws Exception {
        List<Category> categoryList;
        categoryList = categoryRepo.findAll();
        ModelAndView mav = new ModelAndView(LIST_URL);
        mav.addObject("list", categoryList);
        return mav;
    }
    
}
