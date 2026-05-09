# Work Distribution for 4 Team Members

This project can be divided into four clear contributions based on the actual modules in the codebase.

## Person 1: Project Setup and Simulation Control

Handled the main application flow and overall simulation coordination.

- Created the project entry point in `src/main/Main.java`
- Connected the simulation engine with the Swing window
- Built the core update cycle in `src/main/engine/SimulationEngine.java`
- Managed agent updates, event updates, and signal updates in one simulation loop
- Seeded the initial cars and pedestrians into the simulation

## Person 2: Road Network and Traffic Signal System

Handled the map structure and traffic control logic.

- Built the environment model in `src/main/environment/`
- Created `Lane.java`, `Road.java`, `Intersection.java`, and `Grid.java`
- Designed the default city layout with roads, lanes, and crosswalks
- Implemented intersection crossing checks and lane geometry logic
- Built the traffic signal model in `src/main/traffic/TrafficSignal.java`
- Implemented traffic light timing and switching in `src/main/traffic/SignalController.java`

## Person 3: Agents and Behavior Logic

Handled how vehicles and pedestrians move inside the simulation.

- Built the base agent system in `src/main/agents/Agent.java`
- Created specialized agents in `Car.java` and `Pedestrian.java`
- Implemented lane-based movement and progress tracking
- Added signal-respecting movement logic at intersections
- Used the Strategy Pattern in `src/main/behavior/`
- Implemented `BehaviorStrategy`, `NormalBehavior`, and `AggressiveBehavior`

## Person 4: Events, Visual Interface, and Documentation

Handled disruption features, visualization, and project explanation materials.

- Built the event system in `src/main/events/Event.java`
- Implemented `AccidentEvent.java` and `CongestionEvent.java`
- Added random traffic disruptions to make the simulation more realistic
- Created the visual simulation panel in `src/main/ui/SimulationPanel.java`
- Drew roads, signals, agents, event overlays, and HUD information
- Prepared supporting documentation in `README.md` and `CODE_WALKTHROUGH.md`

## Short Version for Viva or Report

If you need a very short distribution:

1. Person 1: Main program flow and simulation engine
2. Person 2: Road network, lanes, intersections, and traffic signals
3. Person 3: Cars, pedestrians, and behavior logic
4. Person 4: Events, GUI visualization, and documentation
