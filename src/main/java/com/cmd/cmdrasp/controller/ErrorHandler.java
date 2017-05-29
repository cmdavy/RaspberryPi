package com.cmd.cmdrasp.controller;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chrisdavy on 5/28/17.
 */
public class ErrorHandler implements ErrorController{

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "Sorry, an unexpected error occurred";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
