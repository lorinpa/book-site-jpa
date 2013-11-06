/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pa.test;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lorinpa public-action.org
 */
public class TestSeed {
    
    private static TestSeed instance = null;
    
     public static TestSeed getInstance() {
        if (instance == null) {
            instance = new TestSeed();
        }
        return instance;
    }

     
     public void say() {
        Logger.getLogger(TestSeed.class.getName()).log(Level.SEVERE, "I am saying");
     }
    
      public static void main(String[] args) {
          TestSeed.getInstance().say();;
      }
     
}
