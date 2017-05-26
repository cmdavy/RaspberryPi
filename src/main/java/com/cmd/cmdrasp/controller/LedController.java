package com.cmd.cmdrasp.controller;

import com.pi4j.io.gpio.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;


/**
 * Created by chrisdavy on 5/24/17.
 */
@RestController
public class LedController {

    private static GpioPinDigitalOutput greenPin;
    private static GpioPinDigitalOutput yellowPin;
    private static GpioPinDigitalOutput redPin;

    private static int count = -1;

    private static String asyncResult = "";
    private static boolean asyncRunning = false;

    @RequestMapping("/")
    public String greeting()
    {
        return "Hello World!";
    }

    @RequestMapping("/light")
    public String light()
    {
        init();

        count++;

        return toggle(count);
    }

    @Async
    @RequestMapping("/streetlight/{duration}")
    public String streetlight(@PathVariable("duration") String duration) throws InterruptedException
    {
        if (asyncRunning)
        {
            return "Busy...";
        }
        asyncRunning = true;
        asyncResult = "Running...";
        init();

        int durationInSec = Integer.parseInt(duration);
        for (int i = 0; i < durationInSec; i++)
        {
            i++;

            toggle(i);
            try {
                Thread.sleep(1000);
            }
            catch (Exception ex)
            {
                asyncResult = ex.getMessage();
                return ex.getMessage();
            }

            asyncResult = String.format("Running for %d out of %d seconds...", i, durationInSec);
        }
        resetPins();
        asyncResult = "Done";
        asyncRunning = false;
        return "Done";
    }

    @RequestMapping("/streetlight/result")
    public String streetLightResult()
    {
        return asyncResult;
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
            if (state.toLowerCase().equals("toggle"))
            {
                greenPin.toggle();
                return String.format("%s LED is %s", color, greenPin.getState().toString());
            }
            else {
                greenPin.setState(pinState);
            }
        }
        else if (color.toLowerCase().equals("yellow"))
        {
            if (state.toLowerCase().equals("toggle"))
            {
                yellowPin.toggle();
                return String.format("%s LED is %s", color, yellowPin.getState().toString());
            }
            else {
                yellowPin.setState(pinState);
            }
        }
        else if (color.toLowerCase().equals("red"))
        {
            if (state.toLowerCase().equals("toggle"))
            {
                redPin.toggle();
                return String.format("%s LED is %s", color, redPin.getState().toString());
            }
            else {
                redPin.setState(pinState);
            }
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

    private String toggle(int index)
    {
        resetPins();

        if (index % 3 == 0)
        {
            greenPin.high();
            return "Green";
        }
        else if (index % 3 == 1)
        {
            yellowPin.high();
            return "Yellow";
        }
        else {
            redPin.high();
            return "Red";
        }
    }

}
