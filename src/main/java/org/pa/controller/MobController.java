/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */

@Controller
public class MobController {
    
     private final static String HOME_URL = "mob/home";
     
     
    @RequestMapping(value = HOME_URL, method = RequestMethod.GET)
    public ModelAndView displayHome() throws Exception {
        ModelAndView mav = new ModelAndView(HOME_URL);
        return mav;
    }
    
}
