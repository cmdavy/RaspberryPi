package com.cmd.cmdrasp.controller;

import com.pi4j.io.gpio.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by chrisdavy on 5/24/17.
 */
@RestController
public class LedController {

    private static GpioPinDigitalOutput greenPin;
    private static GpioPinDigitalOutput yellowPin;
    private static GpioPinDigitalOutput redPin;

    @RequestMapping("/")
    public String greeting()
    {
        return "Hello World!";
    }

    @RequestMapping("/light")
    public String light()
    {
        if (greenPin == null || yellowPin == null || redPin == null) {
            GpioController gpioController = GpioFactory.getInstance();
            greenPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_07, "Green", PinState.HIGH);
            yellowPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_11, "Yellow", PinState.LOW);
            redPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_13, "Red", PinState.HIGH);
        }


        greenPin.toggle();
        if (greenPin.getState().isLow())
        {
            return "Off";
        }
        else
        {
            return "On";
        }
    }

}
