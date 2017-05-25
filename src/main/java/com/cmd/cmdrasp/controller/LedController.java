package com.cmd.cmdrasp.controller;

import com.pi4j.io.gpio.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by chrisdavy on 5/24/17.
 */
@RestController
public class LedController {

    private static GpioPinDigitalOutput pin;

    @RequestMapping("/")
    public String greeting()
    {
        return "Hello World!";
    }

    @RequestMapping("/light")
    public String light()
    {
        if (pin == null) {
            GpioController gpioController = GpioFactory.getInstance();
            pin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_07, "MyLED", PinState.LOW);
        }


        pin.toggle();
        if (pin.getState().isLow())
        {
            return "Off";
        }
        else
        {
            return "On";
        }
    }

}
