package com.cmd.cmdrasp.controller;

import com.pi4j.io.gpio.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.joda.time.LocalDateTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;


/**
 * Created by chrisdavy on 5/24/17.
 */
@RestController
@RequestMapping("/")
@Api(value="Raspberry Pi Led Application")
public class LedController {

    private static GpioPinDigitalOutput greenLed;
    private static GpioPinDigitalOutput yellowLed;
    private static GpioPinDigitalOutput redLed;
    private static GpioPinDigitalOutput blueLed;

    private static int danceSpeed = 200;

    private HashMap<String, GpioPinDigitalOutput> gpioPinDigitalOutputHashMap = new HashMap<String, GpioPinDigitalOutput>();

    private static int count = -1;

    private static String asyncResult = "Not started";
    private static boolean asyncRunning = false;

    private static boolean kill = false;

    private void init()
    {
        kill = false;
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

    @RequestMapping(value="/light", method= RequestMethod.PUT)
    @ApiOperation(value="Move on to the next light")
    public String light()
    {
        init();

        count++;

        return toggle(count, gpioPinDigitalOutputHashMap.size());
    }

    @Async
    @RequestMapping(value="/streetlight/{duration}", method= RequestMethod.PUT)
    @ApiOperation(value="Street Light Simulation - Green, Yellow, Red")
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
        int thisDuration = durationInSec;
        for (int i = 0; i < thisDuration && !kill; i++)
        {
            String color = toggle(i, 3);
            try {
                if (color.toLowerCase().equals("green") || color.toLowerCase().equals("red"))
                {
                    Thread.sleep(3000);
                    thisDuration -= 3;
                }
                else {
                    Thread.sleep(1000);
                    thisDuration--;
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

    @RequestMapping(value="/result", method= RequestMethod.GET)
    @ApiOperation(value="Get the result of the current operation")
    public String getAsyncResult()
    {
        return asyncResult;
    }

    @RequestMapping(value="/light/{color}/{state}", method= RequestMethod.PUT)
    @ApiOperation(value="You have options: Toggle, On, or Off")
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
    @RequestMapping(value="/dance/{duration}", method= RequestMethod.PUT)
    @ApiOperation(value="May I have this dance?")
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
        LocalDateTime startTime = LocalDateTime.now();
        while (LocalDateTime.now().isBefore(startTime.plusSeconds(durationInSec)) && !kill)
        {
            try {
                toggle((int)Math.floor(Math.random() * gpioPinDigitalOutputHashMap.size()), gpioPinDigitalOutputHashMap.size());
                Thread.sleep(danceSpeed);
            }
            catch (Exception ex)
            {
                asyncResult = ex.getMessage();
                return ex.getMessage();
            }

            asyncResult = String.format("Dancing until %s...", startTime.plusSeconds(durationInSec).toString());
        }
        resetPins();
        asyncResult = "Done";
        asyncRunning = false;
        return "Done";
    }

    @RequestMapping(value="/dance/faster", method=RequestMethod.PUT)
    @ApiOperation(value="Break it down now!")
    public String speedUp()
    {
        if (!areWeDancing())
        {
            return "Get on the dance floor first you wallflower!";
        }
        if (danceSpeed > 100)
        {
            danceSpeed = danceSpeed - 100;
        }
        else
        {
            return "We are going as fast as we can Capt'n";
        }
        return "Put the pedal to the metal!";
    }

    @RequestMapping(value="/dance/slower", method=RequestMethod.PUT)
    @ApiOperation(value="Ooohhh Yeeeaaaahhhhh?!")
    public String slowDown()
    {
        if (!areWeDancing())
        {
            return "You have to get on the dance floor before you can slow dance! Don't be shy!";
        }
        if (danceSpeed < 2000)
        {
            danceSpeed = danceSpeed + 100;
        }
        else
        {
            return "We are already watching this train wreck in slow motion!";
        }
        return "Whoa there horsey?!";
    }

    @RequestMapping(value="/off", method= RequestMethod.PUT)
    @ApiOperation(value="You are such a turn off!")
    public String turnOff()
    {
        init();
        resetPins();
        return "All LEDs are off";
    }

    @RequestMapping(value="/stop", method= RequestMethod.PUT)
    @ApiOperation(value="STOP...Hammer Time!")
    public String stop()
    {
        kill = true;
        turnOff();
        return "Crickets";
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

    private boolean areWeDancing()
    {
        if (asyncResult.contains("Dancing"))
        {
            return true;
        }
        return false;
    }

}
