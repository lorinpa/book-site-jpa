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
import org.pa.entity.Author;
import org.pa.exception.MessageDetailDefinitions;
import org.pa.exception.RestException;
import org.pa.repository.AuthorsRepository;
import org.pa.rest.message.MessageDefinitions;
import org.pa.rest.message.SuccessMessage;
import org.pa.validation.AuthorValidator;
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
 * @author lorinpa
 * 
 */
@Controller
public class AuthorRest {

    private AuthorsRepository authorsRepository;
    public final static String EXPORT_ALL_URL = "/export/authors/all";
    public final static String ADD_AUTHOR_URL = "/add/authors/{firstName}/{lastName}";
    public final static String MODIFY_AUTHOR_URL = "/modify/authors/{id}/{firstName}/{lastName}";
    public final static String DELETE_AUTHOR_URL = "/delete/authors/{id}";

    @Inject
    @Qualifier("authorRepository")
    public void setAuthorsRepository(AuthorsRepository authorsRepository) {
        this.authorsRepository = authorsRepository;
    }

    @RequestMapping(value = EXPORT_ALL_URL, method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Map<String, List> exportToJson(Model model) throws Exception {
        Map<String, List> dto = new HashMap<>();
        List<Author> list;
        list = authorsRepository.findAll();
        dto.put("authors", list);
        return dto;
    }

    @RequestMapping(value = ADD_AUTHOR_URL, method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object addAuthor(@PathVariable String firstName, @PathVariable String lastName) {
        Author author = null;
        try {
            author = new Author();
            author.setFirstName(firstName);
            author.setLastName(lastName);
            AuthorValidator authorValidator = new AuthorValidator();
            Map validationMap = new HashMap();
            validationMap.put("lastName", author.getLastName());
            validationMap.put("firstName", author.getFirstName());
            BindingResult result = new MapBindingResult(validationMap, "author");
            authorValidator.validate(author, result);
            if (result.hasErrors()) {
                RestException re = new RestException(MessageDetailDefinitions.ADD_AUTHOR_EXCEPTION);
                re.putTarget("author", author);
                return re.exceptionMap();
            }
            authorsRepository.addNew(author);
            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.ADD_OPERATION, MessageDefinitions.AUTHOR_ENTITY, author);
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException(MessageDetailDefinitions.DUPLICATE_AUTHOR_EXCEPTION);
            } else {
                re = new RestException(MessageDetailDefinitions.SAVE_AUTHOR_EXCEPTION);
            }
            re.putTarget("author", author);
            return re.exceptionMap();
        }
    }

    @RequestMapping(value = MODIFY_AUTHOR_URL, method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Object updateAuthor(@PathVariable Integer id, @PathVariable String firstName, @PathVariable String lastName) {

        Author author = new Author();
        try {
            author.setId(id);
            author.setFirstName(firstName);
            author.setLastName(lastName);
            Map validationMap = new HashMap();
            validationMap.put("id", author.getId());
            validationMap.put("lastName", author.getLastName());
            validationMap.put("firstName", author.getFirstName());
            AuthorValidator authorValidator = new AuthorValidator();
            BindingResult result = new MapBindingResult(validationMap, "author");
            authorValidator.validate(author, result);
            if (result.hasErrors()) {
                RestException re = new RestException(MessageDetailDefinitions.UPDATE_AUTHOR_EXCEPTION);
                re.putTarget("author", author);
                return re.exceptionMap();
            }
            authorsRepository.update(author.getId(), author.getFirstName(), author.getLastName());

            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.MOD_OPERATION, MessageDefinitions.AUTHOR_ENTITY, author);
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException(MessageDetailDefinitions.DUPLICATE_AUTHOR_EXCEPTION);
            } else {
                re = new RestException(MessageDetailDefinitions.SAVE_AUTHOR_EXCEPTION);
            }
            re.putTarget("author", author);
            return re.exceptionMap();
        }
    }

    @RequestMapping(value = DELETE_AUTHOR_URL, method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object deleteBook(@PathVariable Integer id) {
        Author author = null;
        try {
            author = authorsRepository.findById(id);
            authorsRepository.delete(author);
            SuccessMessage successMessage = new SuccessMessage();
            successMessage.setEntity(MessageDefinitions.AUTHOR_ENTITY);
            successMessage.setAction(MessageDefinitions.DEL_OPERATION);
            successMessage.setData(author);
            return successMessage;
        } catch (Exception ex) {
            RestException re = new RestException(MessageDetailDefinitions.DELETE_AUTHOR_EXCEPTION);
            re.putTarget("author", author);
            return re.exceptionMap();
        }
    }
}
