package com.cmd.cmdrasp.controller;

import com.pi4j.io.gpio.*;
import org.springframework.web.bind.annotation.PathVariable;
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

    private static int count = -1;

    @RequestMapping("/")
    public String greeting()
    {
        return "Hello World!";
    }

    @RequestMapping("/light")
    public String light()
    {
        init();

        resetPins();

        count++;

        if (count % 3 == 0)
        {
            greenPin.high();
            return "Green";
        }
        else if (count % 3 == 1)
        {
            yellowPin.high();
            return "Yellow";
        }
        else {
            redPin.high();
            return "Red";
        }
    }


    @RequestMapping("/light/{color}/{state}")
    public String changeLightState(@PathVariable("color") String color, @PathVariable("state") String state)
    {
        init();

        PinState pinState = PinState.LOW;
        if (state.toLowerCase().equals("on"))
        {
            pinState = PinState.HIGH;
        }

        if (color.toLowerCase().equals("green"))
        {
            greenPin.setState(pinState);
        }
        else if (color.toLowerCase().equals("yellow"))
        {
            yellowPin.setState(pinState);
        }
        else if (color.toLowerCase().equals("red"))
        {
            redPin.setState(pinState);
        }
        else
        {
            return String.format("%s LED could not be set to %s", color, state);
        }
        return String.format("%s LED is %s", color, state);
    }

    private void init()
    {
        if (greenPin == null || yellowPin == null || redPin == null) {
            GpioController gpioController = GpioFactory.getInstance();
            greenPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_07, "Green", PinState.LOW);
            yellowPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Yellow", PinState.LOW);
            redPin = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Red", PinState.LOW);
        }
    }

    private void resetPins()
    {
        greenPin.low();
        yellowPin.low();
        redPin.low();
    }

}
