# Line-by-Line Code Walkthrough

This guide explains the Java source files in the same order they appear in the project so you can read the code and this file side by side.

Important note:

- I explain blank lines only when they matter. They are mostly there just to visually separate code blocks.
- For very repetitive getter methods, I explain them as a short ordered group instead of repeating the exact same sentence many times.
- The walkthrough is still in code order, so you can follow the files top to bottom without guessing what happens next.
- This walkthrough reflects the current version of the app, including the improved UI and the newer pedestrian rule where pedestrians wait for conflicting vehicle traffic to stop.

---

## 1. `src/main/Main.java`

```java
import engine.SimulationEngine;
import ui.SimulationPanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationEngine engine = new SimulationEngine();
            JFrame frame = new JFrame("Traffic Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new SimulationPanel(engine));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
```

### What each part does

| Lines | Explanation |
| --- | --- |
| 1 | Imports `SimulationEngine`, the class that contains all simulation logic. |
| 2 | Imports `SimulationPanel`, the Swing panel that draws the simulation. |
| 4 | Imports `JFrame`, which is the main desktop window. |
| 5 | Imports `SwingUtilities`, which helps start Swing UI code safely. |
| 7 | Declares the `Main` class, which is the entry point of the app. |
| 8 | Declares the `main` method. Java starts execution here. |
| 9 | Runs UI creation on Swing's event dispatch thread, which is the correct thread for GUI work. |
| 10 | Creates the simulation engine. This builds the map, signals, agents, and event system. |
| 11 | Creates the window and gives it the title `Traffic Simulation`. |
| 12 | Says the program should exit when the window is closed. |
| 13 | Places our custom panel inside the window. The panel receives the engine so it can update and draw it. |
| 14 | Sizes the window based on the panel's preferred size. |
| 15 | Centers the window on the screen. |
| 16 | Makes the window visible to the user. |
| 17 | Closes the lambda passed to `invokeLater`. |
| 18 | Closes the `main` method. |
| 19 | Closes the `Main` class. |

---

## 2. `src/main/behavior/BehaviorStrategy.java`

This file defines the contract for agent behavior.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `behavior` package. |
| 3 | Imports `Agent` because behaviors operate on agents. |
| 4 | Imports `SimulationEngine` because a behavior may use the engine state when deciding speed. |
| 6 | Declares the `BehaviorStrategy` interface. Any behavior class must implement it. |
| 7 | `chooseSpeed(...)` asks the behavior how fast the agent wants to move this tick. |
| 9 | `canProceedOnYellow(...)` asks whether this behavior allows the agent to cross on a yellow light. |
| 11 | `getName()` returns a readable name such as `Normal` or `Aggressive`. |
| 12 | Ends the interface. |

### Why this file matters

This is the Strategy Pattern. Instead of hardcoding all driving logic inside `Agent`, the agent can use different behavior objects.

---

## 3. `src/main/behavior/NormalBehavior.java`

This is the safe, standard behavior.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `behavior` package. |
| 3-4 | Imports `Agent` and `SimulationEngine` because the interface methods require them. |
| 6 | Declares `NormalBehavior` and says it implements `BehaviorStrategy`. |
| 8-10 | `chooseSpeed(...)` returns exactly the agent's base speed. Normal behavior does not try to go faster. |
| 13-15 | `canProceedOnYellow(...)` returns `false`, so normal agents stop at yellow. |
| 18-20 | `getName()` returns the label `Normal`. |
| 21 | Ends the class. |

---

## 4. `src/main/behavior/AggressiveBehavior.java`

This is the faster, riskier behavior.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `behavior` package. |
| 3-4 | Imports `Agent` and `SimulationEngine` for the interface methods. |
| 6 | Declares `AggressiveBehavior`. |
| 8-10 | `chooseSpeed(...)` multiplies base speed by `1.35`, so this agent tries to move faster than normal. |
| 13-15 | `canProceedOnYellow(...)` returns `true`, so aggressive agents are allowed to continue on yellow. |
| 18-20 | `getName()` returns `Aggressive`. |
| 21 | Ends the class. |

---

## 5. `src/main/environment/Lane.java`

This file is very important. A lane is the path an agent follows.

### Enums and fields

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `environment` package. |
| 3 | Imports `Point2D` because lanes can return positions as `(x, y)` points. |
| 5 | Declares the `Lane` class. |
| 6-15 | Declares the `Direction` enum. A lane can point `EAST`, `WEST`, `NORTH`, or `SOUTH`. |
| 12-14 | `isHorizontal()` returns `true` for east/west lanes and `false` for north/south lanes. This is used often in drawing and signal logic. |
| 17-20 | Declares the `LaneType` enum with `VEHICLE` and `PEDESTRIAN`. |
| 22 | `id` is a unique string like `main-east`. |
| 23 | `roadName` stores which road this lane belongs to. |
| 24 | `laneType` says whether this lane is for cars or pedestrians. |
| 25 | `direction` stores which way movement goes along this lane. |
| 26 | `speedLimit` limits how fast agents may move on the lane. |
| 27-30 | `startX`, `startY`, `endX`, `endY` define the lane path in grid coordinates. |
| 31 | `blocked` says whether an accident currently blocks the lane. |
| 32 | `blockageReason` stores text explaining why it is blocked. |

### Constructor and getters

| Lines | Explanation |
| --- | --- |
| 34-53 | Constructor for `Lane`. It receives all lane information and saves it into the fields. |
| 55-89 | Getter methods return lane properties. These let other classes read the lane safely without changing it directly. |
| 91-97 | `isBlocked()` and `getBlockageReason()` expose the lane's current blockage state. |
| 99-102 | `setBlocked(...)` changes whether the lane is blocked and stores the reason. Accident events use this. |

### Geometry and intersection logic

| Lines | Explanation |
| --- | --- |
| 104-106 | `getLength()` returns the length of the lane. Because lanes are straight, this is just the larger difference between start/end x or y. |
| 108-114 | `getPointAt(progress)` converts lane progress into an `(x, y)` position. If progress is 0, you are at the lane start. If progress is near lane length, you are near the end. |
| 109 | `clamp(...)` keeps progress inside the valid range before calculating position. |
| 110 | Prevents division by zero if something weird creates a zero-length lane. |
| 111-112 | Calculates the direction vector of the lane as one unit of movement. |
| 113 | Builds and returns the actual point on the lane. |
| 116-132 | `crosses(intersection)` checks whether this lane passes through the rectangular junction area. This is more accurate than checking only the exact center line, because the vehicle lanes are offset from the center. |
| 134-145 | `getProgressAtIntersection(...)` returns the progress value at the edge of the junction box. This is the stop point agents use before entering the intersection. |
| 144-146 | Small helper to keep a number inside a minimum and maximum range. |
| 147 | Ends the class. |

### Why this file matters

A lane is the reason motion is controlled and not random. Agents never choose arbitrary coordinates. They only advance along their lane.

---

## 6. `src/main/environment/Road.java`

This file groups multiple lanes under one road name.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `environment` package. |
| 3-5 | Imports collection classes because a road stores multiple lanes. |
| 7 | Declares the `Road` class. |
| 8 | `name` stores the road name, such as `Main Street`. |
| 9 | `lanes` stores all lanes that belong to that road. |
| 11-14 | Constructor sets the road name and creates an empty lane list. |
| 16-18 | `getName()` returns the road name. |
| 20-22 | `addLane(...)` adds a lane to the road. |
| 24-26 | `getLanes()` returns a read-only view of the lane list so outside code cannot modify it directly. |
| 27 | Ends the class. |

---

## 7. `src/main/environment/Intersection.java`

This file owns the traffic signals and the rules for whether an agent can enter the crossing.

### Fields and constructor

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `environment` package. |
| 3 | Imports `Agent` because signal permission depends on agent behavior. |
| 4-5 | Imports traffic signal classes and signal state enum. |
| 7 | Declares the `Intersection` class. |
| 8 | `id` stores the intersection name. |
| 9-10 | `centerX` and `centerY` store the center point of the intersection. |
| 11 | `size` controls how large the intersection area is when drawn. |
| 12 | `horizontalSignal` controls east/west traffic. |
| 13 | `verticalSignal` controls north/south traffic. |
| 15-22 | Constructor saves the intersection geometry and creates one horizontal and one vertical signal. |

### Getters

| Lines | Explanation |
| --- | --- |
| 24-62 | Getter methods return the ID, center point, junction bounds, size, and both traffic signals. |

### Signal cycle

| Lines | Explanation |
| --- | --- |
| 48-49 | `updateSignals(...)` receives the current phase tick and computes the full cycle length. |
| 50 | `current` is the current position inside that repeating signal cycle. |
| 52-55 | First phase: horizontal is `GREEN`, vertical is `RED`. |
| 55-58 | Second phase: horizontal becomes `YELLOW`, vertical stays `RED`. |
| 58-61 | Third phase: horizontal is `RED`, vertical becomes `GREEN`. |
| 61-64 | Fourth phase: horizontal stays `RED`, vertical becomes `YELLOW`. |

### Permission checks

| Lines | Explanation |
| --- | --- |
| 83-95 | `canProceed(...)` now has separate handling for pedestrians and vehicles. |
| 84-85 | If the lane is pedestrian-only, the method delegates to `canPedestrianProceed(...)`. |
| 88-95 | Vehicle logic still works the same way: green means go, yellow depends on behavior, and red means stop. |
| 98-100 | `getSignalFor(...)` maps east/west directions to the horizontal signal and north/south to the vertical signal. |
| 102-105 | `canPedestrianProceed(...)` checks the conflicting vehicle signal. Pedestrians only walk when that vehicle direction is red. |
| 106 | Ends the class. |

### What the current pedestrian rule means

- The crosswalk in this map runs horizontally across `Central Avenue`.
- Because of that, pedestrians watch the vertical vehicle signal.
- If `Central Avenue` is green or yellow, pedestrians wait.
- If `Central Avenue` is red, pedestrians may cross.

---

## 8. `src/main/environment/Grid.java`

This file builds the whole simulation map.

### Fields and constructor

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `environment` package. |
| 3-4 | Imports `Direction` and `LaneType` so lane creation is easier to read. |
| 6-8 | Imports list classes. |
| 10 | Declares the `Grid` class. |
| 11-12 | `width` and `height` describe the world size in grid units. |
| 13 | `roads` stores every road in the world. |
| 14 | `intersections` stores every intersection in the world. |
| 16-21 | Constructor saves the world size and provided collections. |

### `createDefaultGrid()`

| Lines | Explanation |
| --- | --- |
| 23 | Declares a static helper that builds the default map used by the simulation. |
| 24-25 | Creates empty road and intersection lists. |
| 27 | Creates `Main Street`. |
| 28 | Adds the eastbound vehicle lane from `(0,10)` to `(29,10)`. |
| 29 | Adds the westbound vehicle lane from `(29,13)` to `(0,13)`. |
| 30 | Adds `Main Street` to the road list. |
| 32 | Creates `Central Avenue`. |
| 33 | Adds the southbound lane from `(17,0)` to `(17,23)`. |
| 34 | Adds the northbound lane from `(14,23)` to `(14,0)`. |
| 35 | Adds `Central Avenue` to the road list. |
| 37 | Creates the pedestrian `Crosswalk` road. |
| 38 | Adds the eastbound pedestrian lane from `(8,12)` to `(22,12)`. |
| 39 | Adds the westbound pedestrian lane from `(22,11)` to `(8,11)`. |
| 40 | Adds `Crosswalk` to the road list. |
| 42 | Adds one central intersection at `(15,12)` with visual size `4`. |
| 44 | Returns a fully built `Grid` with width `30` and height `24`. |

### Utility methods

| Lines | Explanation |
| --- | --- |
| 47-60 | Getter methods return world size, roads, and intersections. |
| 63-69 | `getAllLanes()` collects lanes from every road into one list. |
| 71-79 | `getLanesByType(...)` filters lanes by `VEHICLE` or `PEDESTRIAN`. |
| 81-88 | `getLaneById(...)` finds a lane by its ID string. |
| 90-98 | `getIntersectionsForLane(...)` returns every intersection a given lane crosses. This is used when agents check signals. |
| 99 | Ends the class. |

---

## 9. `src/main/traffic/TrafficSignal.java`

This is a small state-holding class.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `traffic` package. |
| 3 | Declares the `TrafficSignal` class. |
| 4-8 | Declares the `SignalState` enum with `RED`, `GREEN`, and `YELLOW`. |
| 10 | `id` stores the signal ID. |
| 11 | `state` stores the current signal state. |
| 13-16 | Constructor saves the ID and starts the signal at `RED`. |
| 18-20 | `getId()` returns the signal ID. |
| 22-24 | `getState()` returns the current state. |
| 26-28 | `setState(...)` changes the signal state. |
| 29 | Ends the class. |

---

## 10. `src/main/traffic/SignalController.java`

This class advances all signal timing.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `traffic` package. |
| 3 | Imports `Intersection` because the controller updates intersections. |
| 5-7 | Imports list classes. |
| 9 | Declares `SignalController`. |
| 10 | Stores the intersections controlled by this object. |
| 11 | `greenDuration` stores how many ticks green lasts. |
| 12 | `yellowDuration` stores how many ticks yellow lasts. |
| 13 | `phaseTick` stores the current time inside the signal schedule. |
| 15-17 | First constructor uses default durations: `36` green and `10` yellow. |
| 19-25 | Second constructor stores inputs and immediately calls `refreshSignals()` so signals are valid from the start. |
| 27-30 | `update()` increments the phase counter by one tick and refreshes every intersection. |
| 32-42 | Getter methods return the intersections and durations. |
| 44-48 | `refreshSignals()` loops through all intersections and asks each one to update itself using the current phase tick. |
| 49 | Ends the class. |

---

## 11. `src/main/events/Event.java`

This is the base class for all events.

### Fields and constructor

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `events` package. |
| 3 | Imports `SimulationEngine` because events act on the engine. |
| 5 | Declares the abstract `Event` class. |
| 6 | `name` stores the event type, such as `Accident`. |
| 7 | `description` stores a longer explanation for the HUD. |
| 8 | `remainingTicks` stores how long the event will continue. |
| 9 | `active` stores whether the event has already been applied. |
| 11-16 | Constructor saves name, description, duration, and starts the event as inactive. |

### Lifecycle

| Lines | Explanation |
| --- | --- |
| 18 | `update(...)` is called every simulation tick while the event is active in the engine list. |
| 19-22 | If the event has not started yet, `apply(...)` is called once, then `active` becomes true. |
| 24 | Calls `onTick(...)`, a hook subclasses may use if they want per-tick behavior. |
| 25 | Decreases the remaining lifetime by one tick. |
| 28-33 | `finish(...)` is called when the event expires. It runs `clear(...)` and marks the event inactive. |
| 35-37 | `onTick(...)` is an optional hook. By default it does nothing. |
| 39 | `apply(...)` must be implemented by subclasses. This is where the event starts affecting the world. |
| 41 | `clear(...)` must be implemented by subclasses. This is where the event removes its effects. |

### Utility getters

| Lines | Explanation |
| --- | --- |
| 43-45 | `isExpired()` returns true once duration runs out. |
| 47-49 | `isActive()` returns whether the event has been applied and not yet cleared. |
| 51-60 | Getter methods return the name, description, and remaining lifetime. |
| 62 | Ends the class. |

---

## 12. `src/main/events/AccidentEvent.java`

This event blocks one lane completely.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `events` package. |
| 3-4 | Imports the engine type and `Lane`. |
| 6 | Declares `AccidentEvent` as a subclass of `Event`. |
| 7 | Stores which lane is affected. |
| 9-15 | Constructor builds a description like `Accident blocking Main Street / main-east`, sets the duration, and stores the lane. |
| 17-20 | `apply(...)` blocks the lane by calling `affectedLane.setBlocked(true, getDescription())`. |
| 22-25 | `clear(...)` unblocks the lane once the event expires. |
| 27-29 | `getAffectedLane()` returns the lane for drawing and inspection. |
| 30 | Ends the class. |

---

## 13. `src/main/events/CongestionEvent.java`

This event creates a rectangular slowdown area.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `events` package. |
| 3 | Imports `SimulationEngine`. |
| 5 | Imports `Point2D` because congestion checks whether a point lies inside the zone. |
| 7 | Declares `CongestionEvent`. |
| 8-11 | Store the top-left position and size of the congestion rectangle. |
| 12 | `slowdownFactor` stores how much to multiply speed by inside the zone. |
| 14-24 | Constructor creates the name, description, duration, rectangle geometry, and slowdown factor. |
| 26-29 | `apply(...)` does nothing special. The engine only needs this event to exist in the active list. |
| 31-34 | `clear(...)` also does nothing because no lane state was directly changed. |
| 36-41 | `contains(point)` returns true if an agent's position lies inside the congestion rectangle. |
| 43-60 | Getter methods return the rectangle geometry and slowdown factor. |
| 62 | Ends the class. |

---

## 14. `src/main/agents/Agent.java`

This is the most important movement class. Cars and pedestrians both inherit from it.

### Fields

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `agents` package. |
| 3-7 | Import behavior, engine, grid, intersection, and lane classes used for movement logic. |
| 9-12 | Import support classes for color, geometry, lists, and ID generation. |
| 14 | Declares abstract class `Agent`. |
| 15 | `NEXT_ID` is a static counter shared by all agents. Every new agent gets a unique number. |
| 17 | Stores the unique agent ID. |
| 18 | Stores the agent's name. |
| 19 | Stores which lane the agent belongs to. |
| 20 | Stores the behavior strategy object. |
| 21 | Stores the base speed before modifiers. |
| 22 | Stores the color used to draw the agent. |
| 23 | Stores the size used to draw the agent. |
| 24 | Stores current progress along the lane. |
| 25 | Stores how much the agent actually moved this tick. |

### Constructor

| Lines | Explanation |
| --- | --- |
| 27-44 | Constructor receives the core agent data and saves it. |
| 35 | Assigns a new unique ID using the static counter. |
| 39 | Sets the initial lane progress. This decides where the agent starts. |
| 43 | Starts current speed at zero because the agent has not moved yet. |

### Update logic

| Lines | Explanation |
| --- | --- |
| 46 | `update(engine)` is called once per simulation tick. |
| 47-50 | If the whole lane is blocked, the agent cannot move at all. Current speed becomes zero and the method returns. |
| 52 | Asks the behavior for the desired speed, then caps it with the lane speed limit. |
| 53 | Gives the desired speed to the engine so congestion or other world effects can modify it. |
| 54 | Calculates where the agent would end up if it moved fully this tick. |
| 55 | Adjusts that target if a traffic signal says the agent must stop before the intersection. |
| 57 | Stores the actual moved distance for this tick. |
| 58 | Normalizes progress so the lane loops cleanly when the agent reaches the end. |

### Signal handling

| Lines | Explanation |
| --- | --- |
| 61 | `respectTrafficSignals(...)` checks whether an agent is about to cross an intersection this tick. |
| 62 | Gets the full lane length. |
| 63 | Starts by assuming the agent can move all the way to the target. |
| 64 | Fetches all intersections that the lane crosses. |
| 66-84 | Loops through those intersections and decides whether one of them forces the agent to stop. |
| 67 | Finds where along the lane this intersection lies. |
| 68-70 | If the lane does not really cross this intersection, skip it. |
| 72-74 | Handles the edge case where movement wraps around the lane end in the same tick. |
| 76-80 | If the agent would cross the stop point during this tick and the intersection says no, the target is reduced to just before the stop line. This now works correctly for both cars and pedestrians. |
| 84 | Returns the possibly reduced target progress. |

### Progress normalization

| Lines | Explanation |
| --- | --- |
| 87-97 | `normalizeProgress(...)` wraps progress back into the valid lane range using modulo arithmetic. This is what makes agents loop forever instead of disappearing. |

### Getters and abstract methods

| Lines | Explanation |
| --- | --- |
| 99-137 | Getter methods return ID, name, lane, behavior, speeds, position, color, and size. |
| 123-125 | `getPosition()` converts current progress into a real `(x, y)` point by asking the lane. |
| 139 | `getAgentType()` must be implemented by subclasses. |
| 141 | `isCircular()` must be implemented by subclasses so drawing code knows whether to draw a circle or a rounded rectangle. |
| 142 | Ends the class. |

---

## 15. `src/main/agents/Car.java`

This is a thin subclass of `Agent`.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `agents` package. |
| 3-4 | Imports behavior and lane types needed by the constructor. |
| 6 | Imports `Color` for drawing. |
| 8 | Declares the `Car` class, which extends `Agent`. |
| 9-11 | Constructor passes car-specific defaults into the base class: base speed `1.35` and render size `18`. |
| 13-16 | `getAgentType()` returns `Car`. |
| 18-21 | `isCircular()` returns `false`, so cars are drawn as rounded rectangles. |
| 22 | Ends the class. |

---

## 16. `src/main/agents/Pedestrian.java`

This is the pedestrian version of `Agent`.

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `agents` package. |
| 3-4 | Imports behavior and lane classes. |
| 6 | Imports `Color`. |
| 8 | Declares the `Pedestrian` class. |
| 9-11 | Constructor passes pedestrian-specific defaults: base speed `0.65` and render size `10`. |
| 13-16 | `getAgentType()` returns `Pedestrian`. |
| 18-21 | `isCircular()` returns `true`, so pedestrians are drawn as circles. |
| 22 | Ends the class. |

---

## 17. `src/main/engine/SimulationEngine.java`

This is the central coordinator. If you want to understand the whole simulation, this is the file to study first after `Agent.java` and `Grid.java`.

### Fields and constructor

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `engine` package. |
| 3-15 | Imports agents, behaviors, environment classes, event types, and the signal controller. |
| 17-23 | Imports colors, geometry, and collection helpers. |
| 25 | Declares `SimulationEngine`. |
| 26 | `grid` stores the simulation world. |
| 27 | `signalController` stores the logic that cycles traffic lights. |
| 28 | `agents` stores all current moving objects. |
| 29 | `activeEvents` stores disruptions like accidents and congestion. |
| 30 | `random` is used for random event creation. |
| 31 | `tick` stores the current simulation time. |
| 33-41 | Constructor creates the grid, signal controller, agent list, event list, random generator, sets time to zero, and seeds the starting agents. |

### Main tick update

| Lines | Explanation |
| --- | --- |
| 43 | `update()` is called once every timer step from the UI. |
| 44 | Advances simulation time. |
| 45 | Updates all traffic signals by one phase tick. |
| 46 | Updates active events and removes expired ones. |
| 48-50 | Every 70 ticks, if fewer than 3 events are active, there is a `55%` chance of creating a new random event. |
| 52-54 | After world state is updated, every agent is updated once. |

### Speed modifiers

| Lines | Explanation |
| --- | --- |
| 57 | `applySpeedModifiers(...)` lets the world reduce an agent's requested speed. |
| 58 | Reads the agent's current position. |
| 59 | Starts with the desired speed unchanged. |
| 61-65 | Loops through active events. If the event is a congestion event and contains the agent position, multiply speed by the event slowdown factor. |
| 67 | Returns at least `0.2`, so congestion slows movement but does not freeze it completely. |

### Read-only accessors

| Lines | Explanation |
| --- | --- |
| 70-88 | Getter methods expose grid, controller, agents, events, and current tick. |
| 78-79 | `getAgents()` returns an unmodifiable list. |
| 82-83 | `getActiveEvents()` also returns an unmodifiable list. |

### HUD helper methods

| Lines | Explanation |
| --- | --- |
| 90-98 | `getRunningCarCount()` counts cars whose current speed is greater than almost zero. This drives the HUD metric. |
| 100-108 | `getBlockedLaneCount()` counts how many lanes are currently blocked by accidents. |

### Seeding agents

| Lines | Explanation |
| --- | --- |
| 110 | `seedAgents()` creates the starting cars and pedestrians. |
| 111-116 | Fetches lane objects by ID from the grid. |
| 118-123 | Creates six cars with different lanes, starting positions, behaviors, and colors. |
| 124-126 | Creates three pedestrians on the crosswalk lanes. |

### Event updates

| Lines | Explanation |
| --- | --- |
| 129 | `updateEvents()` loops through active events safely using an iterator. |
| 132 | Updates one event for the current tick. |
| 134-136 | If the event has expired, call `finish(...)` so it clears its effects, then remove it from the list. |

### Random event generation

| Lines | Explanation |
| --- | --- |
| 141-147 | `spawnRandomEvent()` randomly chooses between accident and congestion. |
| 149 | `spawnAccident()` creates a new accident event. |
| 150 | Starts from all vehicle lanes only. Pedestrian lanes are not accident candidates. |
| 151 | Removes already blocked lanes so we do not pile an accident on the same lane. |
| 152-154 | If no valid lane remains, stop. |
| 156 | Chooses one random candidate lane. |
| 157 | Creates an `AccidentEvent` lasting between 40 and 74 ticks. |
| 160 | `spawnCongestion()` creates a congestion zone. |
| 161 | Uses the first intersection as the anchor point if one exists. |
| 162-163 | Default fallback coordinates. |
| 165-168 | If an intersection exists, choose a zone roughly near it with a bit of randomness. |
| 170 | Creates a congestion event with size `8x8`, duration between 45 and 79 ticks, and slowdown factor `0.45`. |
| 172 | Ends the class. |

---

## 18. `src/main/ui/SimulationPanel.java`

This file is the visual layer. It does not decide the rules. It reads the simulation state and draws it.

The current version of this file is aimed at readability. It adds road labels, stop markers, a live status card, a clearer HUD, and wrapped event text so the UI is easier to understand.

### Constants and fields

| Lines | Explanation |
| --- | --- |
| 1 | Declares the `ui` package. |
| 3-12 | Imports domain classes that the panel needs to render. |
| 15-26 | Imports Swing and AWT drawing classes. |
| 28 | Declares `SimulationPanel`. |
| 29 | `CELL_SIZE = 24` means one world grid unit becomes 24 screen pixels. |
| 30 | `WORLD_MARGIN = 30` adds padding around the map. |
| 31 | `HUD_WIDTH = 320` reserves a wider space for the right-side dashboard. |
| 34-37 | Shared font constants keep headings, body text, and small labels visually consistent. |
| 39 | Stores the `SimulationEngine`. |
| 40 | Stores the Swing `Timer` that drives animation. |

### Constructor

| Lines | Explanation |
| --- | --- |
| 42-56 | Constructor saves the engine, computes panel size, sets background, creates the timer, and starts the timer. |
| The panel is slightly taller than before so the lower dashboard sections do not overlap. |

### Paint flow

| Lines | Explanation |
| --- | --- |
| `paintComponent(...)` is Swing's standard drawing method. It clears old content, enables antialiasing, paints the background, draws the world, draws the HUD, and disposes the graphics copy. |
| `paintBackground(...)` draws the dark gradient background. |
| `drawWorld(...)` now renders roads, stop lines, the junction, events, signal lamps, road labels, a live status banner, and finally agents. This order keeps information readable. |

### Roads and intersection

| Lines | Explanation |
| --- | --- |
| `drawRoads(...)` loops through all roads and their lanes and draws each lane as a thick line. Vehicle lanes are darker and thicker than pedestrian lanes. A dashed line is drawn on top as a visual lane divider. |
| `drawStopLines(...)` adds colored stop markers before the junction so viewers can immediately see which vehicle approach has red, yellow, or green. |
| `drawIntersection(...)` draws the central junction box and the small `JUNCTION` label. |

### Events

| Lines | Explanation |
| --- | --- |
| 118-140 | `drawEvents(...)` draws visual overlays for active events. |
| 120-125 | If an event is an accident, draw a red line over the affected lane. |
| 127-138 | If an event is congestion, draw an orange rounded rectangle over the slowdown region. |

### Signals

| Lines | Explanation |
| --- | --- |
| `drawSignals(...)` places four visible lamps around each intersection. Two represent the horizontal signal and two represent the vertical signal. |
| `drawRoadLabels(...)` places map labels such as `Main Street`, `Central Avenue`, `Crosswalk`, and directional hints near the edges instead of crowding the center. |
| `drawLiveStatusBanner(...)` adds the top-left card that explains which road currently has `GO`, `STOP`, or `SLOW / CLEAR`. |
| `drawSignalLamp(...)` draws a small signal housing and colors the active lamp based on signal state. |

### Agents

| Lines | Explanation |
| --- | --- |
| `drawAgents(...)` draws every agent. |
| Cars are rounded rectangles and pedestrians are circles. |
| The current version no longer draws agent ID labels beside each object, because that caused clutter and overlap. |

### HUD

| Lines | Explanation |
| --- | --- |
| `drawHud(...)` draws the right-side dashboard. |
| The HUD now uses metric cards, a road-based signal status section, section dividers, a simplified legend, and a wrapped event list. |

### Legend

| Lines | Explanation |
| --- | --- |
| `drawLegend(...)` writes a cleaner legend than the original version. |
| `drawLegendItem(...)` draws one colored marker and one label. |

### Event feed

| Lines | Explanation |
| --- | --- |
| `drawEventFeed(...)` lists currently active events in the HUD. |
| If there are no active events, it shows `No disruptions right now.` |
| Event descriptions are wrapped, and if the section fills up the UI shows `More events active...` instead of letting text collide. |
| `colorForEvent(...)` chooses red for accidents, orange for congestion, and gray as fallback. |

### Coordinate conversion

| Lines | Explanation |
| --- | --- |
| `worldX(...)` converts a grid x-coordinate into a pixel x-coordinate. |
| `worldY(...)` converts a grid y-coordinate into a pixel y-coordinate. |
| `drawWrappedText(...)` is a helper that prevents long event descriptions from overflowing the panel width. |
| The file also includes small helpers such as `colorForSignal(...)` and `drawSectionDivider(...)` to keep the UI consistent. |

---

## How all files work together

If you want the shortest mental model, use this flow:

1. `Main.java` starts the app.
2. `SimulationPanel` creates a timer.
3. Every timer tick, `SimulationEngine.update()` runs.
4. The engine updates signals, events, and agents.
5. Each `Agent` asks its `BehaviorStrategy` for speed.
6. The engine applies world effects like congestion.
7. The agent checks signals using `Intersection`.
8. The agent moves forward on its `Lane`.
9. The panel repaints roads, events, signals, and agents.

That is the full loop.

---

## What to read first if you feel lost

Read in this order:

1. `Main.java`
2. `SimulationPanel.java`
3. `SimulationEngine.java`
4. `Agent.java`
5. `Grid.java`
6. `Lane.java`
7. `Intersection.java`
8. `Event.java`
9. `AccidentEvent.java`
10. `CongestionEvent.java`
11. Behavior classes

That order matches how the program actually feels when it runs: start window, tick engine, move agents, obey map, obey signals, react to events.
