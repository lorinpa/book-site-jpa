/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.rest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;
import org.pa.exception.RestException;
import org.pa.rest.message.MessageDefinitions;
import org.pa.rest.message.SuccessMessage;
import org.pa.validation.CategoryValidator;
import org.pa.entity.Category;
import org.pa.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */

@Controller
public class CategoryRest {

     public final static String EXPORT_ALL_URL = "/export/category/all";
    public final static String ADD_CATEGORY_URL = "/add/category/{title}";
    public final static String MODIFY_CATEGORY_URL = "/modify/category/{id}/{title}";
    public final static String DELETE_CATEGORY_URL = "/delete/category/{id}";

    private CategoriesRepository categoryRepo;

    @Inject
    @Qualifier("categoryRepository")
    public void setCategoryRepo(CategoriesRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @RequestMapping(value = EXPORT_ALL_URL, method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Map<String, List> exportToJson(Model model) throws Exception {

        Map<String, List> dto = new HashMap<>();
        List<Category>  list = categoryRepo.findAll();
        dto.put("categories", list);

        return dto;
    }


    @RequestMapping(value = ADD_CATEGORY_URL, method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object addCategory(@PathVariable String title) {
        Category category = null;

        try {
            category = new Category();
            category.setTitle(title);
            CategoryValidator categoryValidator = new CategoryValidator();
            Map validationMap = new HashMap();
            validationMap.put("title", category.getTitle());
            BindingResult result = new MapBindingResult(validationMap, "category");
            categoryValidator.validate(category, result);
            if (result.hasErrors()) {
                RestException re = new RestException("Unable to Add Category");
                re.putTarget("category", category);
                return re.exceptionMap();
            }
            categoryRepo.addNew(category);
            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.ADD_OPERATION, MessageDefinitions.CATEGORY_ENTITY, category);
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException("Category Already Exists! Can not add again!");
            } else {
                re = new RestException("Unexpected Exception. Unable to Save Category!");
            }
            re.putTarget("category", category);
            return re.exceptionMap();
        }
    }


    @RequestMapping(value = MODIFY_CATEGORY_URL, method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Object updateCategory(@PathVariable Integer id, @PathVariable String title) {

        Category category = null;
        try {
            category = new Category();
            category.setTitle(title);
            category.setId(id);
            Map validationMap = new HashMap();
            validationMap.put("id", category.getId());
            validationMap.put("title", category.getTitle());
            CategoryValidator categoryValidator = new CategoryValidator();
            BindingResult result = new MapBindingResult(validationMap, "category");
            categoryValidator.validate(category, result);
            if (result.hasErrors()) {
                RestException re = new RestException("Unable to Update Category");
                re.putTarget("category", category);
                return re.exceptionMap();
            }
            categoryRepo.update(category.getId(), category.getTitle());

            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.MOD_OPERATION, MessageDefinitions.CATEGORY_ENTITY, category);
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException("Category Already Exists! Can not add again!");
            } else {
                re = new RestException("Unexpected Exception. Unable to Save Category!");
            }
            re.putTarget("category", category);
            return re.exceptionMap();
        }
    }

 @RequestMapping(value = DELETE_CATEGORY_URL, method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object deleteBook(@PathVariable Integer id) {
        Category category = null;
        try {
            category = categoryRepo.findById(id);
            categoryRepo.delete(category);
            SuccessMessage successMessage = new SuccessMessage();
            successMessage.setEntity(MessageDefinitions.CATEGORY_ENTITY);
            successMessage.setAction(MessageDefinitions.DEL_OPERATION);
            successMessage.setData(category);
            return successMessage;

        } catch (Exception ex) {
            RestException re = new RestException(ex.getMessage());
            re.putTarget("category", category);
            return re.exceptionMap();
        }
    }

}
