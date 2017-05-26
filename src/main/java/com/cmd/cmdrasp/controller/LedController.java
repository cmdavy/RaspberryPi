package com.cmd.cmdrasp.controller;

import com.pi4j.io.gpio.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

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

        count++;

        return toggle(count);
    }

    @Async
    @RequestMapping("/streetlight/{duration}")
    public String streetlight(@PathVariable("duration") String duration) throws InterruptedException
    {
        init();

        int duration = Integer.parseInt(duration);
        int index = -1;
        LocalDateTime startTime = LocalDateTime.now();
        for (int i = 0; i < 10; i++)
        {
            index++;

            toggle(index);
            try {
                Thread.sleep(1000);
            }
            catch (Exception ex)
            {
                return "Error sleeping";
            }
        }
        index = -1;
        while (LocalDateTime.now().isBefore(startTime.plusSeconds(duration / 1000)))
        {
            index ++;
            toggle(index);
            try {
                Thread.sleep(1000);
            }
            catch (Exception ex)
            {
                return "Error sleeping";
            }
        }
        return "All done!";
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
