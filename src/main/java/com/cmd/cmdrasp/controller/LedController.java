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

    private static GpioPinDigitalOutput greenLed;
    private static GpioPinDigitalOutput yellowLed;
    private static GpioPinDigitalOutput redLed;
    private static GpioPinDigitalOutput blueLed;

    private HashMap<String, GpioPinDigitalOutput> gpioPinDigitalOutputHashMap = new HashMap<String, GpioPinDigitalOutput>();

    private static int count = -1;

    private static String asyncResult = "Not started";
    private static boolean asyncRunning = false;

    private void init()
    {
        if (greenLed == null || yellowLed == null || redLed == null) {
            GpioController gpioController = GpioFactory.getInstance();
            greenLed = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_07, "Green", PinState.LOW);
            yellowLed = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Yellow", PinState.LOW);
            redLed = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Red", PinState.LOW);
            blueLed = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03, "Blue", PinState.LOW);

            gpioPinDigitalOutputHashMap.put("GREEN", greenLed);
            gpioPinDigitalOutputHashMap.put("YELLOW", yellowLed);
            gpioPinDigitalOutputHashMap.put("RED", redLed);
            gpioPinDigitalOutputHashMap.put("BLUE", blueLed);
        }
    }

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

        return toggle(count, gpioPinDigitalOutputHashMap.size());
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
            String color = toggle(i, 3);
            try {
                if (color.toLowerCase().equals("green") || color.toLowerCase().equals("red"))
                {
                    Thread.sleep(3000);
                }
                else {
                    Thread.sleep(1000);
                }
            }
            catch (Exception ex)
            {
                asyncResult = ex.getMessage();
                return ex.getMessage();
            }

            asyncResult = String.format("Running for %d of %d seconds...", i, durationInSec);
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

        if (gpioPinDigitalOutputHashMap.containsKey(color.toUpperCase()))
        {
            GpioPinDigitalOutput thisPin = gpioPinDigitalOutputHashMap.get(color.toUpperCase());
            if (state.toLowerCase().equals("toggle"))
            {
                thisPin.toggle();
                return String.format("%s LED is %s", color, thisPin.getState().toString());
            }
            else {
                thisPin.setState(pinState);
            }
        }
        else
        {
            return String.format("%s LED could not be set to %s", color, state);
        }
        return String.format("%s LED is %s", color, state);
    }

    @Async
    @RequestMapping("/dance/{duration}")
    public String dance(@PathVariable("duration") String duration) throws InterruptedException
    {
        if (asyncRunning)
        {
            return "Busy...";
        }
        asyncRunning = true;
        asyncResult = "Dancing...";
        init();

        int durationInSec = Integer.parseInt(duration);
        for (int i = 0; i < durationInSec; i++)
        {
            try {
                toggle((int)Math.floor(Math.random() * gpioPinDigitalOutputHashMap.size()), gpioPinDigitalOutputHashMap.size());
                Thread.sleep(500);
            }
            catch (Exception ex)
            {
                asyncResult = ex.getMessage();
                return ex.getMessage();
            }

            asyncResult = String.format("Dancing for %d of %d seconds...", i, durationInSec);
        }
        resetPins();
        asyncResult = "Done";
        asyncRunning = false;
        return "Done";
    }


    @RequestMapping("/off")
    public String turnOff()
    {
        init();
        resetPins();
        return "All LEDs are off";
    }

    private void resetPins()
    {
        greenLed.low();
        yellowLed.low();
        redLed.low();
        blueLed.low();
    }

    private String toggle(int index, int count)
    {
        resetPins();

        if (index % count == 0)
        {
            greenLed.high();
            return "Green";
        }
        else if (index % count == 1)
        {
            yellowLed.high();
            return "Yellow";
        }
        else if (index % count == 2){
            redLed.high();
            return "Red";
        }
        else
        {
            blueLed.high();
            return "Blue";
        }
    }

}
